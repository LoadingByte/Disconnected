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
 * This exception occurs if a path on a {@link FileSystem} does not exist or is invalid.
 * For example, the path {@code test1/test2/test.txt} could be invalid if {@code test2} is not a directory.
 *
 * @see FileSystem
 * @see File
 */
public class InvalidPathException extends Exception {

    private static final long serialVersionUID = 4866038832089520912L;

    private final FileSystem  fileSystem;
    private final String      path;

    /**
     * Creates a new invalid path exception.
     *
     * @param fileSystem The {@link FileSystem} on which the given path is invalid.
     * @param path The invalid path.
     */
    public InvalidPathException(FileSystem fileSystem, String path) {

        super("Invalid path: " + path);

        this.fileSystem = fileSystem;
        this.path = path;
    }

    /**
     * Returns the {@link FileSystem} on which the set path ({@link #getPath()}) is invalid.
     *
     * @return The file system that contains the invalidity.
     */
    public FileSystem getFileSystem() {

        return fileSystem;
    }

    /**
     * Returns the invalid path.
     * For example, the path {@code test1/test2/test.txt} could be invalid when {@code test2} is not a directory.
     *
     * @return The invalid path.
     */
    public String getPath() {

        return path;
    }

}
