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

package com.quartercode.disconnected.sim;

import java.util.List;

/**
 * This class implements the controls for the tick system which then calls several actions.
 * It's like a central hub for controlling the activity of the tick thread.
 * 
 * @see TickThread
 */
public interface Ticker {

    /**
     * The amount of milliseconds the ticker will wait from one tick to another by default.
     */
    public static final int DEFAULT_DELAY            = 50;

    /**
     * The amount of ticks called in one second by default.
     */
    public static final int DEFAULT_TICKS_PER_SECOND = 1000 / DEFAULT_DELAY;

    /**
     * Returns a list of tick actions which get called on every tick.
     * 
     * @return A list of tick actions which get called on every tick.
     */
    public List<TickAction> getActions();

    /**
     * Returns the tick actions which has the given type as a superclass.
     * 
     * @param type The type to use.
     * @return The tick actions which has the given type as a superclass.
     */
    public <T> T getAction(Class<T> type);

    /**
     * Adds a tick action which gets called on every tick.
     * You can only add actions if there no other action using the same class.
     * 
     * @param action A tick action which gets called on every tick.
     * @throws IllegalStateException If there is already an other action using the same class.
     */
    public void addAction(TickAction action);

    /**
     * Removes a tick action from the tick thread.
     * 
     * @param action The tick action to remove from the tick thread.
     */
    public void removeAction(TickAction action);

    /**
     * Returns the delay the thread should wait until the next tick.
     * 
     * @return The delay the thread should wait until the next tick.
     */
    public int getDelay();

    /**
     * Sets the delay the thread should wait until the next tick.
     * 
     * @param delay The delay the thread should wait until the next tick.
     */
    public void setDelay(int delay);

    /**
     * Returns if the tick thread is currently running.
     * 
     * @return If the tick thread is currently running.
     */
    public boolean isRunning();

    /**
     * Changes the status of the tick thread.
     * This can start and stop the tick update.
     * 
     * @param running If the tick thread should run.
     */
    public void setRunning(boolean running);

}
