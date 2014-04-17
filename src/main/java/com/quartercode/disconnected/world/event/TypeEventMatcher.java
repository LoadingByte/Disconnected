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

import org.apache.commons.lang3.Validate;

/**
 * The type event matcher only accepts {@link Event}s that derive from a given supertype or superinterface or are equal to a provided type.
 * For example, it could be used to make the {@link QueueEventListener#NEXT_EVENT} method return the last received {@link Event} of a given type
 * 
 * @see Event
 * @see QueueEventListener#NEXT_EVENT
 */
public class TypeEventMatcher implements EventMatcher {

    private final Class<? extends Event> type;

    /**
     * Creates a new type event matcher that only accepts {@link Event}s that derive from the given type or are equal to the given type.
     * 
     * @param type The type every matching {@link Event} must have as supertype or be equal to.
     */
    public TypeEventMatcher(Class<? extends Event> type) {

        Validate.notNull(type, "Type for matching cannot be null");
        this.type = type;
    }

    @Override
    public boolean matches(Event event) {

        return type.isAssignableFrom(event.getClass());
    }

}
