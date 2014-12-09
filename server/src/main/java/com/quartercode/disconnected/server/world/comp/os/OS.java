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

import static com.quartercode.classmod.factory.ClassmodFactory.factory;
import com.quartercode.classmod.extra.conv.CFeatureHolder;
import com.quartercode.classmod.extra.func.FunctionDefinition;
import com.quartercode.classmod.extra.func.FunctionExecutor;
import com.quartercode.classmod.extra.func.FunctionInvocation;
import com.quartercode.classmod.extra.prop.PropertyDefinition;
import com.quartercode.classmod.extra.storage.StandardStorage;
import com.quartercode.classmod.extra.valuefactory.ValueFactory;
import com.quartercode.classmod.factory.FunctionDefinitionFactory;
import com.quartercode.classmod.factory.PropertyDefinitionFactory;
import com.quartercode.disconnected.server.world.comp.Computer;
import com.quartercode.disconnected.server.world.comp.file.FSModule;
import com.quartercode.disconnected.server.world.comp.file.FileSystem;
import com.quartercode.disconnected.server.world.comp.net.NetModule;
import com.quartercode.disconnected.server.world.comp.net.Packet;
import com.quartercode.disconnected.server.world.comp.net.Socket;
import com.quartercode.disconnected.server.world.comp.prog.ProcModule;
import com.quartercode.disconnected.server.world.comp.prog.ProcState;
import com.quartercode.disconnected.server.world.comp.prog.Process;
import com.quartercode.disconnected.server.world.comp.prog.RootProcess;
import com.quartercode.disconnected.server.world.util.WorldChildFeatureHolder;
import com.quartercode.disconnected.shared.world.comp.Version;

/**
 * This class stores information about an operating system.
 * It is the core part of a running computer and manages all required modules (e.g. the {@link FSModule file system module}).
 * 
 * @see FSModule
 */
public class OS extends WorldChildFeatureHolder<Computer> {

    // ----- Properties -----

    /**
     * The name of the operating system.
     */
    public static final PropertyDefinition<String>     NAME;

    /**
     * The {@link Version} of the operating system.
     */
    public static final PropertyDefinition<Version>    VERSION;

    /**
     * The {@link FSModule file system module} for managing and accessing {@link FileSystem file systems}.
     */
    public static final PropertyDefinition<FSModule>   FS_MODULE;

    /**
     * The {@link ProcModule process module} which manages the whole {@link Process} tree.
     */
    public static final PropertyDefinition<ProcModule> PROC_MODULE;

    /**
     * The {@link NetModule network module} which takes care of sending and receiving {@link Packet}s.
     * It also allows to create {@link Socket}s for easier connection management.
     */
    public static final PropertyDefinition<NetModule>  NET_MODULE;

    static {

        NAME = factory(PropertyDefinitionFactory.class).create("name", new StandardStorage<>());
        VERSION = factory(PropertyDefinitionFactory.class).create("version", new StandardStorage<>());

        FS_MODULE = factory(PropertyDefinitionFactory.class).create("fsModule", new StandardStorage<>(), new ValueFactory<FSModule>() {

            @Override
            public FSModule get() {

                return new FSModule();
            }

        });

        PROC_MODULE = factory(PropertyDefinitionFactory.class).create("procModule", new StandardStorage<>(), new ValueFactory<ProcModule>() {

            @Override
            public ProcModule get() {

                return new ProcModule();
            }

        });

        NET_MODULE = factory(PropertyDefinitionFactory.class).create("netModule", new StandardStorage<>(), new ValueFactory<NetModule>() {

            @Override
            public NetModule get() {

                return new NetModule();
            }

        });

    }

    // ----- Functions -----

    /**
     * Returns whether the operating system is running or not.
     * The state is determined by the running state of the {@link RootProcess}.
     */
    public static final FunctionDefinition<Boolean>    IS_RUNNING;

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
    public static final FunctionDefinition<Void>       SET_RUNNING;

    static {

        IS_RUNNING = factory(FunctionDefinitionFactory.class).create("isRunning", new Class[0]);
        IS_RUNNING.addExecutor("default", OS.class, new FunctionExecutor<Boolean>() {

            @Override
            public Boolean invoke(FunctionInvocation<Boolean> invocation, Object... arguments) {

                CFeatureHolder holder = invocation.getCHolder();
                boolean running = holder.getObj(PROC_MODULE).getObj(ProcModule.ROOT_PROCESS).getObj(Process.STATE) != ProcState.STOPPED;

                invocation.next(arguments);
                return running;
            }

        });

        SET_RUNNING = factory(FunctionDefinitionFactory.class).create("setRunning", new Class[] { Boolean.class });
        SET_RUNNING.addExecutor("fsModule", OS.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                invocation.getCHolder().getObj(FS_MODULE).invoke(OSModule.SET_RUNNING, arguments);
                return invocation.next(arguments);
            }

        });
        SET_RUNNING.addExecutor("procModule", OS.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                invocation.getCHolder().getObj(PROC_MODULE).invoke(OSModule.SET_RUNNING, arguments);
                return invocation.next(arguments);
            }

        });
        SET_RUNNING.addExecutor("netModule", OS.class, new FunctionExecutor<Void>() {

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
    public OS() {

        setParentType(Computer.class);
    }

}
