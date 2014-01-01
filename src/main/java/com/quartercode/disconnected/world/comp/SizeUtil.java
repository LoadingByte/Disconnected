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

package com.quartercode.disconnected.world.comp;

import java.math.BigDecimal;
import com.quartercode.disconnected.mocl.base.FeatureDefinition;
import com.quartercode.disconnected.mocl.base.FeatureHolder;
import com.quartercode.disconnected.mocl.extra.ExecutorInvokationException;
import com.quartercode.disconnected.mocl.extra.FunctionDefinition;
import com.quartercode.disconnected.mocl.extra.FunctionExecutionException;
import com.quartercode.disconnected.mocl.extra.FunctionExecutor;
import com.quartercode.disconnected.mocl.extra.Property;
import com.quartercode.disconnected.mocl.util.FunctionDefinitionFactory;

/**
 * This utility calculates the size of certain objects in bytes (of course, it's a fictional size).
 * It is also useful for deriving the size of {@link FeatureHolder}s.
 */
public class SizeUtil {

    /**
     * Returns the size of an object in bytes (of course, it's a fictional size).
     * If the object is a {@link FeatureHolder}, the size can be derived using the "getSize" method whose definition can be aquired with {@link #createDefinition()}.
     * In the case of a {@link String}, the size is equally to the length.
     * A {@link Boolean} always has a size of 1, a {@link Number} needs a bit for every digit (ceil rounding to bytes).
     * If the object is an {@link Iterable}, every entry of the collection will add to the size.
     * 
     * @param object The object to calculate the size of.
     * @return The size of the object in bytes (of course, it's a fictitious size).
     * @throws FunctionExecutionException The object is a {@link FeatureHolder} and an exception occurres during deriving.
     */
    public static long getSize(Object object) throws FunctionExecutionException {

        if (object == null) {
            return 0; // Nulls have no size
        } else if (object instanceof DerivableSize) {
            // Feature holders which implement DerivableSize have the size provided by DerivableSize.GET_SIZE
            long size = 0;
            for (long sizePart : ((DerivableSize) object).get(DerivableSize.GET_SIZE).invokeRA()) {
                size += sizePart;
            }
            return size;
        } else if (object instanceof Boolean) {
            return 1; // Booleans only need one bit -> one byte
        } else if (object instanceof Character || object instanceof String) {
            return object.toString().length(); // Every character needs one byte (for the simulation; of course, you can use unicode)
        } else if (object instanceof Number) {
            if (object instanceof BigDecimal) {
                object = ((Number) object).longValue(); // Ignore the decimal places
            }
            // Calculate the actual amount of bits the number would need
            int bits = Long.SIZE - Long.numberOfLeadingZeros( ((Number) object).longValue());
            bits += 1; // Extra bit for the positive/negative flag
            return (long) Math.ceil(bits / 8F); // Calculate amount of bytes from amount of bits
        } else if (object instanceof Iterable) {
            // Go over all elements and derive their size
            long size = 0;
            for (Object entry : (Iterable<?>) object) {
                size += getSize(entry);
            }
            return size;
        } else {
            return 0; // Unknown object -> 0 bytes
        }
    }

    /**
     * Creates a new size getter {@link FunctionExecutor} for the given {@link Property} definition.
     * A size getter {@link FunctionExecutor} returns the size of a {@link Property} using {@link #getSize(Object)}.
     * 
     * @param propertyDefinition The {@link FeatureDefinition} of the {@link Property} to access.
     * @return The created {@link FunctionExecutor}.
     */
    public static <T> FunctionExecutor<Long> createGetSize(final FeatureDefinition<? extends Property<T>> propertyDefinition) {

        return new FunctionExecutor<Long>() {

            @Override
            public Long invoke(FeatureHolder holder, Object... arguments) throws ExecutorInvokationException {

                return SizeUtil.getSize(holder.get(propertyDefinition).get());
            }

        };
    }

    private SizeUtil() {

    }

    /**
     * The derivable size interface declares the {@link #GET_SIZE} function for getting the size of an object.
     */
    public static interface DerivableSize extends FeatureHolder {

        /**
         * Derives the size of the implementing {@link FeatureHolder} in bytes.
         */
        public static final FunctionDefinition<Long> GET_SIZE = FunctionDefinitionFactory.create("getSize", Long.class);

    }

}
