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

import static com.quartercode.classmod.ClassmodFactory.create;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.reflect.TypeLiteral;
import com.quartercode.classmod.base.FeatureHolder;
import com.quartercode.classmod.extra.CollectionPropertyDefinition;
import com.quartercode.classmod.extra.FunctionDefinition;
import com.quartercode.classmod.extra.FunctionExecutor;
import com.quartercode.classmod.extra.FunctionInvocation;
import com.quartercode.classmod.extra.Prioritized;
import com.quartercode.classmod.extra.PropertyDefinition;
import com.quartercode.classmod.extra.storage.ReferenceStorage;
import com.quartercode.classmod.extra.storage.StandardStorage;
import com.quartercode.classmod.extra.valuefactory.CloneValueFactory;
import com.quartercode.classmod.extra.valuefactory.ConstantValueFactory;
import com.quartercode.disconnected.sim.scheduler.SchedulerUser;
import com.quartercode.disconnected.world.WorldChildFeatureHolder;
import com.quartercode.disconnected.world.comp.file.ContentFile;
import com.quartercode.disconnected.world.comp.file.File;
import com.quartercode.disconnected.world.comp.file.FileRights.FileRight;
import com.quartercode.disconnected.world.comp.file.FileUtils;
import com.quartercode.disconnected.world.comp.os.EnvironmentVariable;
import com.quartercode.disconnected.world.comp.os.OperatingSystem;
import com.quartercode.disconnected.world.comp.os.Session;
import com.quartercode.disconnected.world.comp.os.User;

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
         * If a process notes this state, it should try to execute last activities and then stop the execution.
         */
        INTERRUPTED (true),
        /**
         * The execution is permanently stopped.
         * If a process is stopped, it won't be able to start again.
         */
        STOPPED (true);

        private final boolean tickState;

        /**
         * Creates a new process state enumeration entry.
         * 
         * @param tickState True if the state should allow execution of the program it is applied on.
         */
        private ProcessState(boolean tickState) {

            this.tickState = tickState;
        }

        /**
         * Returns true if the state should allow execution of the program it is applied on.
         * 
         * @return Whether the state should allow the execution of the program it is applied on.
         */
        public boolean isTickState() {

            return tickState;
        }

    }

    // ----- Properties -----

    /**
     * The unique id the process has.
     * It is used to identify the process.
     */
    public static final PropertyDefinition<Integer>                                PID;

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
    public static final PropertyDefinition<ContentFile>                            SOURCE;

    /**
     * The environment variables that are assigned to the process.
     * See {@link EnvironmentVariable} for more information.
     */
    public static final PropertyDefinition<Map<String, String>>                    ENVIRONMENT;

    /**
     * The {@link ProcessState} which defines the global state of the process the os can see.
     * It stores whether the process is running, interrupted etc.<br>
     * Note that using the {@link #APPLY_STATE} or {@link #SUSPEND}/{@link #RESUME}/{@link #INTERRUPT}/{@link #STOP} methods
     * is preferred over directly accessing the property.
     */
    public static final PropertyDefinition<ProcessState>                           STATE;

    /**
     * The {@link ProgramExecutor} which contains the logic of the process.
     * This value is only available after an {@link #INITIALIZE} call.<br>
     * Argument properties should be passed to this object.<br>
     * The {@link ProgramExecutor#RUN} method on the stored value finally starts the process.
     */
    public static final PropertyDefinition<ProgramExecutor>                        EXECUTOR;

    /**
     * The child processes the process launched.<br>
     * Note that using the {@link #CREATE_CHILD} method is preferred over directly accessing the property.
     */
    public static final CollectionPropertyDefinition<Process<?>, List<Process<?>>> CHILDREN;

    static {

        PID = create(new TypeLiteral<PropertyDefinition<Integer>>() {}, "name", "pid", "storage", new StandardStorage<>());

        SOURCE = create(new TypeLiteral<PropertyDefinition<ContentFile>>() {}, "name", "source", "storage", new ReferenceStorage<>());
        SOURCE.addSetterExecutor("checkFileContent", Process.class, new FunctionExecutor<Void>() {

            @Override
            @Prioritized (Prioritized.LEVEL_6)
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                Validate.isInstanceOf(Program.class, ((ContentFile) arguments[0]).get(ContentFile.CONTENT).get(), "Source must contain a program");
                return invocation.next(arguments);
            }

        });

        ENVIRONMENT = create(new TypeLiteral<PropertyDefinition<Map<String, String>>>() {}, "name", "environment", "storage", new StandardStorage<>(), "initialValue", new CloneValueFactory<>(new HashMap<>()));

        STATE = create(new TypeLiteral<PropertyDefinition<ProcessState>>() {}, "name", "state", "storage", new StandardStorage<>(), "initialValue", new ConstantValueFactory<>(ProcessState.RUNNING));
        STATE.addSetterExecutor("setExecutorSchedulerActive", Process.class, new FunctionExecutor<Void>() {

            @Override
            @Prioritized (Prioritized.LEVEL_6)
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                ProgramExecutor executor = invocation.getHolder().get(EXECUTOR).get();

                if (executor instanceof SchedulerUser) {
                    boolean active = ((ProcessState) arguments[0]).isTickState();
                    executor.get(SchedulerUser.SCHEDULER).setActive(active);
                }

                return invocation.next(arguments);
            }

        });

        EXECUTOR = create(new TypeLiteral<PropertyDefinition<ProgramExecutor>>() {}, "name", "executor", "storage", new StandardStorage<>());
        CHILDREN = create(new TypeLiteral<CollectionPropertyDefinition<Process<?>, List<Process<?>>>>() {}, "name", "children", "storage", new StandardStorage<>(), "collection", new CloneValueFactory<>(new ArrayList<>()));

    }

    // ----- Functions -----

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
    public static final FunctionDefinition<Boolean>                                IS_STATE_APPLIED;

    /**
     * Changes the {@link ProcessState} which defines the global state of the process the os can see.
     * The state change can also apply to every child process using the recursive parameter.<br>
     * Note that using the specific state-changing methods like {@link #SUSPEND} is preferred over using this plain function.
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
     * 
     * @see #SUSPEND
     * @see #RESUME
     * @see #INTERRUPT
     * @see #STOP
     */
    public static final FunctionDefinition<Void>                                   APPLY_STATE;

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
    public static final FunctionDefinition<Void>                                   SUSPEND;

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
    public static final FunctionDefinition<Void>                                   RESUME;

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
    public static final FunctionDefinition<Void>                                   INTERRUPT;

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
    public static final FunctionDefinition<Void>                                   STOP;

    /**
     * Returns all child processes the process launched.
     * That includes all children which are present in the child datastructure of this object, all child objects etc.
     */
    public static final FunctionDefinition<List<Process<?>>>                       GET_ALL_CHILDREN;

    /**
     * Creates a new empty {@link ChildProcess} which uses the same environment variables as this one.
     * You can run the returned process after creation using {@link Process#INITIALIZE} and then {@link ProgramExecutor#RUN} on {@link Process#EXECUTOR}.
     */
    public static final FunctionDefinition<ChildProcess>                           CREATE_CHILD;

    /**
     * Returns the root {@link RootProcess} which is the parent of every other process somewhere in the tree.
     */
    public static final FunctionDefinition<RootProcess>                            GET_ROOT;

    /**
     * Returns the {@link OperatingSystem} which is hosting the {@link RootProcess} which is the parent of every other process.
     */
    public static final FunctionDefinition<OperatingSystem>                        GET_OPERATING_SYSTEM;

    /**
     * Resolves the {@link Session} process this process is running under.
     * This process is running with the rights of that {@link Session}.
     */
    public static final FunctionDefinition<Process<?>>                             GET_SESSION_PROCESS;

    /**
     * Resolves the actual {@link Session} executor this process is running under.
     * This process is running with the rights of that {@link Session}.
     */
    public static final FunctionDefinition<Session>                                GET_SESSION;

    /**
     * Resolves the {@link User} this process is running under.
     * This uses the {@link #GET_SESSION} function for resolving the Session} object.
     */
    public static final FunctionDefinition<User>                                   GET_USER;

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
     * <td>The {@link #PID} of the new process (can be generated using {@link ProcessModule#NEXT_PID}). Its uniqueness is validated by the function.</td>
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
    public static final FunctionDefinition<Void>                                   INITIALIZE;

    static {

        IS_STATE_APPLIED = create(new TypeLiteral<FunctionDefinition<Boolean>>() {}, "name", "isStateApplied", "parameters", new Class[] { ProcessState.class });
        IS_STATE_APPLIED.addExecutor("default", Process.class, new FunctionExecutor<Boolean>() {

            @Override
            public Boolean invoke(FunctionInvocation<Boolean> invocation, Object... arguments) {

                FeatureHolder holder = invocation.getHolder();
                boolean stateApplied = true;

                if (holder.get(STATE).get() != arguments[0]) {
                    stateApplied = false;
                } else {
                    for (Process<?> child : holder.get(CHILDREN).get()) {
                        if (!child.get(IS_STATE_APPLIED).invoke()) {
                            stateApplied = false;
                            break;
                        }
                    }
                }

                invocation.next(arguments);
                return stateApplied;
            }

        });
        APPLY_STATE = create(new TypeLiteral<FunctionDefinition<Void>>() {}, "name", "setState", "parameters", new Class[] { ProcessState.class, Boolean.class });
        APPLY_STATE.addExecutor("default", Process.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                FeatureHolder holder = invocation.getHolder();

                holder.get(STATE).set((ProcessState) arguments[0]);
                if ((Boolean) arguments[1]) {
                    for (Process<?> child : holder.get(CHILDREN).get()) {
                        child.get(APPLY_STATE).invoke(arguments[0], arguments[1]);
                    }
                }

                return invocation.next(arguments);
            }

        });
        SUSPEND = create(new TypeLiteral<FunctionDefinition<Void>>() {}, "name", "suspend", "parameters", new Class[] { Boolean.class });
        SUSPEND.addExecutor("default", Process.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                FeatureHolder holder = invocation.getHolder();

                if (holder.get(STATE).get() == ProcessState.RUNNING) {
                    holder.get(APPLY_STATE).invoke(ProcessState.SUSPENDED, arguments[0]);
                }

                return invocation.next(arguments);
            }

        });
        RESUME = create(new TypeLiteral<FunctionDefinition<Void>>() {}, "name", "resume", "parameters", new Class[] { Boolean.class });
        RESUME.addExecutor("default", Process.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                FeatureHolder holder = invocation.getHolder();

                if (holder.get(STATE).get() == ProcessState.SUSPENDED) {
                    holder.get(APPLY_STATE).invoke(ProcessState.RUNNING, arguments[0]);
                }

                return invocation.next(arguments);
            }

        });
        INTERRUPT = create(new TypeLiteral<FunctionDefinition<Void>>() {}, "name", "interrupt", "parameters", new Class[] { Boolean.class });
        INTERRUPT.addExecutor("default", Process.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                FeatureHolder holder = invocation.getHolder();

                if (holder.get(STATE).get() == ProcessState.RUNNING) {
                    holder.get(APPLY_STATE).invoke(ProcessState.INTERRUPTED, arguments[0]);
                }

                return invocation.next(arguments);
            }

        });
        STOP = create(new TypeLiteral<FunctionDefinition<Void>>() {}, "name", "stop", "parameters", new Class[] { Boolean.class });
        STOP.addExecutor("default", Process.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                Process<?> holder = (Process<?>) invocation.getHolder();

                if (holder.get(STATE).get() != ProcessState.STOPPED) {
                    holder.get(APPLY_STATE).invoke(ProcessState.STOPPED, arguments[0]);
                    // Unregister stopped process from parent
                    if (holder.getParent() != null) {
                        holder.getParent().get(CHILDREN).remove(holder);
                    }
                }

                return invocation.next(arguments);
            }

        });

        GET_ALL_CHILDREN = create(new TypeLiteral<FunctionDefinition<List<Process<?>>>>() {}, "name", "getAllChildren", "parameters", new Class[0]);
        GET_ALL_CHILDREN.addExecutor("default", Process.class, new FunctionExecutor<List<Process<?>>>() {

            @Override
            public List<Process<?>> invoke(FunctionInvocation<List<Process<?>>> invocation, Object... arguments) {

                List<Process<?>> children = getAllChildren((Process<?>) invocation.getHolder());
                invocation.next(arguments);
                return children;
            }

            private List<Process<?>> getAllChildren(Process<?> parent) {

                List<Process<?>> allChildren = new ArrayList<>();
                for (Process<?> directChild : parent.get(CHILDREN).get()) {
                    allChildren.addAll(directChild.get(GET_ALL_CHILDREN).invoke());
                }

                return allChildren;
            }

        });

        CREATE_CHILD = create(new TypeLiteral<FunctionDefinition<ChildProcess>>() {}, "name", "createChild", "parameters", new Class[0]);
        CREATE_CHILD.addExecutor("default", Process.class, new FunctionExecutor<ChildProcess>() {

            @Override
            public ChildProcess invoke(FunctionInvocation<ChildProcess> invocation, Object... arguments) {

                FeatureHolder holder = invocation.getHolder();

                ChildProcess process = new ChildProcess();
                process.setParent((Process<?>) holder);
                process.get(ENVIRONMENT).set(new HashMap<>(holder.get(ENVIRONMENT).get()));
                holder.get(CHILDREN).add(process);

                invocation.next(arguments);
                return process;
            }

        });

        GET_ROOT = create(new TypeLiteral<FunctionDefinition<RootProcess>>() {}, "name", "getRoot", "parameters", new Class[0]);
        GET_ROOT.addExecutor("default", Process.class, new FunctionExecutor<RootProcess>() {

            @Override
            public RootProcess invoke(FunctionInvocation<RootProcess> invocation, Object... arguments) {

                FeatureHolder holder = invocation.getHolder();
                RootProcess root = null;

                if (holder instanceof RootProcess) {
                    root = (RootProcess) holder;
                } else {
                    root = ((Process<?>) holder).getParent().get(GET_ROOT).invoke();
                }

                invocation.next(arguments);
                return root;
            }

        });

        GET_OPERATING_SYSTEM = create(new TypeLiteral<FunctionDefinition<OperatingSystem>>() {}, "name", "getOperatingSystem", "parameters", new Class[0]);
        GET_OPERATING_SYSTEM.addExecutor("default", Process.class, new FunctionExecutor<OperatingSystem>() {

            @Override
            public OperatingSystem invoke(FunctionInvocation<OperatingSystem> invocation, Object... arguments) {

                OperatingSystem operatingSystem = invocation.getHolder().get(GET_ROOT).invoke().getParent().getParent();
                invocation.next(arguments);
                return operatingSystem;
            }

        });

        GET_SESSION_PROCESS = create(new TypeLiteral<FunctionDefinition<Process<?>>>() {}, "name", "getSessionProcess", "parameters", new Class[0]);
        GET_SESSION_PROCESS.addExecutor("default", Process.class, new FunctionExecutor<Process<?>>() {

            @Override
            public Process<?> invoke(FunctionInvocation<Process<?>> invocation, Object... arguments) {

                FeatureHolder holder = invocation.getHolder();
                Process<?> sessionProcess = null;

                // Error check
                if ( ((Process<?>) holder).getParent() != null) {
                    if ( ((Process<?>) holder).getParent().get(EXECUTOR).get() instanceof Session) {
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

        GET_SESSION = create(new TypeLiteral<FunctionDefinition<Session>>() {}, "name", "getSession", "parameters", new Class[0]);
        GET_SESSION.addExecutor("default", Process.class, new FunctionExecutor<Session>() {

            @Override
            public Session invoke(FunctionInvocation<Session> invocation, Object... arguments) {

                Process<?> sessionProcess = invocation.getHolder().get(GET_SESSION_PROCESS).invoke();
                if (sessionProcess == null) {
                    return null;
                } else {
                    Session session = (Session) sessionProcess.get(EXECUTOR).get();
                    invocation.next(arguments);
                    return session;
                }
            }

        });

        GET_USER = create(new TypeLiteral<FunctionDefinition<User>>() {}, "name", "getUser", "parameters", new Class[0]);
        GET_USER.addExecutor("default", Process.class, new FunctionExecutor<User>() {

            @Override
            public User invoke(FunctionInvocation<User> invocation, Object... arguments) {

                Session session = invocation.getHolder().get(GET_SESSION).invoke();
                if (session == null) {
                    return null;
                } else {
                    User user = session.get(Session.USER).get();
                    invocation.next(arguments);
                    return user;
                }
            }

        });

        INITIALIZE = create(new TypeLiteral<FunctionDefinition<Void>>() {}, "name", "initialize", "parameters", new Class[] { Integer.class });
        INITIALIZE.addExecutor("setPid", Process.class, new FunctionExecutor<Void>() {

            @Override
            @Prioritized (Prioritized.DEFAULT + Prioritized.SUBLEVEL_7)
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                FeatureHolder holder = invocation.getHolder();

                int pid = (int) arguments[0];

                // Check given pid
                Set<Integer> existingPids = new HashSet<>();
                Process<?> root = holder.get(GET_ROOT).invoke();
                existingPids.add(root.get(PID).get());
                for (Process<?> process : root.get(GET_ALL_CHILDREN).invoke()) {
                    existingPids.add(process.get(PID).get());
                }
                Validate.isTrue(!existingPids.contains(pid), "Pid {} is already used by another process", pid);

                holder.get(PID).set(pid);
                return invocation.next(arguments);
            }

        });
        INITIALIZE.addExecutor("setExecutor", Process.class, new FunctionExecutor<Void>() {

            @Override
            @Prioritized (Prioritized.DEFAULT + Prioritized.SUBLEVEL_5)
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                FeatureHolder holder = invocation.getHolder();

                // Check read and execution right on source file
                ContentFile source = holder.get(SOURCE).get();
                User user = holder.get(GET_USER).invoke();
                if (!FileUtils.hasRight(user, source, FileRight.READ) || !FileUtils.hasRight(user, source, FileRight.EXECUTE)) {
                    throw new IllegalStateException("Cannot initialize process: No read right and execute right on file");
                }

                // Create new executor
                Program program = (Program) source.get(ContentFile.CONTENT).get();
                ProgramExecutor executor = program.get(Program.CREATE_EXECUTOR).invoke();

                // Set new executor
                holder.get(EXECUTOR).set(executor);

                return invocation.next(arguments);
            }

        });

    }

}
