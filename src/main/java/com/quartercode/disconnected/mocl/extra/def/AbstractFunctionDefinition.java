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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import com.quartercode.disconnected.mocl.base.Feature;
import com.quartercode.disconnected.mocl.base.FeatureHolder;
import com.quartercode.disconnected.mocl.base.def.AbstractFeatureDefinition;
import com.quartercode.disconnected.mocl.extra.Function;
import com.quartercode.disconnected.mocl.extra.FunctionDefinition;
import com.quartercode.disconnected.mocl.extra.FunctionExecutor;

/**
 * An abstract function definition is used to get a {@link Function} from a {@link FeatureHolder}.
 * It's an implementation of the {@link FunctionDefinition} interface.
 * It contains the name of the {@link Function} and the {@link FunctionExecutor}s which are used.
 * You can use an abstract function definition to construct a new instance of the defined {@link Function} through {@link #create(FeatureHolder)}.
 * 
 * @param <R> The type of the return value of the defined {@link Function}.
 * @see FunctionDefinition
 * @see Function
 */
public abstract class AbstractFunctionDefinition<R> extends AbstractFeatureDefinition<Function<R>> implements FunctionDefinition<R> {

    private final Map<Class<? extends FeatureHolder>, Set<FunctionExecutor<R>>> executors = new HashMap<Class<? extends FeatureHolder>, Set<FunctionExecutor<R>>>();

    /**
     * Creates a new abstract function definition for defining a {@link Function} with the given name.
     * 
     * @param name The name of the defined {@link Function}.
     */
    public AbstractFunctionDefinition(String name) {

        super(name);
    }

    @Override
    public Map<Class<? extends FeatureHolder>, Set<FunctionExecutor<R>>> getExecutors() {

        return Collections.unmodifiableMap(executors);
    }

    @Override
    public void addExecutor(Class<? extends FeatureHolder> variant, String name, FunctionExecutor<R> executor) {

        if (!executors.containsKey(variant)) {
            executors.put(variant, new HashSet<FunctionExecutor<R>>());
        }

        executors.get(variant).add(executor);
    }

    @Override
    public void removeExecutor(Class<? extends FeatureHolder> variant, String name) {

        if (executors.containsKey(variant)) {
            executors.get(variant).remove(name);

            if (executors.get(variant).isEmpty()) {
                executors.remove(variant);
            }
        }
    }

    @Override
    public Function<R> create(FeatureHolder holder) {

        return create(holder, new HashMap<Class<? extends FeatureHolder>, Set<FunctionExecutor<R>>>(executors));
    }

    /**
     * Creates a new {@link Function} which is defined by this function definition using the given {@link FeatureHolder} and {@link FunctionExecutor}s.
     * The holder is a {@link FeatureHolder} which can have different {@link Feature}s.
     * 
     * @param holder The {@link FeatureHolder} which holds the new {@link Function}.
     * @param executors The {@link FunctionExecutor}s which should be used in the new {@link Function} for the different variants.
     * @return The created {@link Function}.
     */
    protected abstract Function<R> create(FeatureHolder holder, Map<Class<? extends FeatureHolder>, Set<FunctionExecutor<R>>> executors);

}
