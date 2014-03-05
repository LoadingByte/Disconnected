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

package com.quartercode.disconnected.world.comp.os;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import com.quartercode.disconnected.mocl.base.FeatureDefinition;
import com.quartercode.disconnected.mocl.base.FeatureHolder;
import com.quartercode.disconnected.mocl.base.def.AbstractFeatureDefinition;
import com.quartercode.disconnected.mocl.extra.Delay;
import com.quartercode.disconnected.mocl.extra.ExecutorInvokationException;
import com.quartercode.disconnected.mocl.extra.FunctionDefinition;
import com.quartercode.disconnected.mocl.extra.FunctionExecutor;
import com.quartercode.disconnected.mocl.extra.Limit;
import com.quartercode.disconnected.mocl.extra.def.LockableFEWrapper;
import com.quartercode.disconnected.mocl.extra.def.ObjectProperty;
import com.quartercode.disconnected.mocl.util.CollectionPropertyAccessorFactory;
import com.quartercode.disconnected.mocl.util.FunctionDefinitionFactory;
import com.quartercode.disconnected.mocl.util.PropertyAccessorFactory;
import com.quartercode.disconnected.sim.run.Ticker;
import com.quartercode.disconnected.world.WorldChildFeatureHolder;
import com.quartercode.disconnected.world.comp.Computer;
import com.quartercode.disconnected.world.comp.Version;
import com.quartercode.disconnected.world.comp.Vulnerability;
import com.quartercode.disconnected.world.comp.file.ContentFile;
import com.quartercode.disconnected.world.comp.file.File;
import com.quartercode.disconnected.world.comp.file.FileSystem;
import com.quartercode.disconnected.world.comp.os.FileSystemModule.KnownFileSystem;
import com.quartercode.disconnected.world.comp.program.Process;
import com.quartercode.disconnected.world.comp.program.Process.ProcessState;
import com.quartercode.disconnected.world.comp.program.RootProcess;

/**
 * This class stores information about an operating system.
 * It is the core part of a running computer and manages all required modules (e.g. the {@link FileSystemModule}).
 * 
 * @see FileSystemModule
 */
public class OperatingSystem extends WorldChildFeatureHolder<Computer> {

    // ----- Properties -----

    /**
     * The name of the operating system.
     */
    protected static final FeatureDefinition<ObjectProperty<String>>             NAME;

    /**
     * The {@link Version} of the operating system.
     */
    protected static final FeatureDefinition<ObjectProperty<Version>>            VERSION;

    /**
     * The {@link Vulnerability}s the operating system has.
     */
    protected static final FeatureDefinition<ObjectProperty<Set<Vulnerability>>> VULNERABILITIES;

    /**
     * The {@link FileSystemModule} for managing and accessing {@link FileSystem}s.
     */
    protected static final FeatureDefinition<ObjectProperty<FileSystemModule>>   FILE_SYSTEM_MODULE;

    /**
     * The {@link ProcessModule} which manages the {@link RootProcess}.
     */
    protected static final FeatureDefinition<ObjectProperty<ProcessModule>>      PROCESS_MODULE;

    static {

        NAME = new AbstractFeatureDefinition<ObjectProperty<String>>("name") {

            @Override
            public ObjectProperty<String> create(FeatureHolder holder) {

                return new ObjectProperty<String>(getName(), holder);
            }

        };

        VERSION = new AbstractFeatureDefinition<ObjectProperty<Version>>("version") {

            @Override
            public ObjectProperty<Version> create(FeatureHolder holder) {

                return new ObjectProperty<Version>(getName(), holder);
            }

        };

        VULNERABILITIES = new AbstractFeatureDefinition<ObjectProperty<Set<Vulnerability>>>("vulnerabilities") {

            @Override
            public ObjectProperty<Set<Vulnerability>> create(FeatureHolder holder) {

                return new ObjectProperty<Set<Vulnerability>>(getName(), holder, new HashSet<Vulnerability>());
            }

        };

        FILE_SYSTEM_MODULE = new AbstractFeatureDefinition<ObjectProperty<FileSystemModule>>("fileSystemModule") {

            @Override
            public ObjectProperty<FileSystemModule> create(FeatureHolder holder) {

                return new ObjectProperty<FileSystemModule>(getName(), holder, new FileSystemModule());
            }

        };

        PROCESS_MODULE = new AbstractFeatureDefinition<ObjectProperty<ProcessModule>>("processModule") {

            @Override
            public ObjectProperty<ProcessModule> create(FeatureHolder holder) {

                return new ObjectProperty<ProcessModule>(getName(), holder, new ProcessModule());
            }

        };

    }

