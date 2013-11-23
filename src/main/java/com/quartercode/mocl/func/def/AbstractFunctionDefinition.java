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

package com.quartercode.mocl.func.def;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.quartercode.mocl.base.Feature;
import com.quartercode.mocl.base.FeatureHolder;
import com.quartercode.mocl.base.def.AbstractFeatureDefinition;
import com.quartercode.mocl.func.Function;
import com.quartercode.mocl.func.FunctionDefinition;
import com.quartercode.mocl.func.FunctionExecutor;

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

    private final Map<String, FunctionExecutor<R>> executors = new HashMap<String, FunctionExecutor<R>>();

    /**
     * Creates a new abstract function definition for defining a {@link Function} with the given name.
     * 
     * @param name The name of the defined {@link Function}.
     */
    public AbstractFunctionDefinition(String name) {

        super(name);
    }

    @Override
    public void addExecutor(String name, FunctionExecutor<R> executor) {

        executors.put(name, executor);
    }

    @Override
    public void removeExecutor(String name) {

        executors.remove(name);
    }

    @Override
    public Function<R> create(FeatureHolder holder) {

        return create(holder, new ArrayList<FunctionExecutor<R>>(executors.values()));
    }

    /**
     * Creates a new {@link Function} which is defined by this function definition using the given {@link FeatureHolder} and {@link FunctionExecutor}s.
     * The holder is a {@link FeatureHolder} which can have different {@link Feature}s.
     * 
     * @param holder The {@link FeatureHolder} which holds the new {@link Function}.
     * @param executors The {@link FunctionExecutor}s which should be used in the new {@link Function}.
     * @return The created {@link Function}.
     */
    protected abstract Function<R> create(FeatureHolder holder, List<FunctionExecutor<R>> executors);

}
