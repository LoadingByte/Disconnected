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

import com.quartercode.classmod.base.FeatureHolder;
import com.quartercode.classmod.extra.Delay;
import com.quartercode.classmod.extra.ExecutorInvocationException;
import com.quartercode.classmod.extra.FunctionDefinition;
import com.quartercode.classmod.extra.FunctionExecutor;
import com.quartercode.classmod.extra.FunctionInvocation;
import com.quartercode.classmod.extra.Prioritized;
import com.quartercode.classmod.util.FunctionDefinitionFactory;
import com.quartercode.disconnected.sim.run.TickSimulator.TickUpdatable;
import com.quartercode.disconnected.sim.run.Ticker;
import com.quartercode.disconnected.world.comp.os.OperatingSystem;

/**
 * The root process is a simple {@link Process} which can be only used as root for the process tree.
 * 
 * @see Process
 */
public class RootProcess extends Process<OperatingSystem> implements TickUpdatable {

    // ----- Functions -----

    /**
     * Waits 5 seconds (5 times the default amount of ticks per second) and then stops the root process.
     */
    public static final FunctionDefinition<Void> WAIT_AND_STOP;

    static {

        WAIT_AND_STOP = FunctionDefinitionFactory.create("waitAndStop", RootProcess.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) throws ExecutorInvocationException {

                invocation.getHolder().get(TICK_UPDATE).getExecutor("stopAfterDelay").setLocked(false);
                return invocation.next(arguments);
            }

        });

        TICK_UPDATE.addExecutor("lockDefaults", RootProcess.class, new FunctionExecutor<Void>() {

            @Override
            @Prioritized (Prioritized.LEVEL_7)
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) throws ExecutorInvocationException {

                invocation.getHolder().get(TICK_UPDATE).getExecutor("stopAfterDelay").setLocked(true);
                return invocation.next(arguments);
            }

        });
        TICK_UPDATE.addExecutor("stopAfterDelay", RootProcess.class, new FunctionExecutor<Void>() {

            @Override
            // 5 seconds delay after interrupt
            @Delay (firstDelay = Ticker.DEFAULT_TICKS_PER_SECOND * 5)
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) throws ExecutorInvocationException {

                FeatureHolder holder = invocation.getHolder();
                holder.get(Process.STOP).invoke();
                holder.get(ProcessModule.ROOT_PROCESS).set(null);

                invocation.getHolder().get(TICK_UPDATE).getExecutor("stopAfterDelay").setLocked(true);
                return invocation.next(arguments);
            }

        });

        GET_ROOT.addExecutor("returnThis", RootProcess.class, new FunctionExecutor<RootProcess>() {

            @Override
            @Prioritized (Prioritized.LEVEL_5)
            public RootProcess invoke(FunctionInvocation<RootProcess> invocation, Object... arguments) throws ExecutorInvocationException {

                invocation.next(arguments);
                return (RootProcess) invocation.getHolder();
            }

        });

        // Stop the execution after the setting of the pid
        LAUNCH.addExecutor("cancel", RootProcess.class, new FunctionExecutor<Void>() {

            @Override
            @Prioritized (Prioritized.LEVEL_5)
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) throws ExecutorInvocationException {

                return null;
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
