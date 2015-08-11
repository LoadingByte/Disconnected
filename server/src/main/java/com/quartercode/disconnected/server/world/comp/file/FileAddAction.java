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

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.Validate;
import com.quartercode.disconnected.server.world.comp.user.User;
import com.quartercode.disconnected.shared.world.comp.file.FileRights;
import com.quartercode.disconnected.shared.world.comp.file.PathUtils;
import com.quartercode.jtimber.api.node.Weak;

/**
 * The file add action is a simple file action that defines the process of adding a {@link File} to a {@link FileSystem}.
 * For doing that, the action takes a path string that describes the location where the file to add should go.<br>
 * <br>
 * See {@link FileAction} for more detail on what file actions actually are.
 *
 * @see FileAction
 * @see File
 * @see FileSystem
 */
public class FileAddAction extends FileAction {

    @Weak
    private final FileSystem          fileSystem;
    @Weak
    private final File<ParentFile<?>> file;
    private final String              path;

    /**
     * Creates a new file add action.
     *
     * @param fileSystem The {@link FileSystem} on which the given file should be stored on execution.
     * @param file The {@link File} that should be added to the given file system under the given path.
     *        Note that the name of this file is changed to the last entry of the given path on execution.
     * @param path The local path on the given file system where the given file should be located.
     *        Any {@link Directory}s in this path that do not yet exist are created on execution; their attributes are copied from the given file.
     *        Note that the name of that given file is changed to the last entry of this path.
     */
    public FileAddAction(FileSystem fileSystem, File<ParentFile<?>> file, String path) {

        Validate.notNull(fileSystem, "Cannot use null file system for file add action");
        Validate.notNull(file, "Cannot use null file for file add action");
        Validate.notBlank(path, "Cannot use blank path for file add action");

        this.fileSystem = fileSystem;
        this.file = file;

        String acutalPath = PathUtils.normalize(path);
        this.path = acutalPath.isEmpty() ? "" : acutalPath.substring(1);
    }

    /**
     * Returns the {@link FileSystem} on which the set {@link #getFile() file} should be stored on execution.
     *
     * @return The target file system.
     */
    public FileSystem getFileSystem() {

        return fileSystem;
    }

    /**
     * Returns the {@link File} that should be added to the set {@link #getFileSystem() file system} under the set {@link #getPath() path}.
     * Note that the name of this file is changed to the last entry of the set path on execution.
     *
     * @return The file that should be added.
     */
    public File<ParentFile<?>> getFile() {

        return file;
    }

    /**
     * Returns the local path on the set {@link #getFileSystem() file system} where the set {@link #getFile() file} should be located.
     * Any {@link Directory}s in this path that do not yet exist are created on execution; their attributes are copied from the set file.
     * Note that the name of that set file is changed to the last entry of this path.
     *
     * @return The target local path (without a mountpoint).
     */
    public String getPath() {

        return path;
    }

    /**
     * Adds the set {@link #getFile() file} to the set {@link #getPath() path} on the set {@link #getFileSystem() file system}.
     * If the path does not exist, this method creates {@link Directory}s to match it.
     * Note that the name of the added file is changed to the last entry of the set path.
     * Furthermore, newly created directories have the same attributes and right settings as the file to add.<br>
     * <br>
     * Also note that no right checks or anything like that are done by this method.
     * If you need such permission checks, use {@link #isExecutableBy(User)} or {@link #getMissingRights(User)}.
     *
     * @throws InvalidPathException If the set file path isn't valid (for example, if a file along the path is not a parent file).
     * @throws OccupiedPathException If the set file path, under which the new file should be added, is already used by another file.
     * @throws OutOfSpaceException If there is not enough space for the new file or a required directory on the target file system.
     */
    @Override
    public void execute() throws InvalidPathException, OccupiedPathException, OutOfSpaceException {

        addPathDirectories();
        addFile();
    }

    private String getParentFilePath() {

        return path.contains(PathUtils.SEPARATOR) ? path.substring(0, path.lastIndexOf(PathUtils.SEPARATOR)) : "";
    }

    private void addPathDirectories() throws InvalidPathException, OutOfSpaceException {

        String[] parentPathParts = getParentFilePath().split(PathUtils.SEPARATOR);

        ParentFile<?> current = fileSystem.getRootFile();
        for (String parentPathPart : parentPathParts) {
            if (!parentPathPart.isEmpty()) {
                File<?> nextCurrent = current.getChildFileByName(parentPathPart);

                if (nextCurrent == null) {
                    Directory directory = new Directory(file.getOwner());
                    directory.setName(parentPathPart);
                    directory.setGroup(file.getGroup());
                    directory.getRights().importRights(file.getRights());
                    current.addChildFile(directory);
                    nextCurrent = directory;
                } else if (! (nextCurrent instanceof ParentFile)) {
                    throw new InvalidPathException(fileSystem, path);
                }

                current = (ParentFile<?>) nextCurrent;
            }
        }
    }

    private void addFile() throws InvalidPathException, OccupiedPathException, OutOfSpaceException {

        // Should not throw an exception because the parent has already been added by the addPathDirectories() method
        ParentFile<?> parent = (ParentFile<?>) fileSystem.getFile(getParentFilePath());

        String fileName = path.substring(path.lastIndexOf(PathUtils.SEPARATOR) + 1);
        if (parent.getChildFileByName(fileName) != null) {
            throw new OccupiedPathException(fileSystem, path);
        }

        file.setName(fileName);
        parent.addChildFile(file);
    }

    @Override
    public Map<File<?>, Character[]> getMissingRights(User user) {

        String[] pathParts = path.split(PathUtils.SEPARATOR);

        File<?> missingRightsFile = null;
        ParentFile<?> current = fileSystem.getRootFile();
        for (String pathPart : pathParts) {
            if (!pathPart.isEmpty()) {
                File<?> nextCurrent = current.getChildFileByName(pathPart);

                // Check whether the current file exists
                if (nextCurrent == null) {
                    // Executor user hasn't rights to create the new file
                    if (!current.hasRight(user, FileRights.WRITE)) {
                        missingRightsFile = current;
                    }
                    break;
                } else if (! (nextCurrent instanceof ParentFile)) {
                    // Break; checking whether the operation is executable at all is not the responsibility of this method
                    break;
                } else {
                    // Continue on
                    current = (ParentFile<?>) nextCurrent;
                }
            }
        }

        // Create the missing rights map
        Map<File<?>, Character[]> missingRights = new HashMap<>();
        if (missingRightsFile != null) {
            missingRights.put(missingRightsFile, new Character[] { FileRights.WRITE });
        }

        return missingRights;
    }

}
