/*
 * This file is part of Disconnected.
 * Copyright (c) 2013 QuarterCode <http://quartercode.com/>
 *
 * Disconnected is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Disconnected is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Disconnected. If not, see <http://www.gnu.org/licenses/>.
 */

package com.quartercode.disconnected.server.world.comp.prog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.quartercode.disconnected.server.registry.ServerRegistries;
import com.quartercode.disconnected.server.registry.WorldProgram;
import com.quartercode.disconnected.server.world.comp.Computer;
import com.quartercode.disconnected.server.world.comp.file.ContentFile;
import com.quartercode.disconnected.server.world.comp.file.MissingFileRightsException;
import com.quartercode.disconnected.server.world.comp.os.OperatingSystem;
import com.quartercode.disconnected.server.world.comp.user.User;
import com.quartercode.disconnected.server.world.util.WorldNode;
import com.quartercode.disconnected.shared.event.comp.prog.control.SBPWorldProcessUserInterruptCommand;
import com.quartercode.disconnected.shared.event.comp.prog.control.WorldProcessLaunchCommand;
import com.quartercode.disconnected.shared.util.registry.Registries;
import com.quartercode.disconnected.shared.util.registry.extra.NamedValueUtils;
import com.quartercode.disconnected.shared.world.comp.file.FileRights;
import com.quartercode.disconnected.shared.world.comp.prog.SBPWorldProcessUserId;
import com.quartercode.disconnected.shared.world.comp.prog.WorldProcessId;
import com.quartercode.disconnected.shared.world.comp.prog.WorldProcessState;
import com.quartercode.eventbridge.bridge.Bridge;
import com.quartercode.jtimber.api.node.Node;
import com.quartercode.jtimber.api.node.Weak;
import com.quartercode.jtimber.api.node.wrapper.SubstituteWithWrapper;
import com.quartercode.jtimber.api.node.wrapper.collection.ListWrapper;

/**
 * This class represents a process which is basically a running instance of a program.
 * On the creation, the program object will be backed up, as well as a new program executor instance.
 *
 * @param <P> The type of the parent {@link CFeatureHolder} which houses the process somehow.
 * @see Program
 * @see ProgramExecutor
 */
public class Process<P extends Node<?>> extends WorldNode<P> {

    private static final Logger              LOGGER         = LoggerFactory.getLogger(Process.class);

    @XmlAttribute
    private int                              pid;
    @Weak
    @XmlAttribute
    @XmlIDREF
    private ContentFile                      source;
    // Transient
    private SBPWorldProcessUserId            worldProcessUser;

    @XmlAttribute
    private WorldProcessState                state          = WorldProcessState.RUNNING;
    @XmlElement
    private ProgramExecutor                  executor;
    @XmlElement (name = "child")
    @SubstituteWithWrapper (ListWrapper.class)
    private final List<Process<?>>           childProcesses = new ArrayList<>();
    @XmlElement (name = "stateListener")
    @SubstituteWithWrapper (ListWrapper.class)
    private final List<ProcessStateListener> stateListeners = new ArrayList<>();

    // JAXB constructor
    protected Process() {

    }

    /**
     * Creates a new process.
     * Note that you will probably not use this constructor by yourself.
     * Instead, you should use {@link #createChild()}.
     *
     * @param pid The unique process id for the new process. Its uniqueness is validated by the constructor.
     *        If you need a unique PID, you can generate one using {@link ProcModule#nextPid()}.
     * @param source The {@link ContentFile} which contains the {@link Program} the new process should run.
     * @param worldProcessUser If the process was launched by an SBP, this parameter takes the {@link SBPWorldProcessUserId} that identifies the SBP and the world process user.
     *        Note that this property may be {@code null} (e.g. for internal processes like the {@link RootProcess}).
     * @throws IllegalArgumentException If the given PID is invalid or already used by another process.
     *         Alternatively, if the provided source file does not contain a program object.
     */
    protected Process(int pid, ContentFile source, SBPWorldProcessUserId worldProcessUser) {

        Validate.isTrue(pid >= 0, "Process PID ('%d') must be >= 0", pid);
        Validate.notNull(source, "Process source file cannot be null");
        Validate.isInstanceOf(Program.class, source.getContent(), "Process source file must contain a program");

        // Check whether the PID is already in use
        Set<Integer> existingPids = new HashSet<>();
        Process<?> root = getRoot();
        existingPids.add(root.getPid());
        for (Process<?> otherProcess : root.getAllChildProcesses()) {
            existingPids.add(otherProcess.getPid());
        }
        Validate.isTrue(!existingPids.contains(pid), "PID '%d' is already used by another process", pid);

        this.pid = pid;
        this.source = source;
        this.worldProcessUser = worldProcessUser;
    }

