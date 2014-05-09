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

import java.io.Serializable;

/**
 * Event predicates are a basically tool for asserting different properties of {@link Event}s.
 * They supply a {@link #test(Event)} method that tests the event and returns whether it matches the defined conditions.
 * The generic parameter {@code <T>} defines the type of event the event handler can handle.
 * That allows handlers to handle specific events without having to perform casts.<br>
 * <br>
 * Event predicates should be immutable. Furthermore, test calls shouldn't affect the predicate.
 * Finally, event preidcates must be {@link Serializable} so they can be stored or sent over the network.
 * 
 * @param <T> The type of event that can be tested by the predicate.
 * @see Event
 */
public interface EventPredicate<T extends Event> extends Serializable {

    /**
     * Returns whether the given {@link Event} matches the conditions that are defined by the event predicate.
     * The generic class parameter {@code <T>} defines the type of event that may be tested.
     * 
     * @param event The event that should be tested to match the defined conditions.
     * @return Whether the given event matches the defined conditions.
     */
    public boolean test(T event);

}
