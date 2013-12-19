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

import com.quartercode.disconnected.mocl.base.FeatureHolder;

/**
 * An updatable function executor can be invoked with a {@link FeatureHolder} and some arguments.
 * It returns a value of a specified type. For no return value, you can use {@link Void}.
 * 
 * @param <R> The type of the return value of the defined function.
 */
public interface UpdatableFunctionExecutor<R> extends FunctionExecutor<R> {

    /**
     * Returns the delay before the task is invoked the first time.
     * 
     * @return The delay before the task is invoked the first time.
     */
    public int getDelay();

    /**
     * Returns the period delay between looped invokations.
     * 
     * @return The task is invoked every time after those ticks elapsed.
     */
    public int getPeriod();

    /**
     * Returns the amount of ticks that have elapsed.
     * 
     * @return The amount of ticks that have elapsed.
     */
    public int getElapsed();

    public void elapse();

    /**
     * Cancels the task so it wont elapse any more ticks.
     */
    public void cancel();

}
