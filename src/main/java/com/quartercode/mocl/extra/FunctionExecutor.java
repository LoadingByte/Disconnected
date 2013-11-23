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

package com.quartercode.mocl.extra;

import com.quartercode.mocl.base.FeatureHolder;

/**
 * A function executor can be invoked with a {@link FeatureHolder} and some arguments.
 * It returns a value of a specified type. For no return value, you can use {@link Void}.
 * 
 * @param <R> The type of the return value of the defined function.
 */
public interface FunctionExecutor<R> {

    /**
     * Invokes the defined function in the given {@link FeatureHolder} with the given arguments.
     * 
     * @param holder The {@link FeatureHolder} the function is invoked on.
     * @param arguments Some arguments for the function.
     * @return The value the invoked function returns. Can be null.
     */
    public R invoke(FeatureHolder holder, Object... arguments);

}
