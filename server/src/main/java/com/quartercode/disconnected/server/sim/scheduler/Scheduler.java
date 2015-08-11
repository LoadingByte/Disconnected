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

package com.quartercode.disconnected.server.sim.scheduler;

import java.util.List;
import com.quartercode.disconnected.server.sim.scheduler.DefaultScheduler.ScheduledTask;
import com.quartercode.jtimber.api.node.Node;

/**
 * A scheduler is a {@link Node} that allows executing actions (in the form of {@link ScheduledTask}s) after a set delay and/or periodically.
 * Such tasks are registered and started through the two {@code schedule()} methods
 * ({@link #schedule(String, String, int, SchedulerTask)} and {@link #schedule(String, String, int, int, SchedulerTask)}).
 * For actually counting down any delay and finally executing the tasks, the {@link #update(String)} method needs to be called
 * once per tick for each group.
 * Note that the update method can be temporarily disabled using the {@link #setActive(boolean)} method.<br>
 * <br>
 * It is important to note that a scheduler expects to have a {@link Node#getSingleParent() single parent} node.
 * Moreover, that single parent must implement the {@link SchedulerRegistryProvider} interface.
 * The scheduler needs such a {@link SchedulerRegistry} in order to register itself for updating if it has any tasks.
 * If its single parent doesn't implement the interface, no exception is thrown; however, the scheduler also isn't able to register itself.
 * Therefore, such a single parent should only be used for testing (when a scheduler index isn't needed for updating all schedulers).
 *
 * @see SchedulerTask
 */
public interface Scheduler<P extends Node<?>> extends Node<P> {

    /**
     * Returns whether the scheduler is active and processes {@link #update(String)} invocations.
     * If this is set to {@code false}, any update calls do nothing.
     *
     * @return Whether the scheduler is active.
     */
    public boolean isActive();

    /**
     * Sets whether the scheduler is active and processes {@link #update(String)} invocations.
     * If this is set to {@code false}, any update calls do nothing.
     *
     * @param active Whether the scheduler should be active.
     */
    public void setActive(boolean active);

    /**
     * Returns all {@link SchedulerTask}s that are currently scheduled.
     * Such tasks must have been added using {@link #schedule(String, String, int, SchedulerTask)} or {@link #schedule(String, String, int, int, SchedulerTask)} before.
     * The order of the returned tasks matches the order in which they were scheduled.
     *
     * @return All currently scheduled tasks.
     */
    public List<SchedulerTask<? super P>> getTasks();

    /**
     * Retrieves the scheduled {@link SchedulerTask} that has the given name.
     * If multiple tasks have the same name, the task that was scheduled first is returned.
     * For example, this method could be used to {@link SchedulerTask#cancel() cancel} the retrieved task.
     *
     * @param name The name of the task that should be returned.
     * @return The first task with the given name, or {@code null} if no one exists.
     */
    public SchedulerTask<? super P> getTaskByName(String name);

    /**
     * Retrieves the {@link SchedulerTask}s that are scheduled for the given group.
     * If no tasks are assigned to the given group, an empty list is returned.
     * The order of the returned tasks matches the order in which they were scheduled.
     *
     * @param group The group of the tasks that should be returned.
     * @return The tasks with the given group.
     */
    public List<SchedulerTask<? super P>> getTasksByGroup(String group);

    /**
     * Schedules the given non-repeating {@link SchedulerTask}.
     * Note that the task can be can be {@link SchedulerTask#cancel() cancelled}.
     * Also note that calling this method is equivalent to calling {@link #schedule(String, String, int, int, SchedulerTask)} with a {@code periodicDelay} of {@code -1}.<br>
     * <br>
     * The {@code name} can be used to retrieve the task from the scheduler using {@link #getTaskByName(String)}.
     * Afterwards, the {@link SchedulerTask#cancel()} method could then be used to remove it, for example.
     * Note that this field may be {@code null}, in which case the scheduler task is anonymous.<br>
     * <br>
     * The {@code initialDelay} defines the delay (amount of ticks) after which the task should be executed once.
     * In other words, that delay must elapse before the task is executed.
     * After the execution, the task is removed and won't be called again.
     * The delay is defined "inclusively". For example, a task with an initial delay of 5 is executed on the fifth tick after it was scheduled.
     * An immediate execution on the next group tick can be achieved by setting the initial delay to {@code 1}.<br>
     * <br>
     * The {@code group} field defines at which point during a tick the task should be executed.
     * A map of groups to priorities is used by the caller of the {@link #update(String)} method.
     * For example, all tasks with a group which has the priority 2 are executed before all tasks of a priority 1 group.
     *
     * @param name The name that can be used to identify the task from inside the scheduler.
     *        This field may be {@code null}, in which case the task is anonymous.
     * @param group The group which defines at which point during a tick the task should be executed.
     * @param initialDelay The amount of ticks that must elapse before the task is executed once.
     * @param task The scheduler task that should be scheduled for execution.
     */
    public void schedule(String name, String group, int initialDelay, SchedulerTask<? super P> task);

    /**
     * Schedules the given repeating {@link SchedulerTask}.
     * Note that the task can be can be {@link SchedulerTask#cancel() cancelled}.<br>
     * <br>
     * The {@code name} can be used to retrieve the task from the scheduler using {@link #getTaskByName(String)}.
     * Afterwards, the {@link SchedulerTask#cancel()} method could then be used to remove it, for example.
     * Note that this field may be {@code null}, in which case the scheduler task is anonymous.<br>
     * <br>
     * The two fields {@code initialDelay} and {@code periodicDelay} define the delays (amount of ticks) after which the task should be executed.
     * The initial delay must elapse before the task is executed for the first time.
     * The periodic delay must elapse before the task is executed for any subsequent time (the task is called repetitively).
     * Note that the periodic delay can also be {@code -1}. In that case, the task is cancelled after it was executed for the first time.
     * Any delay is defined "inclusively". For example, a task with an initial delay of 5 is executed on the fifth tick after it was scheduled.
     * An immediate execution on the next group tick can be achieved by setting the initial delay to {@code 1}.
     * An execution of the task on each subsequent tick is caused by setting the periodic delay to {@code 1}.<br>
     * <br>
     * The {@code group} field defines at which point during a tick the task should be executed.
     * A map of groups to priorities is used by the caller of the {@link #update(String)} method.
     * For example, all tasks with a group which has the priority 2 are executed before all tasks of a priority 1 group.
     *
     * @param name The name that can be used to identify the task from inside the scheduler.
     *        This field may be {@code null}, in which case the task is anonymous.
     * @param group The group which defines at which point during a tick the task should be executed.
     * @param initialDelay The amount of ticks that must elapse before the task is executed for the first time.
     * @param periodicDelay The amount of ticks that must elapse before the task is executed for any subsequent time.
     * @param task The scheduler task that should be scheduled for execution.
     */
    public void schedule(String name, String group, int initialDelay, int periodicDelay, SchedulerTask<? super P> task);

    /**
     * Lets <b>one</b> tick pass and executes all scheduled {@link SchedulerTask}s which are assigned to the given group
     * and need to be executed based on their delays.
     * This method should be called once per tick for each group.
     * Note that this method can be temporarily disabled using the {@link #setActive(boolean)} method.<br>
     * <br>
     * If a scheduler task throws a {@link RuntimeException}, it is thrown up the stack to the update caller.
     * Since it is not the responsibility of the update caller to handle exceptions, they should be handled by the tasks themselves.
     *
     * @param group The group whose scheduler tasks should be updated.
     *        Note that this method should be invoked once per tick for <b>each</b> group.
     */
    public void update(String group);

}
