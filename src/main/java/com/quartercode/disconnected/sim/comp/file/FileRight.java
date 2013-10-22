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

package com.quartercode.disconnected.sim.comp.file;

/**
 * File rights control the access to files by users.
 * You can set if a given user/group is allowed to read, write, execute or delete;
 * 
 * @see Right
 */
public enum FileRight {

    /**
     * The read-right determinates if a user is allowed to read the contents from a file or directory (letter 'r').
     */
    READ ('r'),
    /**
     * The write-right determinates if a user is allowed to write contents into files or create new files in a directory (letter 'w').
     */
    WRITE ('w'),
    /**
     * The execute-right determinates if a user is allowed to execute a file (letter 'x').
     * It cannot be applied to directories.
     */
    EXECUTE ('x'),
    /**
     * The delete-right determinates if a user is allowed to delete a file or a directory (letter 'e').
     * In the case of a directory, the user also needs the delete-right on every file or folder in it.
     */
    DELETE ('e');

    private char letter;

    private FileRight(char letter) {

        this.letter = letter;
    }

    /**
     * Returns the letter which can be used as an acronym for the right.
     * 
     * @return The letter which can be used as an acronym for the right.
     */
    public char getLetter() {

        return letter;
    }

}
