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

package com.quartercode.disconnected.server.world.comp.file;

import static com.quartercode.classmod.extra.func.Priorities.LEVEL_5;
import static com.quartercode.classmod.extra.func.Priorities.LEVEL_6;
import static com.quartercode.classmod.factory.ClassmodFactory.factory;
import java.util.HashMap;
import java.util.Map;
import com.quartercode.classmod.extra.conv.CFeatureHolder;
import com.quartercode.classmod.extra.func.FunctionDefinition;
import com.quartercode.classmod.extra.func.FunctionExecutor;
import com.quartercode.classmod.extra.func.FunctionInvocation;
import com.quartercode.classmod.extra.prop.PropertyDefinition;
import com.quartercode.classmod.extra.storage.ReferenceStorage;
import com.quartercode.classmod.extra.storage.StandardStorage;
import com.quartercode.classmod.factory.PropertyDefinitionFactory;
import com.quartercode.disconnected.server.world.comp.user.User;
import com.quartercode.disconnected.shared.world.comp.file.FileRights;
import com.quartercode.disconnected.shared.world.comp.file.PathUtils;

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

    // ----- Properties -----

    /**
     * The {@link FileSystem} where the set {@link #FILE} should be added to on execution.
     */
    public static final PropertyDefinition<FileSystem>          FILE_SYSTEM;

    /**
     * The {@link File} that should be added to the set {@link #FILE_SYSTEM} under the set {@link #PATH}.
     * The name of the file is changed to the last entry of the path on execution.
     */
    public static final PropertyDefinition<File<ParentFile<?>>> FILE;

    /**
     * The path on the set {@link #FILE_SYSTEM} where the set {@link #FILE} should be located.
     * Any directories in this path that do not yet exist are created on execution.
     * The name of the set {@link #FILE} is also changed to the last entry of this path.
     */
    public static final PropertyDefinition<String>              PATH;

    static {

        FILE_SYSTEM = factory(PropertyDefinitionFactory.class).create("fileSystem", new ReferenceStorage<>());
        FILE = factory(PropertyDefinitionFactory.class).create("file", new StandardStorage<>());

        PATH = factory(PropertyDefinitionFactory.class).create("path", new StandardStorage<>());
        PATH.addSetterExecutor("normalize", FileAddAction.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                String normalizedPath = PathUtils.normalize((String) arguments[0]);
                if (!normalizedPath.isEmpty()) {
                    normalizedPath = normalizedPath.substring(1);
                }
                return invocation.next(normalizedPath);
            }

        }, LEVEL_6);

    }

    // ----- Functions -----

    /**
     * Adds the set {@link #FILE} to the set {@link #FILE_SYSTEM} under the set {@link #PATH}.
     * If the path does not exist, this method creates directories to match it.<br>
     * <br>
     * The name of the file to add is changed to match the path.
     * Furthermore, newly created directories have the same right settings as the file to add.
     * 
     * <table>
     * <tr>
     * <th>Exception</th>
     * <th>When?</th>
     * </tr>
     * <tr>
     * <td>{@link InvalidPathException}</td>
     * <td>The set file path isn't valid (for example, a file along the path is not a parent file).</td>
     * </tr>
     * <tr>
     * <td>{@link OccupiedPathException}</td>
     * <td>The file path, under which the new file should be added, is already used by annother file.</td>
     * </tr>
     * <tr>
     * <td>{@link OutOfSpaceException}</td>
     * <td>There is not enough space for the new file or a required directory.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>                EXECUTE = FileAction.EXECUTE;

    static {

        EXECUTE.addExecutor("addPathDirectories", FileAddAction.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                CFeatureHolder holder = invocation.getCHolder();
                File<ParentFile<?>> addFile = holder.getObj(FILE);

                String path = holder.getObj(PATH);
                String pathToParent = path.contains(PathUtils.SEPARATOR) ? path.substring(0, path.lastIndexOf(PathUtils.SEPARATOR)) : "";
                String[] pathParts = pathToParent.split(PathUtils.SEPARATOR);

                File<?> current = holder.getObj(FILE_SYSTEM).getObj(FileSystem.ROOT);
                for (String pathPart : pathParts) {
                    if (!pathPart.isEmpty()) {
                        File<?> nextCurrent = current.invoke(ParentFile.GET_CHILD_BY_NAME, pathPart);

                        if (nextCurrent == null) {
                            Directory directory = new Directory();
                            directory.setObj(File.NAME, pathPart);
                            directory.setObj(File.OWNER, addFile.getObj(File.OWNER));
                            directory.setObj(File.GROUP, addFile.getObj(File.GROUP));
                            directory.setObj(File.RIGHTS, new FileRights(addFile.getObj(File.RIGHTS)));
                            current.addToColl(ParentFile.CHILDREN, directory);
                            nextCurrent = directory;
                        } else if (! (nextCurrent instanceof ParentFile)) {
                            throw new InvalidPathException(holder.getObj(FILE_SYSTEM), path);
                        }

                        current = nextCurrent;
                    }
                }

                return invocation.next(arguments);
            }

        }, LEVEL_5);
        EXECUTE.addExecutor("addFile", FileAddAction.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                CFeatureHolder holder = invocation.getCHolder();

                String path = holder.getObj(PATH);
                String pathToParent = path.contains(PathUtils.SEPARATOR) ? path.substring(0, path.lastIndexOf(PathUtils.SEPARATOR)) : "";
                File<?> parent = holder.getObj(FILE_SYSTEM).invoke(FileSystem.GET_FILE, pathToParent);

                String addFileName = path.substring(path.lastIndexOf(PathUtils.SEPARATOR) + 1);
                if (parent.invoke(ParentFile.GET_CHILD_BY_NAME, addFileName) != null) {
                    throw new OccupiedPathException(holder.getObj(FILE_SYSTEM), path);
                }

                File<ParentFile<?>> addFile = holder.getObj(FILE);
                addFile.setObj(File.NAME, addFileName);
                parent.addToColl(ParentFile.CHILDREN, addFile);

                return invocation.next(arguments);
            }

        });

        GET_MISSING_RIGHTS.addExecutor("checkFirstUnexisting", FileAddAction.class, new FunctionExecutor<Map<File<?>, Character[]>>() {

            @Override
            public Map<File<?>, Character[]> invoke(FunctionInvocation<Map<File<?>, Character[]>> invocation, Object... arguments) {

                User executor = (User) arguments[0];
                File<?> missingRightsFile = null;

                CFeatureHolder holder = invocation.getCHolder();
                String[] parts = holder.getObj(PATH).split(PathUtils.SEPARATOR);
                File<?> current = holder.getObj(FILE_SYSTEM).getObj(FileSystem.ROOT);
                for (String part : parts) {
                    File<?> newCurrent = current.invoke(ParentFile.GET_CHILD_BY_NAME, part);

                    // Check whether the current file exists
                    if (newCurrent == null) {
                        // Executor user hasn't rights to create the new file
                        if (!current.invoke(File.HAS_RIGHT, executor, FileRights.WRITE)) {
                            missingRightsFile = current;
                        }
                        break;
                    } else {
                        // Continue on
                        current = newCurrent;
                    }
                }

                // Create the missing rights map
                Map<File<?>, Character[]> missingRights = new HashMap<>();
                if (missingRightsFile != null) {
                    missingRights.put(missingRightsFile, new Character[] { FileRights.WRITE });
                }

                invocation.next(arguments);
                return missingRights;
            }

        });

    }

}
