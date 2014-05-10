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

import com.quartercode.disconnected.bridge.Bridge;

/**
 * The tick bridge provider extends the {@link TickRunnableInvoker} by providing a {@link Bridge} for the parent ticker.
 * If bridge functionality is needed, this class should be used over the plain runnable invoker.
 * 
 * @see Bridge
 */
public class TickBridgeProvider extends TickRunnableInvoker {

    private final Bridge bridge = new Bridge();

    /**
     * Creates a new tick bridge provider.
     */
    public TickBridgeProvider() {

        bridge.setHandlerInvoker(this);
    }

    /**
     * Returns the {@link Bridge} that should be used for sending events by anything related to the simulation.
     * 
     * @return The server ticker bridge.
     */
    public Bridge getBridge() {

        return bridge;
    }

}
