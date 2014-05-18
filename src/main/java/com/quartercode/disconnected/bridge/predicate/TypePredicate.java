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

package com.quartercode.disconnected.bridge.predicate;

import com.quartercode.disconnected.bridge.Event;
import com.quartercode.disconnected.bridge.EventPredicate;
import com.quartercode.disconnected.util.DataObjectBase;

/**
 * The type predicate checks whether an event is and instance of a known type.
 * 
 * @param <T> The type of event that can be tested by the predicate.
 * @see EventPredicate
 */
public class TypePredicate<T extends Event> extends DataObjectBase implements EventPredicate<T> {

    private static final long        serialVersionUID = 328857758958950420L;

    private final Class<? extends T> type;

    /**
     * Creates a new type predicate which uses the given type for testing incoming events.
     * 
     * @param type The type (class object) to use for the test.
     */
    public TypePredicate(Class<? extends T> type) {

        this.type = type;
    }

    @Override
    public boolean test(T event) {

        return type.isInstance(event);
    }

}
