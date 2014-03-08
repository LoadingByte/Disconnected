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

package com.quartercode.disconnected.world.comp.program;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.Validate;
import com.quartercode.disconnected.mocl.base.FeatureDefinition;
import com.quartercode.disconnected.mocl.base.FeatureHolder;
import com.quartercode.disconnected.mocl.base.def.AbstractFeatureDefinition;
import com.quartercode.disconnected.mocl.extra.ExecutorInvokationException;
import com.quartercode.disconnected.mocl.extra.FunctionDefinition;
import com.quartercode.disconnected.mocl.extra.FunctionExecutionException;
import com.quartercode.disconnected.mocl.extra.FunctionExecutor;
import com.quartercode.disconnected.mocl.extra.Lockable;
import com.quartercode.disconnected.mocl.extra.Prioritized;
import com.quartercode.disconnected.mocl.extra.def.LockableFEWrapper;
import com.quartercode.disconnected.mocl.extra.def.ObjectProperty;
import com.quartercode.disconnected.mocl.extra.def.ReferenceProperty;
import com.quartercode.disconnected.mocl.util.CollectionPropertyAccessorFactory;
import com.quartercode.disconnected.mocl.util.FunctionDefinitionFactory;
import com.quartercode.disconnected.mocl.util.PropertyAccessorFactory;
import com.quartercode.disconnected.world.WorldChildFeatureHolder;
import com.quartercode.disconnected.world.comp.file.ContentFile;
import com.quartercode.disconnected.world.comp.file.File;
import com.quartercode.disconnected.world.comp.os.EnvironmentVariable;
import com.quartercode.disconnected.world.comp.os.OperatingSystem;
import com.quartercode.disconnected.world.comp.os.Session;
import com.quartercode.disconnected.world.comp.os.User;
import com.quartercode.disconnected.world.comp.program.event.IPCMessageEvent;
import com.quartercode.disconnected.world.comp.program.event.ProcessEvent;

/**
 * This class represents a process which is basically a running instance of a program.
 * On the creation, the program object will be backed up, as well as a new program executor instance.
 * 
 * @param <P> The type of the parent {@link FeatureHolder} which houses the process somehow.
 * @see Program
 * @see ProgramExecutor
 */
public abstract class Process<P extends FeatureHolder> extends WorldChildFeatureHolder<P> {

    /**
     * The process state defines the global state of the process the os can see.
     * It stores if the process is running, interrupted etc.
     */
    public static enum ProcessState {

        /**
         * The process is running and the update executes every tick.
         * This is the default state of a process.
         */
        RUNNING (false),
        /**
         * The execution is suspended, tick updates will be ignored.
         */
        SUSPENDED (true),
        /**
         * The execution is interrupted friendly and should be stopped soon.
         * If a process notes this state, it should try to execute last activities and the stop the execution.
         */
        INTERRUPTED (false),
        /**
         * The execution is permanently stopped.
         * If a process is stopped, it won't be able to start again.
         */
        STOPPED (true);

        private boolean lock;

        /**
         * Creates a new process state enumeration entry.
         * 
         * @param lock True if the state should lock the execution of the program it is applied on.
         */
        private ProcessState(boolean lock) {

            this.lock = lock;
        }

        /**
         * Returns True if the state should lock the execution of the program it is applied on.
         * 
         * @return If the state should lock the execution of the program it is applied on.
         */
        public boolean isLock() {

            return lock;
        }

    }

    // ----- Properties -----

    /**
     * The unique id the process has.
     * It is used to identify the process.
     */
    protected static final FeatureDefinition<ObjectProperty<Integer>>             PID;

    /**
     * The {@link File} which contains the program the process runs.
     */
    protected static final FeatureDefinition<ReferenceProperty<ContentFile>>      SOURCE;

    /**
     * The environment variables that are assigned to the process.
     * See {@link EnvironmentVariable} for more information.
     */
    protected static final FeatureDefinition<ObjectProperty<Map<String, String>>> ENVIRONMENT;

