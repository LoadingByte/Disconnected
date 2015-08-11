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

package com.quartercode.disconnected.server.world.util;

/**
 * The string representable interface defines methods for classes which can be converted to and created from strings.
 * Such classes are called "representable as a string" and only they can be used as program parameters. <br>
 * <br>
 * The {@link #convertToString()} and {@link #convertFromString(String)} methods are required to function as follows:
 *
 * <pre>
 * object1 -> convertToString() -> string -> convertFromString() -> object2
 * object1.equals(object2) == true
 * </pre>
 *
 * An object converted into a string and then converted back into an object must be equal to the original object.
 */
public interface StringRepresentable {

    /**
     * Returns a string which is representing this object in its current state.
     * The returned string must be convertible back into an object using {@link #convertFromString()}.
     * Note that such a new object must be equal to the original one.
     *
     * @return A string representation of this object.
     */
    public String convertToString();

    /**
     * Changes the state of this object so its equal to the state described by the given input string.
     * The new state of the object must be equal to the state of the object that created the string with {@link #convertToString()}.
     *
     * @param string The input string which describes the desired new state of the object.
     */
    public void convertFromString(String string);

}
