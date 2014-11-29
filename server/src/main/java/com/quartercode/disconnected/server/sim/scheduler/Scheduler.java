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
import com.quartercode.classmod.base.Persistent;
import com.quartercode.classmod.base.def.AbstractFeature;
import com.quartercode.classmod.extra.CFeatureHolder;
import com.quartercode.disconnected.shared.util.XmlPersistent;

/**
 * The scheduler is a {@link Feature} that allows executing actions after a delay or periodically.
 * These delayed actions are called {@link SchedulerTask}s.
 * Such tasks are registered and started through the {@link #schedule(SchedulerTask)} method.
 * For actually counting down any delay and finally executing the tasks, the {@link #update(String)} method needs to be called
 * once per time unit (tick) for each group.
 * Note that the update method can be temporarily disabled using the {@link #setActive(boolean)} method.<br>
 * <br>
 * See the scheduler task class for more information on the scheduler system.
 * 
 * @see SchedulerTask
 */
@Persistent
@XmlPersistent
@XmlRootElement
public class Scheduler extends AbstractFeature {

    private boolean             active;
    @XmlElement (name = "scheduledTask")
    private List<ScheduledTask> scheduledTasks;

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
     * Such tasks must have been added using {@link #schedule(SchedulerTask)} before.
     * 
     * @return All currently scheduled tasks.
     */
    public List<SchedulerTask> getTasks() {

        List<SchedulerTask> tasks = new ArrayList<>();

        for (ScheduledTask scheduledTask : scheduledTasks) {
            tasks.add(scheduledTask.getTask());
        }

        return tasks;
    }

    /**
     * Retrieves the scheduled {@link SchedulerTask} that has the given name.
     * If multiple tasks have the same name, the task that was scheduled first is returned.
     * 
     * @param name The name of the task that should be returned.
     * @return The first task with the given name.
     */
    public SchedulerTask getTask(String name) {

        Validate.notNull(name, "Can't search for scheduler task with null name");

        for (ScheduledTask task : scheduledTasks) {
            String taskName = task.getTask().getName();

            if (taskName != null && taskName.equals(name)) {
                return task.getTask();
            }
        }

        return null;
    }

    /**
     * Schedules a stateless copy of the given {@link SchedulerTask} (see {@link SchedulerTask#cloneStateless()}).
     * See the scheduler task class for more information about when the task will be executed.
     * Note that the task can be can be cancelled by setting their {@link SchedulerTask#isCancelled()} flag to {@code true}.
     * 
     * @param task The scheduler task that should be scheduled for execution.
     */
    public void schedule(SchedulerTask task) {

        scheduledTasks.add(new ScheduledTask(task));
    }

    /**
     * Lets <b>one</b> time unit (tick) pass and executes all scheduled {@link SchedulerTask}s which are assigned to the given group
     * and need to be executed based on their delays.
     * This method should be called once per time unit for each group.
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

        for (ScheduledTask scheduledTask : scheduledTasks) {
            if (scheduledTask.getTask().getGroup().equals(group)) {
                if (scheduledTask.getTask().isCancelled()) {
                    scheduledTasks.remove(scheduledTask);
                } else {
                    // Only the usage of ConvenientFeatureHolder is allowed
                    scheduledTask.update((CFeatureHolder) getHolder());
                }
            }
        }
    }

    private static class ScheduledTask {

        private SchedulerTask task;

        @XmlAttribute
        private boolean       executedInitially;
        @XmlAttribute
        private int           ticksSinceLastExecution;

        // JAXB constructor
        @SuppressWarnings ("unused")
        protected ScheduledTask() {

        }

        private ScheduledTask(SchedulerTask task) {

            Validate.notNull(task, "Cannot schedule null task");
            Validate.isTrue(task.getInitialDelay() > 0, "Scheduler task initial delay (%d) must be > 0", task.getInitialDelay());
            Validate.isTrue(task.getPeriodicDelay() == -1 || task.getPeriodicDelay() > 0, "Scheduler task periodic delay (%d) must be -1 or > 0", task.getPeriodicDelay());
            Validate.notBlank(task.getGroup(), "Scheduler task group cannot be blank");

            this.task = task.cloneStateless();
        }

        private SchedulerTask getTask() {

            return task;
        }

        private void update(CFeatureHolder schedulerHolder) {

            ticksSinceLastExecution++;

            if (!executedInitially && reachedDelay(task.getInitialDelay())) {
                if (isPeriodic()) {
                    executedInitially = true;
                } else {
                    task.cancel();
                }

                execute(schedulerHolder);
            } else if (executedInitially && reachedDelay(task.getPeriodicDelay())) {
                execute(schedulerHolder);
            }
        }

        private boolean reachedDelay(int delay) {

            return ticksSinceLastExecution >= delay;
        }

        private boolean isPeriodic() {

            return task.getPeriodicDelay() > 0;
        }

        private void execute(CFeatureHolder schedulerHolder) {

            ticksSinceLastExecution = 0;
            task.execute(schedulerHolder);
        }

        // Use an object reference because JAXB doesn't support interfaces and SchedulerTask is an interface

        @XmlElement (name = "task")
        protected Object getTaskAsObject() {

            return task;
        }

        @SuppressWarnings ("unused")
        protected void setTaskAsObject(Object task) {

            this.task = (SchedulerTask) task;
        }

    }

}
