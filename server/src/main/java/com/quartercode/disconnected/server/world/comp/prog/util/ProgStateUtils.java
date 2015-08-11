/*
 * This file is part of Disconnected.
 * Copyright (c) 2013 QuarterCode <http://quartercode.com/>
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

package com.quartercode.disconnected.server.world.comp.prog.util;

import static com.quartercode.classmod.extra.func.Priorities.LEVEL_3;
import com.quartercode.classmod.extra.func.FunctionExecutor;
import com.quartercode.classmod.extra.func.FunctionInvocation;
import com.quartercode.classmod.extra.func.Priorities;
import com.quartercode.disconnected.server.world.comp.prog.ProcessStateListener;
import com.quartercode.disconnected.server.world.comp.prog.Process;
import com.quartercode.disconnected.server.world.comp.prog.ProgramExecutor;
import com.quartercode.disconnected.server.world.util.WorldFeatureHolder;
import com.quartercode.disconnected.shared.world.comp.prog.WorldProcessState;

/**
 * This utility class provides some utility methods and classes regarding {@link WorldProcessState}s for {@link ProgramExecutor} implementations.
 * They should be used to remove the need for boilerplate code.
 * 
 * @see ProgramExecutor
 */
public class ProgStateUtils {

    /**
     * Adds a function executor for registering the {@link StopOnInterruptPSListener} to the given {@link ProgramExecutor} type.
     * It stops each instance of the program it as soon as its process is interrupted.
     * Note that the executor is added to the priority {@link Priorities#LEVEL_3}.
     * 
     * @param executorType The program executor class the listener should be added to.
     */
    public static void addInterruptionStopperRegisteringExecutor(Class<? extends ProgramExecutor> executorType) {

        ProgramExecutor.RUN.addExecutor("registerInterruptionStopper", executorType, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                Process<?> process = ((ProgramExecutor) invocation.getCHolder()).getParent();
                process.addToColl(Process.STATE_LISTENERS, new StopOnInterruptPSListener());

                return invocation.next(arguments);
            }

        }, LEVEL_3);
    }

    private ProgStateUtils() {

    }

    /**
     * A {@link ProcessStateListener} that stops the {@link Process} it is attached to as soon as that process is interrupted.
     * 
     * @see ProgStateUtils#addInterruptionStopperRegisteringExecutor(Class)
     */
    public static class StopOnInterruptPSListener extends WorldFeatureHolder implements ProcessStateListener {

        static {

            ON_STATE_CHANGE.addExecutor("stopOnInterrupt", StopOnInterruptPSListener.class, new FunctionExecutor<Void>() {

                @Override
                public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                    Object newState = arguments[2];

                    if (newState == WorldProcessState.INTERRUPTED) {
                        ((Process<?>) arguments[0]).invoke(Process.STOP, true);
                    }

                    return invocation.next(arguments);
                }

            });

        }

    }

}
