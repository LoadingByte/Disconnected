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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.apache.commons.lang3.Validate;
import com.quartercode.classmod.base.Feature;
import com.quartercode.classmod.base.Persistable;
import com.quartercode.classmod.def.base.AbstractFeature;
import com.quartercode.classmod.extra.conv.CFeatureHolder;
import com.quartercode.disconnected.shared.util.XmlPersistent;

/**
 * The scheduler is a {@link Feature} that allows executing actions after a delay or periodically.
 * These delayed actions are called {@link SchedulerTask}s.
 * Such tasks are registered and started through the two scheduler methods
 * ({@link #schedule(String, String, int, SchedulerTask)} and {@link #schedule(String, String, int, int, SchedulerTask)}).
 * For actually counting down any delay and finally executing the tasks, the {@link #update(String)} method needs to be called
 * once per tick for each group.
 * Note that the update method can be temporarily disabled using the {@link #setActive(boolean)} method.
 * 
 * @see SchedulerTask
 */
@XmlPersistent
@XmlRootElement
public class Scheduler extends AbstractFeature implements Persistable {

    private boolean                   active;
    @XmlElement (name = "scheduledTask")
    private List<ActiveScheduledTask> scheduledTasks;

    /**
     * Creates a new empty scheduler.
     * This is only recommended for direct field access (e.g. for serialization).
     */
    protected Scheduler() {

    }

    /**
     * Creates a new scheduler with the given name and {@link CFeatureHolder}.
     * 
     * @param name The name of the scheduler.
     * @param holder The feature holder which has and uses the new scheduler.
     */
    public Scheduler(String name, CFeatureHolder holder) {

        super(name, holder);

        active = true;
        scheduledTasks = new CopyOnWriteArrayList<>();
    }

    @Override
    public boolean isPersistent() {

        return true;
    }

    /**
     * Returns whether the scheduler is active and processes {@link #update(String)} invocations.
     * If this is set to {@code false}, any update calls do nothing.
     * 
     * @return Whether the scheduler is active.
     */
    public boolean isActive() {

        return active;
    }

    /**
     * Sets whether the scheduler is active and processes {@link #update(String)} invocations.
     * If this is set to {@code false}, any update calls do nothing.
     * 
     * @param active Whether the scheduler should be active.
     */
    public void setActive(boolean active) {

        this.active = active;
    }

    /**
     * Returns all {@link SchedulerTask}s that are currently scheduled.
     * Such tasks must have been added using {@link #schedule(String, String, int, SchedulerTask)} or {@link #schedule(String, String, int, int, SchedulerTask)} before.
     * 
     * @return All currently scheduled tasks.
     */
    public List<SchedulerTask> getTasks() {

        List<SchedulerTask> tasks = new ArrayList<>();

        for (ActiveScheduledTask scheduledTask : scheduledTasks) {
            tasks.add(scheduledTask.getTask());
        }

        return tasks;
    }

    /**
     * Retrieves the scheduled {@link SchedulerTask} that has the given name.
     * If multiple tasks have the same name, the task that was scheduled first is returned.
     * For example, this method could be used to {@link SchedulerTask#cancel() cancel} the retrieved task.
     * 
     * @param name The name of the task that should be returned.
     * @return The first task with the given name.
     */
    public SchedulerTask getTask(String name) {

        Validate.notNull(name, "Can't search for scheduler task with null name");

        for (ActiveScheduledTask task : scheduledTasks) {
            String taskName = task.getName();

            if (taskName != null && taskName.equals(name)) {
                return task.getTask();
            }
        }

        return null;
    }

    /**
     * Schedules a {@link SchedulerTask#cloneStateless() stateless copy} of the given {@link SchedulerTask}.
     * Note that the task can be can be {@link SchedulerTask#cancel() cancelled}.
     * Also note that calling this method is equivalent to calling {@link #schedule(String, String, int, int, SchedulerTask)} with a {@code periodicDelay} of {@code -1}.<br>
     * <br>
     * The {@code name} can be used to retrieve the task from the scheduler using {@link #getTask(String)}.
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
     *        The same task object can be used multiple times because a stateless copy is created each time it is scheduled.
     *        However, that is only possible if the task itself has no other state or overrides the {@link SchedulerTask#cloneStateless()} method.
     */
    public void schedule(String name, String group, int initialDelay, SchedulerTask task) {

        schedule(name, group, initialDelay, -1, task);
    }

    /**
     * Schedules a {@link SchedulerTask#cloneStateless() stateless copy} of the given {@link SchedulerTask}.
     * Note that the task can be can be {@link SchedulerTask#cancel() cancelled}.<br>
     * <br>
     * The {@code name} can be used to retrieve the task from the scheduler using {@link #getTask(String)}.
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
     *        The same task object can be used multiple times because a stateless copy is created each time it is scheduled.
     *        However, that is only possible if the task itself has no other state or overrides the {@link SchedulerTask#cloneStateless()} method.
     */
    public void schedule(String name, String group, int initialDelay, int periodicDelay, SchedulerTask task) {

        scheduledTasks.add(new ActiveScheduledTask(name, group, initialDelay, periodicDelay, task));
    }

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
    public void update(String group) {

        if (!active) {
            return;
        }

        for (ActiveScheduledTask scheduledTask : scheduledTasks) {
            if (scheduledTask.getGroup().equals(group)) {
                if (scheduledTask.getTask().isCancelled()) {
                    scheduledTasks.remove(scheduledTask);
                } else {
                    // Only the usage of CFeatureHolder is allowed
                    scheduledTask.update((CFeatureHolder) getHolder());
                }
            }
        }
    }

    private static class ActiveScheduledTask extends ScheduledTask {

        @XmlAttribute
        private boolean executedInitially;
        @XmlAttribute
        private int     ticksSinceLastExecution;

        // JAXB constructor
        @SuppressWarnings ("unused")
        protected ActiveScheduledTask() {

        }

        private ActiveScheduledTask(String name, String group, int initialDelay, int periodicDelay, SchedulerTask task) {

            super(name, group, initialDelay, periodicDelay, task);
        }

        private void update(CFeatureHolder schedulerHolder) {

            ticksSinceLastExecution++;

            if (!executedInitially && reachedDelay(getInitialDelay())) {
                if (isPeriodic()) {
                    executedInitially = true;
                } else {
                    getTask().cancel();
                }

                execute(schedulerHolder);
            } else if (executedInitially && reachedDelay(getPeriodicDelay())) {
                execute(schedulerHolder);
            }
        }

        private boolean reachedDelay(int delay) {

            return ticksSinceLastExecution >= delay;
        }

        private void execute(CFeatureHolder schedulerHolder) {

            ticksSinceLastExecution = 0;
            getTask().execute(schedulerHolder);
        }

    }

}
