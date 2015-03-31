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

package com.quartercode.disconnected.server.world.comp.file;

/**
 * This runtime exception occures if no file system is mounted under a certain mountpoint that the user input.
 * 
 * @see FSModule
 */
public class UnknownMountpointException extends RuntimeException {

    private static final long serialVersionUID = 6564332145013501014L;

    private final FSModule    fsModule;
    private final String      mountpoint;

    /**
     * Creates a new unknown mountpoint exception.
     * 
     * @param fsModule The {@link FSModule file system module} that doesn't contain a mounted file system under the given mountpoint.
     * @param mountpoint The mountpoint no file system is mounted under.
     */
    public UnknownMountpointException(FSModule fsModule, String mountpoint) {

        super("Unknown mountpoint: " + mountpoint);

        this.fsModule = fsModule;
        this.mountpoint = mountpoint;
    }

    /**
     * Returns the {@link FSModule file system module} that doesn't contain a mounted file system under the set mountpoint ({@link #getMountpoint()}).
     * 
     * @return The file system module on which the required mountpoint cannot be found.
     */
    public FSModule getFsModule() {

        return fsModule;
    }

    /**
     * Returns the required mountpoint no file system is mounted under.
     * 
     * @return The required mountpoint.
     */
    public String getMountpoint() {

        return mountpoint;
    }

}
