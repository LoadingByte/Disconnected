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
import java.util.Set;
import com.quartercode.disconnected.mocl.base.Feature;
import com.quartercode.disconnected.mocl.base.FeatureHolder;
import com.quartercode.disconnected.mocl.base.Named;

/**
 * A function makes a method (also called a function) avaiable.
 * Functions are executed by different {@link FunctionExecutor}s. That makes the function concept flexible.
 * The function object itself stores a set of those {@link FunctionExecutor}s.
 * 
 * @param <R> The type of the return values of the used {@link FunctionExecutor}s. The function returns a {@link List} with these values.
 * @see FunctionExecutor
 * @see FunctionExecutorContainer
 */
public interface Function<R> extends Feature, LockableClass {

    /**
     * Returns a list of all parameters which are used by the {@link FunctionExecutor}s.
     * See {@link FunctionDefinition#setParameter(int, Class)} for further explanation.
     * 
     * @return All parameters which are used by the function.
     */
    public List<Class<?>> getParameters();

    /**
     * Returns a {@link Set} of all {@link FunctionExecutorContainer}s which are used by the function.
     * They store {@link FunctionExecutor}s which are used for actually handling a function call.
     * 
     * @return All {@link FunctionExecutorContainer}s which are used by the function.
     */
    public Set<FunctionExecutorContainer<R>> getExecutors();

    /**
     * Returns the {@link FunctionExecutorContainer} which is used by the function and has the given name.
     * 
     * @param name The name the returned {@link FunctionExecutorContainer} must have.
     * @return The {@link FunctionExecutorContainer} which has the given name.
     */
    public FunctionExecutorContainer<R> getExecutor(String name);

    /**
     * Invokes the defined function with the given arguments on all {@link FunctionExecutor}s.
     * This returns the return value of the {@link FunctionExecutor}s with the highest priority.
     * If you want the return values of all executors, use {@link #invokeRA(Object...)}.
     * 
     * @param arguments Some arguments for the {@link FunctionExecutor}s.
     * @return The value the {@link FunctionExecutor}s with the highest priority returns. May be null.
     * @throws FunctionExecutionException Something goes wrong during the invokation of a {@link FunctionExecutor}.
     */
    public R invoke(Object... arguments) throws FunctionExecutionException;

    /**
     * Invokes the defined function with the given arguments on all {@link FunctionExecutor}s.
     * This returns the values the {@link FunctionExecutor}s return in invokation order.
     * If you want the value of the executor with the highest priority, use the index 0 or {@link #invoke(Object...)}.
     * 
     * @param arguments Some arguments for the {@link FunctionExecutor}s.
     * @return The values the invoked {@link FunctionExecutor}s return. Also contains null values.
     * @throws FunctionExecutionException Something goes wrong during the invokation of a {@link FunctionExecutor}.
     */
    public List<R> invokeRA(Object... arguments) throws FunctionExecutionException;

    /**
     * The function executor container wraps around {@link FunctionExecutor}s for storing data values along with them.
     * The data isn't stored in the actual {@link FunctionExecutor} object because it should only do the execution and nothing else.
     * 
     * @param <R> The type of the value the stored {@link FunctionExecutor} returns.
     * @see FunctionExecutor
     * @see Function
     */
    public static interface FunctionExecutorContainer<R> extends Named {

        /**
         * Returns the actual {@link FunctionExecutor} which is stored in the container
         * 
         * @return The stored {@link FunctionExecutor}.
         */
        public FunctionExecutor<R> getExecutor();

        /**
         * Sets the internal limit counter for the function executor container to 0.
         * That allows to use {@link FunctionExecutor}s which are already over their {@link Limit}.
         */
        public void resetLimit();

        /**
         * Invokes the stored {@link FunctionExecutor} inside the given {@link FeatureHolder} with the given arguments.
         * 
         * @param holder The {@link FeatureHolder} the stored {@link FunctionExecutor} is invoked in.
         * @param arguments Some arguments for the stored {@link FunctionExecutor}.
         * @return The value the invoked {@link FunctionExecutor} returns. Can be null.
         * @throws StopExecutionException The execution of the invokation queue should stop.
         */
        public R invoke(FeatureHolder holder, Object... arguments) throws StopExecutionException;

    }

}
