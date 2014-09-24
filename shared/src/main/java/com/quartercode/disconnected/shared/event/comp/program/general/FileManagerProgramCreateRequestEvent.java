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
 * This event requests the creation of a file under the current path of the file manager.
 * It transports a subpath, which describes the location of the new file relative to the current path, and the type of the new file.
 * 
 * @see FileManagerProgramCreateSuccessReturnEvent
 * @see FileManagerProgramCreateOccupiedPathReturnEvent
 * @see FileManagerProgramCreateOutOfSpaceReturnEvent
 * @see FileManagerProgramCreateMissingRightsReturnEvent
 */
public class FileManagerProgramCreateRequestEvent extends FileManagerProgramEvent {

    private final String subpath;
    private final String type;

    /**
     * Creates a new file manager program create request event.
     * 
     * @param computerId The id of the computer which runs the program the event is sent to.
     * @param pid The process id of the process which runs the program the event is sent to.
     * @param subpath The path, which is relative to the current path, under which the new child file should be located.
     *        In the simplest case, this is just a file name.
     * @param type A string that describes the type of the new file.
     *        By default, possible strings are {@code rootFile}, {@code directory}, and {@code contentFile}.
     */
    public FileManagerProgramCreateRequestEvent(String computerId, int pid, String subpath, String type) {

        super(computerId, pid);

        this.subpath = subpath;
        this.type = type;
    }

    /**
     * Returns the path which describes the location of the new file relative to the current path.
     * In the simplest case, this is just a file name.
     * 
     * @return The relative path for the new file.
     */
    public String getSubpath() {

        return subpath;
    }

    /**
     * Returns a string that describes the type of the new file.
     * By default, possible types are:
     * 
     * <ul>
     * <li>rootFile</li>
     * <li>directory</li>
     * <li>contentFile</li>
     * </ul>
     * 
     * @return The file type of the new file.
     */
    public String getType() {

        return type;
    }

    /**
     * This event is returned by a file manager program if the file creation was successful.
     * 
     * @see FileManagerProgramCreateRequestEvent
     */
    public static class FileManagerProgramCreateSuccessReturnEvent extends FileManagerProgramEvent {

        /**
         * Creates a new file manager program create success return event.
         * 
         * @param computerId The id of the computer which runs the program the event is fired by.
         * @param pid The process id of the process which runs the program the event is fired by.
         */
        public FileManagerProgramCreateSuccessReturnEvent(String computerId, int pid) {

            super(computerId, pid);
        }

    }

    /**
     * This event is returned by a file manager program if the provided file path is already occupied by another file.
     * 
     * @see FileManagerProgramCreateRequestEvent
     */
    public static class FileManagerProgramCreateOccupiedPathReturnEvent extends FileManagerProgramEvent {

        private final String path;

        /**
         * Creates a new file manager program create occupied path return event.
         * 
         * @param computerId The id of the computer which runs the program the event is fired by.
         * @param pid The process id of the process which runs the program the event is fired by.
         * @param path The global file path which is already occupied by another file.
         */
        public FileManagerProgramCreateOccupiedPathReturnEvent(String computerId, int pid, String path) {

            super(computerId, pid);

            this.path = path;
        }

        /**
         * Returns the global file path which is already occupied by another file.
         * 
         * @return The occupied path.
         */
        public String getPath() {

            return path;
        }

    }

    /**
     * This event is returned by a file manager program if there's not enough space on the file system to create the new file.
     * The reason for the event might not be the actual new file.
     * If there are directories missing along the way and there's not enough space for those, the event is fired as well.
     * 
     * @see FileManagerProgramCreateRequestEvent
     */
    public static class FileManagerProgramCreateOutOfSpaceReturnEvent extends FileManagerProgramEvent {

        private final String fileSystemMountpoint;
        private final long   requiredSpace;

        /**
         * Creates a new file manager program create out of space retzrb event.
         * 
         * @param computerId The id of the computer which runs the program the event is fired by.
         * @param pid The process id of the process which runs the program the event is fired by.
         * @param fileSystemMountpoint The mountpoint of the fuke system where the new file should have been added to.
         * @param requiredSpace The amount of bytes which should have been added to the file system.
         */
        public FileManagerProgramCreateOutOfSpaceReturnEvent(String computerId, int pid, String fileSystemMountpoint, long requiredSpace) {

            super(computerId, pid);

            this.fileSystemMountpoint = fileSystemMountpoint;
            this.requiredSpace = requiredSpace;
        }

        /**
         * Returns the mountpoint of the file system where the new file should have been added to.
         * 
         * @return The mountpoint of the target file system.
         */
        public String getFileSystemMountpoint() {

            return fileSystemMountpoint;
        }

        /**
         * Returns the amount of bytes which should have been added to the file system.
         * 
         * @return The amount of new bytes.
         */
        public long getRequiredSpace() {

            return requiredSpace;
        }

    }

    /**
     * This event is returned by a file manager program if the session that runs the program has not enough rights for the file creation.
     * 
     * @see FileManagerProgramCreateRequestEvent
     */
    public static class FileManagerProgramCreateMissingRightsReturnEvent extends FileManagerProgramEvent {

        private final String path;

        /**
         * Creates a new file manager program create missing rights return event.
         * 
         * @param computerId The id of the computer which runs the program the event is fired by.
         * @param pid The process id of the process which runs the program the event is fired by.
         * @param path The global file path of the new file to which the file manager hasn't got sufficient access.
         */
        public FileManagerProgramCreateMissingRightsReturnEvent(String computerId, int pid, String path) {

            super(computerId, pid);

            this.path = path;
        }

        /**
         * Returns the global file path of the new file to which the file manager hasn't got sufficient access.
         * 
         * @return The path for the new file.
         */
        public String getPath() {

            return path;
        }

    }

}
