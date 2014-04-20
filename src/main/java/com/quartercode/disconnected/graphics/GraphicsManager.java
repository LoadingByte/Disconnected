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

package com.quartercode.disconnected.graphics;

/**
 * This service is the main manager of the graphics system.
 * The manager can create or destroy the lwjgl context and keeps track of all important twl internals.
 */
public interface GraphicsManager {

    /**
     * Returns whether the graphics manager is currently running.
     * 
     * @return The running state of the graphics manager.
     */
    public boolean isRunning();

    /**
     * Changes the status of the graphics manager.
     * This method starts and stops the graphics manager.
     * 
     * @param running Whether the graphics manager should run.
     */
    public void setRunning(boolean running);

    /**
     * Returns the current top-level child widget ({@link GraphicsState}).
     * It represents the state which defines what should be drawn.
     * 
     * @return Returns the current active graphics state.
     */
    public GraphicsState getState();

    /**
     * Sets the current top-level child widget ({@link GraphicsState}) to a new one.
     * It represents the state which defines what should be drawn.
     * 
     * @param state The new active graphics state.
     */
    public void setState(GraphicsState state);

    /**
     * Invokes the given {@link Runnable} in the graphics update thread.
     * 
     * @param runnable The runnable to invoke in the graphics update thread.
     */
    public void invoke(Runnable runnable);

}
