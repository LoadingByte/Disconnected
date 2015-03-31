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
 * This runtime exception occures if a path on a {@link FileSystem}, under which a new file should be added, is already occupied by another file.
 * That just means that two files with the same path cannot exist.
 * 
 * @see FileSystem
 * @see File
 */
public class OccupiedPathException extends RuntimeException {

    private static final long serialVersionUID = -796351484036279039L;

    private final FileSystem  fileSystem;
    private final String      path;

    /**
     * Creates a new occupied path exception.
     * 
     * @param fileSystem The {@link FileSystem} on which the given path is already occupied.
     * @param path The occupied path.
     */
    public OccupiedPathException(FileSystem fileSystem, String path) {

        super("Occupied path: " + path);

        this.fileSystem = fileSystem;
        this.path = path;
    }

    /**
     * Returns the {@link FileSystem} on which the set path ({@link #getPath()}) is already occupied.
     * 
     * @return The file system that already holds a file on the given path.
     */
    public FileSystem getFileSystem() {

        return fileSystem;
    }

    /**
     * Returns the path that is already occupied by another file.
     * 
     * @return The occupied path.
     */
    public String getPath() {

        return path;
    }

}
