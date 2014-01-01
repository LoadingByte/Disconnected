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

import com.quartercode.disconnected.mocl.base.FeatureHolder;
import com.quartercode.disconnected.mocl.extra.FunctionExecutor;
import com.quartercode.disconnected.mocl.extra.Prioritized;
import com.quartercode.disconnected.mocl.extra.StopExecutionException;
import com.quartercode.disconnected.world.comp.os.OperatingSystem;

/**
 * The root process is a simple {@link Process} which can be only used as root for the process tree.
 * 
 * @see Process
 */
public class RootProcess extends Process<OperatingSystem> {

    // ----- Functions -----

    static {

        GET_ROOT.addExecutor(RootProcess.class, "overwrite", new FunctionExecutor<RootProcess>() {

            @Override
            public RootProcess invoke(FeatureHolder holder, Object... arguments) throws StopExecutionException {
            @Prioritized (Prioritized.LEVEL_5)

                return (RootProcess) holder;
            }

        });

    }

    // ----- Functions End -----

    /**
     * Creates a new empty root process.
     * You can start the new process using {@link #LAUNCH}.
     */
    public RootProcess() {

    }

}
