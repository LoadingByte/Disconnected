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

import java.util.HashSet;
import java.util.Set;
import com.quartercode.classmod.base.FeatureDefinition;
import com.quartercode.classmod.base.FeatureHolder;
import com.quartercode.classmod.base.def.AbstractFeatureDefinition;
import com.quartercode.classmod.extra.ExecutorInvocationException;
import com.quartercode.classmod.extra.FunctionDefinition;
import com.quartercode.classmod.extra.FunctionExecutor;
import com.quartercode.classmod.extra.FunctionInvocation;
import com.quartercode.classmod.extra.def.LockableFEWrapper;
import com.quartercode.classmod.extra.def.ObjectProperty;
import com.quartercode.classmod.util.CollectionPropertyAccessorFactory;
import com.quartercode.classmod.util.FunctionDefinitionFactory;
import com.quartercode.classmod.util.PropertyAccessorFactory;
import com.quartercode.disconnected.world.WorldChildFeatureHolder;
import com.quartercode.disconnected.world.comp.Computer;
import com.quartercode.disconnected.world.comp.Version;
import com.quartercode.disconnected.world.comp.Vulnerability;
import com.quartercode.disconnected.world.comp.file.FileSystem;
import com.quartercode.disconnected.world.comp.file.FileSystemModule;
import com.quartercode.disconnected.world.comp.net.NetworkModule;
import com.quartercode.disconnected.world.comp.net.Packet;
import com.quartercode.disconnected.world.comp.program.Process;
import com.quartercode.disconnected.world.comp.program.Process.ProcessState;
import com.quartercode.disconnected.world.comp.program.ProcessModule;
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

    /**
     * The {@link NetworkManager} which takes care of sending and receiving {@link Packet}s.
     */
    protected static final FeatureDefinition<ObjectProperty<NetworkModule>>      NETWORK_MODULE;

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

        NETWORK_MODULE = new AbstractFeatureDefinition<ObjectProperty<NetworkModule>>("networkModule") {

            @Override
            public ObjectProperty<NetworkModule> create(FeatureHolder holder) {

                return new ObjectProperty<NetworkModule>(getName(), holder, new NetworkModule());
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
     * Returns the {@link NetworkManager} which takes care of sending and receiving {@link Packet}s.
     */
    public static final FunctionDefinition<NetworkModule>                        GET_NET_MODULE;

    /**
     * Returns whether the operating system is running or not.
     * The state is determinated by the running state of the {@link RootProcess}.
     */
    public static final FunctionDefinition<Boolean>                              IS_RUNNING;

    /**
     * Boots up (true) or shuts down (false) the operating system.
     * If the operating system already is in the correct state, nothing happens.
     * This method calls the {@link OSModule#SET_RUNNING} function on every used module.
     * Because of the fact that modules are not stored in a central collection, every module has its executor which invokes the module method.
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
        GET_NET_MODULE = FunctionDefinitionFactory.create("getNetModule", OperatingSystem.class, PropertyAccessorFactory.createGet(NETWORK_MODULE));

        IS_RUNNING = FunctionDefinitionFactory.create("isRunning", OperatingSystem.class, new FunctionExecutor<Boolean>() {

            @Override
            public Boolean invoke(FunctionInvocation<Boolean> invocation, Object... arguments) throws ExecutorInvocationException {

                FeatureHolder holder = invocation.getHolder();
                boolean running = holder.get(GET_PROC_MODULE).invoke().get(ProcessModule.GET_ROOT).invoke().get(Process.GET_STATE).invoke() != ProcessState.STOPPED;

                invocation.next(arguments);
                return running;
            }

        });
        SET_RUNNING = FunctionDefinitionFactory.create("setRunning", Boolean.class);

    }

    // ----- Functions End -----

    // ----- Foreign Content -----

    static {

        SET_RUNNING.addExecutor(OperatingSystem.class, "fileSystemModule", new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) throws ExecutorInvocationException {

                invocation.getHolder().get(GET_FS_MODULE).invoke().get(OSModule.SET_RUNNING).invoke(arguments);
                return invocation.next(arguments);
            }

        });
        SET_RUNNING.addExecutor(OperatingSystem.class, "processModule", new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) throws ExecutorInvocationException {

                invocation.getHolder().get(GET_PROC_MODULE).invoke().get(OSModule.SET_RUNNING).invoke(arguments);
                return invocation.next(arguments);
            }

        });

    }

    // ----- Foreign Content End -----

}
