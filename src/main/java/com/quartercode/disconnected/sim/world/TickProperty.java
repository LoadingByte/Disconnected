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

package com.quartercode.disconnected.sim.world;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import com.quartercode.disconnected.util.InfoString;
import com.quartercode.disconnected.util.ObjectInstanceAdapter;

/**
 * A tick property handles and invokes update tasks and is used to create tick updates.
 */
public class TickProperty extends Property {

    @XmlElement (name = "updateTask")
    private final List<UpdateTask> updateTasks = new ArrayList<UpdateTask>();

    /**
     * Creates a new empty tick property.
     * This is only recommended for direct field access (e.g. for serialization).
     */
    protected TickProperty() {

    }

    /**
     * Creates a new tick property with the given name and parent object.
     * 
     * @param name The name the new tick property will have.
     * @param parent The parent object which has the new tick property.
     */
    public TickProperty(String name, WorldObject parent) {

        super(name, parent);
    }

    /**
     * Registers the given {@link UpdateTask} so it is scheduled.
     * Every important parameter for scheduling as well as the algorithm is set in the task object.
     * 
     * @param task The {@link UpdateTask} to schedule.
     */
    public void registerTask(UpdateTask task) {

        if (task.property != null) {
            throw new IllegalStateException("Can only add unused tasks");
        } else {
            task.property = this;
            updateTasks.add(task);
        }
    }

    /**
     * Unregisters the given {@link UpdateTask} so it is cancelled.
     * Canceled tasks wont elapse any more ticks.
     * 
     * @param task The {@link UpdateTask} to cancel.
     */
    protected void unregisterTask(UpdateTask task) {

        updateTasks.remove(task);
        task.property = null;
    }

    /**
     * Elapses one tick on every {@link UpdateTask} and invokes the task if the timing condition is true.
     * The tasks should store any information as {@link Property} objects in the parent {@link WorldObject}s.
     */
    public final void update() {

        for (UpdateTask task : new ArrayList<UpdateTask>(updateTasks)) {
            if (task.getElapsed() < 0) {
                // Unregister cancelled tasks
                unregisterTask(task);
            } else {
                // Elapse one tick.
                task.elapsed++;

                if (task.getPeriod() <= 0 && task.getElapsed() >= task.getDelay()) {
                    task.getRunnable().run();
                    unregisterTask(task);
                } else if (task.getPeriod() > 0 && (task.getElapsed() - task.getDelay()) % task.getPeriod() == 0) {
                    task.getRunnable().run();
                }
            }
        }
    }

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (updateTasks == null ? 0 : updateTasks.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        TickProperty other = (TickProperty) obj;
        if (updateTasks == null) {
            if (other.updateTasks != null) {
                return false;
            }
        } else if (!updateTasks.equals(other.updateTasks)) {
            return false;
        }
        return true;
    }

    @Override
    public String toInfoString() {

        return super.toInfoString() + " with " + updateTasks.size() + " tasks running";
    }

    /**
     * This class represents an update task which may be invoked by a {@link TickProperty}.
     * Update tasks elapse a given amount of ticks and then invoke itself.
     * Such tasks should store any information as {@link Property} objects in the parent {@link WorldObject}s.
     */
    public static class UpdateTask implements InfoString {

        private TickProperty property;

        @XmlJavaTypeAdapter (ObjectInstanceAdapter.class)
        private Runnable     runnable;
        @XmlAttribute
        private int          delay;
        @XmlAttribute
        private int          period;

        @XmlAttribute
        private int          elapsed;

        /**
         * Creates a new empty update task.
         * This is only recommended for direct field access (e.g. for serialization).
         */
        protected UpdateTask() {

        }

        /**
         * Creates a new update task and only sets the delay before the task is invoked.
         * 
         * @param runnable The {@link Runnable} which is called on invokation.
         * @param delay The delay before the task is invoked.
         */
        public UpdateTask(Runnable runnable, int delay) {

            this.runnable = runnable;
            this.delay = delay;
            period = 0;
        }

        /**
         * Creates a new update task and sets the delay before the task is invoked and the period delay between looped invokations.
         * 
         * @param runnable The {@link Runnable} which is called on invokation.
         * @param delay The delay before the task is invoked the first time.
         * @param period The task is invoked every time after those ticks elapsed.
         */
        public UpdateTask(Runnable runnable, int delay, int period) {

            this.runnable = runnable;
            this.delay = delay;
            this.period = period;
        }

        /**
         * Returns the property which handles the update task.
         * 
         * @return The property which holds the task.
         */
        public TickProperty getProperty() {

            return property;
        }

        /**
         * Returns the parent world object which has the property which handles the update task.
         * 
         * @return The parent world object of the task.
         */
        public WorldObject getParent() {

            return property.getParent();
        }

        /**
         * Returns the {@link Runnable} which is called when the update task is invoked.
         * 
         * @return The {@link Runnable} which is called on invokation.
         */
        public Runnable getRunnable() {

            return runnable;
        }

        /**
         * Returns the delay before the task is invoked the first time.
         * 
         * @return The delay before the task is invoked the first time.
         */
        public int getDelay() {

            return delay;
        }

        /**
         * Returns the period delay between looped invokations.
         * 
         * @return The task is invoked every time after those ticks elapsed.
         */
        public int getPeriod() {

            return period;
        }

        /**
         * Returns the amount of ticks that have elapsed.
         * 
         * @return The amount of ticks that have elapsed.
         */
        public int getElapsed() {

            return elapsed;
        }

        /**
         * Cancels the task so it wont elapse any more ticks.
         */
        public void cancel() {

            elapsed = -1;
        }

        /**
         * Resolves the property which handles this update task during umarshalling.
         * 
         * @param unmarshaller The unmarshaller which unmarshals this task.
         * @param parent The object which was unmarshalled as the parent one from the xml structure.
         */
        protected void beforeUnmarshal(Unmarshaller unmarshaller, Object parent) {

            if (parent instanceof TickProperty) {
                property = (TickProperty) parent;
            }
        }

        @Override
        public int hashCode() {

            final int prime = 31;
            int result = 1;
            result = prime * result + delay;
            result = prime * result + period;
            return result;
        }

        @Override
        public boolean equals(Object obj) {

            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            UpdateTask other = (UpdateTask) obj;
            if (delay != other.delay) {
                return false;
            }
            if (period != other.period) {
                return false;
            }
            return true;
        }

        @Override
        public String toInfoString() {

            return "delay of " + delay + " ticks" + (period > 0 ? " with loop every " + period + " ticks" : "") + ", " + elapsed + " elapsed";
        }

        @Override
        public String toString() {

            return getClass().getName() + " [" + toInfoString() + "]";
        }

    }

}
