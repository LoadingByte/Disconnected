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

import java.util.List;
import com.quartercode.disconnected.shared.event.util.FilePlaceholder;

/**
 * This event requests a list of {@link FilePlaceholder} inside the current directory from a file manager program.
 * 
 * @see FileManagerProgramListSuccessReturnEvent
 * @see FileManagerProgramListMissingRightsReturnEvent
 * @see FileManagerProgramUnknownMountpointEvent
 * @see FileManagerProgramInvalidPathEvent
 */
public class FileManagerProgramListRequestEvent extends FileManagerProgramEvent {

    /**
     * Creates a new file manager program list request event.
     * 
     * @param computerId The id of the computer which runs the program the event is sent to.
     * @param pid The process id of the process which runs the program the event is sent to.
     */
    public FileManagerProgramListRequestEvent(String computerId, int pid) {

        super(computerId, pid);
    }

    /**
     * This event is returned by a file manager program if the file listing was successful.
     * It carries some {@link FilePlaceholder}s which represent the listed files.
     * 
     * @see FileManagerProgramListRequestEvent
     */
    public static class FileManagerProgramListSuccessReturnEvent extends FileManagerProgramEvent {

        private final List<FilePlaceholder> files;

        /**
         * Creates a new file manager program list success return event.
         * 
         * @param computerId The id of the computer which runs the program the event is fired by.
         * @param pid The process id of the process which runs the program the event is fired by.
         * @param files The {@link FilePlaceholder} objects that represent the requested files which are children of the input directory.
         */
        public FileManagerProgramListSuccessReturnEvent(String computerId, int pid, List<FilePlaceholder> files) {

            super(computerId, pid);

            this.files = files;
        }

        /**
         * Returns the {@link FilePlaceholder} objects that represent the requested files which are children of the input directory.
         * 
         * @return Placeholder objects for the requested files.
         */
        public List<FilePlaceholder> getFiles() {

            return files;
        }

    }

    /**
     * This event is returned by a file manager program if the session that runs the program has not enough rights for the file listing.
     * 
     * @see FileManagerProgramListRequestEvent
     */
    public static class FileManagerProgramListMissingRightsReturnEvent extends FileManagerProgramEvent {

        private final String currentPath;

        /**
         * Creates a new file manager program list missing rights return event.
         * 
         * @param computerId The id of the computer which runs the program the event is fired by.
         * @param pid The process id of the process which runs the program the event is fired by.
         * @param currentPath The current path to which the file manager hasn't got sufficient access.
         */
        public FileManagerProgramListMissingRightsReturnEvent(String computerId, int pid, String currentPath) {

            super(computerId, pid);

            this.currentPath = computerId;
        }

        /**
         * Returns the current path to which the file manager hasn't got sufficient access.
         * 
         * @return The current path of the file manager.
         */
        public String getCurrentPath() {

            return currentPath;
        }

    }

}
