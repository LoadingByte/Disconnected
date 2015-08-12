/*
 * This file is part of Disconnected.
 * Copyright (c) 2013 QuarterCode <http://quartercode.com/>
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

package com.quartercode.disconnected.server.sim.gen;

import com.quartercode.disconnected.server.world.World;

/**
 * The generation exception can occur while trying to generate a {@link World} or a part of it and indicates a severe bug the generator can not recover from.
 * It is generally thrown by one of the generators in this package.
 */
public class GenerationException extends RuntimeException {

    private static final long serialVersionUID = 1851129437269233199L;

    /**
     * Creates a new generation exception.
     */
    public GenerationException() {

        super();
    }

    /**
     * Creates a new generation exception with the given message.
     *
     * @param message The detail message.
     */
    public GenerationException(String message) {

        super(message);
    }

    /**
     * Creates a new generation exception with the given cause.
     *
     * @param cause The child cause which caused the exception to be thrown.
     */
    public GenerationException(Throwable cause) {

        super(cause);
    }

    /**
     * Creates a new generation exception with the given message and cause.
     *
     * @param message The detail message.
     * @param cause The child cause which caused the exception to be thrown.
     */
    public GenerationException(String message, Throwable cause) {

        super(message, cause);
    }

}
