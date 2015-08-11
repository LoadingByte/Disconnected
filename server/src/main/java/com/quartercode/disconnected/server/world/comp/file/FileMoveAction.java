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
import com.quartercode.disconnected.shared.world.comp.file.PathUtils;
import com.quartercode.jtimber.api.node.Weak;

/**
 * The file move action is a simple file action that defines the process of moving a {@link File} to a new location.
 * Note that the file can also be moved to a new location on another file {@link FileSystem}.
 * For doing that, the action takes a path string that describes the location where the file to add should go, as well as the new file system. <br>
 * <br>
 * See {@link FileAction} for more detail on what file actions actually are.
 *
 * @see FileAction
 * @see File
 */
public class FileMoveAction extends FileAction {

    @Weak
    private final FileSystem          fileSystem;
    @Weak
    private final File<ParentFile<?>> file;
    private final String              path;

    /**
     * Creates a new file move action which moves the file to the given path on the file's current {@link FileSystem}.
     *
     * @param file The {@link File} that should be moved to the given path on its current file system.
     *        Note that the name of this file is changed to the last entry of the given path on execution.
     * @param path The local path on the given file system where the given file should be moved to.
     *        Any {@link Directory}s in this path that do not yet exist are created on execution; their attributes are copied from the given file.
     *        Note that the name of that given file is changed to the last entry of this path.
     */
    public FileMoveAction(File<ParentFile<?>> file, String path) {

        this(file.getFileSystem(), file, path);
    }

    /**
     * Creates a new file move action which moves the file to the given {@link FileSystem}.
     *
     * @param fileSystem The file system on which the given should be moved to on execution.
     * @param file The {@link File} that should be moved to the given path on the given file system.
     *        Note that the name of this file is changed to the last entry of the set path on execution.
     * @param path The local path on the given file system where the given file should be moved to.
     *        Any {@link Directory}s in this path that do not yet exist are created on execution; their attributes are copied from the given file.
     *        Note that the name of that given file is changed to the last entry of this path.
     */
    public FileMoveAction(FileSystem fileSystem, File<ParentFile<?>> file, String path) {

        Validate.notNull(fileSystem, "Cannot use null file system for file move action");
        Validate.notNull(file, "Cannot use null file for file move action");
        Validate.notBlank(path, "Cannot use blank path for file move action");

        this.fileSystem = fileSystem;
        this.file = file;

        String acutalPath = PathUtils.normalize(path);
        this.path = acutalPath.isEmpty() ? "" : acutalPath.substring(1);
    }

    /**
     * Returns the {@link FileSystem} on which the set {@link #getFile() file} should be moved to on execution.
     *
     * @return The target file system.
     */
    public FileSystem getFileSystem() {

        return fileSystem;
    }

    /**
     * Returns the {@link File} that should be moved to the set {@link #getPath() path} on the set {@link #getFileSystem() file system}.
     * Note that the name of this file is changed to the last entry of the set path on execution.
     *
     * @return The file that should be added.
     */
    public File<ParentFile<?>> getFile() {

        return file;
    }

    /**
     * The local path on the set {@link #getFileSystem() file system} where the set {@link #getFile() file} should be moved to.
     * Any {@link Directory}s in this path that do not yet exist are created on execution; their attributes are copied from the set file.
     * Note that the name of that set file is changed to the last entry of this path.
     *
     * @return The target local path (without a mountpoint).
     */
    public String getPath() {

        return path;
    }

    /**
     * Moves the set {@link #getFile() file} to the set {@link #getPath() path} on the set {@link #getFileSystem() file system}.
     * If the new path does not exist, this method creates {@link Directory}s to match it.
     * Note that the name of the added file is changed to the last entry of the set path.
     * Furthermore, newly created directories have the same attributes and right settings as the file to add.<br>
     * <br>
     * Also note that no right checks or anything like that are done by this method.
     * If you need such permission checks, use {@link #isExecutableBy(User)} or {@link #getMissingRights(User)}.
     *
     * @throws InvalidPathException If the set file path isn't valid (for example, if a file along the path is not a parent file).
     * @throws OccupiedPathException If the set file path, to which the file should be moved to, is already used by another file.
     * @throws OutOfSpaceException If there is not enough space for the file or a required directory on the target file system.
     */
    @Override
    public void execute() throws InvalidPathException, OccupiedPathException, OutOfSpaceException {

        // Retrieve the old parent file before the movement
        ParentFile<?> oldParent = file.getSingleParent();

        // Add the file to the target file system under the target path
        fileSystem.prepareAddFile(file, path).execute();

        // Manually remove the file from its old parent file
        // The FileRemoveAction cannot be used here because it would remove the file from its new parent
        oldParent.removeChildFile(file);
    }

    @Override
    public Map<File<?>, Character[]> getMissingRights(User user) {

        Map<File<?>, Character[]> missingRights = new HashMap<>();

        missingRights.putAll(new FileRemoveAction(file).getMissingRights(user));
        missingRights.putAll(new FileAddAction(fileSystem, file, path).getMissingRights(user));

        return missingRights;
    }

}
