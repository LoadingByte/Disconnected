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
 * This event is returned by a file manager program if the a path is not valid.
 * The reason for the path's invalidity could be a file along the path which is not a parent file.
 */
public class FileManagerProgramInvalidPathEvent extends FileManagerProgramEvent {

    private final String path;

    /**
     * Creates a new file manager program invalid path event.
     * 
     * @param computerId The id of the computer which runs the program the event is fired by.
     * @param pid The process id of the process which runs the program the event is fired by.
     * @param path The path which is not valid.
     */
    public FileManagerProgramInvalidPathEvent(String computerId, int pid, String path) {

        super(computerId, pid);

        this.path = path;
    }

    /**
     * Returns the path which is not valid.
     * The reason for its invalidity could be a file along the path which is not a parent file.
     * 
     * @return The invalid path.
     */
    public String getPath() {

        return path;
    }

}
