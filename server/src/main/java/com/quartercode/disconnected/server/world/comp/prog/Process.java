/*
 * This file is part of Disconnected.
 * Copyright (c) 2013 QuarterCode <http://www.quartercode.com/>
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

import static com.quartercode.classmod.extra.func.Priorities.*;
import static com.quartercode.classmod.factory.ClassmodFactory.factory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.quartercode.classmod.extra.conv.CFeatureHolder;
import com.quartercode.classmod.extra.func.FunctionDefinition;
import com.quartercode.classmod.extra.func.FunctionExecutor;
import com.quartercode.classmod.extra.func.FunctionInvocation;
import com.quartercode.classmod.extra.prop.CollectionPropertyDefinition;
import com.quartercode.classmod.extra.prop.PropertyDefinition;
import com.quartercode.classmod.extra.storage.ReferenceStorage;
import com.quartercode.classmod.extra.storage.StandardStorage;
import com.quartercode.classmod.extra.valuefactory.CloneValueFactory;
import com.quartercode.classmod.extra.valuefactory.ConstantValueFactory;
import com.quartercode.classmod.factory.CollectionPropertyDefinitionFactory;
import com.quartercode.classmod.factory.FunctionDefinitionFactory;
import com.quartercode.classmod.factory.PropertyDefinitionFactory;
import com.quartercode.disconnected.server.registry.ServerRegistries;
import com.quartercode.disconnected.server.registry.WorldProgram;
import com.quartercode.disconnected.server.sim.scheduler.SchedulerUser;
import com.quartercode.disconnected.server.world.comp.file.ContentFile;
import com.quartercode.disconnected.server.world.comp.file.File;
import com.quartercode.disconnected.server.world.comp.os.OS;
import com.quartercode.disconnected.server.world.comp.user.User;
import com.quartercode.disconnected.server.world.util.WorldChildFeatureHolder;
import com.quartercode.disconnected.server.world.util.WorldFeatureHolder;
import com.quartercode.disconnected.shared.event.comp.prog.control.SBPWorldProcessUserInterruptCommand;
import com.quartercode.disconnected.shared.util.registry.Registries;
import com.quartercode.disconnected.shared.util.registry.extra.NamedValueUtils;
import com.quartercode.disconnected.shared.world.comp.file.FileRights;
import com.quartercode.disconnected.shared.world.comp.prog.SBPWorldProcessUserId;
import com.quartercode.disconnected.shared.world.comp.prog.WorldProcessId;
import com.quartercode.disconnected.shared.world.comp.prog.WorldProcessState;
import com.quartercode.eventbridge.bridge.Bridge;

/**
 * This class represents a process which is basically a running instance of a program.
 * On the creation, the program object will be backed up, as well as a new program executor instance.
 * 
 * @param <P> The type of the parent {@link CFeatureHolder} which houses the process somehow.
 * @see Program
 * @see ProgramExecutor
 */
public abstract class Process<P extends CFeatureHolder> extends WorldChildFeatureHolder<P> {

    private static final Logger                                                                  LOGGER = LoggerFactory.getLogger(Process.class);

    // ----- Properties -----

    /**
     * The unique id the process has.
     * It is used to identify the process.
     */
    public static final PropertyDefinition<Integer>                                              PID;

    /**
     * The {@link File} which contains the {@link Program} the process runs.<br>
     * <br>
     * Exceptions that can occur when setting:
     * 
     * <table>
     * <tr>
     * <th>Exception</th>
     * <th>When?</th>
     * </tr>
     * <tr>
     * <td>{@link IllegalArgumentException}</td>
     * <td>The provided source file does not contain a program object.</td>
     * </tr>
     * </table>
     */
    public static final PropertyDefinition<ContentFile>                                          SOURCE;

    /**
     * The environment variables that are assigned to the process.
     * See {@link EnvVariable} for more information.
     */
    public static final PropertyDefinition<Map<String, String>>                                  ENVIRONMENT;

    /**
     * The {@link WorldProcessState} which defines the global state of the process as seen by the OS.
     * It stores whether the process is running, interrupted etc.<br>
     * Note that using the {@link #SUSPEND}/{@link #RESUME}/{@link #INTERRUPT}/{@link #STOP} methods is preferred over directly accessing the property.
     */
    public static final PropertyDefinition<WorldProcessState>                                    STATE;

