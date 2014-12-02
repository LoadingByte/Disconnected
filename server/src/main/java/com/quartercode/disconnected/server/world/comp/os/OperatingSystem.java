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

package com.quartercode.disconnected.server.world.comp.os;

import static com.quartercode.classmod.ClassmodFactory.create;
import org.apache.commons.lang3.reflect.TypeLiteral;
import com.quartercode.classmod.extra.CFeatureHolder;
import com.quartercode.classmod.extra.FunctionDefinition;
import com.quartercode.classmod.extra.FunctionExecutor;
import com.quartercode.classmod.extra.FunctionInvocation;
import com.quartercode.classmod.extra.PropertyDefinition;
import com.quartercode.classmod.extra.ValueFactory;
import com.quartercode.classmod.extra.storage.StandardStorage;
import com.quartercode.disconnected.server.world.comp.Computer;
import com.quartercode.disconnected.server.world.comp.file.FileSystem;
import com.quartercode.disconnected.server.world.comp.file.FileSystemModule;
import com.quartercode.disconnected.server.world.comp.net.NetworkModule;
import com.quartercode.disconnected.server.world.comp.net.Packet;
import com.quartercode.disconnected.server.world.comp.program.Process;
import com.quartercode.disconnected.server.world.comp.program.ProcessModule;
import com.quartercode.disconnected.server.world.comp.program.ProcessState;
import com.quartercode.disconnected.server.world.comp.program.RootProcess;
import com.quartercode.disconnected.server.world.util.WorldChildFeatureHolder;
import com.quartercode.disconnected.shared.world.comp.Version;

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
    public static final PropertyDefinition<String>           NAME;

    /**
     * The {@link Version} of the operating system.
     */
    public static final PropertyDefinition<Version>          VERSION;

    /**
     * The {@link FileSystemModule} for managing and accessing {@link FileSystem}s.
     */
    public static final PropertyDefinition<FileSystemModule> FS_MODULE;

    /**
     * The {@link ProcessModule} which manages the {@link RootProcess}.
     */
    public static final PropertyDefinition<ProcessModule>    PROC_MODULE;

    /**
     * The {@link NetworkModule} which takes care of sending and receiving {@link Packet}s.
     */
    public static final PropertyDefinition<NetworkModule>    NET_MODULE;

    static {

        NAME = create(new TypeLiteral<PropertyDefinition<String>>() {}, "name", "name", "storage", new StandardStorage<>());
        VERSION = create(new TypeLiteral<PropertyDefinition<Version>>() {}, "name", "version", "storage", new StandardStorage<>());

        FS_MODULE = create(new TypeLiteral<PropertyDefinition<FileSystemModule>>() {}, "name", "fsModule", "storage", new StandardStorage<>(), "initialValue", new ValueFactory<FileSystemModule>() {

            @Override
            public FileSystemModule get() {

                return new FileSystemModule();
            }

        });

        PROC_MODULE = create(new TypeLiteral<PropertyDefinition<ProcessModule>>() {}, "name", "procModule", "storage", new StandardStorage<>(), "initialValue", new ValueFactory<ProcessModule>() {

            @Override
            public ProcessModule get() {

                return new ProcessModule();
            }

        });

        NET_MODULE = create(new TypeLiteral<PropertyDefinition<NetworkModule>>() {}, "name", "netModule", "storage", new StandardStorage<>(), "initialValue", new ValueFactory<NetworkModule>() {

            @Override
            public NetworkModule get() {

                return new NetworkModule();
            }

        });

    }

    // ----- Functions -----

    /**
     * Returns whether the operating system is running or not.
     * The state is determined by the running state of the {@link RootProcess}.
     */
    public static final FunctionDefinition<Boolean>          IS_RUNNING;

    /**
     * Boots up ({@code true}) or shuts down ({@code false}) the operating system.
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
    public static final FunctionDefinition<Void>             SET_RUNNING;

    static {

        IS_RUNNING = create(new TypeLiteral<FunctionDefinition<Boolean>>() {}, "name", "isRunning", "parameters", new Class[0]);
        IS_RUNNING.addExecutor("default", OperatingSystem.class, new FunctionExecutor<Boolean>() {

            @Override
            public Boolean invoke(FunctionInvocation<Boolean> invocation, Object... arguments) {

                CFeatureHolder holder = invocation.getCHolder();
                boolean running = holder.getObj(PROC_MODULE).getObj(ProcessModule.ROOT_PROCESS).getObj(Process.STATE) != ProcessState.STOPPED;

                invocation.next(arguments);
                return running;
            }

        });

        SET_RUNNING = create(new TypeLiteral<FunctionDefinition<Void>>() {}, "name", "setRunning", "parameters", new Class[] { Boolean.class });
        SET_RUNNING.addExecutor("fsModule", OperatingSystem.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                invocation.getCHolder().getObj(FS_MODULE).invoke(OSModule.SET_RUNNING, arguments);
                return invocation.next(arguments);
            }

        });
        SET_RUNNING.addExecutor("procModule", OperatingSystem.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                invocation.getCHolder().getObj(PROC_MODULE).invoke(OSModule.SET_RUNNING, arguments);
                return invocation.next(arguments);
            }

        });
        SET_RUNNING.addExecutor("netModule", OperatingSystem.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                invocation.getCHolder().getObj(NET_MODULE).invoke(OSModule.SET_RUNNING, arguments);
                return invocation.next(arguments);
            }

        });

    }

    /**
     * Creates a new operating system.
     */
    public OperatingSystem() {

        setParentType(Computer.class);
    }

}
