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

/**
 * The return next exception is thrown if a {@link FunctionExecutor} wants the next invoked executor define the return value of the {@link Function}.
 * By default, the algorithm uses the return value of the {@link FunctionExecutor} with the highest priority as return value for the {@link Function}.
 * If the {@link FunctionExecutor} which should define the return value throws this exception, the {@link FunctionExecutor} with the second highest priority will be used.
 * 
 * @see FunctionExecutor
 */
public class ReturnNextException extends ExecutorInvokationException {

    private static final long serialVersionUID = 9095191065451797814L;

    /**
     * Creates a new return next exception signal.
     */
    public ReturnNextException() {

        super();
    }

}