    /**
     * Returns the unique id the process has.
     * It is used to identify the process.
     *
     * @return The unique process id.
     */
    public int getPid() {

        return pid;
    }

    /**
     * Returns the {@link ContentFile} which contains the {@link Program} the process runs.
     *
     * @return The program file the process executes.
     */
    public ContentFile getSource() {

        return source;
    }

    /**
     * Returns the {@link Program} which is or will be ran by the process.
     * It is stored inside the processe's {@link #getSource() source program file}.
     */
    public Program getProgram() {

        return (Program) source.getContent();
    }

    /**
     * Returns the {@link WorldProcessState} which defines the global state of the process as seen by the OS.
     * It stores whether the process is running, interrupted etc.<br>
     * Note that the state can be changed with the state methods: {@link #suspend(boolean)}, {@link #resume(boolean)}, {@link #interrupt(boolean)}, and {@link #stop(boolean)}.
     *
     * @return The current state of the process.
     */
    public WorldProcessState getState() {

        return state;
    }

    /**
     * Returns the {@link ProgramExecutor} which contains the logic of the process.
     * This value is only available after process {@link #initialize() initialization} since that method derives it from the {@link #getSource() source file.}
     *
     * @return The program executor which runs the process by executing the program's functionalities.
     */
    public ProgramExecutor getExecutor() {

        return executor;
    }

    /**
     * Returns the child processes the process launched through the {@link #createChild()} method.
     * If you want to launch a new process from the {@link #getExecutor() program executor} of this process, you can use that method as well.
     * In order to remove processes from this list, they need to be {@link #stop(boolean) stopped}.
     *
     * @return The child processes which have been launched by this process.
     */
    public List<Process<?>> getChildProcesses() {

        return Collections.unmodifiableList(childProcesses);
    }

    /**
     * Returns the {@link ProcessStateListener}s that are called whenever the {@link #getState() state} of this process changes.
     * New listeners can be added by using the {@link #addStateListener(ProcessStateListener)} method.
     * In contrast, old listeners can be removed with the {@link #removeStateListener(ProcessStateListener)} method.
     * Note that listeners don't have to be manually removed after the program execution has finished.
     * Since the whole process object is disposed, they are disposed as well.
     *
     * @return The process state listeners that are currently registered.
     */
    public List<ProcessStateListener> getStateListeners() {

        return Collections.unmodifiableList(stateListeners);
    }

    /**
     * Adds a new {@link ProcessStateListener} that is called whenever the {@link #getState() state} of this process changes.
     * If you want to remove any listener, you can use the {@link #removeStateListener(ProcessStateListener)} method instead.
     * Note that listeners don't have to be manually removed after the program execution has finished.
     * Since the whole process object is disposed, they are disposed as well.
     *
     * @param stateListener The new process state listener which should listen for state changes.
     */
    public void addStateListener(ProcessStateListener stateListener) {

        stateListeners.add(stateListener);
    }

    /**
     * Removes the given {@link ProcessStateListener} so it is no longer called whenever the {@link #getState() state} of this process changes.
     * If you want to add a new listener, you can use the {@link #addStateListener(ProcessStateListener)} method instead.
     * Note that listeners don't have to be manually removed after the program execution has finished.
     * Since the whole process object is disposed, they are disposed as well.
     *
     * @param stateListener The process state listener which should no longer listen for state changes.
     */
    public void removeStateListener(ProcessStateListener stateListener) {

        stateListeners.remove(stateListener);
    }

    /**
     * If the process was launched by an SBP, this method returns the {@link SBPWorldProcessUserId} that identifies the SBP and the world process user.
     * Such a remote launch is done using the {@link WorldProcessLaunchCommand} event.
     *
     * @return An object that defines the SBP which is communicating with the world process, as well as some information specifying the exact part of the SBP.
     */
    public SBPWorldProcessUserId getWorldProcessUser() {

        return worldProcessUser;
    }

