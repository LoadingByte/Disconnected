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

package com.quartercode.disconnected.server.bridge;

import com.quartercode.disconnected.shared.client.ClientIdentity;
import com.quartercode.eventbridge.bridge.Event;

/**
 * Client-aware event handlers are classes that take an {@link Event} and the {@link ClientIdentity} of the client which sent it.
 * Using that information, they do something depending on the event.
 * For example, the additional client identity object could be used to check whether the client is allowed to execute a requested action.
 * Since events are immutable, client-aware event handlers may not modify the event.<br>
 * <br>
 * The generic parameter {@code <T>} defines the type of client-aware event the handler can handle.
 * That allows event handlers to handle specific events without having to perform casts.
 * 
 * @param <T> The type of event that can be handled by the client-aware handler.
 * @see Event
 * @see ClientIdentity
 */
public interface ClientAwareEventHandler<T extends Event> {

    /**
     * Processes the given {@link Event} and does something depending on the event.
     * Since events are immutable, this method may not modify the event.
     * The generic parameter {@code <T>} defines the type of event the client-aware handler can process.
     * 
     * @param event The event that should be processed by the client-aware handler.
     * @param client The {@link ClientIdentity} of the client which sent the event.
     *        May be {@code null} if the handled event was not sent from a client bridge.
     */
    public void handle(T event, ClientIdentity client);

}
