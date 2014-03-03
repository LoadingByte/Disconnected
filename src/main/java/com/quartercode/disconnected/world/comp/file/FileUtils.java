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
import com.quartercode.disconnected.mocl.extra.FunctionExecutionException;
import com.quartercode.disconnected.world.comp.file.FileRights.FileAccessor;
import com.quartercode.disconnected.world.comp.file.FileRights.FileRight;
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
     * Returns if the given {@link User} has the given {@link FileRight} on the given {@link File}.
     * 
     * @param user The {@link User} who may have the given {@link FileRight} on the given {@link File}.
     * @param file The {@link File} the given {@link User} may have access to.
     * @param right The {@link FileRight} the given {@link User} may have.
     * @return True if the given {@link User} has the given {@link FileRight} on the given {@link File}.
     */
    public static boolean hasRight(User user, File<?> file, FileRight right) {

        try {
            if (user.get(User.IS_SUPERUSER).invoke()) {
                return true;
            } else if (checkRight(file, FileAccessor.OWNER, right) && file.get(File.GET_OWNER).invoke().equals(user)) {
                return true;
            } else if (checkRight(file, FileAccessor.GROUP, right) && user.get(User.GET_GROUPS).invoke().contains(file.get(File.GET_GROUP).invoke())) {
                return true;
            } else if (checkRight(file, FileAccessor.OTHERS, right)) {
                return true;
            }
        }
        catch (FunctionExecutionException e) {
            // Won't happen
        }

        return false;
    }

    private static boolean checkRight(File<?> file, FileAccessor accessor, FileRight right) {

        try {
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
        }
        catch (FunctionExecutionException e) {
            // Won't happen
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
     */
    public static boolean canChangeRights(User user, File<?> file) {

        try {
            return file.get(File.GET_OWNER).invoke().equals(user) || user.get(User.IS_SUPERUSER).invoke();
        }
        catch (FunctionExecutionException e) {
            // Won't happen
            return false;
        }
    }

    private FileUtils() {

    }

}
