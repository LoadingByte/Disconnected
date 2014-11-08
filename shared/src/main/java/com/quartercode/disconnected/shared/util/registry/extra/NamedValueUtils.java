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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A utility class for working with {@link NamedValue} objects.
 */
public class NamedValueUtils {

    /**
     * Iterates over the given {@link Iterable} object, which contains {@link NamedValue}s, and returns the value which has the given name.
     * 
     * @param values The iterable object that contains the named values.
     * @param name The name the returned named value must have.
     * @return The named value with the given name.
     */
    public static <T extends NamedValue> T getByName(Iterable<T> values, String name) {

        for (T value : values) {
            if (value != null && value.getName().equals(name)) {
                return value;
            }
        }

        return null;
    }

    /**
     * Iterates over the given {@link Iterable} object, which contains {@link NamedValue}s, and returns all the values which have the given names.
     * 
     * @param values The iterable object that contains the named values.
     * @param names The names from whose each returned named value must have one of.
     * @return All the named value with the given names.
     */
    public static <T extends NamedValue> List<T> getByNames(Iterable<T> values, Collection<String> names) {

        List<T> selectedValues = new ArrayList<>();

        for (T value : values) {
            if (value != null && names.contains(value.getName())) {
                selectedValues.add(value);
            }
        }

        return selectedValues;
    }

    private NamedValueUtils() {

    }

}
