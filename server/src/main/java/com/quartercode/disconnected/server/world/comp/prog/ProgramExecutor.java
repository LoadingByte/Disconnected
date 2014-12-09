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

package com.quartercode.disconnected.server.world.comp.prog;

import static com.quartercode.classmod.factory.ClassmodFactory.factory;
import com.quartercode.classmod.extra.func.FunctionDefinition;
import com.quartercode.classmod.factory.FunctionDefinitionFactory;
import com.quartercode.disconnected.server.world.util.WorldChildFeatureHolder;

/**
 * This abstract class defines a program executor which takes care of actually running a program.
 * The executor class is set in the {@link Program}.
 * 
 * @see Program
 * @see Process
 */
public abstract class ProgramExecutor extends WorldChildFeatureHolder<Process<?>> {

    // ----- Functions -----

    /**
     * This callback is executed once when the program executor should start running.
     * For example, this method could schedule tasks using the scheduler.<br>
     * <br>
     * <b>Important note:</b>
     * If this method registers some kind of hooks apart from scheduler tasks (e.g. event handlers), they must be removed when the program stops.
     * For doing that, {@link ProcStateListener process state listeners} should be added to the {@link Process#STATE_LISTENERS} callback list of the parent process.
     * Those listeners can then execute all clearance activities as soon as the process state is changed to {@link ProcState#STOPPED}.
     */
    public static final FunctionDefinition<Void> RUN;

    static {

        RUN = factory(FunctionDefinitionFactory.class).create("run", new Class[0]);

    }

    /**
     * Creates a new program executor.
     */
    public ProgramExecutor() {

        setParentType(Process.class);
    }

}
