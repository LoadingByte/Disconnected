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
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.Getter;
import org.apache.commons.lang3.Validate;
import com.quartercode.disconnected.server.world.util.InterfaceAdapter;
import com.quartercode.disconnected.shared.util.XmlPersistent;
import com.quartercode.jtimber.api.node.DefaultNode;
import com.quartercode.jtimber.api.node.Node;
import com.quartercode.jtimber.api.node.wrapper.SubstituteWithWrapper;
import com.quartercode.jtimber.api.node.wrapper.collection.ListWrapper;

/**
 * The default {@link Scheduler} implementation.
 *
 * @see Scheduler
 */
@XmlPersistent
@XmlAccessorType (XmlAccessType.NONE)
public class DefaultScheduler<P extends Node<?>> extends DefaultNode<P> implements Scheduler<P> {

    @XmlAttribute
    private boolean                              active         = true;
    @XmlElement (name = "scheduledTask")
    @SubstituteWithWrapper (ListWrapper.class)
    private final List<ScheduledTask<? super P>> scheduledTasks = new ArrayList<>();

    @Override
    public boolean isActive() {

        return active;
    }

    @Override
    public void setActive(boolean active) {

        this.active = active;
    }

    @Override
    public List<SchedulerTask<? super P>> getTasks() {

        List<SchedulerTask<? super P>> tasks = new ArrayList<>();

        for (ScheduledTask<? super P> scheduledTask : scheduledTasks) {
            tasks.add(scheduledTask.getTask());
        }

        return tasks;
    }

    @Override
    public SchedulerTask<? super P> getTaskByName(String name) {

        Validate.notBlank(name, "Can't search for scheduler task with blank name");

        for (ScheduledTask<? super P> task : scheduledTasks) {
            String taskName = task.getName();

            if (taskName != null && taskName.equals(name)) {
                return task.getTask();
            }
        }

        return null;
    }

    @Override
    public List<SchedulerTask<? super P>> getTasksByGroup(String group) {

        Validate.notBlank(group, "Can't search for scheduler tasks with blank group");

        List<SchedulerTask<? super P>> tasks = new ArrayList<>();

        for (ScheduledTask<? super P> scheduledTask : scheduledTasks) {
            if (scheduledTask.getGroup().equals(group)) {
                tasks.add(scheduledTask.getTask());
            }
        }

        return tasks;
    }

    @Override
    public void schedule(String name, String group, int initialDelay, SchedulerTask<? super P> task) {

        schedule(name, group, initialDelay, -1, task);
    }

    @Override
    public void schedule(String name, String group, int initialDelay, int periodicDelay, SchedulerTask<? super P> task) {

        ScheduledTask<? super P> scheduledTask = new ScheduledTask<>(name, group, initialDelay, periodicDelay, task);

        // Update task list
        // The new task will be added at the end of the task list; therefore, if an update loop is currently iterating the list, the new task will be processed
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

        P schedulerParent = getSingleParent();

        for (int index = 0; index < scheduledTasks.size(); index++) {
            ScheduledTask<? super P> scheduledTask = scheduledTasks.get(index);

            if (scheduledTask.getGroup().endsWith(group)) {
                // Remove the task if it has been cancelled
                if (scheduledTask.getTask().isCancelled()) {
                    scheduledTasks.remove(index);
                    // Decrement the index so that no task will be skipped (we just removed one task; therefore, the next task has moved up by one)
                    index--;

                    // If the scheduler has no more tasks, remove it from the registry
                    if (scheduledTasks.isEmpty() && getSchedulerRegistry() != null) {
                        getSchedulerRegistry().removeScheduler(this);
                    }
                }
                // Otherwise, update the task
                else {
                    scheduledTask.update(schedulerParent);
                }
            }
        }
    }

    private SchedulerRegistry getSchedulerRegistry() {

        P schedulerParent = getSingleParent();

        if (schedulerParent instanceof SchedulerRegistryProvider) {
            return ((SchedulerRegistryProvider) schedulerParent).getSchedulerRegistry();
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
    protected static class ScheduledTask<P extends Node<?>> extends DefaultNode<Scheduler<? extends P>> {

        @Getter
        @XmlAttribute
        private String           name;
        @Getter
        @XmlAttribute
        private String           group;
        @XmlAttribute
        private int              initialDelay;
        @XmlAttribute
        private int              periodicDelay;

        @Getter
        @XmlElement
        @XmlJavaTypeAdapter (InterfaceAdapter.class)
        private SchedulerTask<P> task;

        @XmlAttribute
        private boolean          executedInitially;
        @XmlAttribute
        private int              ticksSinceLastExecution;

        // JAXB constructor
        protected ScheduledTask() {

        }

        private ScheduledTask(String name, String group, int initialDelay, int periodicDelay, SchedulerTask<P> task) {

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

        // ----- Updating -----

        private void update(P schedulerParent) {

            ticksSinceLastExecution++;

            if (!executedInitially && reachedDelay(initialDelay)) {
                if (periodicDelay > 0) { // is periodic
                    executedInitially = true;
                } else {
                    task.cancel();
                }

                execute(schedulerParent);
            } else if (executedInitially && reachedDelay(periodicDelay)) {
                execute(schedulerParent);
            }
        }

        private boolean reachedDelay(int delay) {

            return ticksSinceLastExecution >= delay;
        }

        private void execute(P schedulerParent) {

            ticksSinceLastExecution = 0;
            task.execute(getSingleParent(), schedulerParent);
        }

    }

}
