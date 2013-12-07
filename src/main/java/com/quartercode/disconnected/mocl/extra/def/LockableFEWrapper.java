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

import com.quartercode.disconnected.mocl.base.FeatureHolder;
import com.quartercode.disconnected.mocl.extra.FunctionExecutor;
import com.quartercode.disconnected.mocl.extra.Lockable;
import com.quartercode.disconnected.mocl.extra.StopExecutionException;

/**
 * A lockable function executor wrapper is a {@link Lockable} {@link FunctionExecutor} which wraps around another {@link FunctionExecutor}.
 * That allows the wrapped {@link FunctionExecutor} to be {@link Lockable}.
 * 
 * @param <R> The type of the return value of the defined function.
 * @see FunctionExecutor
 * @see Lockable
 */
public class LockableFEWrapper<R> implements FunctionExecutor<R> {

    private final FunctionExecutor<R> executor;

    /**
     * Creates a new lockable function executor wrapper which wraps aroung the given {@link FunctionExecutor}.
     * 
     * @param executor The {@link FunctionExecutor} to wrap around.
     */
    public LockableFEWrapper(FunctionExecutor<R> executor) {

        this.executor = executor;
    }

    /**
     * Returns the {@link FunctionExecutor} the wrapper wraps around for making it {@link Lockable}.
     * 
     * @return The {@link FunctionExecutor} the wrapper wraps around.
     */
    public FunctionExecutor<R> getExecutor() {

        return executor;
    }

    @Override
    @Lockable
    public R invoke(FeatureHolder holder, Object... arguments) throws StopExecutionException {

        return executor.invoke(holder, arguments);
    }

}
