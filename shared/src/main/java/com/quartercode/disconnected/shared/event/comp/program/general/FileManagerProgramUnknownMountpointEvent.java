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

package com.quartercode.disconnected.shared.event.comp.program.general;

/**
 * This event is returned by a file manager program if a path describes the mountpoint of an unknown or unmounted file system.
 */
public class FileManagerProgramUnknownMountpointEvent extends FileManagerProgramEvent {

    private final String mountpoint;

    /**
     * Creates a new file manager program unknown mountpoint event.
     * 
     * @param computerId The id of the computer which runs the program the event is fired by.
     * @param pid The process id of the process which runs the program the event is fired by.
     * @param mountpoint The mountpoint which describes a file system that is not known or not mounted.
     */
    public FileManagerProgramUnknownMountpointEvent(String computerId, int pid, String mountpoint) {

        super(computerId, pid);

        this.mountpoint = mountpoint;
    }

    /**
     * Returns the mountpoint which describes a file system that is not known or not mounted.
     * 
     * @return The unknown mountpoint.
     */
    public String getMountpoint() {

        return mountpoint;
    }

}
