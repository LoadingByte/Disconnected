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

package com.quartercode.disconnected.sim;

/**
 * The profile serialization exception occurrs if a {@link Profile} cannot be serialized or deserialized.
 * It is generally thrown by the {@link ProfileSerializer}.
 * 
 * @see ProfileSerializer
 */
public class ProfileSerializationException extends Exception {

    private static final long serialVersionUID = -8138944214058307695L;

    private final Profile     profile;

    /**
     * Creates a new profile serialization exception.
     * 
     * @param cause The exception that caused the error.
     * @param profile The {@link Profile} that cannot be serialized or deserialized.
     */
    public ProfileSerializationException(Throwable cause, Profile profile) {

        super(cause);

        this.profile = profile;
    }

    /**
     * Returns the {@link Profile} that cannot be serialized or deserialized.
     * 
     * @return The profile that caused the problem.
     */
    public Profile getProfile() {

        return profile;
    }

}
