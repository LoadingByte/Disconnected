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

import com.quartercode.disconnected.world.comp.os.OperatingSystem;

/**
 * This runtime exception occures if a file system can't be mounted or unmounted.
 * 
 * @see FileSystem
 * @see OperatingSystem
 */
public class MountException extends Exception {

    private static final long serialVersionUID = -6971327868164892094L;

    private FileSystem        fileSystem;
    private final boolean     mount;

    /**
     * Creates a new mount exception and sets the file system which should have been mounted or unmounted.
     * 
     * @param fileSystem The file system which should have been mounted or unmounted.
     * @param mount True if the given file system should have been mounted, false if it should have been unmounted.
     */
    public MountException(FileSystem fileSystem, boolean mount) {

        super("Can't " + (mount ? "mount" : "unmount") + " file system");

        this.mount = mount;
    }

    /**
     * Creates a new mount exception and sets the file system which should have been mounted or unmounted and an informational message.
     * 
     * @param fileSystem The file system which should have been mounted or unmounted.
     * @param mount True if the given file system should have been mounted, false if it should have been unmounted.
     * @param message An informational message describing what happened.
     */
    public MountException(FileSystem fileSystem, boolean mount, String message) {

        super("Can't " + (mount ? "mount" : "unmount") + " file system" + ": " + message);

        this.mount = mount;
    }

    /**
     * Returns the file system which should have been mounted or unmounted.
     * 
     * @return The file system which should have been mounted or unmounted.
     */
    public FileSystem getFileSystem() {

        return fileSystem;
    }

    /**
     * Returns true if the file system should have been mounted.
     * 
     * @return True if the file system should have been mounted.
     */
    public boolean isMount() {

        return mount;
    }

    /**
     * Returns true if the file system should have been unmounted.
     * 
     * @return True if the file system should have been unmounted.
     */
    public boolean isUnmount() {

        return !mount;
    }

}
