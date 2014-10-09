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

import com.quartercode.classmod.extra.CFeatureHolder;

/**
 * A scheduler task is a unit that can be scheduled to be executed using a {@link Scheduler}.
 * It contains several methods that define the behavior of the task, describe whether it has been cancelled or actually execute it the right time has come.
 * Note that one task object should not be used multiple times inside the same scheduler because the task presents its state through {@link #isCancelled()}.<br>
 * <br>
 * The two methods {@link #getInitialDelay()} and {@link #getPeriodicDelay()} define the delays after which the task should be executed.
 * The initial delay must elapse before the task is executed for the first time.
 * The periodic delay must elapse before the task is executed for any subsequent time.
 * Note that the periodic delay can also be -1. If that is the case, the task is cancelled after it was executed for the first time.
 * Any delay is defined inclusive. For example, a task with an initial delay of 5 is executed on the fifth tick after it was scheduled.<br>
 * <br>
 * The group, which is provided by the {@link #getGroup()} method, defines at which point inside a tick the task should be executed.
 * A list of groups with their priorities is stored by the caller of the {@link Scheduler#update(String)} method.
 * All tasks with a group which has the priority 2 are executed before all tasks of a priority 1 group.<br>
 * <br>
 * Finally, setting the {@link #isCancelled()} flag to {@code true} cancels the task and removes it from its scheduler.
 * That means that the task will no longer be executed.
 * Note that the {@link #cloneStateless()} method is used to create clones of the task without the cancelled flag.
 * That way, the same task object can be used inside the {@link Scheduler#schedule(SchedulerTask)} multiple times.<br>
 * <br>
 * Please note that the state of a scheduler task has to be serializable using JAXB persistence.
 * 
 * @see Scheduler
 */
public interface SchedulerTask {

    /**
     * Returns the name of the scheduler task.
     * It can be used to retrieve it from its {@link Scheduler} using {@link Scheduler#getTask(String)}.
     * Afterwards, the {@link #cancel()} method can be used to remove it.
     * Note that this field may be {@code null}, in which case the scheduler task is anonymous.
     * 
     * @return The name of the scheduler task.
     */
    public String getName();

    /**
     * Returns the group which defines at which point inside a tick the scheduler task should be executed.
     * For example, all tasks with a group which has the priority 2 are executed before all tasks of a priority 1 group.
     * Note that a list of groups with their priorities is stored by the caller of the {@link Scheduler#update(String)} method.
     * 
     * @return The group of the scheduler task.
     */
    public String getGroup();

    /**
     * Returns the initial delay which defines the amount of ticks that must elapse before the task is executed for the first time.
     * The delay value is inclusive. For example, a task with an initial delay of 5 is executed on the fifth tick after it was scheduled.
     * 
     * @return The initial delay of the scheduler task.
     */
    public int getInitialDelay();

    /**
     * Returns the periodic delay which defines the amount of ticks that must elapse before the task is executed for any subsequent time after the first time.
     * The delay value is inclusive. For example, a task with a periodic delay of 5 is executed on the fifth tick after it was executed for the last time.
     * 
     * @return The periodic delay of the scheduler task.
     */
    public int getPeriodicDelay();

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
     * @param holder The {@link CFeatureHolder} that contains the {@link Scheduler} which called the method.
     */
    public void execute(CFeatureHolder holder);

    /**
     * Creates a copy of the scheduler task keeping the initial delay, periodic delay, group, and execution behavior.
     * Note that this method must ignore the state of the task (e.g. {@link #isCancelled()}).
     * 
     * @return A stateless copy of the task.
     */
    public SchedulerTask cloneStateless();

}
