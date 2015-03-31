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

import java.util.Collection;
import com.quartercode.disconnected.shared.util.registry.Registry;

/**
 * A multiple value registry is a {@link Registry} that stores multiple values, probably in a {@link Collection}.
 * These values are accessible through {@link #getValues()}.
 * New values can be added using {@link #addValue(Object)}, old ones can be removed with {@link #removeValue(Object)}.
 * 
 * @param <V> The type of the values that can be stored inside the multiple value registry.
 * @see Registry
 */
public interface MultipleValueRegistry<V> extends Registry<V> {

    /**
     * Returns a (possibly unmodifiable) view of all values that are stored in the multiple value registry.
     * 
     * @return All stored values.
     */
    @Override
    public Collection<V> getValues();

    /**
     * Adds the given value object to the multiple value registry.
     * 
     * @param value The value that should be added.
     */
    public void addValue(V value);

    /**
     * Removes the given value object from the multiple value registry.
     * 
     * @param value The value that should be removed.
     */
    public void removeValue(V value);

}
