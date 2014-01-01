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

import java.util.ResourceBundle;
import com.quartercode.disconnected.mocl.base.FeatureHolder;
import com.quartercode.disconnected.mocl.extra.Delay;
import com.quartercode.disconnected.mocl.extra.ExecutorInvokationException;
import com.quartercode.disconnected.mocl.extra.FunctionExecutor;
import com.quartercode.disconnected.mocl.extra.Limit;
import com.quartercode.disconnected.sim.run.Ticker;
import com.quartercode.disconnected.util.ResourceBundles;
import com.quartercode.disconnected.world.comp.os.OperatingSystem;
import com.quartercode.disconnected.world.comp.program.Process.ProcessState;

/**
 * The main kernel which runs the central functions of an operating system.
 * 
 * @see OperatingSystem
 */
public class KernelProgram extends ProgramExecutor {

    // ----- Functions -----

    static {

        GET_RESOURCE_BUNDLE.addExecutor(KernelProgram.class, "default", new FunctionExecutor<ResourceBundle>() {

            @Override
            public ResourceBundle invoke(FeatureHolder holder, Object... arguments) throws ExecutorInvokationException {

                return ResourceBundles.KERNEL;
            }
        });

        UPDATE.addExecutor(KernelProgram.class, "main", new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FeatureHolder holder, Object... arguments) throws ExecutorInvokationException {

                if ( ((KernelProgram) holder).getParent().get(Process.GET_STATE).invoke() == ProcessState.INTERRUPTED) {
                    holder.get(UPDATE).getExecutor("stop").resetInvokationCounter();
                    holder.get(UPDATE).getExecutor("stop").setLocked(false);
                } else {
                    holder.get(UPDATE).getExecutor("stop").setLocked(true);
                }

                return null;
            }

        });
        UPDATE.addExecutor(KernelProgram.class, "stop", new FunctionExecutor<Void>() {

            @Override
            @Delay (firstDelay = Ticker.DEFAULT_TICKS_PER_SECOND * 5)
            @Limit (1)
            public Void invoke(FeatureHolder holder, Object... arguments) throws ExecutorInvokationException {

                ((KernelProgram) holder).getParent().get(Process.STOP).invoke(true);

                return null;
            }

        });

    }

    // ----- Functions End -----

    /**
     * Creates a new kernel program.
     */
    public KernelProgram() {

    }

}