    /**
     * Returns whether the given {@link WorldProcessState} is applied to this process, all its {@link #getChildProcesses() child processes}, all their child processes etc.
     *
     * @param state The process state to check all children for recursively.
     * @return Whether the given state is applied to this process recursively.
     */
    public boolean isStateAppliedRecursively(WorldProcessState state) {

        if (this.state != state) {
            return false;
        } else {
            for (Process<?> childProcess : childProcesses) {
                if (!childProcess.isStateAppliedRecursively(state)) {
                    return false;
                }
            }
        }

        return true;
    }

    private void applyState(WorldProcessState state, boolean recursive) {

        // Conserve the old state of the process for now; the state change listener caller needs this state later on
        WorldProcessState oldState = this.state;

        // Actually change the process state
        this.state = state;

        // Change the states of all child processes if the state change is recursive
        if (recursive) {
            for (Process<?> childProcess : childProcesses) {
                childProcess.applyState(state, recursive);
            }
        }

        // Call any state listeners
        for (ProcessStateListener stateListener : stateListeners) {
            stateListener.onStateChange(this, oldState, state);
        }

        // Enable or disable the program executor's scheduler according to the new state (e.g. SUSPENDED -> inactive, RUNNING -> active)
        if (executor instanceof SchedulerUsing) {
            ((SchedulerUsing) executor).getScheduler().setActive(state.isTickState());
        }
    }

    /**
     * Suspends the execution temporarily. A possibly {@link SchedulerUsing used scheduler} won't run until the process is {@link #resume(boolean) resumed}.
     * Suspension only works when the execution is running. An {@link #interrupt(boolean) interrupted} or {@link #stop(boolean) stopped} process can't be suspended.
     * The state change can additionally be applied to every {@link #getChildProcesses() child process} (and their child processes ...) using the recursive parameter.
     *
     * @param recursive Whether the state change should affect all child processes (and their child processes ...).
     */
    public void suspend(boolean recursive) {

        if (state != WorldProcessState.RUNNING) {
            LOGGER.warn("Cannot suspend non-running process '{}' (current state '{}, program executor '{}')", getWorldProcessId(), state, getExecutor().getClass());
        } else {
            applyState(WorldProcessState.SUSPENDED, recursive);
        }
    }

    /**
     * Resumes a {@link #suspend(boolean) suspended} process. A possibly {@link SchedulerUsing used scheduler} will run again.
     * Resuming only works if the execution is suspended. An {@link #interrupt(boolean) interrupted} or {@link #stop(boolean) stopped} process can't be resumed.
     * The state change can additionally be applied to every {@link #getChildProcesses() child process} (and their child processes ...) using the recursive parameter.
     *
     * @param recursive Whether the state change should affect all child processes (and their child processes ...).
     */
    public void resume(boolean recursive) {

        if (state != WorldProcessState.SUSPENDED) {
            LOGGER.warn("Cannot resume non-suspended process '{}' (current state '{}, program executor '{}')", getWorldProcessId(), state, getExecutor().getClass());
        } else {
            applyState(WorldProcessState.RUNNING, recursive);
        }
    }

    /**
     * Interrupts the execution friendly and asks the process to {@link #stop(boolean) stop} itself as soon as possible.
     * If the {@link #getExecutor() program executor} notices the interruption, it should try to execute all necessary last activities before stopping the execution.
     * For example, if a program executor notices an interruption, it might finish writing some data into files before stopping itself.
     * Interruption only works if the execution is running.
     * Note that an {@link SBPWorldProcessUserInterruptCommand} is sent if the {@link Process#getWorldProcessUser() world process user} of the process is not {@code null}.<br>
     * <br>
     * The state change can additionally be applied to every {@link #getChildProcesses() child process} (and their child processes ...) using the recursive parameter.
     *
     * @param recursive Whether the state change should affect all child processes (and their child processes ...).
     */
    public void interrupt(boolean recursive) {

        if (state != WorldProcessState.RUNNING) {
            LOGGER.warn("Cannot interrupt non-running process '{}' (current state '{}, program executor '{}')", getWorldProcessId(), state, getExecutor().getClass());
        } else {
            // Send SBPWorldProcessUserInterruptCommand if the process is being interrupted in order to shut down any user of the world process
            if (state == WorldProcessState.INTERRUPTED) {
                SBPWorldProcessUserId wpuId = getWorldProcessUser();
                Bridge bridge = getBridge();
                if (wpuId != null && bridge != null) {
                    bridge.send(new SBPWorldProcessUserInterruptCommand(wpuId));
                }
            }

            // Actually change the state
            applyState(WorldProcessState.INTERRUPTED, recursive);
        }
    }

