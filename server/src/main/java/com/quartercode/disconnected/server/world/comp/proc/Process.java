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

package com.quartercode.disconnected.server.world.comp.proc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.quartercode.disconnected.server.world.comp.Computer;
import com.quartercode.disconnected.server.world.comp.os.OperatingSystem;
import com.quartercode.disconnected.server.world.comp.os.config.User;
import com.quartercode.disconnected.server.world.comp.proc.prog.Program;
import com.quartercode.disconnected.server.world.comp.proc.task.def.TaskExecutor;
import com.quartercode.disconnected.server.world.comp.proc.task.exec.InteractiveProcess;
import com.quartercode.disconnected.server.world.comp.proc.task.exec.TaskProcess;
import com.quartercode.disconnected.server.world.util.WorldNode;
import com.quartercode.disconnected.shared.world.comp.proc.WorldProcessId;
import com.quartercode.disconnected.shared.world.comp.proc.WorldProcessState;
import com.quartercode.jtimber.api.node.Node;
import com.quartercode.jtimber.api.node.wrapper.SubstituteWithWrapper;
import com.quartercode.jtimber.api.node.wrapper.collection.ListWrapper;

/**
 * This class represents a process which is basically a running instance of a program.
 * Note that this class is abstract; different subclasses implement different types of processes.
 * Currently, the following subclasses exist:<br>
 * <br>
 * <b>{@link TaskProcess}:</b><br>
 * Task processes basically execute one single isolated {@link TaskExecutor}.
 * However, they allow the creator of the task process to communicate with the task (and vice versa).
 * For example, a task process might host a running script executor task.
 * That task then launches child task processes, each of which hosts another task which is used by the script.
 * Whenever one of the subprocesses finishes, it is able to send its results to the parent process using an abstract API.<br>
 * <br>
 * <b>{@link InteractiveProcess}:</b><br>
 * Interactive processes execute no task by themselves; instead, they function as a host of different tasks for client-side GUIs.
 * Almost all server-side programs, which are of course just a collection of program-related tasks, have client-side GUIs for controlling them.
 * A client can launch such a GUI, which in turn launches an interactive process on the server.
 * Via the interactive process, the GUI can then launch task processes (limited to the tasks provided by the specific program) and be informed about their outcomes.
 * That way, the GUIs interact with the world the same way scripts do, reducing code duplication.<br>
 * When the player lists the processes which are running on his computer, he only sees the interactive process and not that task processes.
 * That way, he is "tricked" into the illusion that the computer runs his client program and nothing else; the individual tasks are hidden.
 *
 * @see Program
 * @see TaskExecutor
 */
public abstract class Process extends WorldNode<Node<?>> {

    private static final Logger              LOGGER         = LoggerFactory.getLogger(Process.class);

    @XmlAttribute
    private int                              pid;

    @XmlAttribute
    private WorldProcessState                state          = WorldProcessState.WAITING;
    @XmlElement (name = "stateListener")
    @SubstituteWithWrapper (ListWrapper.class)
    private final List<ProcessStateListener> stateListeners = new ArrayList<>();
    @XmlElement (name = "child")
    @SubstituteWithWrapper (ListWrapper.class)
    private final List<Process>              childProcesses = new ArrayList<>();

