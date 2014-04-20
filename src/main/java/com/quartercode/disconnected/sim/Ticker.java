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
 * This service implements the controls for the tick system which then calls several actions.
 * It's like a central hub for controlling the activity of the tick thread.
 * 
 * @see TickAction
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
     * Returns a list of {@link TickAction}s which are called on every tick.
     * 
     * @return The registered tick actions.
     */
    public List<TickAction> getActions();

    /**
     * Returns the {@link TickAction} which has the given type as a superclass.
     * 
     * @param type The type to use for the check.
     * @return The tick action which has the given type as a superclass.
     */
    public <T> T getAction(Class<T> type);

    /**
     * Adds a {@link TickAction} which is called on every tick.
     * You can only add actions if there is no other action of the same type already registered.
     * 
     * @param action The tick action to add.
     * @throws IllegalStateException If there is another action of the same type already registered.
     */
    public void addAction(TickAction action);

    /**
     * Removes a {@link TickAction} from the ticker.
     * 
     * @param action The tick action to remove.
     */
    public void removeAction(TickAction action);

    /**
     * Returns the delay the ticker should wait between two ticks.
     * 
     * @return The tick delay.
     */
    public int getDelay();

    /**
     * Sets the delay the thread should wait between two ticks.
     * 
     * @param delay The new tick delay.
     */
    public void setDelay(int delay);

    /**
     * Returns whether the ticker is currently running.
     * 
     * @return The current running state of the ticker.
     */
    public boolean isRunning();

    /**
     * Changes the running state of the ticker.
     * This method starts and stops the tick update.
     * 
     * @param running Whether the ticker should run.
     */
    public void setRunning(boolean running);

}
