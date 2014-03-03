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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang.Validate;
import com.quartercode.disconnected.mocl.extra.FunctionExecutionException;
import com.quartercode.disconnected.world.comp.file.FileRights.FileAccessor;
import com.quartercode.disconnected.world.comp.file.FileRights.FileRight;
import com.quartercode.disconnected.world.comp.os.FileSystemModule;
import com.quartercode.disconnected.world.comp.os.FileSystemModule.KnownFileSystem;
import com.quartercode.disconnected.world.comp.os.User;
import com.quartercode.disconnected.world.comp.program.Process;

/**
 * This file utility contains method related to the {@link File} system.
 */
public class FileUtils {

    /**
     * Creates an absolute path out of the given one.
     * The algorithm starts at the given start path and changes the path according to the "change" path.
     * The "change" path also can be absolute. This will ignore the start path.
     * 
     * Here's an example:
     * 
     * <pre>
     * Start:  /user/homes/test/
     * Change: ../test2/docs
     * Result: /user/home/test2/docs
     * </pre>
     * 
     * @param start The absolute path the algorithm starts at.
     * @param path The "change" path which defines where the start path should change (see above).
     * @return The resolved absolute path.
     */
    public static String resolvePath(String start, String path) {

        if (!start.startsWith(File.SEPARATOR)) {
            throw new IllegalArgumentException("Start path must be absolute (it has to start with " + File.SEPARATOR + "): " + start);
        } else {
            List<String> current = new ArrayList<String>();
            if (!path.startsWith(File.SEPARATOR)) {
                current.addAll(Arrays.asList(start.split(File.SEPARATOR)));
                if (current.size() > 0) {
                    // Remove first entry ([this]/...), it's empty
                    current.remove(0);
                }
            }

            for (String pathChange : path.split(File.SEPARATOR)) {
                if (!pathChange.equals(".") && !pathChange.isEmpty()) {
                    if (pathChange.equals("..")) {
                        current.remove(current.size() - 1);
                    } else {
                        current.add(pathChange);
                    }
                }
            }

            if (current.isEmpty()) {
                return File.SEPARATOR;
            } else {
                String resolvedPath = "";
                for (String part : current) {
                    resolvedPath += File.SEPARATOR + part;
                }
                return resolvedPath;
            }
        }
    }

    /**
     * Splits the given global path into a mountpoint and a local file system path and returns the result.
     * The returned array always has two entries. [0] is the mountpoint and [1] is the local fs path.
     * 
     * The mountpoint of a path is the first path element. Examples:
     * 
     * <pre>
     * Path: /system/etc/test
     * => [system, etc/test]
     * 
     * Path: /user
     * => [user, null]
     * 
     * Path: home/user1/file
     * => [null, home/user1/file]
     * </pre>
     * 
     * @param path The path which should be splitted into its compontents.
     * @return The components of the given path.
     */
    public static String[] getComponents(String path) {

        if (path.startsWith(File.SEPARATOR)) {
            String componentPath = path.substring(1);
            int splitIndex = componentPath.indexOf(File.SEPARATOR);
            if (splitIndex < 0) {
                return new String[] { componentPath, null };
            } else if (splitIndex == componentPath.length() - 1) {
                // Filter out slash at the end of the mountpoint
                return new String[] { componentPath.substring(0, componentPath.length() - 1), null };
            } else {
                return new String[] { componentPath.substring(0, splitIndex), componentPath.substring(splitIndex + 1) };
            }
        } else {
            return new String[] { null, path };
        }
    }

    /**
     * Returns if the given {@link User} has the given {@link FileRight} on the given {@link File}.
     * 
     * @param user The {@link User} who may have the given {@link FileRight} on the given {@link File}.
     * @param file The {@link File} the given {@link User} may have access to.
     * @param right The {@link FileRight} the given {@link User} may have.
     * @return True if the given {@link User} has the given {@link FileRight} on the given {@link File}.
     * @throws FunctionExecutionException Something unexpected goes wrong.
     */
    public static boolean hasRight(User user, File<?> file, FileRight right) throws FunctionExecutionException {

        if (user.get(User.IS_SUPERUSER).invoke()) {
            return true;
        } else if (checkRight(file, FileAccessor.OWNER, right) && file.get(File.GET_OWNER).invoke().equals(user)) {
            return true;
        } else if (checkRight(file, FileAccessor.GROUP, right) && user.get(User.GET_GROUPS).invoke().contains(file.get(File.GET_GROUP).invoke())) {
            return true;
        } else if (checkRight(file, FileAccessor.OTHERS, right)) {
            return true;
        }

        return false;
    }

    private static boolean checkRight(File<?> file, FileAccessor accessor, FileRight right) throws FunctionExecutionException {

        if (file.get(File.GET_RIGHTS).invoke().get(FileRights.GET).invoke(accessor, right)) {
            if (right == FileRight.DELETE && file instanceof ParentFile) {
                for (File<?> child : file.get(ParentFile.GET_CHILDREN).invoke()) {
                    if (!checkRight(child, accessor, right)) {
                        return false;
                    }
                }
            }

            return true;
        }

        return false;
    }

