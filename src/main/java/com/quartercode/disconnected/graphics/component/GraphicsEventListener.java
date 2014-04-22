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

package com.quartercode.disconnected.graphics.component;

import com.quartercode.disconnected.graphics.GraphicsManager;
import com.quartercode.disconnected.util.ServiceRegistry;
import com.quartercode.disconnected.world.event.BridgeEventListener;
import com.quartercode.disconnected.world.event.Event;

/**
 * The graphics event listener syncs {@link BridgeEventListener} with the graphics thread.
 * It should be used whenever an event modifies something related to the ui.
 * 
 * @see Event
 * @see BridgeEventListener
 */
public abstract class GraphicsEventListener extends BridgeEventListener {

    /**
     * Creates a new graphics event listener.
     */
    public GraphicsEventListener() {

    }

    /**
     * Handles a received {@link Event} in the graphics thread.
     * The method could just execute some processing code, or it could distribute or store the received event.
     * 
     * @param event The incoming event that should be handled by the listener.
     */
    public abstract void handleEventSync(Event event);

    @Override
    public void handleEvent(final Event event) {

        ServiceRegistry.lookup(GraphicsManager.class).invoke(new Runnable() {

            @Override
            public void run() {

                handleEventSync(event);

            }
        });
    }

}
