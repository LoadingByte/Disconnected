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

package com.quartercode.disconnected.server.sim.scheduler;

import com.quartercode.classmod.extra.conv.CFeatureHolder;
import com.quartercode.disconnected.shared.util.XmlPersistent;

/**
 * A scheduler task is a unit that can be scheduled to be executed using a {@link Scheduler}.
 * It contains a method to execute it the right time has come ({@link #execute(CFeatureHolder)}).<br>
 * <br>
 * Setting the {@link #isCancelled()} flag to {@code true} cancels the task and removes it from its scheduler.
 * That means that the task will no longer be executed.
 * Note that the {@link #cloneStateless()} method is used to create clones of the task without the cancelled flag.
 * That way, the same task object can be scheduled multiple times (if the task itself has no other state or overrides the clone stateless method).<br>
 * <br>
 * Please note that the state of a scheduler task must be serializable using JAXB persistence.
 * 
 * @see Scheduler
 */
@XmlPersistent
public interface SchedulerTask {

    /**
     * Returns whether the task has been cancelled and should no longer be executed.
     * 
     * @return Whether the scheduler task has been cancelled.
     */
    public boolean isCancelled();

    /**
     * Cancels the scheduler task so it will no longer be executed.
     * This method should set the {@link #isCancelled()} flag to {@code true}.
     */
    public void cancel();

    /**
     * Executes the scheduler task.
     * This method should only be used by the {@link Scheduler} in order to execute a task.
     * 
     * @param holder The {@link CFeatureHolder} that contains the scheduler which called the method.
     */
    public void execute(CFeatureHolder holder);

    /**
     * Creates a copy of the scheduler task without copying the {@link #isCancelled() cancellation} attribute.
     * 
     * @return A stateless copy of the task.
     */
    public SchedulerTask cloneStateless();

}
