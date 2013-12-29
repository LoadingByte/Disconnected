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

package com.quartercode.disconnected.world.comp.file;

import com.quartercode.disconnected.world.comp.file.FileRights.FileRight;
import com.quartercode.disconnected.world.comp.program.Process;

/**
 * This exception occures if a process attempts to execute an action on a file (like move), but it hasn't the rights to do so.
 * 
 * @see FileRights
 */
public class NoFileRightException extends Exception {

    private static final long serialVersionUID = 1461048154676356785L;

    private final Process<?>  process;
    private final File<?>     file;
    private final FileRight   requiredRight;

    /**
     * Creates a new no file right exception and sets the file the given process tried to access and the required right for that access.
     * 
     * @param process The process which tried to access the given file.
     * @param file The file the given process tried to access.
     * @param requiredRight The right the given process didn't have which is required for accessing the file.
     */
    public NoFileRightException(Process<?> process, File<?> file, FileRight requiredRight) {

        super("Error while accessing file: Executing process hasn't right '" + requiredRight.toString().toLowerCase() + "'");

        this.process = process;
        this.file = file;
        this.requiredRight = requiredRight;
    }

    /**
     * Returns the process which tried to access the given file.
     * 
     * @return The process which tried to access the given file.
     */
    public Process<?> getProcess() {

        return process;
    }

    /**
     * Returns the file the given process tried to access.
     * 
     * @return The file the given process tried to access.
     */
    public File<?> getFile() {

        return file;
    }

    /**
     * Returns The right the given process didn't have which is required for accessing the file.
     * 
     * @return The right the given process didn't have.
     */
    public FileRight getRequiredRight() {

        return requiredRight;
    }

}