    /**
     * The {@link ProcessState} which defines the global state of the process the os can see.
     * It stores if the process is running, interrupted etc.
     */
    protected static final FeatureDefinition<ObjectProperty<ProcessState>>        STATE;

    /**
     * The {@link ProgramExecutor} which contains the logic of the process.
     */
    protected static final FeatureDefinition<ObjectProperty<ProgramExecutor>>     EXECUTOR;

    /**
     * The child processes the process launched.
     */
    protected static final FeatureDefinition<ObjectProperty<List<Process<?>>>>    CHILDREN;

    static {

        PID = new AbstractFeatureDefinition<ObjectProperty<Integer>>("pid") {

            @Override
            public ObjectProperty<Integer> create(FeatureHolder holder) {

                return new ObjectProperty<Integer>(getName(), holder);
            }

        };

        SOURCE = new AbstractFeatureDefinition<ReferenceProperty<ContentFile>>("source") {

            @Override
            public ReferenceProperty<ContentFile> create(FeatureHolder holder) {

                return new ReferenceProperty<ContentFile>(getName(), holder);
            }

        };

        ENVIRONMENT = new AbstractFeatureDefinition<ObjectProperty<Map<String, String>>>("environment") {

            @Override
            public ObjectProperty<Map<String, String>> create(FeatureHolder holder) {

                return new ObjectProperty<Map<String, String>>(getName(), holder, new HashMap<String, String>());
            }

        };

        STATE = new AbstractFeatureDefinition<ObjectProperty<ProcessState>>("state") {

            @Override
            public ObjectProperty<ProcessState> create(FeatureHolder holder) {

                return new ObjectProperty<ProcessState>(getName(), holder, ProcessState.RUNNING);
            }

        };

        EXECUTOR = new AbstractFeatureDefinition<ObjectProperty<ProgramExecutor>>("executor") {

            @Override
            public ObjectProperty<ProgramExecutor> create(FeatureHolder holder) {

                return new ObjectProperty<ProgramExecutor>(getName(), holder);
            }

        };

        CHILDREN = new AbstractFeatureDefinition<ObjectProperty<List<Process<?>>>>("children") {

            @Override
            public ObjectProperty<List<Process<?>>> create(FeatureHolder holder) {

                return new ObjectProperty<List<Process<?>>>(getName(), holder, new ArrayList<Process<?>>());
            }

        };

    }

    // ----- Properties End -----

    // ----- Functions -----

    /**
     * Returns the unique id the process has.
     * It is used to identify the process.
     */
    public static final FunctionDefinition<Integer>                               GET_PID;

    /**
     * Returns the {@link File} which contains the {@link Program} the process runs.
     */
    public static final FunctionDefinition<ContentFile>                           GET_SOURCE;

    /**
     * Changes the {@link File} which contains the {@link Program} the process runs.
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
     * <td>{@link ContentFile}</td>
     * <td>source</td>
     * <td>The new {@link ContentFile} which contains the {@link Program} the process runs.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>                                  SET_SOURCE;

    /**
     * Returns the environment variables that are assigned to the process.
     * See {@link EnvironmentVariable} for more information.
     */
    public static final FunctionDefinition<Map<String, String>>                   GET_ENVIRONMENT;

    /**
     * Changes the environment variables that are assigned to the process.
     * See {@link EnvironmentVariable} for more information.
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
     * <td>{@link Map}&lt;{@link String}, {@link String}&gt;</td>
     * <td>environment</td>
     * <td>The new environment variables for the process.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>                                  SET_ENVIRONMENT;

    /**
     * Returns the {@link ProcessState} which defines the global state of the process the os can see.
     * It stores if the process is running, interrupted etc.
     */
    public static final FunctionDefinition<ProcessState>                          GET_STATE;

