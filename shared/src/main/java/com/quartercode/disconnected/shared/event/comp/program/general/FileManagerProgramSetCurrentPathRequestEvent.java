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
 * This event requests the change of the current path of a file manager program.
 * All file manager operations are done in the directory represented by that current path.
 * 
 * @see FileManagerProgramSetCurrentPathSuccessReturnEvent
 * @see FileManagerProgramUnknownMountpointEvent
 * @see FileManagerProgramInvalidPathEvent
 */
public class FileManagerProgramSetCurrentPathRequestEvent extends FileManagerProgramEvent {

    private final String path;

    /**
     * Creates a new file manager program set current path request event.
     * 
     * @param computerId The id of the computer which runs the program the event is fired by.
     * @param pid The process id of the process which runs the program the event is fired by.
     * @param path The new current global path the file manager program should look at.
     */
    public FileManagerProgramSetCurrentPathRequestEvent(String computerId, int pid, String path) {

        super(computerId, pid);

        this.path = path;
    }

    /**
     * Returns the new current global path the file manager program should look at.
     * All file manager operations are done in the directory represented by that current path.
     * Note that the path must be a global one that uses the format {@code /<mountpoint>/<local path>}.
     * 
     * @return The new current global path of the file manager.
     */
    public String getPath() {

        return path;
    }

    /**
     * This event is returned by a file manager program if the current path setting was successful.
     * 
     * @see FileManagerProgramSetCurrentPathRequestEvent
     */
    public static class FileManagerProgramSetCurrentPathSuccessReturnEvent extends FileManagerProgramEvent {

        /**
         * Creates a new file manager program set current path success return event.
         * 
         * @param computerId The id of the computer which runs the program the event is fired by.
         * @param pid The process id of the process which runs the program the event is fired by.
         */
        public FileManagerProgramSetCurrentPathSuccessReturnEvent(String computerId, int pid) {

            super(computerId, pid);
        }

    }

}
