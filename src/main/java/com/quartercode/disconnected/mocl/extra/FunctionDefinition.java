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

package com.quartercode.disconnected.mocl.extra;

import java.util.List;
import com.quartercode.disconnected.mocl.base.FeatureDefinition;
import com.quartercode.disconnected.mocl.base.FeatureHolder;

/**
 * A function definition is used to get a {@link Function} from a {@link FeatureHolder}.
 * The function definition also stores some {@link FunctionExecutor}s which are used for the {@link Function} object.
 * 
 * @param <R> The type of the return value of the defined {@link Function}.
 * @see Function
 * @see FunctionExecutor
 */
public interface FunctionDefinition<R> extends FeatureDefinition<Function<R>> {

    /**
     * Returns a list of all parameters which are used by the function definition.
     * See {@link #setParameter(int, Class)} for further explanation.
     * 
     * @return All parameters which are used by the function definition.
     */
    public List<Class<?>> getParameters();

    /**
     * Sets a function parameter which is required in the {@link Function#invoke(Object...)} method.
     * Such a parameter is comparable with a normal method parameter.
     * This method could be created using the calls:
     * 
     * <pre>
     * setParameter(0, Integer.class);
     * setParameter(1, Class.class);
     * </pre>
     * 
     * @param index The index of the parameter (0 is the first one, 1 the second one etc.).
     * @param type The type the argument for the parameter must have. null removes the parameter.
     */
    public void setParameter(int index, Class<?> type);

    /**
     * Registers a new {@link FunctionExecutor} under the given variant and name to the definition.
     * Registered {@link FunctionExecutor} should transfer to created {@link Function}s.
     * 
     * @param variant The class the {@link FunctionExecutor} is used for. It will also be used for every subclass of this class.
     * @param name The name of the {@link FunctionExecutor} to register. You can use that name to unregister the {@link FunctionExecutor} through {@link #removeExecutor(String)}.
     * @param executor The actual {@link FunctionExecutor} object to register.
     */
    public void addExecutor(Class<? extends FeatureHolder> variant, String name, FunctionExecutor<R> executor);

    /**
     * Unregisters an {@link FunctionExecutor} which is registered under the given variant and name from the definition.
     * The unregistered {@link FunctionExecutor} wont transfer into new {@link Function}s, but it will stay in the ones which are already created.
     * 
     * @param variant The class the {@link FunctionExecutor} for removal is used for.
     * @param name The name the {@link FunctionExecutor} to unregister has. You used that name for {@link #addExecutor(String, Object)}.
     */
    public void removeExecutor(Class<? extends FeatureHolder> variant, String name);

}
