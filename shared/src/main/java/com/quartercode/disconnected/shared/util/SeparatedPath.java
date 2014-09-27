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

package com.quartercode.disconnected.shared.util;

import com.quartercode.disconnected.shared.constant.CommonFiles;

/**
 * A separated path data object stores a file path that is split into a directory and a file name component.
 */
public class SeparatedPath {

    private final String dir;
    private final String file;

    /**
     * Creates a new separated path that represents a file with the given file name inside the given directory.
     * 
     * @param dir The path of the directory that contains the represented file.
     *        When possible, a {@link CommonFiles} constant should be used here.
     * @param file The name of the represented file.
     */
    public SeparatedPath(String dir, String file) {

        this.dir = PathUtils.normalize(dir);
        this.file = PathUtils.normalize(file).substring(1);
    }

    /**
     * Creates a new separated path by using the two path components stored in the given array.
     * The directory path must be located at index 0 while the file name must be located at index 1.
     * 
     * @param pathComponents The array whose two elements should be used.
     */
    public SeparatedPath(String[] pathComponents) {

        this(pathComponents[0], pathComponents[1]);
    }

    /**
     * Creates a new separated path by splitting the given path to a file.
     * 
     * @param path The path that points to the represented file.
     *        It is split into two components.
     */
    public SeparatedPath(String path) {

        String[] splitPath = PathUtils.splitBeforeName(PathUtils.normalize(path));
        dir = splitPath[0];
        file = splitPath[1];
    }

    /**
     * Returns the stored directory path that represents the first component of the path (e.g. {@code /system/bin}).
     * When possible, a {@link CommonFiles} constant should be used here.
     * 
     * @return The directory path.
     */
    public String getDir() {

        return dir;
    }

    /**
     * Returns the stored file name that represents the second component of the path (e.g. {@code session.exe}).
     * 
     * @return The file name.
     */
    public String getFile() {

        return file;
    }

    /**
     * Combines the stored directory path and file name to an array.
     * The directory path is located at index 0 while the file name is located at index 1.
     * 
     * @return The stored components as an array.
     */
    public String[] toArray() {

        return new String[] { dir, file };
    }

    /**
     * Combines the stored directory path and file name to a full file path.
     * 
     * @return The full file path to the file.
     */
    @Override
    public String toString() {

        return dir + PathUtils.SEPARATOR + file;
    }

}
