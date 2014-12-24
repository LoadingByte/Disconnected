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
import com.quartercode.classmod.base.FeatureDefinition;
import com.quartercode.disconnected.server.sim.scheduler.Scheduler;
import com.quartercode.disconnected.server.sim.scheduler.SchedulerDefinitionFactory;

/**
 * {@link ProgramExecutor}s can implement this interface to inherit the {@link #SCHEDULER} feature (which obviously is a {@link Scheduler}).
 * However, that scheduler can be (de)activated depending on the {@link Process#STATE state} of the {@link Process} that uses the program executor.
 * Note that program executors should not use "custom" schedulers apart from this one in order to be controllable by the process.
 * 
 * @see Scheduler
 * @see ProgramExecutor
 */
public interface SchedulerUsing {

    // ----- Schedulers -----

    /**
     * The {@link Scheduler} which is (de)activated depending on the {@link Process#STATE state} of the {@link Process} that uses the program executor.
     */
    public static final FeatureDefinition<Scheduler> SCHEDULER = factory(SchedulerDefinitionFactory.class).create("scheduler");

}