    /**
     * Forces the process to stop its execution immediately and gives the {@link #getExecutor() program executor} no chance to execute any more activites.
     * This will act like {@link #suspend(boolean) suspension}, apart from the fact that a stopped process won't ever be able to {@link #resume(boolean) resume}.
     * The forced stopping action should only be used by the program executor itself when it has finished the interruption activities.
     * For example, if a program executor notices an {@link #interrupt(boolean) interruption}, it might finish writing some data into files before stopping itself.
     * Calling this method on an uninterrupted process might cause internal conflicts and is therefore not possible.<br>
     * <br>
     * Note that there is no way of applying the state change recursively to other processes because this change should only be executed by the program executor.
     */
    public void stop() {

        if (state != WorldProcessState.INTERRUPTED) {
            LOGGER.warn("Cannot suspend non-interrupted process '{}' (current state '{}, program executor '{}')", getWorldProcessId(), state, getExecutor().getClass());
        } else if (state == WorldProcessState.STOPPED) {
            LOGGER.warn("Cannot stop already stopped process '{}' again (current state '{}, program executor '{}')", getWorldProcessId(), state, getExecutor().getClass());
        } else {
            // Actually change the state
            applyState(WorldProcessState.STOPPED, false);

            // Unregister the stopped process from parent
            if (getSingleParent() instanceof Process) {
                ((Process<?>) getSingleParent()).childProcesses.remove(this);
            }

            // Promote the old child processes of this process to child processes of the parent process
            for (Process<?> childProcess : new ArrayList<>(childProcesses)) {
                childProcesses.remove(childProcess);
                ((Process<?>) getSingleParent()).childProcesses.add(childProcess);
            }
        }
    }

    /**
     * Returns all {@link #getChildProcesses() child processes} of this process, and all child processes of those processes etc. recursively.
     * That means that all processes this process launched directly or indirectly (that means that a child process launched another child process) are returned by this method.
     * Essentially, it just traverses the process tree recursively from this process onwards and returns the collected processes.
     *
     * @return All processes that originated from this process directly or indirectly.
     */
    public List<Process<?>> getAllChildProcesses() {

        List<Process<?>> allChildProcesses = new ArrayList<>();
        for (Process<?> directChildProcess : childProcesses) {
            allChildProcesses.add(directChildProcess);
            allChildProcesses.addAll(directChildProcess.getAllChildProcesses());
        }

        return allChildProcesses;
    }

    /**
     * Returns the root {@link RootProcess} which is the parent of every other process somewhere in the tree.
     * It is the topmost process and probably has the {@link #getPid() PID} {@code 0}.
     *
     * @return The process all other processes originates from directly or indirectly.
     */
    public RootProcess getRoot() {

        return ((Process<?>) getSingleParent()).getRoot();
    }

    /**
     * Returns the {@link ProcessModule} which is hosting the {@link #getRoot() root process} that is the direct or indirect parent of every other process.
     *
     * @return The process module the process belongs to.
     */
    public ProcessModule getProcModule() {

        return getRoot().getSingleParent();
    }

    /**
     * Returns the {@link OperatingSystem} which is hosting the {@link #getRoot() root process} that is the direct or indirect parent of every other process.
     * Note that the OS is not directly holding the process. Instead, the {@link ProcessModule} has that responsibility.
     *
     * @return The operating system the process belongs to.
     */
    public OperatingSystem getOs() {

        return getProcModule().getSingleParent();
    }

    /**
     * Resolves the process that runs the {@link Session} this process is part of.
     * Effectively, this process is running under the {@link User} which is set inside that session.
     * Note that the session process is internally retrieved by returning the first parent (or parent of a parent ...) process that runs a session.
     *
     * @return The session process this process is running under.
     */
    public Process<?> getSessionProcess() {

        if (executor instanceof Session) {
            return this;
        } else {
            // Check parent process
            if (getSingleParent() instanceof Process) {
                return ((Process<?>) getSingleParent()).getSessionProcess();
            } else {
                return null;
            }
        }
    }

