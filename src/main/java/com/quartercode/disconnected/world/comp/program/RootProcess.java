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

import com.quartercode.classmod.extra.FunctionExecutor;
import com.quartercode.classmod.extra.FunctionInvocation;
import com.quartercode.classmod.extra.Prioritized;

/**
 * The root process is a simple {@link Process} which can be only used as root for the process tree.
 * 
 * @see Process
 */
public class RootProcess extends Process<ProcessModule> {

    // ----- Functions -----

    static {

        GET_ROOT.addExecutor("returnThis", RootProcess.class, new FunctionExecutor<RootProcess>() {

            @Override
            @Prioritized (Prioritized.LEVEL_6)
            public RootProcess invoke(FunctionInvocation<RootProcess> invocation, Object... arguments) {

                invocation.next(arguments);
                return (RootProcess) invocation.getHolder();
            }

        });

    }

    /**
     * Creates a new empty root process.
     */
    public RootProcess() {

        setParentType(ProcessModule.class);
    }

}
