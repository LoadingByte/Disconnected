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

package com.quartercode.disconnected.server.world.util;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import com.quartercode.classmod.extra.conv.CFeatureHolder;
import com.quartercode.classmod.extra.func.FunctionExecutor;
import com.quartercode.classmod.extra.func.FunctionInvocation;
import com.quartercode.classmod.extra.prop.CollectionProperty;
import com.quartercode.classmod.extra.prop.CollectionPropertyDefinition;
import com.quartercode.classmod.extra.prop.Property;
import com.quartercode.classmod.extra.prop.PropertyDefinition;
import com.quartercode.disconnected.server.util.NullPreventer;

/**
 * This utility calculates the size of certain objects in bytes (of course, it's a fictional size).
 * 
 * @see DerivableSize
 */
public class SizeUtils {

    /**
     * Returns the size of an object in bytes (of course, it's a fictional size).
     * If the object is {@code null}, {@code 0} is returned.
     * If the object is a {@link CFeatureHolder}, the size can be derived using the {@link DerivableSize#GET_SIZE} function.
     * In other cases, the following methods are used to calculate the size:
     * 
     * <ul>
     * <li>{@link #getSize(boolean)}</li>
     * <li>{@link #getSize(char)}</li>
     * <li>{@link #getSize(String)}</li>
     * <li>{@link #getSize(Number)}</li>
     * <li>{@link #getSize(Iterable)}</li>
     * <li>{@link #getSize(Map)}</li>
     * </ul>
     * 
     * If the object does not match any of the methods above, an {@link IllegalArgumentException} is thrown.
     * 
     * @param value The object to calculate the size of.
     * @return The size of the object in bytes (of course, it's a fictitious size).
     * @throws IllegalArgumentException Thrown if the size of the object cannot be derivded.
     */
    public static long getSize(Object value) throws IllegalArgumentException {

        if (value == null) {
            return 0;
        } else if (value instanceof DerivableSize) {
            // Feature holders which implement DerivableSize have the size provided by DerivableSize.GET_SIZE
            return NullPreventer.prevent( ((DerivableSize) value).invoke(DerivableSize.GET_SIZE));
        } else if (value instanceof Boolean) {
            return getSize((boolean) value);
        } else if (value instanceof Character) {
            return getSize((char) value);
        } else if (value instanceof String) {
            return getSize((String) value);
        } else if (value instanceof Number) {
            return getSize((Number) value);
        } else if (value instanceof Iterable) {
            return getSize((Iterable<?>) value);
        } else if (value instanceof Map) {
            return getSize((Map<?, ?>) value);
        } else {
            // Unsupported object
            throw new IllegalArgumentException("Cannot derive the size of objects of the type '" + value.getClass().getName() + "'");
        }
    }

    /**
     * Returns the size of the given {@link Boolean} value.
     * It is always {@code 1}.
     * 
     * @param value The boolean value whose size should be derived.
     * @return The size of the given boolean value.
     */
    public static long getSize(boolean value) {

        // Booleans only need one bit.
        // However, one byte is returned because that is the smallest possible value.
        return 1;
    }

    /**
     * Returns the size of the given {@link Character}.
     * It is always {@code 1}.
     * 
     * @param value The character whose size should be derived.
     * @return The size of the given character.
     */
    public static long getSize(char value) {

        // Every character needs one byte.
        // That is the case for the simulation; of course, in the real world, more bytes would be required due to unicode.
        return 1;
    }

    /**
     * Returns the size of the given {@link String}.
     * It is just the length of the string.
     * 
     * @param value The string whose size should be derived.
     * @return The size of the given string, or {@code 0} if the value is {@code null}.
     */
    public static long getSize(String value) {

        // Every character of the string needs one byte.
        // That is the case for the simulation; of course, in the real world, more bytes would be required due to unicode.
        return value == null ? 0 : value.length();
    }

    /**
     * Returns the size of the given {@link Number} value.
     * Each digit uses one bit.
     * Because single bits are not supported, the result is rounded up to an amount of bytes.
     * 
     * @param value The number value whose size should be derived.
     * @return The size of the given number value, or {@code 0} if the value is {@code null}.
     */
    public static long getSize(Number value) {

        if (value == null) {
            return 0;
        }

        // Calculate the actual amount of bits the number would need
        int bits = Long.SIZE - Long.numberOfLeadingZeros(value.longValue());
        // Add an extra bit for the sign
        bits += 1;
        // Calculate the amount of bytes from the amount of bits
        return (long) Math.ceil(bits / 8D);
    }

    /**
     * Returns the size of the given {@link Iterable} object (e.g. a {@link Collection}).
     * The size of the whole iterable object is derived by summing up the sizes of the items it iterates over.
     * 
     * @param value The iterable object whose size should be derived.
     * @return The size of the given iterable object, or {@code 0} if the object is {@code null}.
     */
    public static long getSize(Iterable<?> value) {

        if (value == null) {
            return 0;
        }

        // Go over all elements and derive their sizes
        long size = 0;
        for (Object item : value) {
            size += getSize(item);
        }
        return size;
    }

    /**
     * Returns the size of the given {@link Map} object.
     * The size of the whole map is derived by summing up the sizes of all the keys and values it contains.
     * 
     * @param value The map whose size should be derived.
     * @return The size of the given map, or {@code 0} if the object is {@code null}.
     */
    public static long getSize(Map<?, ?> value) {

        if (value == null) {
            return 0;
        }

        // Go over all key-value-pairs and derive their sizes
        long size = 0;
        for (Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
            size += getSize(entry.getKey());
            size += getSize(entry.getValue());
        }
        return size;
    }

    /**
     * Creates a new size getter {@link FunctionExecutor} for the given {@link PropertyDefinition}.
     * It returns the size of the content of a {@link Property} using {@link #getSize(Object)}.
     * Note that it also adds the size value returned by the function executor which is called after it.
     * 
     * @param propertyDefinition The property definition of the property to access.
     * @return The created getter function executor.
     */
    public static FunctionExecutor<Long> createGetSize(final PropertyDefinition<?> propertyDefinition) {

        return new FunctionExecutor<Long>() {

            @Override
            public Long invoke(FunctionInvocation<Long> invocation, Object... arguments) {

                return SizeUtils.getSize(invocation.getCHolder().getObj(propertyDefinition)) + NullPreventer.prevent(invocation.next(arguments));
            }

        };
    }

    /**
     * Creates a new size getter {@link FunctionExecutor} for the given {@link CollectionPropertyDefinition}.
     * It returns the size of the contents of a {@link CollectionProperty} using {@link #getSize(Object)}.
     * Note that it also adds the size value returned by the function executor which is called after it.
     * 
     * @param propertyDefinition The collection property definition of the collection property to access.
     * @return The created getter function executor.
     */
    public static FunctionExecutor<Long> createGetSize(final CollectionPropertyDefinition<?, ?> propertyDefinition) {

        return new FunctionExecutor<Long>() {

            @Override
            public Long invoke(FunctionInvocation<Long> invocation, Object... arguments) {

                return SizeUtils.getSize(invocation.getCHolder().getColl(propertyDefinition)) + NullPreventer.prevent(invocation.next(arguments));
            }

        };
    }

    private SizeUtils() {

    }

}
