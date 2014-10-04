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

import com.quartercode.disconnected.shared.event.comp.program.ProgramMissingFileRightsEvent;

/**
 * This event requests the deletion of a file under the current path of the file manager.
 * It actually transports a subpath which describes the location of the new file relative to the current path.
 * 
 * @see FileManagerProgramRemoveSuccessReturnEvent
 * @see ProgramMissingFileRightsEvent
 */
public class FileManagerProgramRemoveRequestEvent extends FileManagerProgramEvent {

    private final String subpath;

    /**
     * Creates a new file manager program remove request event.
     * 
     * @param computerId The id of the computer which runs the program the event is sent to.
     * @param pid The process id of the process which runs the program the event is sent to.
     * @param subpath The path, which is relative to the current path, under which the file for removal is located.
     *        In the simplest case, this is just a file name.
     */
    public FileManagerProgramRemoveRequestEvent(String computerId, int pid, String subpath) {

        super(computerId, pid);

        this.subpath = subpath;
    }

    /**
     * Returns the path which describes the location of the file for removal relative to the current path.
     * In the simplest case, this is just a file name.
     * 
     * @return The relative of the file for removal.
     */
    public String getSubpath() {

        return subpath;
    }

    /**
     * This event is returned by a file manager program if the file removal was successful.
     * 
     * @see FileManagerProgramRemoveRequestEvent
     */
    public static class FileManagerProgramRemoveSuccessReturnEvent extends FileManagerProgramEvent {

        /**
         * Creates a new file manager program remove success return event.
         * 
         * @param computerId The id of the computer which runs the program the event is fired by.
         * @param pid The process id of the process which runs the program the event is fired by.
         */
        public FileManagerProgramRemoveSuccessReturnEvent(String computerId, int pid) {

            super(computerId, pid);
        }

    }

}
