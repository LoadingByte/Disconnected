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

import java.util.Set;
import com.quartercode.disconnected.mocl.base.Feature;

/**
 * A function makes a method (also called a function) avaiable.
 * Functions are executed by different {@link FunctionExecutor}s. That makes the function concept expandable.
 * The function object itself stores a set of those {@link FunctionExecutor}s.
 * 
 * @param <R> The type of the return value of the defined function.
 */
public interface Function<R> extends Feature {

    /**
     * Returns a set of all {@link FunctionExecutor}s which are used by the function. {@link FunctionExecutor}s are used for actually handling a function call.
     * 
     * @return All {@link FunctionExecutor}s which are used by the function.
     */
    public Set<FunctionExecutor<R>> getExecutors();

    /**
     * Invokes the defined function with the given arguments on all {@link FunctionExecutor}s.
     * 
     * @param arguments Some arguments for the function.
     * @return The value the invoked function returns. Can be null.
     * @throws FunctionExecutionException Something goes wrong during the invokation of a {@link FunctionExecutor}.
     */
    public R invoke(Object... arguments) throws FunctionExecutionException;

}
