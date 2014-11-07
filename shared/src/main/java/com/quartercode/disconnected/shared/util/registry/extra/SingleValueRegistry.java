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

package com.quartercode.disconnected.shared.util.registry.extra;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import com.quartercode.disconnected.shared.util.registry.Registry;

/**
 * A single value registry is a {@link Registry} that only stores one value.
 * That value is accessible and changeable through {@link #getValue()} and {@link #setValue(Object)}.
 * The interface method {@link #getValues()} returns a list that contains the single stored value.
 * 
 * @param <V> The type of value that can be stored inside the single value registry.
 * @see Registry
 */
public class SingleValueRegistry<V> implements Registry<V> {

    private V       value;
    private List<V> listCache;

    /**
     * Returns the value which is stored inside the single value registry.
     * 
     * @return The stored value.
     */
    public V getValue() {

        return value;
    }

    /**
     * Changes the value which is stored inside the single value registry.
     * 
     * @param value The new value to be stored.
     */
    public void setValue(V value) {

        this.value = value;

        // Invalidate the list cache
        listCache = null;
    }

    @Override
    public List<V> getValues() {

        // Update the list cache
        if (listCache == null) {
            listCache = Collections.unmodifiableList(Arrays.asList(value));
        }

        return listCache;
    }

    @Override
    public Iterator<V> iterator() {

        return getValues().iterator();
    }

}
