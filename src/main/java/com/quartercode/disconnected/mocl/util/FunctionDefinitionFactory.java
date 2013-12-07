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

import java.util.Map;
import java.util.Set;
import com.quartercode.disconnected.mocl.base.FeatureHolder;
import com.quartercode.disconnected.mocl.extra.Function;
import com.quartercode.disconnected.mocl.extra.FunctionDefinition;
import com.quartercode.disconnected.mocl.extra.FunctionExecutor;
import com.quartercode.disconnected.mocl.extra.def.AbstractFunction;
import com.quartercode.disconnected.mocl.extra.def.AbstractFunctionDefinition;

/**
 * A utility class for creating very basic {@link FunctionDefinition}s.
 * 
 * @see FunctionDefinition
 */
public class FunctionDefinitionFactory {

    /**
     * Creates a new {@link FunctionDefinition} which accepts {@link FunctionExecutor}s with the given return type.
     * 
     * @param name The name of the new {@link FunctionDefinition}.
     * @param returnType The type of the objects {@link Function}s created by the definition return.
     * @return The new {@link FunctionDefinition}.
     */
    public static <R> FunctionDefinition<R> create(String name, Class<R> returnType) {

        return new AbstractFunctionDefinition<R>(name) {

            @Override
            protected Function<R> create(FeatureHolder holder, Map<Class<? extends FeatureHolder>, Set<FunctionExecutor<R>>> executors) {

                return new AbstractFunction<R>(getName(), holder, executors);
            }

        };
    }

    /**
     * Creates a new {@link FunctionDefinition} and adds the given default {@link FunctionExecutor} for the given default variant.
     * 
     * @param name The name of the new {@link FunctionDefinition}.
     * @param defaultVariation The class which defines the {@link FunctionDefinition} constant. The default executor is added here.
     * @param defaultExecutor The default {@link FunctionExecutor} to add to the definition.
     * @return The new {@link FunctionDefinition}.
     */
    public static <R> FunctionDefinition<R> create(String name, Class<? extends FeatureHolder> defaultVariation, FunctionExecutor<R> defaultExecutor) {

        FunctionDefinition<R> definition = new AbstractFunctionDefinition<R>(name) {

            @Override
            protected Function<R> create(FeatureHolder holder, Map<Class<? extends FeatureHolder>, Set<FunctionExecutor<R>>> executors) {

                return new AbstractFunction<R>(getName(), holder, executors);
            }

        };

        definition.addExecutor(defaultVariation, "default", defaultExecutor);

        return definition;
    }

    private FunctionDefinitionFactory() {

    }

}