    /**
     * Returns true if the given {@link ProcessState} is applied to this process and all child processes (recursively).
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
     * <td>{@link ProcessState}</td>
     * <td>state</td>
     * <td>The {@link ProcessState} to check all children for recursively.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Boolean>                               IS_STATE_APPLIED;

    /**
     * Changes the {@link ProcessState} which defines the global state of the process the os can see.
     * It stores if the process is running, interrupted etc.
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
     * <td>{@link ProcessState}</td>
     * <td>state</td>
     * <td>The new {@link ProcessState}.</td>
     * </tr>
     * <tr>
     * <td>1</td>
     * <td>{@link Boolean}</td>
     * <td>recursive</td>
     * <td>True if the state change should affect all child processes.</td>
     * </tr>
     * </table>
     */
    protected static final FunctionDefinition<Void>                               SET_STATE;

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
    public static final FunctionDefinition<Void>                                  SUSPEND;

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
    public static final FunctionDefinition<Void>                                  RESUME;

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
    public static final FunctionDefinition<Void>                                  INTERRUPT;

    /**
     * Suspends the execution temporarily, tick updates will be ignored.
     * Suspension only works when the execution is running. During an interruption, an execution can't be suspended.
     * The state change can also apply to every child process using the recursive parameter.
     * 
     * Forces the process to stop the execution.
     * This will act like {@link #SUSPEND}, apart from the fact that a stopped process won't ever be able to resume/restart.
     * The forced stopping action should only be used if the further execution of the process must be stopped or when the interruption finished.
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
    public static final FunctionDefinition<Void>                                  STOP;

    /**
     * Returns the {@link ProgramExecutor} which contains the logic of the process.
     */
    public static final FunctionDefinition<ProgramExecutor>                       GET_EXECUTOR;

    /**
     * Returns the direct child processes the process launched.
     * Direct children are present in the child datastructure of this object.
     */
    public static final FunctionDefinition<List<Process<?>>>                      GET_CHILDREN;

    /**
     * Returns all child processes the process launched.
     * That includes all children which are present in the child datastructure of this object, all child objects etc.
     */
    public static final FunctionDefinition<List<Process<?>>>                      GET_ALL_CHILDREN;

    /**
     * Creates a new empty {@link ChildProcess} which uses the same environment variables as this one.
     * You can run the returned process after creation using {@link #LAUNCH}.
     */
    public static final FunctionDefinition<ChildProcess>                          CREATE_CHILD;

    /**
     * Returns the root {@link RootProcess} which is the parent of every other process somewhere in the tree.
     */
    public static final FunctionDefinition<RootProcess>                           GET_ROOT;

    /**
     * Returns the {@link OperatingSystem} which is hosting the {@link RootProcess} which is the parent of every other process.
     */
    public static final FunctionDefinition<OperatingSystem>                       GET_OPERATING_SYSTEM;

    /**
     * Resolves the {@link Session} process this process is running under.
     * This process is running with the rights of that {@link Session}.
     */
    public static final FunctionDefinition<Process<?>>                            GET_SESSION_PROCESS;

    /**
     * Resolves the actual {@link Session} executor this process is running under.
     * This process is running with the rights of that {@link Session}.
     */
    public static final FunctionDefinition<Session>                               GET_SESSION;

    /**
     * Resolves the {@link User} this process is running under.
     * This uses the {@link #GET_SESSION} function for resolving the Session} object.
     */
    public static final FunctionDefinition<User>                                  GET_USER;

    /**
     * Launches a new process using the {@link Program} stored in the set source {@link ContentFile}.
     * The given argument map is handed over to the new {@link ProgramExecutor}.
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
     * <td>{@link Map}&lt;{@link String}, {@link Object}&gt;</td>
     * <td>arguments</td>
     * <td>Some arguments the {@link ProgramExecutor} which is running the process needs to operate.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>                                  LAUNCH;

    /**
     * Sends a new {@link ProcessEvent} to the given receiving {@link Process} with the given payload map.
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
     * <td>{@link Process}</td>
     * <td>receiver</td>
     * <td>The {@link Process} which should receive the message.</td>
     * </tr>
     * <tr>
     * <td>1</td>
     * <td>{@link Map}&lt;{@link String}, {@link Object}&gt;</td>
     * <td>data</td>
     * <td>The data map which contains the payload {@link Object}s to send.</td>
     * </tr>
     * </table>
     * 
     * @see ProcessEvent
     */
    public static final FunctionDefinition<Void>                                  SEND_MESSAGE;