    // ----- Properties End -----

    // ----- Functions -----

    /**
     * Returns the name of the operating system.
     */
    public static final FunctionDefinition<String>                               GET_NAME;

    /**
     * Changes the name of the operating system.
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
     * <td>{@link String}</td>
     * <td>name</td>
     * <td>The new name for the operating system.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>                                 SET_NAME;

    /**
     * Returns the {@link Version} of the operating system.
     */
    public static final FunctionDefinition<Version>                              GET_VERSION;

    /**
     * Changes the {@link Version} of the operating system.
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
     * <td>{@link Version}</td>
     * <td>version</td>
     * <td>The new {@link Version} for the operating system.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>                                 SET_VERSION;

    /**
     * Returns the {@link Vulnerability}s the operating system has.
     */
    public static final FunctionDefinition<Set<Vulnerability>>                   GET_VULNERABILITIES;

    /**
     * Adds {@link Vulnerability}s to the operating system.
     * 
     * <table>
     * <tr>
     * <th>Index</th>
     * <th>Type</th>
     * <th>Parameter</th>
     * <th>Description</th>
     * </tr>
     * <tr>
     * <td>0...</td>
     * <td>{@link Vulnerability}...</td>
     * <td>vulnerabilities</td>
     * <td>The {@link Vulnerability}s to add to the operating system.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>                                 ADD_VULNERABILITIES;

    /**
     * Removes {@link Vulnerability}s from the operating system.
     * 
     * <table>
     * <tr>
     * <th>Index</th>
     * <th>Type</th>
     * <th>Parameter</th>
     * <th>Description</th>
     * </tr>
     * <tr>
     * <td>0...</td>
     * <td>{@link Vulnerability}...</td>
     * <td>vulnerabilities</td>
     * <td>The {@link Vulnerability}s to remove from the operating system.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>                                 REMOVE_VULNERABILITIES;

    /**
     * Returns the {@link FileSystemModule} for managing and accessing {@link FileSystem}s.
     */
    public static final FunctionDefinition<FileSystemModule>                     GET_FS_MODULE;

    /**
     * Returns the {@link FileSystemModule} for managing and accessing {@link FileSystem}s.
     */
    public static final FunctionDefinition<ProcessModule>                        GET_PROC_MODULE;

    /**
     * Returns whether the operating system is running or not.
     * The state is determinated by the running state of the {@link RootProcess}.
     */
    public static final FunctionDefinition<Boolean>                              IS_RUNNING;