    /**
     * The {@link ProgramExecutor} which contains the logic of the process.
     * This value is only available after an {@link #INITIALIZE} call.<br>
     * Argument properties should be passed to this object.<br>
     * The {@link ProgramExecutor#RUN} method on the stored value finally starts the process.
     */
    public static final PropertyDefinition<ProgramExecutor>                                      EXECUTOR;

    /**
     * The child processes the process launched.<br>
     * Note that using the {@link #CREATE_CHILD} method is preferred over directly accessing the property.
     */
    public static final CollectionPropertyDefinition<Process<?>, List<Process<?>>>               CHILDREN;

    /**
     * The {@link ProcStateListener process state listeners} which are called after the process {@link #STATE} has changed.
     * Listeners can be directly added to this list and must not be removed after the program execution has finished.
     */
    public static final CollectionPropertyDefinition<ProcStateListener, List<ProcStateListener>> STATE_LISTENERS;

    /**
     * If the process was launched by an SBP, this property stores the {@link SBPWorldProcessUserId} that identifies the SBP and the world process user.
     * Such a remote launch is done using a command event.
     */
    public static final PropertyDefinition<SBPWorldProcessUserId>                                WORLD_PROCESS_USER;

    static {

        PID = factory(PropertyDefinitionFactory.class).create("pid", new StandardStorage<>());

        SOURCE = factory(PropertyDefinitionFactory.class).create("source", new ReferenceStorage<>());
        SOURCE.addSetterExecutor("checkFileContent", Process.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                Validate.isInstanceOf(Program.class, ((ContentFile) arguments[0]).getObj(ContentFile.CONTENT), "Source must contain a program");
                return invocation.next(arguments);
            }

        }, LEVEL_6);

        ENVIRONMENT = factory(PropertyDefinitionFactory.class).create("environment", new StandardStorage<>(), new CloneValueFactory<>(new HashMap<>()));

        STATE = factory(PropertyDefinitionFactory.class).create("state", new StandardStorage<>(), new ConstantValueFactory<>(WorldProcessState.RUNNING));
        STATE.addSetterExecutor("callStateListeners", Process.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                CFeatureHolder holder = invocation.getCHolder();
                WorldProcessState oldState = holder.getObj(STATE);

                invocation.next(arguments);

                WorldProcessState newState = (WorldProcessState) arguments[0];
                for (ProcStateListener stateListener : holder.getColl(STATE_LISTENERS)) {
                    stateListener.invoke(ProcStateListener.ON_STATE_CHANGE, holder, oldState, newState);
                }

                return null;
            }

        }, LEVEL_7);
        STATE.addSetterExecutor("setExecutorSchedulerActive", Process.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                ProgramExecutor executor = invocation.getCHolder().getObj(EXECUTOR);

                if (executor instanceof SchedulerUser) {
                    boolean active = ((WorldProcessState) arguments[0]).isTickState();
                    executor.get(SchedulerUser.SCHEDULER).setActive(active);
                }

                return invocation.next(arguments);
            }

        }, LEVEL_6);

        EXECUTOR = factory(PropertyDefinitionFactory.class).create("executor", new StandardStorage<>());
        CHILDREN = factory(CollectionPropertyDefinitionFactory.class).create("children", new StandardStorage<>(), new CloneValueFactory<>(new ArrayList<>()));
        STATE_LISTENERS = factory(CollectionPropertyDefinitionFactory.class).create("stateListeners", new StandardStorage<>(), new CloneValueFactory<>(new ArrayList<>()));
        WORLD_PROCESS_USER = factory(PropertyDefinitionFactory.class).create("worldProcessUser", new StandardStorage<>());

    }

    // ----- Functions -----

    /**
     * Returns true if the given {@link WorldProcessState} is applied to this process and all child processes (recursively).
     * It stores if the process is running, interrupted etc.
     * 
     * <table>
     * <tr>
     * <th>Index</th>
     * <th>Type</th>
     * <th>Parameter</th>
     * <th>Description</th>
     * </tr>
     * <tr>
     * <td>0</td>
     * <td>{@link WorldProcessState}</td>
     * <td>state</td>
     * <td>The process state to check all children for recursively.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Boolean>                                              IS_STATE_APPLIED_RECURSIVELY;

    /**
     * Suspends the execution temporarily, tick updates will be ignored.
     * Suspension only works when the execution is running. During an interruption, an execution can't be suspended.
     * The state change can also apply to every child process using the recursive parameter.
     * 
     * <table>
     * <tr>
     * <th>Index</th>
     * <th>Type</th>
     * <th>Parameter</th>
     * <th>Description</th>
     * </tr>
     * <tr>
     * <td>0</td>
     * <td>{@link Boolean}</td>
     * <td>recursive</td>
     * <td>True if the state change should affect all child processes.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>                                                 SUSPEND;

    /**
     * Resumes a suspended process.
     * Resuming only works if the execution is suspended. An interrupted process can't be resumed.
     * The state change can also apply to every child process using the recursive parameter.
     * 
     * <table>
     * <tr>
     * <th>Index</th>
     * <th>Type</th>
     * <th>Parameter</th>
     * <th>Description</th>
     * </tr>
     * <tr>
     * <td>0</td>
     * <td>{@link Boolean}</td>
     * <td>recursive</td>
     * <td>True if the state change should affect all child processes.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>                                                 RESUME;

    /**
     * Interrupts the execution friendly and expresses that it should be stopped as soon as possible.
     * If the process notes the interruption, it should try to execute last activities and then the stop the execution.
     * Interruption only works if the execution is running.
     * The state change can also apply to every child process using the recursive parameter.
     * 
     * <table>
     * <tr>
     * <th>Index</th>
     * <th>Type</th>
     * <th>Parameter</th>
     * <th>Description</th>
     * </tr>
     * <tr>
     * <td>0</td>
     * <td>{@link Boolean}</td>
     * <td>recursive</td>
     * <td>True if the state change should affect the child processes.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>                                                 INTERRUPT;

    /**
     * Forces the process to stop the execution.
     * This will act like {@link #SUSPEND}, apart from the fact that a stopped process won't ever be able to resume/restart.
     * The forced stopping action should only be used if the further execution of the process must be stopped or when the interruption finished.
     * Calling this method on an uninterrupted process might cause internal conflicts and is therefore disallowed.
     * 
     * <table>
     * <tr>
     * <th>Index</th>
     * <th>Type</th>
     * <th>Parameter</th>
     * <th>Description</th>
     * </tr>
     * <tr>
     * <td>0</td>
     * <td>{@link Boolean}</td>
     * <td>recursive</td>
     * <td>True if the state change should affect the child processes.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>                                                 STOP;

    /**
     * Returns all child processes of this process, and all child processes of those processes etc.
     * That means that all processes this process launched directly or indirectly (that means that a child process launched a child process) are returned by this method.
     * Essentially, it just traverses the process tree recursively from this process onwards and returns the collected processes.
     */
    public static final FunctionDefinition<List<Process<?>>>                                     GET_ALL_CHILDREN;

    /**
     * Creates a new empty {@link ChildProcess} which uses the same environment variables as this one.
     * You can run the returned process after creation using {@link #INITIALIZE} and then {@link ProgramExecutor#RUN} on {@link #EXECUTOR}.
     */
    public static final FunctionDefinition<ChildProcess>                                         CREATE_CHILD;

    /**
     * Returns the root {@link RootProcess} which is the parent of every other process somewhere in the tree.
     */
    public static final FunctionDefinition<RootProcess>                                          GET_ROOT;

    /**
     * Returns the {@link OS operating system} which is hosting the {@link RootProcess} that is the direct or indirect parent of every other process.
     */
    public static final FunctionDefinition<OS>                                                   GET_OS;

    /**
     * Resolves the {@link Session} process this process is running under.
     * This process is running with the rights of that session.
     */
    public static final FunctionDefinition<Process<?>>                                           GET_SESSION_PROCESS;

    /**
     * Resolves the actual {@link Session} executor this process is running under.
     * This process is running with the rights of that session.
     */
    public static final FunctionDefinition<Session>                                              GET_SESSION;

    /**
     * Resolves the {@link User} this process is running under.
     * This uses the {@link #GET_SESSION} function for resolving the Session} object.
     */
    public static final FunctionDefinition<User>                                                 GET_USER;

    /**
     * Returns the {@link Program} which is ran by the process.
     * It is stored inside the {@link #SOURCE} file.
     */
    public static final FunctionDefinition<Program>                                              GET_PROGRAM;

    /**
     * Returns a {@link WorldProcessId} object that identifies this process.
     * The process is identified using the id of the computer it is running on, as well as the actual {@link #PID process id (pid)}.
     */
    public static final FunctionDefinition<WorldProcessId>                                       GET_WORLD_PROCESS_ID;

    /**
     * Initializes the process using the {@link Program} that is stored in the set source {@link ContentFile}.
     * Initialization means setting the {@link #PID} and creating a {@link ProgramExecutor} instance.
     * Please note that the process needs to be launched using the {@link ProgramExecutor#RUN} method on the {@link #EXECUTOR} after intialization.
     * 
     * <table>
     * <tr>
     * <th>Index</th>
     * <th>Type</th>
     * <th>Parameter</th>
     * <th>Description</th>
     * </tr>
     * <tr>
     * <td>0</td>
     * <td>{@link Integer}</td>
     * <td>pid</td>
     * <td>The {@link #PID} of the new process (can be generated using {@link ProcModule#NEXT_PID}). Its uniqueness is validated by the function.</td>
     * </tr>
     * </table>
     * 
     * <table>
     * <tr>
     * <th>Exception</th>
     * <th>When?</th>
     * </tr>
     * <tr>
     * <td>{@link IllegalArgumentException}</td>
     * <td>The given pid is already used by another process.</td>
     * </tr>
     * <tr>
     * <td>{@link IllegalStateException}</td>
     * <td>The {@link User} the process runs under hasn't got the read and execute rights on the {@link #SOURCE} file.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>                                                 INITIALIZE;

    static {

        IS_STATE_APPLIED_RECURSIVELY = factory(FunctionDefinitionFactory.class).create("isStateApplied", new Class[] { WorldProcessState.class });
        IS_STATE_APPLIED_RECURSIVELY.addExecutor("default", Process.class, new FunctionExecutor<Boolean>() {

            @Override
            public Boolean invoke(FunctionInvocation<Boolean> invocation, Object... arguments) {

                CFeatureHolder holder = invocation.getCHolder();
                boolean stateApplied = true;

                if (holder.getObj(STATE) != arguments[0]) {
                    stateApplied = false;
                } else {
                    for (Process<?> child : holder.getColl(CHILDREN)) {
                        if (!child.invoke(IS_STATE_APPLIED_RECURSIVELY)) {
                            stateApplied = false;
                            break;
                        }
                    }
                }

                invocation.next(arguments);
                return stateApplied;
            }

        });
        SUSPEND = factory(FunctionDefinitionFactory.class).create("suspend", new Class[] { Boolean.class });
        SUSPEND.addExecutor("default", Process.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                CFeatureHolder holder = invocation.getCHolder();

                if (holder.getObj(STATE) != WorldProcessState.RUNNING) {
                    LOGGER.warn("Cannot suspend non-running process '{}' (current state '{}, program executor '{}')", holder.invoke(GET_WORLD_PROCESS_ID), holder.getObj(STATE), holder.getObj(EXECUTOR).getClass());
                } else {
                    holder.setObj(STATE, WorldProcessState.SUSPENDED);

                    if ((Boolean) arguments[0]) {
                        for (Process<?> child : holder.getColl(CHILDREN)) {
                            child.invoke(SUSPEND, true);
                        }
                    }
                }

                return invocation.next(arguments);
            }

        });
        RESUME = factory(FunctionDefinitionFactory.class).create("resume", new Class[] { Boolean.class });
        RESUME.addExecutor("default", Process.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                CFeatureHolder holder = invocation.getCHolder();

                if (holder.getObj(STATE) != WorldProcessState.SUSPENDED) {
                    LOGGER.warn("Cannot resume non-suspended process '{}' (current state '{}, program executor '{}')", holder.invoke(GET_WORLD_PROCESS_ID), holder.getObj(STATE), holder.getObj(EXECUTOR).getClass());
                } else {
                    holder.setObj(STATE, WorldProcessState.RUNNING);

                    if ((Boolean) arguments[0]) {
                        for (Process<?> child : holder.getColl(CHILDREN)) {
                            child.invoke(RESUME, true);
                        }
                    }
                }

                return invocation.next(arguments);
            }

        });
        INTERRUPT = factory(FunctionDefinitionFactory.class).create("interrupt", new Class[] { Boolean.class });
        INTERRUPT.addExecutor("default", Process.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                CFeatureHolder holder = invocation.getCHolder();

                if (holder.getObj(STATE) != WorldProcessState.RUNNING) {
                    LOGGER.warn("Cannot interrupt non-running process '{}' (current state '{}, program executor '{}')", holder.invoke(GET_WORLD_PROCESS_ID), holder.getObj(STATE), holder.getObj(EXECUTOR).getClass());
                } else {
                    holder.setObj(STATE, WorldProcessState.INTERRUPTED);

                    if ((Boolean) arguments[0]) {
                        for (Process<?> child : holder.getColl(CHILDREN)) {
                            child.invoke(INTERRUPT, true);
                        }
                    }
                }

                return invocation.next(arguments);
            }

        });
        STOP = factory(FunctionDefinitionFactory.class).create("stop", new Class[] { Boolean.class });
        STOP.addExecutor("default", Process.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                Process<?> holder = (Process<?>) invocation.getCHolder();

                if (holder.getObj(STATE) != WorldProcessState.INTERRUPTED) {
                    LOGGER.warn("Cannot suspend non-interrupted process '{}' (current state '{}, program executor '{}')", holder.invoke(GET_WORLD_PROCESS_ID), holder.getObj(STATE), holder.getObj(EXECUTOR).getClass());
                } else if (holder.getObj(STATE) == WorldProcessState.STOPPED) {
                    LOGGER.warn("Cannot stop already stopped process '{}' again (current state '{}, program executor '{}')", holder.invoke(GET_WORLD_PROCESS_ID), holder.getObj(STATE), holder.getObj(EXECUTOR).getClass());
                } else {
                    holder.setObj(STATE, WorldProcessState.STOPPED);

                    // Unregister stopped process from parent
                    if (holder.getParent() != null) {
                        holder.getParent().removeFromColl(CHILDREN, holder);
                    }

                    // Promote the old child processes of this process to child processes of the parent process
                    for (Process<?> child : new ArrayList<>(holder.getColl(CHILDREN))) {
                        holder.removeFromColl(CHILDREN, child);
                        holder.getParent().addToColl(CHILDREN, child);
                    }

                    if ((Boolean) arguments[0]) {
                        for (Process<?> child : holder.getColl(CHILDREN)) {
                            child.invoke(SUSPEND, true);
                        }
                    }
                }

                return invocation.next(arguments);
            }

        });

        GET_ALL_CHILDREN = factory(FunctionDefinitionFactory.class).create("getAllChildren", new Class[0]);
        GET_ALL_CHILDREN.addExecutor("default", Process.class, new FunctionExecutor<List<Process<?>>>() {

            @Override
            public List<Process<?>> invoke(FunctionInvocation<List<Process<?>>> invocation, Object... arguments) {

                List<Process<?>> children = getAllChildren((Process<?>) invocation.getCHolder());
                invocation.next(arguments);
                return children;
            }

            private List<Process<?>> getAllChildren(Process<?> parent) {

                List<Process<?>> allChildren = new ArrayList<>();
                for (Process<?> directChild : parent.getColl(CHILDREN)) {
                    allChildren.add(directChild);
                    allChildren.addAll(getAllChildren(directChild));
                }

                return allChildren;
            }

        });

        CREATE_CHILD = factory(FunctionDefinitionFactory.class).create("createChild", new Class[0]);
        CREATE_CHILD.addExecutor("default", Process.class, new FunctionExecutor<ChildProcess>() {

            @Override
            public ChildProcess invoke(FunctionInvocation<ChildProcess> invocation, Object... arguments) {

                CFeatureHolder holder = invocation.getCHolder();

                ChildProcess process = new ChildProcess();
                process.setParent((Process<?>) holder);
                process.setObj(ENVIRONMENT, new HashMap<>(holder.getObj(ENVIRONMENT)));
                holder.addToColl(CHILDREN, process);

                invocation.next(arguments);
                return process;
            }

        });

        GET_ROOT = factory(FunctionDefinitionFactory.class).create("getRoot", new Class[0]);
        GET_ROOT.addExecutor("default", Process.class, new FunctionExecutor<RootProcess>() {

            @Override
            public RootProcess invoke(FunctionInvocation<RootProcess> invocation, Object... arguments) {

                CFeatureHolder holder = invocation.getCHolder();
                RootProcess root = null;

                if (holder instanceof RootProcess) {
                    root = (RootProcess) holder;
                } else {
                    root = ((Process<?>) holder).getParent().invoke(GET_ROOT);
                }

                invocation.next(arguments);
                return root;
            }

        });

        GET_OS = factory(FunctionDefinitionFactory.class).create("getOS", new Class[0]);
        GET_OS.addExecutor("default", Process.class, new FunctionExecutor<OS>() {

            @Override
            public OS invoke(FunctionInvocation<OS> invocation, Object... arguments) {

                OS os = invocation.getCHolder().invoke(GET_ROOT).getParent().getParent();
                invocation.next(arguments);
                return os;
            }

        });

        GET_SESSION_PROCESS = factory(FunctionDefinitionFactory.class).create("getSessionProcess", new Class[0]);
        GET_SESSION_PROCESS.addExecutor("default", Process.class, new FunctionExecutor<Process<?>>() {

            @Override
            public Process<?> invoke(FunctionInvocation<Process<?>> invocation, Object... arguments) {

                CFeatureHolder holder = invocation.getCHolder();
                Process<?> sessionProcess = null;

                // Error check
                if ( ((Process<?>) holder).getParent() != null) {
                    if ( ((Process<?>) holder).getParent().getObj(EXECUTOR) instanceof Session) {
                        sessionProcess = (Process<?>) ((Process<?>) holder).getParent();
                    } else {
                        // Ask parent process
                        sessionProcess = ((Process<?>) holder).getParent().invoke(GET_SESSION_PROCESS);
                    }
                }

                invocation.next(arguments);
                return sessionProcess;
            }

        });

        GET_SESSION = factory(FunctionDefinitionFactory.class).create("getSession", new Class[0]);
        GET_SESSION.addExecutor("default", Process.class, new FunctionExecutor<Session>() {

            @Override
            public Session invoke(FunctionInvocation<Session> invocation, Object... arguments) {

                Process<?> sessionProcess = invocation.getCHolder().invoke(GET_SESSION_PROCESS);
                if (sessionProcess == null) {
                    return null;
                } else {
                    Session session = (Session) sessionProcess.getObj(EXECUTOR);
                    invocation.next(arguments);
                    return session;
                }
            }

        });

        GET_USER = factory(FunctionDefinitionFactory.class).create("getUser", new Class[0]);
        GET_USER.addExecutor("default", Process.class, new FunctionExecutor<User>() {

            @Override
            public User invoke(FunctionInvocation<User> invocation, Object... arguments) {

                Session session = invocation.getCHolder().invoke(GET_SESSION);
                if (session == null) {
                    return null;
                } else {
                    User user = session.getObj(Session.USER);
                    invocation.next(arguments);
                    return user;
                }
            }

        });

        GET_PROGRAM = factory(FunctionDefinitionFactory.class).create("getProgram", new Class[0]);
        GET_PROGRAM.addExecutor("default", Process.class, new FunctionExecutor<Program>() {

            @Override
            public Program invoke(FunctionInvocation<Program> invocation, Object... arguments) {

                CFeatureHolder holder = invocation.getCHolder();
                Program program = (Program) holder.getObj(SOURCE).getObj(ContentFile.CONTENT);

                invocation.next(arguments);
                return program;
            }

        });

        GET_WORLD_PROCESS_ID = factory(FunctionDefinitionFactory.class).create("getWorldProcessId", new Class[0]);
        GET_WORLD_PROCESS_ID.addExecutor("default", Process.class, new FunctionExecutor<WorldProcessId>() {

            @Override
            public WorldProcessId invoke(FunctionInvocation<WorldProcessId> invocation, Object... arguments) {

                CFeatureHolder holder = invocation.getCHolder();

                int pid = holder.getObj(Process.PID);
                String computerId = holder.invoke(Process.GET_OS).getParent().getId();
                WorldProcessId worldProcessId = new WorldProcessId(computerId, pid);

                invocation.next(arguments);
                return worldProcessId;
            }

        });

        INITIALIZE = factory(FunctionDefinitionFactory.class).create("initialize", new Class[] { Integer.class });
        INITIALIZE.addExecutor("setPid", Process.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                CFeatureHolder holder = invocation.getCHolder();

                int pid = (int) arguments[0];

                // Check given pid
                Set<Integer> existingPids = new HashSet<>();
                Process<?> root = holder.invoke(GET_ROOT);
                existingPids.add(root.getObj(PID));
                for (Process<?> process : root.invoke(GET_ALL_CHILDREN)) {
                    existingPids.add(process.getObj(PID));
                }
                Validate.isTrue(!existingPids.contains(pid), "Pid {} is already used by another process", pid);

                holder.setObj(PID, pid);
                return invocation.next(arguments);
            }

        }, DEFAULT + SUBLEVEL_7);
        INITIALIZE.addExecutor("setExecutor", Process.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                CFeatureHolder holder = invocation.getCHolder();

                // Check read and execution right on source file
                ContentFile source = holder.getObj(SOURCE);
                User user = holder.invoke(GET_USER);
                if (!source.invoke(File.HAS_RIGHT, user, FileRights.READ) || !source.invoke(File.HAS_RIGHT, user, FileRights.EXECUTE)) {
                    throw new IllegalStateException("Cannot initialize process: No read right and execute right on file");
                }

                // Retrieve the program data object
                String programName = ((Program) source.getObj(ContentFile.CONTENT)).getObj(Program.NAME);
                WorldProgram programData = NamedValueUtils.getByName(Registries.get(ServerRegistries.WORLD_PROGRAMS), programName);
                Validate.validState(programData != null, "Cannot find world program with name '%s' for launching a process", programName);

                // Create new executor
                ProgramExecutor executor;
                try {
                    executor = (ProgramExecutor) programData.getType().newInstance();
                } catch (Exception e) {
                    throw new RuntimeException("Unexpected exception during initialization of new program executor (class '" + programData.getType().getName() + "'", e);
                }

                // Set new executor
                holder.setObj(EXECUTOR, executor);

                return invocation.next(arguments);
            }

        }, DEFAULT + SUBLEVEL_5);
        INITIALIZE.addExecutor("registerInterruptionCPICommandSender", Process.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                invocation.getCHolder().addToColl(STATE_LISTENERS, new SendClientProcessInterruptCommandOnInterruptPSListener());

                return invocation.next(arguments);
            }

        }, DEFAULT + SUBLEVEL_2);

    }

    /**
     * A {@link ProcStateListener} that sends a {@link SBPWorldProcessUserInterruptCommand} when the {@link Process} it is attached to is interrupted.
     * Note that the event is only sent if the {@link Process#WORLD_PROCESS_USER} property is not {@code null}.
     * Also note that the listener is added to each new process by default.
     */
    public static class SendClientProcessInterruptCommandOnInterruptPSListener extends WorldFeatureHolder implements ProcStateListener {

        static {

            ON_STATE_CHANGE.addExecutor("sendClientProcessInterruptCommandOnInterrupt", SendClientProcessInterruptCommandOnInterruptPSListener.class, new FunctionExecutor<Void>() {

                @Override
                public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                    Object newState = arguments[2];

                    if (newState == WorldProcessState.INTERRUPTED) {
                        SBPWorldProcessUserId wpuId = ((Process<?>) arguments[0]).getObj(WORLD_PROCESS_USER);

                        if (wpuId != null) {
                            Bridge bridge = ((SendClientProcessInterruptCommandOnInterruptPSListener) invocation.getCHolder()).getBridge();
                            bridge.send(new SBPWorldProcessUserInterruptCommand(wpuId));
                        }
                    }

                    return invocation.next(arguments);
                }

            });

        }

    }

}
