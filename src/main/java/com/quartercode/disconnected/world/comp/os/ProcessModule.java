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

import java.util.ArrayList;
import java.util.List;
import com.quartercode.disconnected.mocl.base.FeatureDefinition;
import com.quartercode.disconnected.mocl.base.FeatureHolder;
import com.quartercode.disconnected.mocl.base.def.AbstractFeatureDefinition;
import com.quartercode.disconnected.mocl.extra.ExecutorInvokationException;
import com.quartercode.disconnected.mocl.extra.FunctionDefinition;
import com.quartercode.disconnected.mocl.extra.FunctionExecutor;
import com.quartercode.disconnected.mocl.extra.def.DefaultChildFeatureHolder;
import com.quartercode.disconnected.mocl.extra.def.ObjectProperty;
import com.quartercode.disconnected.mocl.util.FunctionDefinitionFactory;
import com.quartercode.disconnected.mocl.util.PropertyAccessorFactory;
import com.quartercode.disconnected.world.comp.program.Process;
import com.quartercode.disconnected.world.comp.program.RootProcess;

/**
 * This class represents a kernel module which is used to manage the {@link RootProcess}.
 * It is an essential part of the {@link OperatingSystem} and directly used by it.
 * 
 * @see RootProcess
 * @see OperatingSystem
 */
public class ProcessModule extends DefaultChildFeatureHolder<OperatingSystem> {

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

    }

    // ----- Functions End -----

    /**
     * Creates a new process module.
     */
    public ProcessModule() {

    }

}
