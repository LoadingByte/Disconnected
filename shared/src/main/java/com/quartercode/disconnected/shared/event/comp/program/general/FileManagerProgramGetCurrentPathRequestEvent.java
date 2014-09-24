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
 * This event requests the current path of a file manager program.
 * All file manager operations are done in the directory represented by that current path.
 * 
 * @see FileManagerProgramGetCurrentPathReturnEvent
 */
public class FileManagerProgramGetCurrentPathRequestEvent extends FileManagerProgramEvent {

    /**
     * Creates a new file manager program get current path request event.
     * 
     * @param computerId The id of the computer which runs the program the event is sent to.
     * @param pid The process id of the process which runs the program the event is sent to.
     */
    public FileManagerProgramGetCurrentPathRequestEvent(String computerId, int pid) {

        super(computerId, pid);
    }

    /**
     * This event is returned by a file manager program in response to the {@link FileManagerProgramGetCurrentPathRequestEvent}.
     * It contains the current path of the file manager.
     * 
     * @see FileManagerProgramGetCurrentPathRequestEvent
     */
    public static class FileManagerProgramGetCurrentPathReturnEvent extends FileManagerProgramEvent {

        private final String path;

        /**
         * Creates a new file manager program get current path return event.
         * 
         * @param computerId The id of the computer which runs the program the event is fired by.
         * @param pid The process id of the process which runs the program the event is fired by.
         * @param path The current global path the file manager program looks at.
         */
        public FileManagerProgramGetCurrentPathReturnEvent(String computerId, int pid, String path) {

            super(computerId, pid);

            this.path = path;
        }

        /**
         * Returns the current global path the file manager program looks at.
         * All file manager operations are done in the directory represented by that current path.
         * Note that the path is a global one that uses the format {@code /<mountpoint>/<local path>}.
         * 
         * @return The global path of the file manager.
         */
        public String getPath() {

            return path;
        }

    }

}
