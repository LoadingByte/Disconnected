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

package com.quartercode.disconnected.sim.run;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.Validate;

/**
 * This thread calls the tick update on a given simulator.
 * 
 * @see Simulator
 */
public class TickThread extends Thread {

    private static final Logger    LOGGER      = Logger.getLogger(TickThread.class.getName());

    private final List<TickAction> tickActions = new ArrayList<TickAction>();
    private int                    delay       = 50;

    /**
     * Creates a new empty tick thread.
     */
    public TickThread() {

        super("tick");
    }

    /**
     * Creates a new tick thread and sets a list of tick actions which get called on every tick.
     * 
     * @param tickActions A list of tick actions which get called on every tick.
     */
    public TickThread(TickAction... tickActions) {

        this();

        this.tickActions.addAll(Arrays.asList(tickActions));
    }

    /**
     * Returns a list of tick actions which get called on every tick.
     * 
     * @return A list of tick actions which get called on every tick.
     */
    public List<TickAction> getTickActions() {

        return tickActions;
    }

    /**
     * Adds a tick action which gets called on every tick.
     * 
     * @param tickAction A tick action which gets called on every tick.
     */
    public void addTickAction(TickAction tickAction) {

        tickActions.add(tickAction);
    }

    /**
     * Removes a tick action from the tick thread.
     * 
     * @param tickAction The tick action to remove from the tick thread.
     */
    public void removeTickAction(TickAction tickAction) {

        tickActions.remove(tickAction);
    }

    /**
     * Returns the delay the thread should wait until the next tick.
     * 
     * @return The delay the thread should wait until the next tick.
     */
    public int getDelay() {

        return delay;
    }

    /**
     * Sets the delay the thread should wait until the next tick.
     * 
     * @param delay The delay the thread should wait until the next tick.
     */
    public void setDelay(int delay) {

        Validate.isTrue(delay > 0, "Delay must be > 0");
        this.delay = delay;
    }

    @Override
    public void run() {

        while (!isInterrupted()) {
            synchronized (this) {
                for (TickAction tickAction : new ArrayList<TickAction>(tickActions)) {
                    try {
                        tickAction.update();
                    }
                    catch (Throwable t) {
                        LOGGER.log(Level.SEVERE, "An exception occurred while executing tick action update (tick action " + tickAction.getClass().getName() + ")", t);
                    }
                }

                try {
                    Thread.sleep(delay);
                }
                catch (InterruptedException e) {
                    LOGGER.log(Level.SEVERE, "Tick thread has been interrupted", e);
                }
            }
        }
    }

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = prime * result + delay;
        result = prime * result + (tickActions == null ? 0 : tickActions.hashCode());
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
        TickThread other = (TickThread) obj;
        if (delay != other.delay) {
            return false;
        }
        if (tickActions == null) {
            if (other.tickActions != null) {
                return false;
            }
        } else if (!tickActions.equals(other.tickActions)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {

        return getClass().getName() + " [tickActions=" + tickActions + ", delay=" + delay + "]";
    }

}
