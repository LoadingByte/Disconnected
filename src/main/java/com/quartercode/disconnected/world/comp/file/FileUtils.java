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
import com.quartercode.disconnected.world.comp.file.FileRights.FileAccessor;
import com.quartercode.disconnected.world.comp.file.FileRights.FileRight;
import com.quartercode.disconnected.world.comp.os.User;

/**
 * This file utility contains methods related to {@link File}s and {@link FileSystem}s.
 * 
 * @see File
 * @see FileSystem
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
     * @throws IllegalArgumentException The start path is not absolute (it does not start with {@link File#SEPARATOR}).
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
     */
    public static boolean hasRight(User user, File<?> file, FileRight right) {

        if (user == null || user.get(User.IS_SUPERUSER).invoke()) {
            return true;
        } else if (file instanceof RootFile) {
            // Only superusers (filtered out by the previous check) can add files to the root file
            return false;
        } else if (checkRight(file, FileAccessor.OWNER, right) && file.get(File.OWNER).get().equals(user)) {
            return true;
        } else if (checkRight(file, FileAccessor.GROUP, right) && user.get(User.GROUPS).get().contains(file.get(File.GROUP).get())) {
            return true;
        } else if (checkRight(file, FileAccessor.OTHERS, right)) {
            return true;
        }

        return false;
    }

    private static boolean checkRight(File<?> file, FileAccessor accessor, FileRight right) {

        if (file.get(File.RIGHTS).get().get(FileRights.GET).invoke(accessor, right)) {
            if (right == FileRight.DELETE && file instanceof ParentFile) {
                for (File<?> child : file.get(ParentFile.CHILDREN).get()) {
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
     * Returns if the given {@link User} can change the {@link FileRights} attributes of the given {@link File}.
     * 
     * @param user The {@link User} who may can change the {@link FileRights} attributes.
     * @param file The {@link File} the given {@link User} may have access to.
     * @return True if the given {@link User} can change the {@link FileRights} attributes of the given {@link File}.
     */
    public static boolean canChangeRights(User user, File<?> file) {

        return file.get(File.OWNER).get().equals(user) || user.get(User.IS_SUPERUSER).invoke();
    }

    private FileUtils() {

    }

}
