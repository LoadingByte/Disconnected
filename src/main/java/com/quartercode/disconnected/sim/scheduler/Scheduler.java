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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.apache.commons.lang3.Validate;
import com.quartercode.classmod.base.Feature;
import com.quartercode.classmod.base.FeatureDefinition;
import com.quartercode.classmod.base.FeatureHolder;
import com.quartercode.classmod.base.Persistent;
import com.quartercode.classmod.base.def.AbstractFeature;
import com.quartercode.classmod.base.def.AbstractFeatureDefinition;

/**
 * The scheduler is a {@link Feature} that allows executing delayed actions.<br>
 * These delayed actions are called {@link ScheduleTask}s.
 * Such tasks are registered and started through the {@link #schedule(ScheduleTask, int)} method.<br>
 * For actually counting down the delay, the {@link #update()} method needs to be called once every time unit (tick).
 * 
 * @see ScheduleTask
 */
@Persistent
@XmlRootElement
public class Scheduler extends AbstractFeature {

    /**
     * Creates a new {@link FeatureDefinition} that describes an scheduler with the given name.
     * 
     * @param name The name of the scheduler which the returned {@link FeatureDefinition} describes.
     * @return A {@link FeatureDefinition} which can be used to describe a scheduler.
     */
    public static FeatureDefinition<Scheduler> createDefinition(String name) {

        return new AbstractFeatureDefinition<Scheduler>(name) {

            @Override
            public Scheduler create(FeatureHolder holder) {

                return new Scheduler(getName(), holder);
            }

        };
    }

    @XmlElement (name = "runningTask")
    private List<ScheduleTaskContext> runningTasks;

    /**
     * Creates a new empty scheduler.
     * This is only recommended for direct field access (e.g. for serialization).
     */
    protected Scheduler() {

    }

    /**
     * Creates a new scheduler with the given name and {@link FeatureHolder}.
     * 
     * @param name The name of the scheduler.
     * @param holder The feature holder which has and uses the new scheduler.
     */
    public Scheduler(String name, FeatureHolder holder) {

        super(name, holder);

        runningTasks = new ArrayList<>();
    }

    /**
     * Returns the amount of {@link ScheduleTask}s that are currently running.
     * Such tasks must have been scheduled with {@link #schedule(ScheduleTask, int)} before.
     * 
     * @return The amount of currently running tasks.
     */
    public int countTasks() {

        return runningTasks.size();
    }

    /**
     * Schedules the given {@link ScheduleTask}. It will be executed after the given amount of time units (ticks).
     * 
     * @param task The actual {@link ScheduleTask} that should be scheduled for invocation.
     * @param delay The amount of time units (ticks) that pass before the task is invoked.
     *        Zero is not allowed, one would execute the task on the next {@link #update()} call.
     */
    public void schedule(ScheduleTask task, int delay) {

        Validate.notNull(task, "Cannot schedule null task");
        Validate.isTrue(delay > 0, "Delay (%d) must be > 0", delay);

        runningTasks.add(new ScheduleTaskContext(task, delay));
    }

    /**
     * Executes a time unit (tick) pass and invokes {@link ScheduleTask}s that need to be invoked.
     * This method should be called once every time unit (tick).<br>
     * <br>
     * If a {@link ScheduleTask} throws an exception, it is given up the stack to the update caller.
     * The exception might cause an error there because no one can handle it, so exceptions should be handled in the task classes.
     */
    public void update() {

        Iterator<ScheduleTaskContext> iterator = runningTasks.iterator();
        while (iterator.hasNext()) {
            ScheduleTaskContext task = iterator.next();
            task.update();
            if (task.isComplete()) {
                task.getTask().execute(getHolder());
                iterator.remove();
            }
        }
    }

    private static class ScheduleTaskContext {

        private ScheduleTask task;
        @XmlAttribute
        private int          remainingUpdates;

        @SuppressWarnings ("unused")
        protected ScheduleTaskContext() {

        }

        public ScheduleTaskContext(ScheduleTask task, int delay) {

            this.task = task;
            remainingUpdates = delay;
        }

        public ScheduleTask getTask() {

            return task;
        }

        public void update() {

            remainingUpdates--;
        }

        public boolean isComplete() {

            return remainingUpdates <= 0;
        }

        // Use an object reference to make it possible for JAXB to map the ScheduleTask interface

        @XmlElement (name = "task")
        protected Object getTaskAsObject() {

            return task;
        }

        @SuppressWarnings ("unused")
        protected void setTaskAsObject(Object task) {

            this.task = (ScheduleTask) task;
        }

    }

}
