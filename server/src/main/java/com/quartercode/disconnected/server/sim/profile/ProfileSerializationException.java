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

package com.quartercode.disconnected.server.sim.profile;

/**
 * The profile serialization exception can occur while trying to serialize a whole {@link ProfileData} object or a part of it.
 * It is generally thrown by the {@link ProfileSerializationService}.
 * 
 * @see ProfileSerializationService
 */
public class ProfileSerializationException extends Exception {

    private static final long serialVersionUID = 3032649287084608186L;

    /**
     * Creates a new profile serialization exception.
     */
    public ProfileSerializationException() {

        super();
    }

    /**
     * Creates a new profile serialization exception with the given message.
     * 
     * @param message The detail message.
     */
    public ProfileSerializationException(String message) {

        super(message);
    }

    /**
     * Creates a new profile serialization exception with the given cause.
     * 
     * @param cause The child cause which caused the exception to be thrown.
     */
    public ProfileSerializationException(Throwable cause) {

        super(cause);
    }

    /**
     * Creates a new profile serialization exception with the given message and cause.
     * 
     * @param message The detail message.
     * @param cause The child cause which caused the exception to be thrown.
     */
    public ProfileSerializationException(String message, Throwable cause) {

        super(message, cause);
    }

}