    /**
     * Throws a {@link NoFileRightException} if the given {@link Process} hasn't the given {@link FileRight} on the given {@link File}.
     * 
     * @param process The {@link Process} which may have the given {@link FileRight} on the given {@link File}.
     * @param file The {@link File} the given {@link Process} may have access to.
     * @param right The {@link FileRight} the given {@link Process} may have.
     * @throws NoFileRightException The given {@link Process} hasn't the given {@link FileRight} on the given {@link File}.
     * @throws FunctionExecutionException Something goes wrong while retrieving the session {@link User} for the given {@link Process}.
     */
    public static void checkRight(Process<?> process, File<?> file, FileRight right) throws NoFileRightException, FunctionExecutionException {

        if (!hasRight(process.get(Process.GET_USER).invoke(), file, right)) {
            throw new NoFileRightException(process, file, right);
        }
    }

    /**
     * Returns if the given {@link User} can change the {@link FileRights} attributes of the given {@link File}.
     * 
     * @param user The {@link User} who may can change the {@link FileRights} attributes.
     * @param file The {@link File} the given {@link User} may have access to.
     * @return True if the given {@link User} can change the {@link FileRights} attributes of the given {@link File}.
     * @throws FunctionExecutionException Something unexpected goes wrong.
     */
    public static boolean canChangeRights(User user, File<?> file) throws FunctionExecutionException {

        return file.get(File.GET_OWNER).invoke().equals(user) || user.get(User.IS_SUPERUSER).invoke();
    }

    /**
     * Returns the {@link File} which is stored on a mounted {@link FileSystem} under the given path.
     * A path is a collection of {@link File}s seperated by a separator.
     * This will look up the {@link File} using a global os path.
     * 
     * @param fsModule The {@link FileSystemModule} which houses the {@link FileSystem} the {@link File} for return is stored on.
     * @param path The path to search under.
     * @return The {@link File} which is stored under the given path. Can be null if the {@link File} doesn't exist or the used {@link FileSystem} isn't mounted.
     * @throws FunctionExecutionException Something unexpected goes wrong while asking the {@link FileSystemModule} or the queried {@link FileSystem}.
     */
    public static File<?> getFile(FileSystemModule fsModule, String path) throws FunctionExecutionException {

        String[] pathComponents = getComponents(path);
        Validate.isTrue(pathComponents[0] != null && pathComponents[1] != null, "Must provide an absolute path");

        FileSystem fileSystem = fsModule.get(FileSystemModule.GET_MOUNTED_BY_MOUNTPOINT).invoke(pathComponents[0]).get(KnownFileSystem.GET_FILE_SYSTEM).invoke();
        if (fileSystem != null) {
            return fileSystem.get(FileSystem.GET_FILE).invoke(pathComponents[1]);
        } else {
            return null;
        }
    }

    /**
     * Adds the given {@link File} to a mounted {@link FileSystem} and locates it under the given path.
     * If the given path doesn't exist, this creates {@link Directory Directories} to match it.
     * The name of the {@link File} and the parent object will be changed to match the path.
     * 
     * @param fsModule The {@link FileSystemModule} which houses the {@link FileSystem} the given {@link File} should be stored on.
     * @param file The {@link File} to add under the given path.
     * @param path The path for the new {@link File}. The name of the {@link File} will be changed to the last entry.
     * @throws OutOfSpaceException There is not enough space on the target {@link FileSystem} for the new {@link File}.
     * @throws IllegalStateException The given {@link File} path isn't valid or the {@link FileSystem} for the path can't be found.
     * @throws FunctionExecutionException Something unexpected goes wrong while asking the {@link FileSystemModule} or the queried {@link FileSystem}.
     */
    public static void addFile(FileSystemModule fsModule, File<?> file, String path) throws OutOfSpaceException, FunctionExecutionException {

        String[] pathComponents = getComponents(path);
        Validate.isTrue(pathComponents[0] != null && pathComponents[1] != null, "Must provide an absolute path");

        FileSystem fileSystem = fsModule.get(FileSystemModule.GET_MOUNTED_BY_MOUNTPOINT).invoke(pathComponents[0]).get(KnownFileSystem.GET_FILE_SYSTEM).invoke();
        if (fileSystem != null) {
            try {
                fileSystem.get(FileSystem.ADD_FILE).invoke(file, pathComponents[1]);
            } catch (FunctionExecutionException e) {
                if (e.getCause() instanceof OutOfSpaceException) {
                    throw (OutOfSpaceException) e.getCause();
                } else if (e.getCause() instanceof IllegalStateException) {
                    throw (IllegalStateException) e.getCause();
                } else {
                    throw e;
                }
            }
        } else {
            throw new IllegalStateException();
        }
    }

    private FileUtils() {

    }

}
