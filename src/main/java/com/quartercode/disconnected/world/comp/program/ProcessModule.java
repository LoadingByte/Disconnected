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
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.reflect.TypeLiteral;
import com.quartercode.classmod.base.FeatureHolder;
import com.quartercode.classmod.extra.FunctionDefinition;
import com.quartercode.classmod.extra.FunctionExecutor;
import com.quartercode.classmod.extra.FunctionInvocation;
import com.quartercode.classmod.extra.Prioritized;
import com.quartercode.classmod.extra.PropertyDefinition;
import com.quartercode.classmod.extra.storage.StandardStorage;
import com.quartercode.classmod.extra.valuefactory.ConstantValueFactory;
import com.quartercode.disconnected.sim.TickService;
import com.quartercode.disconnected.sim.scheduler.FunctionCallScheduleTask;
import com.quartercode.disconnected.sim.scheduler.SchedulerUser;
import com.quartercode.disconnected.world.comp.file.ContentFile;
import com.quartercode.disconnected.world.comp.file.File;
import com.quartercode.disconnected.world.comp.file.FileSystemModule;
import com.quartercode.disconnected.world.comp.os.CommonFiles;
import com.quartercode.disconnected.world.comp.os.Configuration;
import com.quartercode.disconnected.world.comp.os.Configuration.ConfigurationEntry;
import com.quartercode.disconnected.world.comp.os.EnvironmentVariable;
import com.quartercode.disconnected.world.comp.os.OSModule;
import com.quartercode.disconnected.world.comp.os.OperatingSystem;
import com.quartercode.disconnected.world.comp.os.Session;
import com.quartercode.disconnected.world.comp.os.User;

/**
 * This class represents an {@link OperatingSystem} module which is used to manage the {@link RootProcess}.
 * It is an essential part of the {@link OperatingSystem} and is directly used by it.
 * 
 * @see RootProcess
 * @see OSModule
 * @see OperatingSystem
 */
public class ProcessModule extends OSModule implements SchedulerUser {

    // ----- Properties -----

    /**
     * The {@link RootProcess} which is the root of the entire {@link Process} tree.
     * It always has a pid of 0.
     */
    public static final PropertyDefinition<RootProcess>      ROOT_PROCESS;

    /**
     * The next pid value is used as a counter for {@link Process#PID}s (process ids).
     * Every time a new process is created, its pid should be set to a new value returned by {@link #NEXT_PID}.
     * That function uses this property as a counter.<br>
     * The next pid value is reset to {@code 0} every time when the process module starts up or shuts down.
     */
    public static final PropertyDefinition<Integer>          NEXT_PID_VALUE;

    static {

        ROOT_PROCESS = create(new TypeLiteral<PropertyDefinition<RootProcess>>() {}, "name", "rootProcess", "storage", new StandardStorage<>());
        NEXT_PID_VALUE = create(new TypeLiteral<PropertyDefinition<Integer>>() {}, "name", "nextPidValue", "storage", new StandardStorage<>(), "initialValue", new ConstantValueFactory<>(0));

    }

    // ----- Functions -----

    /**
     * Returns a {@link List} containing all currently running {@link Process}es.
     */
    public static final FunctionDefinition<List<Process<?>>> GET_ALL;

    /**
     * Every time a new process is created, its pid should be set to the next pid returned by this function.
     * It uses the {@link #NEXT_PID_VALUE} as an internal counter.
     */
    public static final FunctionDefinition<Integer>          NEXT_PID;

    /**
     * Kills the whole {@link Process} tree immediately.
     * By default, this function just stops the root process.
     */
    public static final FunctionDefinition<Void>             KILL;

    static {

        GET_ALL = create(new TypeLiteral<FunctionDefinition<List<Process<?>>>>() {}, "name", "getAll", "parameters", new Class[0]);
        GET_ALL.addExecutor("default", ProcessModule.class, new FunctionExecutor<List<Process<?>>>() {

            @Override
            public List<Process<?>> invoke(FunctionInvocation<List<Process<?>>> invocation, Object... arguments) {

                List<Process<?>> processes = new ArrayList<>();
                RootProcess root = invocation.getHolder().get(ROOT_PROCESS).get();
                processes.add(root);
                processes.addAll(root.get(Process.GET_ALL_CHILDREN).invoke());

                invocation.next(arguments);
                return processes;
            }

        });

        NEXT_PID = create(new TypeLiteral<FunctionDefinition<Integer>>() {}, "name", "nextPid", "parameters", new Class[0]);
        NEXT_PID.addExecutor("default", ProcessModule.class, new FunctionExecutor<Integer>() {

            @Override
            public Integer invoke(FunctionInvocation<Integer> invocation, Object... arguments) {

                FeatureHolder holder = invocation.getHolder();
                int value = holder.get(NEXT_PID_VALUE).get();
                holder.get(NEXT_PID_VALUE).set(value + 1);

                invocation.next(arguments);
                return value;
            }

        });

        SET_RUNNING.addExecutor("resetNextPidValue", ProcessModule.class, new FunctionExecutor<Void>() {

            @Override
            @Prioritized (Prioritized.LEVEL_7)
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                invocation.getHolder().get(NEXT_PID_VALUE).set(0);
                return invocation.next(arguments);
            }
        });

