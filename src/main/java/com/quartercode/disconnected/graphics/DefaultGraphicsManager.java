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
 * This is the default implementation of the {@link GraphicsManager} service.
 * 
 * @see GraphicsManager
 */
public class DefaultGraphicsManager implements GraphicsManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultGraphicsManager.class);

    private GraphicsThread      thread;
    private GraphicsState       state;

    /**
     * Creates a new default graphics manager.
     */
    public DefaultGraphicsManager() {

    }

    @Override
    public boolean isRunning() {

        return thread != null && thread.isAlive();
    }

    @Override
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

    @Override
    public GraphicsState getState() {

        return state;
    }

    @Override
    public void setState(GraphicsState state) {

        this.state = state;

        if (thread != null) {
            thread.changeState(state);
        }
    }

    @Override
    public void invoke(Runnable runnable) {

        thread.invoke(runnable);
    }

}
