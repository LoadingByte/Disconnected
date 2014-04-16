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
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang.Validate;

/**
 * This class implements the controls for the tick system which then calls several actions.
 * It's like a central hub for controlling the activity of the tick thread.
 * 
 * @see TickThread
 */
public class Ticker {

    /**
     * The amount of milliseconds the ticker will wait from one tick to another by default.
     */
    public static final int        DEFAULT_DELAY            = 50;

    /**
     * The amount of ticks called in one second by default.
     */
    public static final int        DEFAULT_TICKS_PER_SECOND = 1000 / DEFAULT_DELAY;

    private TickThread             thread;
    private final List<TickAction> actions                  = new ArrayList<TickAction>();
    private int                    delay                    = DEFAULT_DELAY;

    // Performance: Object cache
    private List<TickAction>       unmodifiableActions      = Collections.unmodifiableList(actions);

    /**
     * Creates a new ticker without any tick actions.
     */
    public Ticker() {

    }

    /**
     * Creates a new ticker and initalizes some default tick actions.
     * 
     * @param actions A list of default tick actions which get called on every tick.
     */
    public Ticker(TickAction... actions) {

        this.actions.addAll(Arrays.asList(actions));
    }

    /**
     * Returns a list of tick actions which get called on every tick.
     * 
     * @return A list of tick actions which get called on every tick.
     */
    public List<TickAction> getActions() {

        return unmodifiableActions;
    }

    /**
     * Returns the tick actions which has the given type as a superclass.
     * 
     * @param type The type to use.
     * @return The tick actions which has the given type as a superclass.
     */
    public <T> T getAction(Class<T> type) {

        for (TickAction action : actions) {
            if (type.isAssignableFrom(action.getClass())) {
                return type.cast(action);
            }
        }

        return null;
    }

    /**
     * Adds a tick action which gets called on every tick.
     * You can only add actions if there no other action using the same class.
     * 
     * @param action A tick action which gets called on every tick.
     * @throws IllegalStateException If there is already an other action using the same class.
     */
    public void addAction(TickAction action) {

        for (TickAction testAction : actions) {
            if (testAction.getClass().equals(action.getClass())) {
                throw new IllegalStateException("There is already a tick action using the class " + testAction.getClass().getName());
            }
        }

        actions.add(action);
        unmodifiableActions = Collections.unmodifiableList(actions);
    }

    /**
     * Removes a tick action from the tick thread.
     * 
     * @param action The tick action to remove from the tick thread.
     */
    public void removeAction(TickAction action) {

        actions.remove(action);
        unmodifiableActions = Collections.unmodifiableList(actions);
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

        Validate.isTrue(delay > 0, "Delay must be > 0: ", delay);
        this.delay = delay;
    }

    /**
     * Returns if the tick thread is currently running.
     * 
     * @return If the tick thread is currently running.
     */
    public boolean isRunning() {

        return thread != null && thread.isAlive();
    }

    /**
     * Changes the status of the tick thread.
     * This can start and stop the tick update.
     * 
     * @param running If the tick thread should run.
     */
    public void setRunning(boolean running) {

        if (running && !isRunning()) {
            thread = new TickThread(this);
            thread.start();
        } else if (!running && isRunning()) {
            thread.interrupt();
            thread = null;
        }
    }

}
