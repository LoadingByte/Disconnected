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

package com.quartercode.disconnected.server.util;

import java.util.Arrays;

/**
 * An ObjArray object wraps around an {@link Object} array in order to present the array as a normal object to the outside.
 * That is useful when a method or constructor takes a vararg argument and an object array should be passed into the method/constructor
 * without being used as a vararg.
 * Example:
 *
 * <pre>
 * Method:                    do(Object...)
 *
 * Call:                      do(new Object[] { "test1", "test2" })
 * Compiler Interpretation:   do("test1", "test2")                  [2 String arguments]
 *
 * Fixed call using ObjArray: do(new ObjArray("test1", "test2"))
 * Compiler Interpretation:   do(<b>ObjArray{</b>"test1", "test2"<b>}</b>)        [1 ObjArray argument]
 * </pre>
 *
 * However, because ObjArray does not act as an array, any consumers must know how to handle ObjArray objects.
 */
public class ObjArray {

    private final Object[] array;

    /**
     * Creates a new ObjArray object which wraps around the given object array.
     *
     * @param array The object array (or vararg) the new object should wrap around.
     */
    public ObjArray(Object... array) {

        this.array = array;
    }

    /**
     * Returns the object array the ObjArray object wraps around.
     *
     * @return The wrapped object array.
     */
    public Object[] getArray() {

        return array;
    }

    @Override
    public int hashCode() {

        return Arrays.hashCode(array);
    }

    @Override
    public boolean equals(Object obj) {

        if (obj instanceof ObjArray) {
            return Arrays.equals(array, ((ObjArray) obj).getArray());
        } else {
            return false;
        }
    }

    @Override
    public String toString() {

        return Arrays.toString(array);
    }

}
