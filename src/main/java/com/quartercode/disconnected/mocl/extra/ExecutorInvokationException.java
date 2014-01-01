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
 * Executor invokation exceptions are the only exceptions which can be thrown by {@link FunctionExecutor}s if something goes wrong (apart from {@link IllegalArgumentException}s).
 * The exception has several subclasses which require special handling.
 * 
 * @see FunctionExecutor
 * @see StopExecutionException
 * @see ReturnNextException
 */
public class ExecutorInvokationException extends Exception {

    private static final long serialVersionUID = -5415802666609924794L;

    /**
     * Creates a new empty executor invokation exception.
     */
    public ExecutorInvokationException() {

    }

    /**
     * Creates a new executor invokation exception with the given message.
     * 
     * @param message A detailed message which is assigned to the exception.
     */
    protected ExecutorInvokationException(String message) {

        super(message);
    }

    /**
     * Creates a new executor invokation exception with the given causing {@link Throwable}.
     * 
     * @param cause The {@link Throwable} which caused the exception.
     */
    protected ExecutorInvokationException(Throwable cause) {

        super(cause);
    }

    /**
     * Creates a new executor invokation exception with the given message and causing {@link Throwable}.
     * 
     * @param message A detailed message which is assigned to the exception.
     * @param cause The {@link Throwable} which caused the exception.
     */
    protected ExecutorInvokationException(String message, Throwable cause) {

        super(message, cause);
    }

}
