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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the main manager of the graphics system.
 * The manager can create or destroy the lwjgl context and keeps track of all important twl internals.
 */
public class GraphicsManager {

    private static final Logger         LOGGER   = LoggerFactory.getLogger(GraphicsManager.class);

    /**
     * The singleton instance of profile manager.
     */
    public static final GraphicsManager INSTANCE = new GraphicsManager();

    private GraphicsThread              thread;
    private GraphicsState               state;

    private GraphicsManager() {

    }

    /**
     * Returns if the graphics manager is currently running.
     * 
     * @return If the graphics manager is currently running.
     */
    public boolean isRunning() {

        return thread != null && thread.isAlive();
    }

    /**
     * Changes the status of the graphics manager.
     * This can start and stop the graphics manager.
     * 
     * @param running If the graphics manager should run.
     */
    public void setRunning(boolean running) {

        if (running && !isRunning()) {
            LOGGER.info("Starting up graphics thread");
            thread = new GraphicsThread();
            thread.changeState(state);
            thread.start();
        } else if (!running && isRunning()) {
            LOGGER.info("Shutting down graphics thread");
            thread.exit();
            thread = null;
        }
    }

    /**
     * Returns the current top-level child widget.
     * It represents the state which defines what should be drawn.
     * 
     * @return Returns the current top-level child widget.
     */
    public GraphicsState getState() {

        return state;
    }

    /**
     * Sets the current top-level child widget to a new one.
     * It represents the state which defines what should be drawn.
     * 
     * @param state The new top-level child widget.
     */
    public void setState(GraphicsState state) {

        this.state = state;

        if (thread != null) {
            thread.changeState(state);
        }
    }

    /**
     * Invokes the given {@link Runnable} in the graphics update thread.
     * 
     * @param runnable The runnable to invoke in the graphics update thread.
     */
    public void invoke(Runnable runnable) {

        thread.invoke(runnable);
    }

}