    /**
     * Boots up (true) or shuts down (false) the operating system.
     * If the operating system already is in the correct state, nothing happens.
     * This method executes some actions like creating the {@link RootProcess} or mounting some {@link FileSystem}s.
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
     * <td>running</td>
     * <td>True to boot up the operating system, false to shut it down.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>                                 SET_RUNNING;

    static {

        GET_NAME = FunctionDefinitionFactory.create("getName", OperatingSystem.class, PropertyAccessorFactory.createGet(NAME));
        SET_NAME = FunctionDefinitionFactory.create("setName", OperatingSystem.class, new LockableFEWrapper<Void>(PropertyAccessorFactory.createSet(NAME)), String.class);

        GET_VERSION = FunctionDefinitionFactory.create("getVersion", OperatingSystem.class, PropertyAccessorFactory.createGet(VERSION));
        SET_VERSION = FunctionDefinitionFactory.create("setVersion", OperatingSystem.class, new LockableFEWrapper<Void>(PropertyAccessorFactory.createSet(VERSION)), Version.class);

        GET_VULNERABILITIES = FunctionDefinitionFactory.create("getVulnerabilities", OperatingSystem.class, CollectionPropertyAccessorFactory.createGet(VULNERABILITIES));
        ADD_VULNERABILITIES = FunctionDefinitionFactory.create("addVulnerabilities", OperatingSystem.class, new LockableFEWrapper<Void>(CollectionPropertyAccessorFactory.createAdd(VULNERABILITIES)), Vulnerability[].class);
        REMOVE_VULNERABILITIES = FunctionDefinitionFactory.create("removeVulnerabilities", OperatingSystem.class, new LockableFEWrapper<Void>(CollectionPropertyAccessorFactory.createRemove(VULNERABILITIES)), Vulnerability[].class);

        GET_FS_MODULE = FunctionDefinitionFactory.create("getFsModule", OperatingSystem.class, PropertyAccessorFactory.createGet(FILE_SYSTEM_MODULE));
        GET_PROC_MODULE = FunctionDefinitionFactory.create("getProcModule", OperatingSystem.class, PropertyAccessorFactory.createGet(PROCESS_MODULE));

        IS_RUNNING = FunctionDefinitionFactory.create("isRunning", OperatingSystem.class, new FunctionExecutor<Boolean>() {

            @Override
            public Boolean invoke(FeatureHolder holder, Object... arguments) throws ExecutorInvokationException {

                return holder.get(GET_PROC_MODULE).invoke().get(ProcessModule.GET_ROOT).invoke().get(Process.GET_STATE).invoke() != ProcessState.STOPPED;
            }

        });
        SET_RUNNING = FunctionDefinitionFactory.create("setRunning", Boolean.class);

    }

    // ----- Functions End -----

    // ----- Foreign Content -----

    static {

        SET_RUNNING.addExecutor(OperatingSystem.class, "fsManagerMountSystem", new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FeatureHolder holder, Object... arguments) throws ExecutorInvokationException {

                // Only invoke on bootstrap
                if ((Boolean) arguments[0]) {
                    for (KnownFileSystem fileSystem : holder.get(GET_FS_MODULE).invoke().get(FileSystemModule.GET_KNOWN).invoke()) {
                        if (fileSystem.get(KnownFileSystem.GET_MOUNTPOINT).invoke().equals(CommonFiles.SYSTEM_MOUNTPOINT)) {
                            fileSystem.get(KnownFileSystem.SET_MOUNTED).invoke(true);
                            break;
                        }
                    }
                }

                return null;
            }

        });

        SET_RUNNING.addExecutor(OperatingSystem.class, "procManagerStartRoot", new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FeatureHolder holder, Object... arguments) throws ExecutorInvokationException {

                // Only invoke on bootstrap
                if ((Boolean) arguments[0]) {
                    RootProcess root = new RootProcess();
                    root.setLocked(false);

                    File<?> environmentFile = holder.get(GET_FS_MODULE).invoke().get(FileSystemModule.GET_FILE).invoke(CommonFiles.ENVIRONMENT_CONFIG);
                    Environment environment = ((Environment) environmentFile.get(ContentFile.GET_CONTENT).invoke()).clone();
                    root.get(Process.SET_ENVIRONMENT).invoke(environment);

                    root.get(Process.LAUNCH).invoke(new HashMap<String, Object>());
                    root.setLocked(true);

                    holder.get(GET_PROC_MODULE).invoke().get(ProcessModule.ROOT_PROCESS).set(root);
                }

                return null;
            }

        });
        SET_RUNNING.addExecutor(OperatingSystem.class, "procManagerInterruptRoot", new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FeatureHolder holder, Object... arguments) throws ExecutorInvokationException {

                // Only invoke on shutdown
                if (! ((Boolean) arguments[0])) {
                    holder.get(GET_PROC_MODULE).invoke().get(ProcessModule.GET_ROOT).invoke().get(Process.INTERRUPT).invoke();
                    // Stop the root process after 5 seconds
                    holder.get(SET_RUNNING).getExecutor("procManagerStopRoot").resetInvokationCounter();
                    holder.get(SET_RUNNING).getExecutor("procManagerStopRoot").setLocked(false);
                }

                return null;
            }

        });
        SET_RUNNING.addExecutor(OperatingSystem.class, "procManagerStopRoot", new FunctionExecutor<Void>() {

            @Override
            @Limit (1)
            @Delay (firstDelay = Ticker.DEFAULT_TICKS_PER_SECOND * 5)
            public Void invoke(FeatureHolder holder, Object... arguments) throws ExecutorInvokationException {

                holder.get(GET_PROC_MODULE).invoke().get(ProcessModule.GET_ROOT).invoke().get(Process.STOP).invoke();
                holder.get(GET_PROC_MODULE).invoke().get(ProcessModule.ROOT_PROCESS).set(null);

                return null;
            }

        });

    }

    // ----- Foreign Content End -----

    /**
     * Creates a new operating system.
     */
    public OperatingSystem() {

        // ----- Temporary -----
        networkManager = new NetworkManager(this);
        // ----- Temporary End -----
    }

    // TODO: Remove
    // ----- Temporary -----

    private final NetworkManager networkManager;

    /**
     * Returns the network manager which is used for storing and delivering packets.
     * 
     * @return The network manager which is used for storing and delivering packets.
     */
    public NetworkManager getNetworkManager() {

        return networkManager;
    }

    // ----- Temporary End -----

}
