/*
 * This file is part of Disconnected.
 * Copyright (c) 2013 QuarterCode <http://quartercode.com/>
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

package com.quartercode.disconnected.shared.util.registry.extra;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * A {@link MultipleValueRegistry} that uses a {@link Set} as internal data structure.
 * This implementation uses a {@link HashSet}.
 * 
 * @param <V> The type of the values that can be stored inside the set registry.
 * @see MultipleValueRegistry
 */
public class SetRegistry<V> implements MultipleValueRegistry<V> {

    private final Set<V>     values = new HashSet<>();
    private transient Set<V> unmodifiableCache;

    @Override
    public Set<V> getValues() {

        // Update the unmodifiable cache
        if (unmodifiableCache == null) {
            unmodifiableCache = Collections.unmodifiableSet(values);
        }

        return unmodifiableCache;
    }

    @Override
    public void addValue(V value) {

        values.add(value);

        // Invalidate the unmodifiable cache
        unmodifiableCache = null;
    }

    @Override
    public void removeValue(V value) {

        values.remove(value);

        // Invalidate the unmodifiable cache
        unmodifiableCache = null;
    }

    @Override
    public Iterator<V> iterator() {

        return getValues().iterator();
    }

}
