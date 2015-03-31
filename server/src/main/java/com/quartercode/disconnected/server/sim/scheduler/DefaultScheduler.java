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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import org.apache.commons.lang3.Validate;
import com.quartercode.classmod.def.base.AbstractFeature;
import com.quartercode.classmod.extra.conv.CFeatureHolder;
import com.quartercode.disconnected.shared.util.XmlPersistent;

/**
 * The default {@link Scheduler} implementation.
 * 
 * @see Scheduler
 */
@XmlPersistent
@XmlRootElement
public class DefaultScheduler extends AbstractFeature implements Scheduler {

    private boolean             active;
    @XmlElement (name = "scheduledTask")
    private List<ScheduledTask> scheduledTasks;

    /**
     * Creates a new empty default scheduler.
     * This is only recommended for direct field access (e.g. for serialization).
     */
    protected DefaultScheduler() {

    }

    /**
     * Creates a new default scheduler with the given name and {@link CFeatureHolder}.
     * 
     * @param name The name of the default scheduler.
     * @param holder The feature holder which has and uses the new default scheduler.
     */
    public DefaultScheduler(String name, CFeatureHolder holder) {

        super(name, holder);

        active = true;
        scheduledTasks = new CopyOnWriteArrayList<>();
    }

    @Override
    public boolean isPersistent() {

        return !scheduledTasks.isEmpty();
    }

    @Override
    public boolean isActive() {

        return active;
    }

    @Override
    public void setActive(boolean active) {

        this.active = active;
    }

    @Override
    public List<SchedulerTask> getTasks() {

        List<SchedulerTask> tasks = new ArrayList<>();

        for (ScheduledTask scheduledTask : scheduledTasks) {
            tasks.add(scheduledTask.getTask());
        }

        return tasks;
    }

    @Override
    public SchedulerTask getTaskByName(String name) {

        Validate.notBlank(name, "Can't search for scheduler task with blank name");

        for (ScheduledTask task : scheduledTasks) {
            String taskName = task.getName();

            if (taskName != null && taskName.equals(name)) {
                return task.getTask();
            }
        }

        return null;
    }

    @Override
    public List<SchedulerTask> getTasksByGroup(String group) {

        Validate.notBlank(group, "Can't search for scheduler tasks with blank group");

        List<SchedulerTask> tasks = new ArrayList<>();

        for (ScheduledTask scheduledTask : scheduledTasks) {
            if (scheduledTask.getGroup().equals(group)) {
                tasks.add(scheduledTask.getTask());
            }
        }

        return tasks;
    }

    @Override
    public void schedule(String name, String group, int initialDelay, SchedulerTask task) {

        schedule(name, group, initialDelay, -1, task);
    }

    @Override
    public void schedule(String name, String group, int initialDelay, int periodicDelay, SchedulerTask task) {

        ScheduledTask scheduledTask = new ScheduledTask(name, group, initialDelay, periodicDelay, task);

        // Update task list
        scheduledTasks.add(scheduledTask);

        // If the first task has just been scheduled, add the scheduler to the registry
        // Note that the scheduler is removed from the registry once it has no more tasks
        if (scheduledTasks.size() == 1 && getSchedulerRegistry() != null) {
            getSchedulerRegistry().addScheduler(this);
        }
    }

    @Override
    public void update(String group) {

        if (!active) {
            return;
        }

        CFeatureHolder holder = (CFeatureHolder) getHolder();

        for (ScheduledTask scheduledTask : scheduledTasks) {
            if (scheduledTask.getGroup().endsWith(group)) {
                if (scheduledTask.getTask().isCancelled()) {
                    removeTask(scheduledTask, group);
                } else {
                    scheduledTask.update(holder);
                }
            }
        }
    }

    private void removeTask(ScheduledTask task, String group) {

        // Update task list
        scheduledTasks.remove(task);

        // If the scheduler has no more tasks, remove it from the registry
        if (scheduledTasks.isEmpty() && getSchedulerRegistry() != null) {
            getSchedulerRegistry().removeScheduler(this);
        }
    }

    private SchedulerRegistry getSchedulerRegistry() {

        if (getHolder() instanceof SchedulerRegistryProvider) {
            return ((SchedulerRegistryProvider) getHolder()).getSchedulerRegistry();
        } else {
            return null;
        }
    }

    // Override these methods to use the object's identity because that is required for making the SchedulerRegistry work

    @Override
    public int hashCode() {

        return System.identityHashCode(this);
    }

    @Override
    public boolean equals(Object obj) {

        return this == obj;
    }

    @XmlPersistent
    protected static class ScheduledTask {

        @Getter
        @XmlAttribute
        private String        name;
        @Getter
        @XmlAttribute
        private String        group;
        @XmlAttribute
        private int           initialDelay;
        @XmlAttribute
        private int           periodicDelay;

        @Getter
        private SchedulerTask task;

        @XmlAttribute
        private boolean       executedInitially;
        @XmlAttribute
        private int           ticksSinceLastExecution;

        // JAXB constructor
        protected ScheduledTask() {

        }

        private ScheduledTask(String name, String group, int initialDelay, int periodicDelay, SchedulerTask task) {

            Validate.notBlank(group, "Scheduler task group cannot be blank");
            Validate.isTrue(initialDelay > 0, "Scheduler task initial delay (%d) must be > 0", initialDelay);
            Validate.isTrue(periodicDelay == -1 || periodicDelay > 0, "Scheduler task periodic delay (%d) must be -1 or > 0", periodicDelay);
            Validate.notNull(task, "Cannot schedule null task");

            this.name = name;
            this.group = group;
            this.initialDelay = initialDelay;
            this.periodicDelay = periodicDelay;

            this.task = task;
        }

        // Use an object reference because JAXB doesn't support interfaces and SchedulerTask is an interface

        @XmlElement (name = "task")
        protected Object getTaskAsObject() {

            return task;
        }

        protected void setTaskAsObject(Object task) {

            this.task = (SchedulerTask) task;
        }

        // ----- Updating -----

        private void update(CFeatureHolder schedulerHolder) {

            ticksSinceLastExecution++;

            if (!executedInitially && reachedDelay(initialDelay)) {
                if (periodicDelay > 0) { // is periodic
                    executedInitially = true;
                } else {
                    task.cancel();
                }

                execute(schedulerHolder);
            } else if (executedInitially && reachedDelay(periodicDelay)) {
                execute(schedulerHolder);
            }
        }

        private boolean reachedDelay(int delay) {

            return ticksSinceLastExecution >= delay;
        }

        private void execute(CFeatureHolder schedulerHolder) {

            ticksSinceLastExecution = 0;
            task.invoke(SchedulerTask.EXECUTE, schedulerHolder);
        }

    }

}