        SET_RUNNING.addExecutor("startRootProcess", ProcessModule.class, new FunctionExecutor<Void>() {

            @Override
            @Prioritized (Prioritized.LEVEL_5 + Prioritized.SUBLEVEL_7)
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                FeatureHolder holder = invocation.getHolder();

                // Only invoke on bootstrap
                if ((Boolean) arguments[0]) {
                    RootProcess root = new RootProcess();

                    FileSystemModule fsModule = ((ProcessModule) holder).getParent().get(OperatingSystem.FS_MODULE).get();

                    // Get environment
                    Map<String, String> environment = new HashMap<>();
                    File<?> environmentFile = fsModule.get(FileSystemModule.GET_FILE).invoke(CommonFiles.ENVIRONMENT_CONFIG);
                    if (environmentFile != null) {
                        Configuration environmentConfig = (Configuration) environmentFile.get(ContentFile.CONTENT).get();
                        for (ConfigurationEntry variable : environmentConfig.get(Configuration.ENTRIES).get()) {
                            environment.put(variable.get(EnvironmentVariable.NAME).get(), variable.get(EnvironmentVariable.VALUE).get());
                        }
                    }
                    root.get(Process.ENVIRONMENT).set(environment);

                    // Get session program
                    String sessionProgramFilePath = ProgramUtils.getCommonLocation(Session.class);
                    ContentFile sessionProgramFile = (ContentFile) fsModule.get(FileSystemModule.GET_FILE).invoke(sessionProgramFilePath);
                    if (sessionProgramFile == null) {
                        throw new IllegalStateException("Cannot start process module: Session program not found");
                    }
                    root.get(Process.SOURCE).set(sessionProgramFile);

                    // Get superuser
                    File<?> userConfigFile = fsModule.get(FileSystemModule.GET_FILE).invoke(CommonFiles.USER_CONFIG);
                    Configuration userConfig = (Configuration) userConfigFile.get(ContentFile.CONTENT).get();
                    User superuser = null;
                    for (ConfigurationEntry entry : userConfig.get(Configuration.ENTRIES).get()) {
                        if (entry instanceof User && ((User) entry).get(User.NAME).get().equals(User.SUPERUSER_NAME)) {
                            superuser = (User) entry;
                        }
                    }

                    // Start root process
                    holder.get(ROOT_PROCESS).set(root);
                    root.get(Process.INITIALIZE).invoke(holder.get(NEXT_PID).invoke());
                    ProgramExecutor rootProgram = root.get(Process.EXECUTOR).get();
                    rootProgram.get(Session.USER).set(superuser);
                    rootProgram.get(ProgramExecutor.RUN).invoke();
                }

                return invocation.next(arguments);
            }
        });

        SET_RUNNING.addExecutor("interruptRootProcess", OperatingSystem.class, new FunctionExecutor<Void>() {

            @Override
            @Prioritized (Prioritized.LEVEL_5 + Prioritized.SUBLEVEL_7)
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                FeatureHolder holder = invocation.getHolder();

                // Only invoke on shutdown
                if (! ((Boolean) arguments[0])) {
                    holder.get(ROOT_PROCESS).get().get(Process.INTERRUPT).invoke();
                    // Kill the process tree after 5 seconds
                    holder.get(SCHEDULER).schedule(new FunctionCallScheduleTask(KILL, ProcessModule.class), TickService.DEFAULT_TICKS_PER_SECOND * 5);
                }

                return invocation.next(arguments);
            }

        });

        KILL = create(new TypeLiteral<FunctionDefinition<Void>>() {}, "name", "kill", "parameters", new Class[0]);
        KILL.addExecutor("default", ProcessModule.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                FeatureHolder holder = invocation.getHolder();
                holder.get(ROOT_PROCESS).get().get(Process.STOP).invoke();
                holder.get(ROOT_PROCESS).set(null);

                return invocation.next(arguments);
            }

        });

    }

}
