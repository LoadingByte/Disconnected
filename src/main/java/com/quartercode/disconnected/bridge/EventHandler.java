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

package com.quartercode.disconnected.bridge;

/**
 * Event handlers are classes that take an {@link Event} and do something depending on the event.
 * Since events are immutable, event handlers may not modify the event.<br>
 * <br>
 * The generic parameter {@code <T>} defines the type of event the predicate can test.
 * That allows predicates to test specific events without having to perform casts.
 * Finally, every event handler provides an {@link EventPredicate} that defines the kind of event the handler can/would like to process.
 * 
 * @param <T> The type of event that can be handled by the handler.
 * @see Event
 * @see EventPredicate
 */
public interface EventHandler<T extends Event> {

    /**
     * Returns the immutable {@link EventPredicate} that can be used to determine which {@link Event}s the handler can handle.
     * It may also work as a request for certain events that wouldn't be sent if the handler wasn't there.
     * Since the predicate is immutable, this method might always return the same object.
     * 
     * @return The event predicate that should be used for the event handler.
     */
    public EventPredicate<T> getPredicate();

    /**
     * Processes the given {@link Event} and does something depending on the event.
     * Since events are immutable, this methodmay not modify the event.
     * The generic parameter {@code <T>} defines the type of event the handler can process.<br>
     * <br>
     * Please note that this method only processes events that are accepted by the {@link EventPredicate} which is supplied by {@link #getPredicate()}.
     * 
     * @param event The event that should be processed by the handler.
     */
    public void handle(T event);

}
