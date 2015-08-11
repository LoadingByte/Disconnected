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

package com.quartercode.disconnected.server.world.comp.prog;

import com.quartercode.disconnected.server.sim.scheduler.Scheduler;

/**
 * {@link ProgramExecutor}s which use a {@link Scheduler} must implement this interface and provide their scheduler through the {@link #getScheduler()} method.
 * That provided scheduler can then be (de)activated depending on the {@link Process#getState() state} of the {@link Process} that uses the program executor.
 * Note that program executors should not use other "custom" schedulers apart from the provided one because those schedulers would not be controllable by the process.
 *
 * @see Scheduler
 * @see ProgramExecutor
 */
public interface SchedulerUsing {

    /**
     * Returns the <b>one and only</b> {@link Scheduler} used by the program executor.
     * It is (de)activated depending on the {@link Process#getState() state} of the {@link Process} that uses the program executor.
     *
     * @return The scheduler used by the program executor.
     */
    public Scheduler<?> getScheduler();

}
