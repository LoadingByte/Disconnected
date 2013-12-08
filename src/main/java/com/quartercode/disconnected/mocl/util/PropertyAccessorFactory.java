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

package com.quartercode.disconnected.mocl.util;

import org.apache.commons.lang.Validate;
import com.quartercode.disconnected.mocl.base.FeatureDefinition;
import com.quartercode.disconnected.mocl.base.FeatureHolder;
import com.quartercode.disconnected.mocl.extra.ChildFeatureHolder;
import com.quartercode.disconnected.mocl.extra.FunctionExecutor;
import com.quartercode.disconnected.mocl.extra.Property;
import com.quartercode.disconnected.mocl.extra.StopExecutionException;

/**
 * A utility class for creating {@link FunctionExecutor}s which can access simple {@link Property}s (like getters or setters).
 * 
 * @see Property
 * @see FunctionExecutor
 */
public class PropertyAccessorFactory {

    /**
     * Creates a new getter {@link FunctionExecutor} for the given {@link Property} definition.
     * A getter {@link FunctionExecutor} returns the value of a {@link Property}.
     * 
     * @param propertyDefinition The {@link FeatureDefinition} of the {@link Property} to access.
     * @return The created {@link FunctionExecutor}.
     */
    public static <T> FunctionExecutor<T> createGet(final FeatureDefinition<? extends Property<T>> propertyDefinition) {

        return new FunctionExecutor<T>() {

            @Override
            public T invoke(FeatureHolder holder, Object... arguments) throws StopExecutionException {

                return holder.get(propertyDefinition).get();
            }

        };
    }

    /**
     * Creates a new setter {@link FunctionExecutor} for the given {@link Property} definition.
     * A setter function changes the value of a {@link Property}.
     * 
     * @param propertyDefinition The {@link FeatureDefinition} of the {@link Property} to access.
     * @return The created {@link FunctionExecutor}.
     */
    public static <T> FunctionExecutor<Void> createSet(final FeatureDefinition<? extends Property<T>> propertyDefinition) {

        return new FunctionExecutor<Void>() {

            @SuppressWarnings ("unchecked")
            @Override
            public Void invoke(FeatureHolder holder, Object... arguments) throws StopExecutionException {

                Validate.isTrue(arguments.length == 1, "Wrong arguments: '? value' required");

                try {
                    // Set the parent of the old object to null
                    if (holder.get(propertyDefinition).get() instanceof ChildFeatureHolder) {
                        ((ChildFeatureHolder<FeatureHolder>) holder.get(propertyDefinition).get()).setParent(null);
                    }

                    holder.get(propertyDefinition).set((T) arguments[0]);

                    // Set the parent of the new object the new holder
                    if (arguments[0] instanceof ChildFeatureHolder) {
                        ((ChildFeatureHolder<FeatureHolder>) arguments[0]).setParent(holder);
                    }
                }
                catch (ClassCastException e) {
                    String type = e.getMessage().substring(e.getMessage().indexOf(" cannot be cast to ") + 19, e.getMessage().length());
                    throw new IllegalArgumentException("Wrong arguments: '" + type + " value' required");
                }

                return null;
            }

        };
    }

    private PropertyAccessorFactory() {

    }

}
