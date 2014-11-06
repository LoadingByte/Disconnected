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

package com.quartercode.disconnected.server.world.comp.program;

import static com.quartercode.classmod.ClassmodFactory.create;
import static com.quartercode.disconnected.server.world.comp.program.ProgramUtils.getProgramFileFromPaths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.reflect.TypeLiteral;
import com.quartercode.classmod.extra.CFeatureHolder;
import com.quartercode.classmod.extra.FunctionDefinition;
import com.quartercode.classmod.extra.FunctionExecutor;
import com.quartercode.classmod.extra.FunctionInvocation;
import com.quartercode.classmod.extra.Prioritized;
import com.quartercode.classmod.extra.PropertyDefinition;
import com.quartercode.classmod.extra.storage.StandardStorage;
import com.quartercode.classmod.extra.valuefactory.ConstantValueFactory;
import com.quartercode.classmod.util.FeatureDefinitionReference;
import com.quartercode.disconnected.server.registry.ServerRegistries;
import com.quartercode.disconnected.server.sim.TickService;
import com.quartercode.disconnected.server.sim.scheduler.FunctionCallSchedulerTask;
import com.quartercode.disconnected.server.sim.scheduler.SchedulerUser;
import com.quartercode.disconnected.server.world.comp.file.ContentFile;
import com.quartercode.disconnected.server.world.comp.file.File;
import com.quartercode.disconnected.server.world.comp.file.FileSystemModule;
import com.quartercode.disconnected.server.world.comp.os.EnvironmentVariable;
import com.quartercode.disconnected.server.world.comp.os.OSModule;
import com.quartercode.disconnected.server.world.comp.os.OperatingSystem;
import com.quartercode.disconnected.server.world.comp.os.Session;
import com.quartercode.disconnected.server.world.comp.os.config.Configuration;
import com.quartercode.disconnected.server.world.comp.os.config.ConfigurationEntry;
import com.quartercode.disconnected.server.world.comp.os.user.User;
import com.quartercode.disconnected.shared.util.registry.Registries;
import com.quartercode.disconnected.shared.util.registry.extra.NamedValueUtils;
import com.quartercode.disconnected.shared.world.comp.file.CommonFiles;
import com.quartercode.disconnected.shared.world.comp.program.names.SystemProgs;

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
                RootProcess root = invocation.getCHolder().getObj(ROOT_PROCESS);
                processes.add(root);
                processes.addAll(root.invoke(Process.GET_ALL_CHILDREN));

                invocation.next(arguments);
                return processes;
            }

        });

        NEXT_PID = create(new TypeLiteral<FunctionDefinition<Integer>>() {}, "name", "nextPid", "parameters", new Class[0]);
        NEXT_PID.addExecutor("default", ProcessModule.class, new FunctionExecutor<Integer>() {

            @Override
            public Integer invoke(FunctionInvocation<Integer> invocation, Object... arguments) {

                CFeatureHolder holder = invocation.getCHolder();
                int value = holder.getObj(NEXT_PID_VALUE);
                holder.setObj(NEXT_PID_VALUE, value + 1);

                invocation.next(arguments);
                return value;
            }

        });

        SET_RUNNING.addExecutor("resetNextPidValue", ProcessModule.class, new FunctionExecutor<Void>() {

            @Override
            @Prioritized (Prioritized.LEVEL_7)
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                invocation.getCHolder().setObj(NEXT_PID_VALUE, 0);
                return invocation.next(arguments);
            }
        });

        SET_RUNNING.addExecutor("startRootProcess", ProcessModule.class, new FunctionExecutor<Void>() {

            @Override
            @Prioritized (Prioritized.LEVEL_5 + Prioritized.SUBLEVEL_7)
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                CFeatureHolder holder = invocation.getCHolder();

                // Only invoke on bootstrap
                if ((Boolean) arguments[0]) {
                    RootProcess root = new RootProcess();

                    FileSystemModule fsModule = ((ProcessModule) holder).getParent().getObj(OperatingSystem.FS_MODULE);

                    // Get environment
                    Map<String, String> environment = new HashMap<>();
                    File<?> environmentFile = fsModule.invoke(FileSystemModule.GET_FILE, CommonFiles.ENVIRONMENT_CONFIG);
                    if (environmentFile != null) {
                        Configuration environmentConfig = (Configuration) environmentFile.getObj(ContentFile.CONTENT);
                        for (ConfigurationEntry variable : environmentConfig.getColl(Configuration.ENTRIES)) {
                            environment.put(variable.getObj(EnvironmentVariable.NAME), variable.getObj(EnvironmentVariable.VALUE));
                        }
                    }
                    root.setObj(Process.ENVIRONMENT, environment);

                    // Get session program
                    List<String> path = Arrays.asList(environment.get("PATH").split(":"));
                    String sessionProgramFileName = NamedValueUtils.getByName(Registries.get(ServerRegistries.WORLD_PROGRAMS).getValues(), SystemProgs.SESSION).getCommonLocation().toString();
                    ContentFile sessionProgramFile = getProgramFileFromPaths(fsModule, path, sessionProgramFileName);
                    if (sessionProgramFile == null) {
                        throw new IllegalStateException("Cannot start process module: Session program not found");
                    }
                    root.setObj(Process.SOURCE, sessionProgramFile);

                    // Get superuser
                    File<?> userConfigFile = fsModule.invoke(FileSystemModule.GET_FILE, CommonFiles.USER_CONFIG);
                    Configuration userConfig = (Configuration) userConfigFile.getObj(ContentFile.CONTENT);
                    User superuser = null;
                    for (ConfigurationEntry entry : userConfig.getColl(Configuration.ENTRIES)) {
                        if (entry instanceof User && ((User) entry).getObj(User.NAME).equals(User.SUPERUSER_NAME)) {
                            superuser = (User) entry;
                        }
                    }

                    // Start root process
                    holder.setObj(ROOT_PROCESS, root);
                    root.invoke(Process.INITIALIZE, holder.invoke(NEXT_PID));
                    ProgramExecutor rootProgram = root.getObj(Process.EXECUTOR);
                    rootProgram.setObj(Session.USER, superuser);
                    rootProgram.invoke(ProgramExecutor.RUN);
                }

                return invocation.next(arguments);
            }

        });

        SET_RUNNING.addExecutor("interruptRootProcess", OperatingSystem.class, new FunctionExecutor<Void>() {

            @Override
            @Prioritized (Prioritized.LEVEL_5 + Prioritized.SUBLEVEL_7)
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                CFeatureHolder holder = invocation.getCHolder();

                // Only invoke on shutdown
                if (! ((Boolean) arguments[0])) {
                    holder.getObj(ROOT_PROCESS).invoke(Process.INTERRUPT);
                    // Kill the process tree after 5 seconds
                    FeatureDefinitionReference<FunctionDefinition<?>> killFunction = new FeatureDefinitionReference<FunctionDefinition<?>>(ProcessModule.class, KILL);
                    holder.get(SCHEDULER).schedule(new FunctionCallSchedulerTask("killRootProcess", "computerProgramUpdate", TickService.DEFAULT_TICKS_PER_SECOND * 5, killFunction));
                }

                return invocation.next(arguments);
            }

        });

        KILL = create(new TypeLiteral<FunctionDefinition<Void>>() {}, "name", "kill", "parameters", new Class[0]);
        KILL.addExecutor("default", ProcessModule.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                CFeatureHolder holder = invocation.getCHolder();
                holder.getObj(ROOT_PROCESS).invoke(Process.STOP);
                holder.setObj(ROOT_PROCESS, null);

                return invocation.next(arguments);
            }

        });

    }

}
