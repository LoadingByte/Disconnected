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

package com.quartercode.disconnected.world.event;

import com.quartercode.classmod.base.def.DefaultFeatureHolder;
import com.quartercode.classmod.extra.FunctionExecutor;
import com.quartercode.classmod.extra.FunctionInvocation;

/**
 * The bridge event listener bridges {@link EventListener#HANDLE_EVENT} calls to a standard {@link #handleEvent(Event)} method.
 * That allows non-classmod parts of the application to receive and handle {@link Event}s without using their own bridge.
 * 
 * @see Event
 * @see EventListener
 */
public abstract class BridgeEventListener extends DefaultFeatureHolder implements EventListener {

    // ----- Functions -----

    static {

        HANDLE_EVENT.addExecutor("bridge", BridgeEventListener.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                BridgeEventListener holder = (BridgeEventListener) invocation.getHolder();
                Event event = (Event) arguments[0];
                holder.handleEvent(event);

                return invocation.next(arguments);
            }

        });

    }

    /**
     * Creates a new bridge event listener.
     */
    public BridgeEventListener() {

    }

    /**
     * Handles a received {@link Event}.
     * The method could just execute some processing code, or it could distribute or store the received event.
     * 
     * @param event The incoming event that should be handled by the listener.
     */
    public abstract void handleEvent(Event event);

}
