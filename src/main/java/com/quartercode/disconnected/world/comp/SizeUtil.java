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

import static com.quartercode.classmod.ClassmodFactory.create;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.lang3.reflect.TypeLiteral;
import com.quartercode.classmod.base.FeatureHolder;
import com.quartercode.classmod.extra.CollectionProperty;
import com.quartercode.classmod.extra.CollectionPropertyDefinition;
import com.quartercode.classmod.extra.FunctionDefinition;
import com.quartercode.classmod.extra.FunctionExecutor;
import com.quartercode.classmod.extra.FunctionInvocation;
import com.quartercode.classmod.extra.Property;
import com.quartercode.classmod.extra.PropertyDefinition;
import com.quartercode.disconnected.util.NullPreventer;

/**
 * This utility calculates the size of certain objects in bytes (of course, it's a fictional size).
 * It is also useful for deriving the size of {@link FeatureHolder}s.
 */
public class SizeUtil {

    /**
     * Returns the size of an object in bytes (of course, it's a fictional size).
     * If the object is a {@link FeatureHolder}, the size can be derived using the {@link DerivableSize#GET_SIZE} function.
     * In the case of a {@link String}, the size is equally to the length.
     * A {@link Boolean} always has a size of 1, a {@link Number} needs a bit for every digit (ceil rounding to bytes).
     * If the object is an {@link Iterable}, every entry of the collection will add to the size.
     * 
     * @param object The object to calculate the size of.
     * @return The size of the object in bytes (of course, it's a fictitious size).
     */
    public static long getSize(Object object) {

        if (object == null) {
            return 0; // Nulls have no size
        } else if (object instanceof DerivableSize) {
            // Feature holders which implement DerivableSize have the size provided by DerivableSize.GET_SIZE
            return NullPreventer.prevent( ((DerivableSize) object).get(DerivableSize.GET_SIZE).invoke());
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
        } else if (object instanceof Map) {
            // Go over all keys and values and derive their size
            long size = 0;
            for (Entry<?, ?> entry : ((Map<?, ?>) object).entrySet()) {
                size += getSize(entry.getKey());
                size += getSize(entry.getValue());
            }
            return size;
        } else {
            return 0; // Unknown object -> 0 bytes
        }
    }

    /**
     * Creates a new size getter {@link FunctionExecutor} for the given {@link PropertyDefinition}.
     * A size getter {@link FunctionExecutor} returns the size of a {@link Property} using {@link #getSize(Object)}.
     * 
     * @param propertyDefinition The {@link PropertyDefinition} of the {@link Property} to access.
     * @return The created {@link FunctionExecutor}.
     */
    public static FunctionExecutor<Long> createGetSize(final PropertyDefinition<?> propertyDefinition) {

        return new FunctionExecutor<Long>() {

            @Override
            public Long invoke(FunctionInvocation<Long> invocation, Object... arguments) {

                return SizeUtil.getSize(invocation.getHolder().get(propertyDefinition).get()) + NullPreventer.prevent(invocation.next(arguments));
            }

        };
    }

    /**
     * Creates a new size getter {@link FunctionExecutor} for the given {@link CollectionPropertyDefinition}.
     * A size getter {@link FunctionExecutor} returns the size of a {@link CollectionProperty} using {@link #getSize(Object)}.
     * 
     * @param propertyDefinition The {@link CollectionPropertyDefinition} of the {CollectionProperty Property} to access.
     * @return The created {@link FunctionExecutor}.
     */
    public static FunctionExecutor<Long> createGetSize(final CollectionPropertyDefinition<?, ?> propertyDefinition) {

        return new FunctionExecutor<Long>() {

            @Override
            public Long invoke(FunctionInvocation<Long> invocation, Object... arguments) {

                return SizeUtil.getSize(invocation.getHolder().get(propertyDefinition).get()) + NullPreventer.prevent(invocation.next(arguments));
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
        public static final FunctionDefinition<Long> GET_SIZE = create(new TypeLiteral<FunctionDefinition<Long>>() {}, "name", "getSize", "parameters", new Class[0]);

    }

}
