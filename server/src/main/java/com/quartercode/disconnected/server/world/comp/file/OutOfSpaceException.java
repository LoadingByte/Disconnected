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
 * This exception occurs if there is not enough space on a {@link FileSystem} for storing some new data (e.g. from a {@link File}).
 *
 * @see FileSystem
 */
public class OutOfSpaceException extends Exception {

    private static final long serialVersionUID = 3277220106399425382L;

    private final FileSystem  fileSystem;
    private final long        size;

    /**
     * Creates a new out of space exception.
     *
     * @param fileSystem The {@link FileSystem} which should have stored the new bytes.
     * @param size The amount of new bytes.
     */
    public OutOfSpaceException(FileSystem fileSystem, long size) {

        super("Out of space: Can't store " + size + "b");

        this.fileSystem = fileSystem;
        this.size = size;
    }

    /**
     * Returns the {@link FileSystem} which should have stored the new bytes.
     *
     * @return The file system which should have stored the new bytes.
     */
    public FileSystem getFileSystem() {

        return fileSystem;
    }

    /**
     * Returns the amount of new bytes.
     *
     * @return The amount of new bytes.
     */
    public long getSize() {

        return size;
    }

}
