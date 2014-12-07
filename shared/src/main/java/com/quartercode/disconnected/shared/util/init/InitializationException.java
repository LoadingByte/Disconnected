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

package com.quartercode.disconnected.shared.util.init;

/**
 * The initialization exception is thrown by an {@link Initializer} if a major unrecoverable error occurs during the initialization process.
 * However, it causes the whole application to halt.
 * Because of that, minor errors should be ignored since the application might be able to work regardless of them.
 */
public class InitializationException extends Exception {

    private static final long serialVersionUID = 175552483768930997L;

    /**
     * Creates a new initialization exception.
     */
    public InitializationException() {

    }

    /**
     * Creates a new initialization exception with the given message.
     * 
     * @param message The detail message.
     */
    public InitializationException(String message) {

        super(message);
    }

    /**
     * Creates a new initialization exception with the given cause.
     * 
     * @param cause The child cause which caused the exception to be thrown.
     */
    public InitializationException(Throwable cause) {

        super(cause);
    }

    /**
     * Creates a new initialization exception with the given message and cause.
     * 
     * @param message The detail message.
     * @param cause The child cause which caused the exception to be thrown.
     */
    public InitializationException(String message, Throwable cause) {

        super(message, cause);
    }

}
