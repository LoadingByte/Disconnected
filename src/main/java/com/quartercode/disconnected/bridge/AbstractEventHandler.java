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

import com.quartercode.disconnected.bridge.predicate.TypePredicate;

/**
 * The abstract event handler class hides some boilerplate code every {@link EventHandler} must implement.
 * It basically provides an implementation for the {@link #getPredicate()} method by requesting the predicate on construction.
 * 
 * @param <T> The type of event that can be handled by the handler.
 * @see EventHandler
 */
public abstract class AbstractEventHandler<T extends Event> implements EventHandler<T> {

    private final EventPredicate<T> predicate;

    /**
     * Creates a new abstract event handler and uses a {@link TypePredicate} with the given event type.
     * The event type must equal the generic type parameter of the new event handler.
     * 
     * @param eventType The type of events the new handler can handle.
     */
    public AbstractEventHandler(Class<T> eventType) {

        this(new TypePredicate<>(eventType));
    }

    /**
     * Creates a new abstract event handler and sets the {@link EventPredicate} that the {@link #getPredicate()} method always returns.
     * 
     * @param predicate The immutable event predicate that can be used to determine which events the handler can handle.
     */
    public AbstractEventHandler(EventPredicate<T> predicate) {

        this.predicate = predicate;
    }

    @Override
    public EventPredicate<T> getPredicate() {

        return predicate;
    }

}
