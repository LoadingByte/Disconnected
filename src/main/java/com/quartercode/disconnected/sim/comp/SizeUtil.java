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

package com.quartercode.disconnected.sim.comp;

/**
 * This utility calculates the size of certain objects in bytes (of course, it's a fictitious size).
 * 
 * @see SizeObject
 */
public class SizeUtil {

    /**
     * Returns true if the size of the given object can be derived using this utility.
     * 
     * @param object The object to check.
     * @return True if the size of the given object can be derived using this utility.
     */
    public static boolean accept(Object object) {

        return object == null || object instanceof SizeObject || object instanceof String || object instanceof Boolean || object instanceof Number || object instanceof Iterable;
    }

    /**
     * Returns the size of an object in bytes (of course, it's a fictitious size).
     * If the object is a {@link SizeObject}. the size can be derived using {@link SizeObject#getSize()}.
     * In the case of a string, the size is equally to the length * 256. A boolean always has a size of 1, a number needs a byte for every digit.
     * If the object is an {@link Iterable}, every entry of the collection will add to the size.
     * 
     * @param object The object to calculate the size of.
     * @return The size of the object in bytes (of course, it's a fictitious size).
     * @throws IllegalArgumentException The given object isn't a {@link SizeObject}, string, boolean or number.
     */
    public static long getSize(Object object) {

        if (object == null) {
            return 0;
        } else if (object instanceof SizeObject) {
            return ((SizeObject) object).getSize();
        } else if (object instanceof Boolean) {
            return 1;
        } else if (object instanceof String) {
            return object.toString().length() * 256;
        } else if (object instanceof Number) {
            return object.toString().length();
        } else if (object instanceof Iterable) {
            long size = 0;
            for (Object entry : (Iterable<?>) object) {
                size += getSize(entry);
            }
            return size;
        } else {
            throw new IllegalArgumentException("Type " + object.getClass().getName() + " isn't a SizeObject, string, boolean or number");
        }
    }

    private SizeUtil() {

    }

    /**
     * This interface declares a method to derive the size of an object.
     * The size usually is given as an amount of bytes.
     */
    public static interface SizeObject {

        /**
         * Returns the size of the implementing object in bytes.
         * 
         * @return The size of the implementing object in bytes.
         */
        public long getSize();

    }

}
