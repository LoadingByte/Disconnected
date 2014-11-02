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

package com.quartercode.disconnected.client.graphics;

import com.quartercode.disconnected.shared.util.RunnableInvocationProvider;
import com.quartercode.eventbridge.bridge.Bridge;

/**
 * This service is the main manager of the graphics system.
 * The service can create or destroy the lwjgl context and keeps track of all important twl internals.
 */
public interface GraphicsService extends RunnableInvocationProvider {

    /**
     * Returns whether the graphics service is currently running.
     * 
     * @return The running state of the graphics service.
     */
    public boolean isRunning();

    /**
     * Changes the status of the graphics service.
     * This method starts and stops the graphics service.
     * 
     * @param running Whether the graphics service should run.
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
     * Returns the {@link Bridge} that is used by the graphics service.
     * It should also be used for sending events by anything related to graphics.
     * 
     * @return The client graphics bridge.
     */
    public Bridge getBridge();

    /**
     * Invokes the given {@link Runnable} in the graphics update thread.
     * 
     * @param runnable The runnable to invoke in the graphics update thread.
     */
    @Override
    public void invoke(Runnable runnable);

}
