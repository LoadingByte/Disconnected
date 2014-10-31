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

package com.quartercode.disconnected.shared.registry;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link MultipleValueRegistry} that uses a {@link List} as internal data structure.
 * This implementation uses an {@link ArrayList}.
 * 
 * @param <V> The type of the values that can be stored inside the list registry.
 * @see MultipleValueRegistry
 */
public class ListRegistry<V> implements MultipleValueRegistry<V> {

    private final List<V> values = new ArrayList<>();

    @Override
    public List<V> getValues() {

        return values;
    }

    @Override
    public void addValue(V value) {

        values.add(value);
    }

    @Override
    public void removeValue(V value) {

        values.remove(value);
    }

}