    static {

        GET_PID = FunctionDefinitionFactory.create("getPid", Process.class, PropertyAccessorFactory.createGet(PID));

        GET_SOURCE = FunctionDefinitionFactory.create("getSource", Process.class, PropertyAccessorFactory.createGet(SOURCE));
        SET_SOURCE = FunctionDefinitionFactory.create("setSource", Process.class, new LockableFEWrapper<Void>(PropertyAccessorFactory.createSet(SOURCE)), ContentFile.class);
        SET_SOURCE.addExecutor(Process.class, "checkFileContent", new FunctionExecutor<Void>() {

            @Override
            @Prioritized (Prioritized.DEFAULT + Prioritized.SUBLEVEL_4)
            @Lockable
            public Void invoke(FeatureHolder holder, Object... arguments) throws ExecutorInvokationException {

                Validate.isTrue( ((ContentFile) arguments[0]).get(ContentFile.GET_CONTENT).invoke() instanceof Program, "Source must contain a program");
                return null;
            }

        });

        GET_ENVIRONMENT = FunctionDefinitionFactory.create("getEnvironment", Process.class, PropertyAccessorFactory.createGet(ENVIRONMENT));
        SET_ENVIRONMENT = FunctionDefinitionFactory.create("setEnvironment", Process.class, PropertyAccessorFactory.createSet(ENVIRONMENT), Map.class);

        GET_STATE = FunctionDefinitionFactory.create("getState", Process.class, PropertyAccessorFactory.createGet(STATE));
        IS_STATE_APPLIED = FunctionDefinitionFactory.create("isStateApplied", Process.class, new FunctionExecutor<Boolean>() {

            @Override
            public Boolean invoke(FeatureHolder holder, Object... arguments) throws ExecutorInvokationException {

                if (holder.get(GET_STATE).invoke() != arguments[0]) {
                    return false;
                } else {
                    for (Process<?> child : holder.get(GET_CHILDREN).invoke()) {
                        if (!child.get(IS_STATE_APPLIED).invoke()) {
                            return false;
                        }
                    }
                    return true;
                }
            }

        }, ProcessState.class);
        SET_STATE = FunctionDefinitionFactory.create("setState", Process.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FeatureHolder holder, Object... arguments) throws ExecutorInvokationException {

                holder.get(STATE).set((ProcessState) arguments[0]);
                holder.get(GET_EXECUTOR).invoke().setLocked(holder.get(GET_STATE).invoke().isLock());

                if ((Boolean) arguments[1]) {
                    for (Process<?> child : holder.get(GET_CHILDREN).invoke()) {
                        child.get(SET_STATE).invoke(arguments[0], arguments[1]);
                    }
                }

                return null;
            }

        }, ProcessState.class, Boolean.class);
        SUSPEND = FunctionDefinitionFactory.create("suspend", Process.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FeatureHolder holder, Object... arguments) throws ExecutorInvokationException {

                if (holder.get(GET_STATE).invoke() == ProcessState.RUNNING) {
                    holder.get(SET_STATE).invoke(ProcessState.SUSPENDED, arguments[0]);
                }

                return null;
            }

        }, Boolean.class);
        RESUME = FunctionDefinitionFactory.create("resume", Process.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FeatureHolder holder, Object... arguments) throws ExecutorInvokationException {

                if (holder.get(GET_STATE).invoke() == ProcessState.SUSPENDED) {
                    holder.get(SET_STATE).invoke(ProcessState.RUNNING, arguments[0]);
                }

                return null;
            }

        }, Boolean.class);
        INTERRUPT = FunctionDefinitionFactory.create("interrupt", Process.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FeatureHolder holder, Object... arguments) throws ExecutorInvokationException {

                if (holder.get(GET_STATE).invoke() == ProcessState.RUNNING) {
                    holder.get(SET_STATE).invoke(ProcessState.INTERRUPTED, arguments[0]);
                }

                return null;
            }

        }, Boolean.class);
        STOP = FunctionDefinitionFactory.create("stop", Process.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FeatureHolder holder, Object... arguments) throws ExecutorInvokationException {

                if (holder.get(GET_STATE).invoke() != ProcessState.STOPPED) {
                    holder.get(SET_STATE).invoke(ProcessState.STOPPED, arguments[0]);
                    // Unregister stopped process from parent
                    if ( ((Process<?>) holder).getParent() != null) {
                        ((Process<?>) holder).getParent().get(CHILDREN).get().remove(holder);
                    }
                }

                return null;
            }

        }, Boolean.class);

        GET_EXECUTOR = FunctionDefinitionFactory.create("getExecutor", Process.class, PropertyAccessorFactory.createGet(EXECUTOR));

        GET_CHILDREN = FunctionDefinitionFactory.create("getChildren", Process.class, CollectionPropertyAccessorFactory.createGet(CHILDREN));

        GET_ALL_CHILDREN = FunctionDefinitionFactory.create("getAllChildren", Process.class, new FunctionExecutor<List<Process<?>>>() {

            @Override
            public List<Process<?>> invoke(FeatureHolder holder, Object... arguments) throws ExecutorInvokationException {

                return getAllChildren((Process<?>) holder);
            }

            private List<Process<?>> getAllChildren(Process<?> parent) throws FunctionExecutionException {

                List<Process<?>> allChildren = new ArrayList<Process<?>>();
                for (Process<?> directChild : parent.get(GET_CHILDREN).invoke()) {
                    allChildren.addAll(directChild.get(GET_ALL_CHILDREN).invoke());
                }

                return allChildren;
            }

        });

        CREATE_CHILD = FunctionDefinitionFactory.create("createChild", Process.class, new FunctionExecutor<ChildProcess>() {

            @Override
            public ChildProcess invoke(FeatureHolder holder, Object... arguments) throws ExecutorInvokationException {

                ChildProcess process = new ChildProcess();
                process.setParent((Process<?>) holder);
                process.get(SET_ENVIRONMENT).invoke(new HashMap<String, String>(holder.get(GET_ENVIRONMENT).invoke()));
                return process;
            }

        });

        GET_ROOT = FunctionDefinitionFactory.create("getRoot", Process.class, new FunctionExecutor<RootProcess>() {

            @Override
            public RootProcess invoke(FeatureHolder holder, Object... arguments) throws ExecutorInvokationException {

                if ( ((Process<?>) holder).getParent() == null) {
                    return (RootProcess) holder;
                } else {
                    return ((RootProcess) holder).getParent().get(GET_ROOT).invoke();
                }
            }

        });

        GET_OPERATING_SYSTEM = FunctionDefinitionFactory.create("getOperatingSystem", Process.class, new FunctionExecutor<OperatingSystem>() {

            @Override
            public OperatingSystem invoke(FeatureHolder holder, Object... arguments) throws ExecutorInvokationException {

                return holder.get(GET_ROOT).invoke().getParent();
            }

        });

        GET_SESSION_PROCESS = FunctionDefinitionFactory.create("getSessionProcess", Process.class, new FunctionExecutor<Process<?>>() {

            @Override
            public Process<?> invoke(FeatureHolder holder, Object... arguments) throws ExecutorInvokationException {

                if ( ((Process<?>) holder).getParent() == null) {
                    // Error
                    return null;
                } else if ( ((Process<?>) holder).getParent().get(GET_EXECUTOR).invoke() instanceof Session) {
                    return (Process<?>) ((Process<?>) holder).getParent();
                } else {
                    // Ask parent process
                    return ((Process<?>) holder).getParent().get(GET_SESSION_PROCESS).invoke();
                }
            }

        });

        GET_SESSION = FunctionDefinitionFactory.create("getSession", Process.class, new FunctionExecutor<Session>() {

            @Override
            public Session invoke(FeatureHolder holder, Object... arguments) throws ExecutorInvokationException {

                return (Session) holder.get(GET_SESSION_PROCESS).invoke().get(GET_EXECUTOR).invoke();
            }

        });

        GET_USER = FunctionDefinitionFactory.create("getUser", Process.class, new FunctionExecutor<User>() {

            @Override
            public User invoke(FeatureHolder holder, Object... arguments) throws ExecutorInvokationException {

                return holder.get(GET_SESSION).invoke().get(Session.GET_USER).invoke();
            }

        });

        LAUNCH = FunctionDefinitionFactory.create("launch", Map.class);
        LAUNCH.addExecutor(Process.class, "setPid", new FunctionExecutor<Void>() {

            @Override
            @Lockable
            @Prioritized (Prioritized.DEFAULT + Prioritized.SUBLEVEL_7)
            public Void invoke(FeatureHolder holder, Object... arguments) throws ExecutorInvokationException {

                // Calculate new pid
                List<Integer> existingPids = new ArrayList<Integer>();
                Process<?> root = holder.get(GET_ROOT).invoke();
                existingPids.add(root.get(GET_PID).invoke());
                for (Process<?> process : root.get(GET_ALL_CHILDREN).invoke()) {
                    existingPids.add(process.get(GET_PID).invoke());
                }
                int pid = 0;
                while (existingPids.contains(pid)) {
                    pid++;
                }

                holder.get(PID).set(pid);
                return null;
            }

        });
        LAUNCH.addExecutor(Process.class, "launchExecutor", new FunctionExecutor<Void>() {

            @Override
            @Lockable
            @Prioritized (Prioritized.DEFAULT + Prioritized.SUBLEVEL_5)
            public Void invoke(FeatureHolder holder, Object... arguments) throws ExecutorInvokationException {

                // Launch new executor
                Program program = (Program) holder.get(GET_SOURCE).invoke().get(ContentFile.GET_CONTENT).invoke();

                // Set new executor
                holder.get(EXECUTOR).set(program.get(Program.CREATE_EXECUTOR).invoke());
                holder.get(GET_EXECUTOR).invoke().setParent((Process<?>) holder);
                holder.get(GET_EXECUTOR).invoke().get(ProgramExecutor.SET_ARGUMENTS).invoke(arguments[0]);

                return null;
            }

        });
        LAUNCH.addExecutor(Process.class, "checkArguments", new FunctionExecutor<Void>() {

            @Override
            @Lockable
            @Prioritized (Prioritized.DEFAULT + Prioritized.SUBLEVEL_2)
            public Void invoke(FeatureHolder holder, Object... arguments) throws ExecutorInvokationException {

                // Check for wrong arguments
                if (holder.get(GET_EXECUTOR).invoke().get(ProgramExecutor.GET_ARGUMENTS).invoke() == null) {
                    // Stop process
                    holder.get(EXECUTOR).set(null);
                    holder.get(STOP).invoke(false);
                }

                return null;
            }

        });

        SEND_MESSAGE = FunctionDefinitionFactory.create("sendMessage", Process.class, new FunctionExecutor<Void>() {

            @Override
            @Lockable
            public Void invoke(FeatureHolder holder, Object... arguments) throws ExecutorInvokationException {

                // Construct message
                IPCMessageEvent message = new IPCMessageEvent();
                message.setLocked(false);
                message.get(IPCMessageEvent.SET_SENDER).invoke(holder);
                message.get(ProcessEvent.SET_RECEIVER).invoke(arguments[0]);
                message.get(IPCMessageEvent.SET_DATA).invoke(arguments[1]);
                message.setLocked(true);

                // Send message
                ((Process<?>) arguments[0]).get(Process.GET_EXECUTOR).invoke().get(ProgramExecutor.RECEIVE_EVENTS).invoke(message);

                return null;
            }

        }, Process.class, Map.class);

    }

    // ----- Functions End -----

    /**
     * Creates a new empty process.
     * You can start the new process using {@link #LAUNCH}.
     */
    public Process() {

    }

}
