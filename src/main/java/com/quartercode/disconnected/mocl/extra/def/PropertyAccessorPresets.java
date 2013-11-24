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

package com.quartercode.disconnected.mocl.extra.def;

import java.util.List;
import org.apache.commons.lang.Validate;
import com.quartercode.disconnected.mocl.base.FeatureDefinition;
import com.quartercode.disconnected.mocl.base.FeatureHolder;
import com.quartercode.disconnected.mocl.extra.Function;
import com.quartercode.disconnected.mocl.extra.FunctionDefinition;
import com.quartercode.disconnected.mocl.extra.FunctionExecutor;
import com.quartercode.disconnected.mocl.extra.Property;

/**
 * A utility class for creating {@link FunctionDefinition}s which can access simple {@link Property}s.
 * 
 * @see Property
 * @see FunctionDefinition
 */
public class PropertyAccessorPresets {

    /**
     * Creates a new getter {@link FunctionDefinition} for the given {@link Property} definition.
     * A getter function returns the value of a {@link Property}.
     * 
     * @param name The name of the new {@link FunctionDefinition}.
     * @param propertyDefinition The {@link FeatureDefinition} of the {@link Property} to access.
     * @return The created {@link FunctionDefinition}.
     */
    public static <T> FunctionDefinition<T> createGet(String name, final FeatureDefinition<? extends Property<T>> propertyDefinition) {

        FunctionDefinition<T> definition = new AbstractFunctionDefinition<T>(name) {

            @Override
            protected Function<T> create(FeatureHolder holder, List<FunctionExecutor<T>> executors) {

                return new AbstractFunction<T>(getName(), holder, executors);
            }

        };

        definition.addExecutor("default", new FunctionExecutor<T>() {

            @Override
            public T invoke(FeatureHolder holder, Object... arguments) {

                return holder.get(propertyDefinition).get();
            }

        });

        return definition;
    }

    /**
     * Creates a new setter {@link FunctionDefinition} for the given {@link Property} definition.
     * A setter function changes the value of a {@link Property}.
     * 
     * @param name The name of the new {@link FunctionDefinition}.
     * @param propertyDefinition The {@link FeatureDefinition} of the {@link Property} to access.
     * @return The created {@link FunctionDefinition}.
     */
    public static <T> FunctionDefinition<Void> createSet(String name, final FeatureDefinition<? extends Property<T>> propertyDefinition) {

        FunctionDefinition<Void> definition = new AbstractFunctionDefinition<Void>(name) {

            @Override
            protected Function<Void> create(FeatureHolder holder, List<FunctionExecutor<Void>> executors) {

                return new AbstractFunction<Void>(getName(), holder, executors);
            }

        };

        definition.addExecutor("default", new FunctionExecutor<Void>() {

            @SuppressWarnings ("unchecked")
            @Override
            public Void invoke(FeatureHolder holder, Object... arguments) {

                Validate.isTrue(arguments.length == 1, "Wrong arguments: 'T value'");

                try {
                    holder.get(propertyDefinition).set((T) arguments[0]);
                }
                catch (ClassCastException e) {
                    throw new IllegalArgumentException("Wrong arguments: 'T value'");
                }

                return null;
            }

        });

        return definition;
    }

    private PropertyAccessorPresets() {

    }

}