    /**
     * Resolves the {@link Session} this process is part of.
     * Effectively, this process is running under the {@link User} which is set inside that session.
     * Note that the session's process is internally retrieved using the {@link #getSessionProcess()} method.
     *
     * @return The session process this process is running under.
     */
    public Session getSession() {

        Process<?> sessionProcess = getSessionProcess();
        return sessionProcess == null ? null : (Session) sessionProcess.getExecutor();
    }

    /**
     * Resolves the {@link User} this process is running under.
     * When the process tries to access files etc., the returned user must be allowed to do so.
     * Note that this method internally uses the {@link #getSession()} function for resolving the {@link Session} that stores the user.
     */
    public User getUser() {

        Session session = getSession();
        return session == null ? null : session.getUser();
    }

    /**
     * Returns a {@link WorldProcessId} object that identifies this process uniquely in the whole world.
     * It can be used to communicate with the process from the outside (e.g. from a graphical client).
     * Internally, the process is identified using the {@link #getUUID() UUID} of the {@link Computer} it is running on, as well as the processes's {@link #getPid() process id (pid)}.
     *
     * @return A world-wide unique identifier for this specific process.
     */
    public WorldProcessId getWorldProcessId() {

        return new WorldProcessId(getUUID(), pid);
    }

    /**
     * Initializes the process by creating a new {@link ProgramExecutor} instance of the {@link Program} that is stored in the set {@link #getSource() source file}.
     * Moreover, the newly set {@link #getExecutor() executor} is directly {@link ProgramExecutor#run() started} by the method.
     * Please note that this method should not be used publicly. Instead, it is automatically called by {@link #createChild(int, ContentFile, SBPWorldProcessUserId)}.
     * However, there are internal situations where calling this method manually is necessary (e.g. when a new {@link RootProcess} is created).
     *
     * @throws IllegalStateException If the {@link Program#getName() name} of the set {@link #getProgram() program} is unknown and no executor can therefore be retrieved.
     * @throws MissingFileRightsException If the {@link User} the process runs under hasn't got the read and execute rights on the {@link #getSource() source file}.
     */
    protected void initialize() throws MissingFileRightsException {

        // Check the read and execution rights on the source file
        source.checkRights("initialize process using source file", getUser(), FileRights.READ, FileRights.EXECUTE);

        // Retrieve the program data object
        String programName = getProgram().getName();
        WorldProgram programData = NamedValueUtils.getByName(Registries.get(ServerRegistries.WORLD_PROGRAMS), programName);
        Validate.validState(programData != null, "Cannot find world program with name '%s' for launching a process", programName);

        // Create and set a new executor
        try {
            executor = (ProgramExecutor) programData.getType().newInstance();
            executor.run();
        } catch (Exception e) {
            throw new RuntimeException("Unexpected exception during initialization of new program executor (class '" + programData.getType().getName() + "'", e);
        }
    }

    /**
     * Creates <b>and launches</b> a new {@link #getChildProcesses() child process} of this process.
     * The {@link #getPid() PID} of the new process will be obtained through a call to {@link ProcessModule#nextPid()}.
     * Note that the {@link ProgramExecutor#run()} method is automatically called by this method.
     * The new process will immediately start running.
     *
     * @param source The {@link ContentFile} which contains the {@link Program} the new process should run.
     * @param worldProcessUser If the process was launched by an SBP, this parameter takes the {@link SBPWorldProcessUserId} that identifies the SBP and the world process user.
     *        Note that this property may be {@code null} (e.g. for internal processes like the {@link RootProcess}).
     * @return The newly created and launched child process.
     * @throws IllegalArgumentException If the given PID is invalid or already used by another process.
     *         Alternatively, if the provided source file does not contain a program object.
     * @throws IllegalStateException If the {@link Program#getName() name} of the program stored in the given source file is unknown and no executor can therefore be retrieved.
     * @throws MissingFileRightsException If the {@link User} the new process runs under hasn't got the read and execute rights on the source file.
     *         If you are not starting a new {@link Session}, the user of the new process will be the same one as the {@link #getUser() user of this process}.
     */
    public Process<Process<?>> launchChild(ContentFile source, SBPWorldProcessUserId worldProcessUser) throws MissingFileRightsException {

        Process<Process<?>> childProcess = new Process<>(getProcModule().nextPid(), source, worldProcessUser);
        childProcesses.add(childProcess);

        try {
            childProcess.initialize();
        } catch (IllegalStateException | MissingFileRightsException e) {
            childProcesses.remove(childProcess);
            throw e;
        }

        return childProcess;
    }

}
