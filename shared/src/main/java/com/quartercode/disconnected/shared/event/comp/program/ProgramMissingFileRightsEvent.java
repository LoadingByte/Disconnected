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

package com.quartercode.disconnected.shared.event.comp.program;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import com.quartercode.disconnected.shared.world.comp.file.FilePlaceholder;
import com.quartercode.disconnected.shared.world.comp.file.FileRights;

/**
 * This event is fired by a program if the session that runs the program hasn't got enough rights for some file operations.
 */
public class ProgramMissingFileRightsEvent extends ProgramEvent {

    private final String                            user;
    private final Map<FilePlaceholder, Character[]> filesAndMissingRights;

    /**
     * Creates a new program missing file rights event that represents missing rights for a single file.
     * 
     * @param computerId The id of the computer which runs the program the event is fired by.
     * @param pid The process id of the process which runs the program the event is fired by.
     * @param user The name of the user the program is running under.
     * @param file A {@link FilePlaceholder} for the file to which the program has insufficient access.
     * @param rights The file right characters the program needs but isn't granted on the given file.
     *        See {@link #getSingleRights()} for more detail on which rights are allowed.
     */
    public ProgramMissingFileRightsEvent(String computerId, int pid, String user, FilePlaceholder file, Character... rights) {

        super(computerId, pid);

        this.user = user;

        filesAndMissingRights = new HashMap<>();
        filesAndMissingRights.put(file, rights.clone());
    }

    /**
     * Creates a new program missing file rights event that represents missing rights for multiple files.
     * 
     * @param computerId The id of the computer which runs the program the event is fired by.
     * @param pid The process id of the process which runs the program the event is fired by.
     * @param user The name of the user the program is running under.
     * @param filesAndMissingRights A {@link Map} that contains the files the program has insufficient access to, alongside with their missing rights.
     *        See {@link #getFilesAndMissingRights()} for more detail on that.
     */
    public ProgramMissingFileRightsEvent(String computerId, int pid, String user, Map<FilePlaceholder, Character[]> filesAndMissingRights) {

        super(computerId, pid);

        this.user = user;
        this.filesAndMissingRights = Collections.unmodifiableMap(filesAndMissingRights);
    }

    /**
     * Returns the name of the user the program, which hasn't got enough rights, is running under.
     * That means that the provided rights aren't granted to the user for the corresponding files.
     * 
     * @return The user that hasn't enough rights.
     */
    public String getUser() {

        return user;
    }

    /**
     * Returns whether multiple "file -&gt; missing rights" pairs are stored inside the event.
     * That would mean that rights for more than one file are missing.
     * 
     * @return Whether rights are missing for multiple files.
     */
    public boolean containsMultipleFiles() {

        return filesAndMissingRights.size() > 1;
    }

    /**
     * If rights for only one file are missing, this returns a {@link FilePlaceholder} for the file to which the program has insufficient access.
     * If multiple files are stored ({@link #containsMultipleFiles()} is {@code true}), this throws an {@link IllegalStateException}.
     * 
     * @return The file the program hasn't got enough rights for.
     */
    public FilePlaceholder getSingleFile() {

        if (containsMultipleFiles()) {
            throw new IllegalStateException("Cannot retrieve single file from an event that contains multiple ones");
        }

        return filesAndMissingRights.keySet().iterator().next();
    }

    /**
     * If rights for only one file are missing, this returns the file right characters the program needs but isn't granted on the single file.
     * Note that the program hasn't got access to all of the returned characters.
     * If a program has access to a right, its character is not allowed to be placed inside this array.<br>
     * <br>
     * By default, the following file rights are possible:
     * 
     * <ul>
     * <li>{@link FileRights#READ}</li>
     * <li>{@link FileRights#WRITE}</li>
     * <li>{@link FileRights#DELETE}</li>
     * <li>{@link FileRights#EXECUTE}</li>
     * </ul>
     * 
     * If multiple files are stored ({@link #containsMultipleFiles()} is {@code true}), this throws an {@link IllegalStateException}.
     * 
     * @return The missing right characters.
     */
    public Character[] getSingleRights() {

        if (containsMultipleFiles()) {
            throw new IllegalStateException("Cannot retrieve single right array from an event that contains multiple files");
        }

        return filesAndMissingRights.values().iterator().next();
    }

    /**
     * If rights for multiple files are missing, this returns a {@link Map} that contains all affected files alongside with their missing rights.
     * The keys of the map are {@link FilePlaceholder}s that represent the files the program has insufficient access to.
     * The values store the file right characters the program needs but isn't granted for each file.<br>
     * <br>
     * Note that the program hasn't got access to all of the right characters.
     * If a program has access to a right, its character is not allowed to be placed inside a character array.
     * By default, the following file rights are possible:
     * 
     * <ul>
     * <li>{@link FileRights#READ}</li>
     * <li>{@link FileRights#WRITE}</li>
     * <li>{@link FileRights#DELETE}</li>
     * <li>{@link FileRights#EXECUTE}</li>
     * </ul>
     * 
     * @return The files the program has insufficient access to, alongside with their missing rights.
     */
    public Map<FilePlaceholder, Character[]> getFilesAndMissingRights() {

        return filesAndMissingRights;
    }

}
