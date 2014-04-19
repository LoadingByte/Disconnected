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

package com.quartercode.disconnected.sim.scheduler;

import com.quartercode.classmod.base.FeatureHolder;

/**
 * A schedule task is a unit that can be called by a {@link Scheduler}.
 * It only contains one {@link #execute(FeatureHolder)} method that is invoked whenever the {@link Scheduler} calls the task.
 * A task can be used multiple times in the same {@link Scheduler}, even at the same time.<br>
 * <br>
 * Please note that the state of a schedule task has to be restorable using JAXB persistence.
 * That means that every attribute must have a JAXB annotation etc.
 * 
 * @see Scheduler
 */
public interface ScheduleTask {

    /**
     * Executes some functionality the schedule task defines.
     * This method should only be used by the {@link Scheduler} in order to execute a task.
     * 
     * @param holder The {@link FeatureHolder} where the {@link Scheduler} that calls the method is used in.
     */
    public abstract void execute(FeatureHolder holder);

}
