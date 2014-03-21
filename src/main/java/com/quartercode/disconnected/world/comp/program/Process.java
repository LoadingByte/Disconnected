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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.Validate;
import com.quartercode.classmod.base.FeatureDefinition;
import com.quartercode.classmod.base.FeatureHolder;
import com.quartercode.classmod.extra.ExecutorInvocationException;
import com.quartercode.classmod.extra.FunctionDefinition;
import com.quartercode.classmod.extra.FunctionExecutor;
import com.quartercode.classmod.extra.FunctionInvocation;
import com.quartercode.classmod.extra.Lockable;
import com.quartercode.classmod.extra.Prioritized;
import com.quartercode.classmod.extra.def.LockableFEWrapper;
import com.quartercode.classmod.extra.def.ObjectProperty;
import com.quartercode.classmod.extra.def.ReferenceProperty;
import com.quartercode.classmod.util.CollectionPropertyAccessorFactory;
import com.quartercode.classmod.util.FunctionDefinitionFactory;
import com.quartercode.classmod.util.PropertyAccessorFactory;
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
    protected static final FeatureDefinition<ObjectProperty<Set<Process<?>>>>     CHILDREN;

    static {

        PID = ObjectProperty.createDefinition("pid");
        SOURCE = ReferenceProperty.createDefinition("source");
        ENVIRONMENT = ObjectProperty.<Map<String, String>> createDefinition("environment", new HashMap<String, String>());
        STATE = ObjectProperty.createDefinition("state", ProcessState.RUNNING);
        EXECUTOR = ObjectProperty.createDefinition("executor");
        CHILDREN = ObjectProperty.<Set<Process<?>>> createDefinition("children", new HashSet<Process<?>>());

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
    public static final FunctionDefinition<Set<Process<?>>>                       GET_CHILDREN;

    /**
     * Returns all child processes the process launched.
     * That includes all children which are present in the child datastructure of this object, all child objects etc.
     */
    public static final FunctionDefinition<Set<Process<?>>>                       GET_ALL_CHILDREN;

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
     * 
     * <table>
     * <tr>
     * <th>Exception</th>
     * <th>When?</th>
     * </tr>
     * <tr>
     * <td>{@link ArgumentException}</td>
     * <td>The input arguments don't match the parameters provided by {@link ProgramExecutor#GET_PARAMETERS}.</td>
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
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) throws ExecutorInvocationException {

                Validate.isTrue( ((ContentFile) arguments[0]).get(ContentFile.GET_CONTENT).invoke() instanceof Program, "Source must contain a program");
                return invocation.next(arguments);
            }

        });

        GET_ENVIRONMENT = FunctionDefinitionFactory.create("getEnvironment", Process.class, PropertyAccessorFactory.createGet(ENVIRONMENT));
        SET_ENVIRONMENT = FunctionDefinitionFactory.create("setEnvironment", Process.class, PropertyAccessorFactory.createSet(ENVIRONMENT), Map.class);

        GET_STATE = FunctionDefinitionFactory.create("getState", Process.class, PropertyAccessorFactory.createGet(STATE));
        IS_STATE_APPLIED = FunctionDefinitionFactory.create("isStateApplied", Process.class, new FunctionExecutor<Boolean>() {

            @Override
            public Boolean invoke(FunctionInvocation<Boolean> invocation, Object... arguments) throws ExecutorInvocationException {

                FeatureHolder holder = invocation.getHolder();
                boolean stateApplied = true;

                if (holder.get(GET_STATE).invoke() != arguments[0]) {
                    stateApplied = false;
                } else {
                    for (Process<?> child : holder.get(GET_CHILDREN).invoke()) {
                        if (!child.get(IS_STATE_APPLIED).invoke()) {
                            stateApplied = false;
                            break;
                        }
                    }
                }

                invocation.next(arguments);
                return stateApplied;
            }

        }, ProcessState.class);
        SET_STATE = FunctionDefinitionFactory.create("setState", Process.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) throws ExecutorInvocationException {

                FeatureHolder holder = invocation.getHolder();

                holder.get(STATE).set((ProcessState) arguments[0]);
                holder.get(GET_EXECUTOR).invoke().setLocked(holder.get(GET_STATE).invoke().isLock());

                if ((Boolean) arguments[1]) {
                    for (Process<?> child : holder.get(GET_CHILDREN).invoke()) {
                        child.get(SET_STATE).invoke(arguments[0], arguments[1]);
                    }
                }

                return invocation.next(arguments);
            }

        }, ProcessState.class, Boolean.class);
        SUSPEND = FunctionDefinitionFactory.create("suspend", Process.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) throws ExecutorInvocationException {

                FeatureHolder holder = invocation.getHolder();

                if (holder.get(GET_STATE).invoke() == ProcessState.RUNNING) {
                    holder.get(SET_STATE).invoke(ProcessState.SUSPENDED, arguments[0]);
                }

                return invocation.next(arguments);
            }

        }, Boolean.class);
        RESUME = FunctionDefinitionFactory.create("resume", Process.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) throws ExecutorInvocationException {

                FeatureHolder holder = invocation.getHolder();

                if (holder.get(GET_STATE).invoke() == ProcessState.SUSPENDED) {
                    holder.get(SET_STATE).invoke(ProcessState.RUNNING, arguments[0]);
                }

                return invocation.next(arguments);
            }

        }, Boolean.class);
        INTERRUPT = FunctionDefinitionFactory.create("interrupt", Process.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) throws ExecutorInvocationException {

                FeatureHolder holder = invocation.getHolder();

                if (holder.get(GET_STATE).invoke() == ProcessState.RUNNING) {
                    holder.get(SET_STATE).invoke(ProcessState.INTERRUPTED, arguments[0]);
                }

                return invocation.next(arguments);
            }

        }, Boolean.class);
        STOP = FunctionDefinitionFactory.create("stop", Process.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) throws ExecutorInvocationException {

                FeatureHolder holder = invocation.getHolder();

                if (holder.get(GET_STATE).invoke() != ProcessState.STOPPED) {
                    holder.get(SET_STATE).invoke(ProcessState.STOPPED, arguments[0]);
                    // Unregister stopped process from parent
                    if ( ((Process<?>) holder).getParent() != null) {
                        ((Process<?>) holder).getParent().get(CHILDREN).get().remove(holder);
                    }
                }

                return invocation.next(arguments);
            }

        }, Boolean.class);

        GET_EXECUTOR = FunctionDefinitionFactory.create("getExecutor", Process.class, PropertyAccessorFactory.createGet(EXECUTOR));

        GET_CHILDREN = FunctionDefinitionFactory.create("getChildren", Process.class, CollectionPropertyAccessorFactory.createGet(CHILDREN));

        GET_ALL_CHILDREN = FunctionDefinitionFactory.create("getAllChildren", Process.class, new FunctionExecutor<Set<Process<?>>>() {

            @Override
            public Set<Process<?>> invoke(FunctionInvocation<Set<Process<?>>> invocation, Object... arguments) throws ExecutorInvocationException {

                Set<Process<?>> children = getAllChildren((Process<?>) invocation.getHolder());
                invocation.next(arguments);
                return children;
            }

            private Set<Process<?>> getAllChildren(Process<?> parent) throws ExecutorInvocationException {

                Set<Process<?>> allChildren = new HashSet<Process<?>>();
                for (Process<?> directChild : parent.get(GET_CHILDREN).invoke()) {
                    allChildren.addAll(directChild.get(GET_ALL_CHILDREN).invoke());
                }

                return allChildren;
            }

        });

        CREATE_CHILD = FunctionDefinitionFactory.create("createChild", Process.class, new FunctionExecutor<ChildProcess>() {

            @Override
            public ChildProcess invoke(FunctionInvocation<ChildProcess> invocation, Object... arguments) throws ExecutorInvocationException {

                FeatureHolder holder = invocation.getHolder();

                ChildProcess process = new ChildProcess();
                process.setParent((Process<?>) holder);
                process.get(SET_ENVIRONMENT).invoke(new HashMap<String, String>(holder.get(GET_ENVIRONMENT).invoke()));

                invocation.next(arguments);
                return process;
            }

        });

        GET_ROOT = FunctionDefinitionFactory.create("getRoot", Process.class, new FunctionExecutor<RootProcess>() {

            @Override
            public RootProcess invoke(FunctionInvocation<RootProcess> invocation, Object... arguments) throws ExecutorInvocationException {

                FeatureHolder holder = invocation.getHolder();
                RootProcess root = null;

                if ( ((Process<?>) holder).getParent() == null) {
                    root = (RootProcess) holder;
                } else {
                    root = ((RootProcess) holder).getParent().get(GET_ROOT).invoke();
                }

                invocation.next(arguments);
                return root;
            }

        });

        GET_OPERATING_SYSTEM = FunctionDefinitionFactory.create("getOperatingSystem", Process.class, new FunctionExecutor<OperatingSystem>() {

            @Override
            public OperatingSystem invoke(FunctionInvocation<OperatingSystem> invocation, Object... arguments) throws ExecutorInvocationException {

                OperatingSystem operatingSystem = invocation.getHolder().get(GET_ROOT).invoke().getParent();
                invocation.next(arguments);
                return operatingSystem;
            }

        });

        GET_SESSION_PROCESS = FunctionDefinitionFactory.create("getSessionProcess", Process.class, new FunctionExecutor<Process<?>>() {

            @Override
            public Process<?> invoke(FunctionInvocation<Process<?>> invocation, Object... arguments) throws ExecutorInvocationException {

                FeatureHolder holder = invocation.getHolder();
                Process<?> sessionProcess = null;

                // Error check
                if ( ((Process<?>) holder).getParent() != null) {
                    if ( ((Process<?>) holder).getParent().get(GET_EXECUTOR).invoke() instanceof Session) {
                        sessionProcess = (Process<?>) ((Process<?>) holder).getParent();
                    } else {
                        // Ask parent process
                        sessionProcess = ((Process<?>) holder).getParent().get(GET_SESSION_PROCESS).invoke();
                    }
                }

                invocation.next(arguments);
                return sessionProcess;
            }

        });

        GET_SESSION = FunctionDefinitionFactory.create("getSession", Process.class, new FunctionExecutor<Session>() {

            @Override
            public Session invoke(FunctionInvocation<Session> invocation, Object... arguments) throws ExecutorInvocationException {

                Session session = (Session) invocation.getHolder().get(GET_SESSION_PROCESS).invoke().get(GET_EXECUTOR).invoke();
                invocation.next(arguments);
                return session;
            }

        });

        GET_USER = FunctionDefinitionFactory.create("getUser", Process.class, new FunctionExecutor<User>() {

            @Override
            public User invoke(FunctionInvocation<User> invocation, Object... arguments) throws ExecutorInvocationException {

                User user = invocation.getHolder().get(GET_SESSION).invoke().get(Session.GET_USER).invoke();
                invocation.next(arguments);
                return user;
            }

        });

        LAUNCH = FunctionDefinitionFactory.create("launch", Map.class);
        LAUNCH.addExecutor(Process.class, "setPid", new FunctionExecutor<Void>() {

            @Override
            @Lockable
            @Prioritized (Prioritized.DEFAULT + Prioritized.SUBLEVEL_7)
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) throws ExecutorInvocationException {

                FeatureHolder holder = invocation.getHolder();

                // Calculate new pid
                Set<Integer> existingPids = new HashSet<Integer>();
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
                return invocation.next(arguments);
            }

        });
        LAUNCH.addExecutor(Process.class, "launchExecutor", new FunctionExecutor<Void>() {

            @Override
            @Lockable
            @Prioritized (Prioritized.DEFAULT + Prioritized.SUBLEVEL_5)
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) throws ExecutorInvocationException {

                FeatureHolder holder = invocation.getHolder();

                // Create new executor
                Program program = (Program) holder.get(GET_SOURCE).invoke().get(ContentFile.GET_CONTENT).invoke();
                ProgramExecutor executor = program.get(Program.CREATE_EXECUTOR).invoke();

                // Initialize new executor
                executor.setLocked(false);
                executor.setParent((Process<?>) holder);
                executor.get(ProgramExecutor.SET_ARGUMENTS).invoke(arguments[0]);
                executor.setLocked(true);

                // Set new executor
                holder.get(EXECUTOR).set(executor);

                return invocation.next(arguments);
            }

        });
        LAUNCH.addExecutor(Process.class, "checkArguments", new FunctionExecutor<Void>() {

            @Override
            @Lockable
            @Prioritized (Prioritized.DEFAULT + Prioritized.SUBLEVEL_2)
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) throws ExecutorInvocationException {

                FeatureHolder holder = invocation.getHolder();

                // Check for wrong arguments
                if (holder.get(GET_EXECUTOR).invoke().get(ProgramExecutor.GET_ARGUMENTS).invoke() == null) {
                    // Stop process
                    holder.get(EXECUTOR).set(null);
                    holder.get(STOP).invoke(false);
                }

                return invocation.next(arguments);
            }

        });

        SEND_MESSAGE = FunctionDefinitionFactory.create("sendMessage", Process.class, new FunctionExecutor<Void>() {

            @Override
            @Lockable
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) throws ExecutorInvocationException {

                // Construct message
                IPCMessageEvent message = new IPCMessageEvent();
                message.setLocked(false);
                message.get(IPCMessageEvent.SET_SENDER).invoke(invocation.getHolder());
                message.get(ProcessEvent.SET_RECEIVER).invoke(arguments[0]);
                message.get(IPCMessageEvent.SET_DATA).invoke(arguments[1]);
                message.setLocked(true);

                // Send message
                ((Process<?>) arguments[0]).get(Process.GET_EXECUTOR).invoke().get(ProgramExecutor.RECEIVE_EVENTS).invoke(message);

                return invocation.next(arguments);
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
