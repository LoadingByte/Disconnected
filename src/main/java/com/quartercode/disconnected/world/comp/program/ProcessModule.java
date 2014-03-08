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
import com.quartercode.disconnected.mocl.base.FeatureDefinition;
import com.quartercode.disconnected.mocl.base.FeatureHolder;
import com.quartercode.disconnected.mocl.base.def.AbstractFeatureDefinition;
import com.quartercode.disconnected.mocl.extra.Delay;
import com.quartercode.disconnected.mocl.extra.ExecutorInvokationException;
import com.quartercode.disconnected.mocl.extra.FunctionDefinition;
import com.quartercode.disconnected.mocl.extra.FunctionExecutor;
import com.quartercode.disconnected.mocl.extra.Limit;
import com.quartercode.disconnected.mocl.extra.def.ObjectProperty;
import com.quartercode.disconnected.mocl.util.FunctionDefinitionFactory;
import com.quartercode.disconnected.mocl.util.PropertyAccessorFactory;
import com.quartercode.disconnected.sim.run.Ticker;
import com.quartercode.disconnected.world.comp.file.ContentFile;
import com.quartercode.disconnected.world.comp.file.File;
import com.quartercode.disconnected.world.comp.file.FileSystemModule;
import com.quartercode.disconnected.world.comp.os.CommonFiles;
import com.quartercode.disconnected.world.comp.os.Configuration;
import com.quartercode.disconnected.world.comp.os.Configuration.ConfigurationEntry;
import com.quartercode.disconnected.world.comp.os.EnvironmentVariable;
import com.quartercode.disconnected.world.comp.os.OSModule;
import com.quartercode.disconnected.world.comp.os.OperatingSystem;

/**
 * This class represents an {@link OperatingSystem} module which is used to manage the {@link RootProcess}.
 * It is an essential part of the {@link OperatingSystem} and is directly used by it.
 * 
 * @see RootProcess
 * @see OSModule
 * @see OperatingSystem
 */
public class ProcessModule extends OSModule {

    // ----- Properties -----

    /**
     * The {@link RootProcess} which is the root of the entire {@link Process} tree.
     * It always has a pid of 0.
     */
    protected static final FeatureDefinition<ObjectProperty<RootProcess>> ROOT_PROCESS;

    static {

        ROOT_PROCESS = new AbstractFeatureDefinition<ObjectProperty<RootProcess>>("rootProcess") {

            @Override
            public ObjectProperty<RootProcess> create(FeatureHolder holder) {

                return new ObjectProperty<RootProcess>(getName(), holder);
            }

        };

    }

    // ----- Properties End -----

    // ----- Functions -----

    /**
     * Returns the {@link RootProcess} which always has a pid of 0.
     * The {@link RootProcess} is started by the os kernel.
     */
    public static final FunctionDefinition<RootProcess>                   GET_ROOT;

    /**
     * Returns a {@link List} containing all currently running {@link Process}es.
     */
    public static final FunctionDefinition<List<Process<?>>>              GET_ALL;

    static {

        GET_ROOT = FunctionDefinitionFactory.create("getRoot", ProcessModule.class, PropertyAccessorFactory.createGet(ROOT_PROCESS));

        GET_ALL = FunctionDefinitionFactory.create("getAll", ProcessModule.class, new FunctionExecutor<List<Process<?>>>() {

            @Override
            public List<Process<?>> invoke(FeatureHolder holder, Object... arguments) throws ExecutorInvokationException {

                List<Process<?>> processes = new ArrayList<Process<?>>();
                RootProcess root = holder.get(GET_ROOT).invoke();
                processes.add(root);
                processes.addAll(root.get(Process.GET_ALL_CHILDREN).invoke());
                return processes;
            }

        });

        SET_RUNNING.addExecutor(ProcessModule.class, "startRootProcess", new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FeatureHolder holder, Object... arguments) throws ExecutorInvokationException {

                // Only invoke on bootstrap
                if ((Boolean) arguments[0]) {
                    RootProcess root = new RootProcess();
                    root.setLocked(false);

                    FileSystemModule fsModule = ((ProcessModule) holder).getParent().get(OperatingSystem.GET_FS_MODULE).invoke();
                    File<?> environmentFile = fsModule.get(FileSystemModule.GET_FILE).invoke(CommonFiles.ENVIRONMENT_CONFIG);
                    Configuration environmentConfig = (Configuration) environmentFile.get(ContentFile.GET_CONTENT).invoke();
                    Map<String, String> environment = new HashMap<String, String>();
                    for (ConfigurationEntry variable : environmentConfig.get(Configuration.GET_ENTRIES).invoke()) {
                        environment.put(variable.get(EnvironmentVariable.GET_NAME).invoke(), variable.get(EnvironmentVariable.GET_VALUE).invoke());
                    }
                    root.get(Process.SET_ENVIRONMENT).invoke(environment);

                    root.get(Process.LAUNCH).invoke(new HashMap<String, Object>());
                    root.setLocked(true);

                    holder.get(ProcessModule.ROOT_PROCESS).set(root);
                }

                return null;
            }
        });

        SET_RUNNING.addExecutor(OperatingSystem.class, "interruptRootProcess", new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FeatureHolder holder, Object... arguments) throws ExecutorInvokationException {

                // Only invoke on shutdown
                if (! ((Boolean) arguments[0])) {
                    holder.get(ProcessModule.GET_ROOT).invoke().get(Process.INTERRUPT).invoke();
                    // Stop the root process after 5 seconds
                    holder.get(SET_RUNNING).getExecutor("procManagerStopRoot").resetInvokationCounter();
                    holder.get(SET_RUNNING).getExecutor("procManagerStopRoot").setLocked(false);
                }

                return null;
            }

        });
        SET_RUNNING.addExecutor(OperatingSystem.class, "stopRootProcess", new FunctionExecutor<Void>() {

            @Override
            @Limit (1)
            // 5 seconds delay after interrupt
            @Delay (firstDelay = Ticker.DEFAULT_TICKS_PER_SECOND * 5)
            public Void invoke(FeatureHolder holder, Object... arguments) throws ExecutorInvokationException {

                holder.get(ProcessModule.GET_ROOT).invoke().get(Process.STOP).invoke();
                holder.get(ProcessModule.ROOT_PROCESS).set(null);

                return null;
            }

        });

    }

    // ----- Functions End -----

    /**
     * Creates a new process module.
     */
    public ProcessModule() {

    }

}
