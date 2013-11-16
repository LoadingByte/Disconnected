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

package com.quartercode.disconnected.world.comp.program;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

/**
 * This class represents an update task which invokes different update methods in a program executor.
 * Update tasks elapse a given amount of ticks and then invoke the given method.
 */
public class UpdateTask {

    @XmlAttribute
    private String method;
    @XmlAttribute
    private int    delay;
    @XmlAttribute
    private int    period;

    @XmlValue
    private int    elapsed;

    /**
     * Creates a new empty update task.
     * This is only recommended for direct field access (e.g. for serialization).
     */
    protected UpdateTask() {

    }

    /**
     * Creates a new timer task and sets the delay before the given method gets invoked.
     * 
     * @param method The name of the method which should be invoked.
     * @param delay The delay before the method gets invoked.
     */
    public UpdateTask(String method, int delay) {

        this.method = method;
        this.delay = delay;
        period = 0;
    }

    /**
     * Creates a new abstract timer task and sets the delay before the given method gets invoked and the period delay between looped invokations.
     * 
     * @param method The name of the method which should be invoked.
     * @param delay The delay before the method gets invoked the first time.
     * @param period Every time after those ticks elapsed, the method gets invoked.
     */
    public UpdateTask(String method, int delay, int period) {

        this.method = method;
        this.delay = delay;
        this.period = period;
    }

    /**
     * Returns the name of the method which should be invoked.
     * 
     * @return The name of the method which should be invoked.
     */
    public String getMethod() {

        return method;
    }

    /**
     * Returns the delay before the method gets invoked the first time.
     * 
     * @return The delay before the method gets invoked the first time.
     */
    public int getDelay() {

        return delay;
    }

    /**
     * Returns the period delay between looped invokations.
     * 
     * @return Every time after those ticks elapsed, the method gets invoked.
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

    protected void elapse() {

        elapsed++;
    }

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = prime * result + delay;
        result = prime * result + elapsed;
        result = prime * result + (method == null ? 0 : method.hashCode());
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
        if (elapsed != other.elapsed) {
            return false;
        }
        if (method == null) {
            if (other.method != null) {
                return false;
            }
        } else if (!method.equals(other.method)) {
            return false;
        }
        if (period != other.period) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {

        return getClass().getName() + " [method=" + method + ", delay=" + delay + ", period=" + period + ", elapsed=" + elapsed + "]";
    }

}