    /**
     * Creates a new process.
     * Note that you will probably not construct instances of this generic process class.
     * Instead, you will use subclasses of this class.
     */
    protected Process() {

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
     * This internal method is called whenever the {@link #state} of the process is changed.
     * It takes care of calling all the registered {@link ProcessStateListener}s.
     *
     * @param state The new {@link WorldProcessState}.
     */
    protected void applyState(WorldProcessState state) {

        // Conserve the old state of the process for now; the state change listener caller needs this state later on
        WorldProcessState oldState = this.state;

        // Actually change the process state
        this.state = state;

        // Call any state listeners
        for (ProcessStateListener stateListener : stateListeners) {
            stateListener.onStateChange(this, oldState, state);
        }
    }

    /**
     * Interrupts the execution friendly and asks the process to {@link #stop(boolean) stop} itself as soon as possible.
     * If the {@link #getExecutor() program executor} notices the interruption, it should try to execute all necessary last activities before stopping the execution.
     * For example, if a program executor notices an interruption, it might finish writing some data into files before stopping itself.
     * Interruption only works if the execution is running.
     */
    public void interrupt() {

        if (state != WorldProcessState.RUNNING) {
            LOGGER.warn("Cannot interrupt non-running process '{}' (current state '{}')", getWorldProcessId(), state);
        } else {
            // Actually change the state
            applyState(WorldProcessState.INTERRUPTED);
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
     * Also note that a process can only be stopped if it has no more {@link #hasActiveChildProcesses() running child processes}.
     */
    public void stop() {

        if (state != WorldProcessState.INTERRUPTED) {
            LOGGER.warn("Cannot suspend non-interrupted process '{}' (current state '{}')", getWorldProcessId(), state);
        } else if (state == WorldProcessState.STOPPED) {
            LOGGER.warn("Cannot stop already stopped process '{}' again", getWorldProcessId());
        } else if (hasActiveChildProcesses()) {
            LOGGER.warn("Cannot stop process '{}' because it still has active child processes (current state '{}')", getWorldProcessId(), state);
        } else {
            // Actually change the state
            applyState(WorldProcessState.STOPPED);

            // Unregister the stopped process from its parent
            if (getSingleParent() instanceof Process) {
                ((Process) getSingleParent()).childProcesses.remove(this);
            }
        }
    }

    /**
     * Returns the child processes the process launched through the {@link #createChild()} method.
     * If you want to launch a new process from the {@link #getExecutor() program executor} of this process, you can use that method as well.
     * In order to remove processes from this list, they need to be {@link #stop(boolean) stopped}.
     *
     * @return The child processes which have been launched by this process.
     */
    public List<Process> getChildProcesses() {

        return Collections.unmodifiableList(childProcesses);
    }

    /**
     * Returns all {@link #getChildProcesses() child processes} of this process, and all child processes of those processes etc. recursively.
     * That means that all processes this process launched directly or indirectly (that means that a child process launched another child process) are returned by this method.
     * Essentially, it just traverses the process tree recursively from this process onwards and returns the collected processes.
     *
     * @return All processes that originated from this process directly or indirectly.
     */
    public List<Process> getAllChildProcesses() {

        List<Process> allChildProcesses = new ArrayList<>();
        for (Process directChildProcess : childProcesses) {
            allChildProcesses.add(directChildProcess);
            allChildProcesses.addAll(directChildProcess.getAllChildProcesses());
        }

        return allChildProcesses;
    }

    /**
     * Returns whether this process has any {@link #getChildProcesses() child processes} which are still running and haven't been {@link #stop() stopped} yet.
     * Although processes with the state {@link WorldProcessState#STOPPED} are extremely rarely found in the child process list, they can still occur under very special circumstances.
     * Therefore, you should use this method to check whether a process has any children instead of just checking the size of the child process list.
     *
     * @return Whether this process has any waiting, running, or interrupted child processes.
     */
    public boolean hasActiveChildProcesses() {

        for (Process childProcess : childProcesses) {
            if (childProcess.getState() != WorldProcessState.STOPPED) {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns the root process which is the parent of every other process somewhere in the tree.
     * It is the topmost process and probably has the {@link #getPid() PID} {@code 0}.
     *
     * @return The process all other processes originates from directly or indirectly.
     */
    public Process getRootProcess() {

        Object parent = getSingleParent();

        if (parent instanceof Process) {
            return ((Process) parent).getRootProcess();
        } else {
            // If this process doesn't have another process as its parent, it must be the root process
            return this;
        }
    }

    /**
     * Returns the {@link ProcessModule} which is hosting the {@link #getRootProcess() root process} that is the direct or indirect parent of every other process.
     *
     * @return The process module the process belongs to.
     */
    public ProcessModule getProcessModule() {

        return (ProcessModule) getRootProcess().getSingleParent();
    }

    /**
     * Returns the {@link OperatingSystem} which is hosting the {@link #getRootProcess() root process} that is the direct or indirect parent of every other process.
     * Note that the OS is not directly holding the process. Instead, the {@link ProcessModule} has that responsibility.
     *
     * @return The operating system the process belongs to.
     */
    public OperatingSystem getOs() {

        return getProcessModule().getSingleParent();
    }

    /**
     * Resolves the {@link TaskProcess} that runs the {@link Session} this process is part of.
     * Effectively, this process is running under the {@link User} which is set inside that session.
     * Note that the session process is internally retrieved by returning the first parent (or parent of a parent ...) process that runs a session.
     * The session process A of a session process B is not the session process B itself, but instead the first session process parent of B ({@code A != B}).
     *
     * @return The session process this process is running under.
     */
    public TaskProcess getSessionProcess() {

        Object parent = getSingleParent();

        if (parent instanceof TaskProcess && ((TaskProcess) parent).getTaskExecutor() instanceof Session) {
            return (TaskProcess) parent;
        } else if (parent instanceof Process) {
            return ((Process) parent).getSessionProcess();
        } else {
            return null;
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

        TaskProcess sessionProcess = getSessionProcess();
        return sessionProcess == null ? null : (Session) sessionProcess.getTaskExecutor();
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
     * Initializes the process by generating a {@link #getPid() PID} for the process and setting up some other stuff which requires knowledge about the processe's parent.
     * Therefore, this method should only be called once the process has been added to its parent.
     * Please note that this method should not be used publicly. Instead, it is automatically called by internal methods related to process management.
     *
     * @throws Exception If something goes wrong while initializing the process.
     */
    protected void initialize() throws Exception {

        // Generate a new PID
        pid = getProcessModule().nextPid();
    }

    /**
     * Launches an already created {@link Process} object as a {@link #getChildProcesses() child process} of this process.
     * That includes {@link #initialize() initializing} and therefore starting the process, as well as running any executables (e.g. tasks) it should be executing.
     * The initialization process will be conducted immediately and is finished when this method returns.
     *
     * @param childProcess The process object which should be launched as a child of this process.
     * @throws IllegalStateException If this process is not in {@link #getState() state} {@link WorldProcessState#RUNNING}.
     * @throws IllegalArgumentException If the given child process is not in {@link #getState() state} {@link WorldProcessState#WAITING}.
     *         That would be the case if it has already been launched once.
     * @throws Exception If something goes wrong while initializing and starting the process.
     */
    public void launchChild(Process childProcess) throws Exception {

        Validate.validState(state == WorldProcessState.RUNNING, "A non-running process is not able to launch a child process");
        Validate.isTrue(childProcess.getState() == WorldProcessState.WAITING, "Cannot relaunch a child process which has already been launched once");

        childProcesses.add(childProcess);

        try {
            childProcess.initialize();
        } catch (Exception e) {
            childProcesses.remove(childProcess);
            throw e;
        }
    }

}
