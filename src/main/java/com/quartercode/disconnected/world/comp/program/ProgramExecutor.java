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

import static com.quartercode.classmod.ClassmodFactory.create;
import org.apache.commons.lang3.reflect.TypeLiteral;
import com.quartercode.classmod.extra.FunctionDefinition;
import com.quartercode.classmod.extra.FunctionExecutor;
import com.quartercode.classmod.extra.FunctionInvocation;
import com.quartercode.classmod.extra.Prioritized;
import com.quartercode.disconnected.sim.scheduler.SchedulerUser;
import com.quartercode.disconnected.world.World;
import com.quartercode.disconnected.world.WorldChildFeatureHolder;
import com.quartercode.disconnected.world.event.ProgramLaunchEvent;
import com.quartercode.eventbridge.bridge.Bridge;

/**
 * This abstract class defines a program executor which takes care of acutally running a program.
 * The executor class is set in the {@link Program}.
 * 
 * @see Program
 * @see Process
 */
public abstract class ProgramExecutor extends WorldChildFeatureHolder<Process<?>> implements SchedulerUser {

    // ----- Functions -----

    /**
     * This callback is executed once when the program executor should start running.
     * For example, this method could schedule tasks using the scheduler.
     */
    public static final FunctionDefinition<Void> RUN;

    static {

        // Prevent the scheduler from updating if the current process state isn't an active one
        TICK_UPDATE.addExecutor("checkAllowTick", ProgramExecutor.class, new FunctionExecutor<Void>() {

            @Override
            @Prioritized (Prioritized.LEVEL_9)
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                if ( ((ProgramExecutor) invocation.getHolder()).getParent().get(Process.STATE).get().isTickState()) {
                    invocation.next(arguments);
                }

                return null;
            }

        });

        RUN = create(new TypeLiteral<FunctionDefinition<Void>>() {}, "name", "run", "parameters", new Class[0]);
        RUN.addExecutor("sendLaunchEvent", ProgramExecutor.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                ProgramExecutor holder = (ProgramExecutor) invocation.getHolder();
                World world = holder.getWorld();

                if (world != null) {
                    Process<?> parent = holder.getParent();
                    Bridge bridge = world.getBridge();

                    String computerId = parent.get(Process.GET_OPERATING_SYSTEM).invoke().getParent().getId();
                    int pid = parent.get(Process.PID).get();
                    bridge.send(new ProgramLaunchEvent(computerId, pid, holder.getClass()));
                }

                return invocation.next(arguments);
            }

        });

    }

    /**
     * Creates a new program executor.
     */
    public ProgramExecutor() {

        setParentType(Process.class);
    }

}
