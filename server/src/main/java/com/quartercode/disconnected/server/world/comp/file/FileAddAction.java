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

import static com.quartercode.classmod.ClassmodFactory.create;
import org.apache.commons.lang3.reflect.TypeLiteral;
import com.quartercode.classmod.base.FeatureHolder;
import com.quartercode.classmod.extra.FunctionDefinition;
import com.quartercode.classmod.extra.FunctionExecutor;
import com.quartercode.classmod.extra.FunctionInvocation;
import com.quartercode.classmod.extra.Prioritized;
import com.quartercode.classmod.extra.PropertyDefinition;
import com.quartercode.classmod.extra.storage.ReferenceStorage;
import com.quartercode.classmod.extra.storage.StandardStorage;
import com.quartercode.disconnected.server.world.WorldFeatureHolder;
import com.quartercode.disconnected.server.world.comp.os.User;
import com.quartercode.disconnected.shared.util.PathUtils;
import com.quartercode.disconnected.shared.world.comp.file.FileRights;

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
public class FileAddAction extends WorldFeatureHolder implements FileAction {

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

        FILE_SYSTEM = create(new TypeLiteral<PropertyDefinition<FileSystem>>() {}, "name", "fileSystem", "storage", new ReferenceStorage<>());
        FILE = create(new TypeLiteral<PropertyDefinition<File<ParentFile<?>>>>() {}, "name", "file", "storage", new StandardStorage<>());

        PATH = create(new TypeLiteral<PropertyDefinition<String>>() {}, "name", "path", "storage", new StandardStorage<>());
        PATH.addSetterExecutor("normalize", FileAddAction.class, new FunctionExecutor<Void>() {

            @Override
            @Prioritized (Prioritized.LEVEL_6)
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                String normalizedPath = PathUtils.normalize((String) arguments[0]);
                if (!normalizedPath.isEmpty()) {
                    normalizedPath = normalizedPath.substring(1);
                }
                return invocation.next(normalizedPath);
            }

        });

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
            @Prioritized (Prioritized.LEVEL_5)
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                FeatureHolder holder = invocation.getHolder();
                File<ParentFile<?>> addFile = holder.get(FILE).get();

                String path = holder.get(PATH).get();
                String pathToParent = path.contains(PathUtils.SEPARATOR) ? path.substring(0, path.lastIndexOf(PathUtils.SEPARATOR)) : "";
                String[] pathParts = pathToParent.split(PathUtils.SEPARATOR);

                File<?> current = holder.get(FILE_SYSTEM).get().get(FileSystem.ROOT).get();
                for (String pathPart : pathParts) {
                    if (!pathPart.isEmpty()) {
                        File<?> nextCurrent = current.get(ParentFile.GET_CHILD_BY_NAME).invoke(pathPart);

                        if (nextCurrent == null) {
                            Directory directory = new Directory();
                            directory.get(File.NAME).set(pathPart);
                            directory.get(File.OWNER).set(addFile.get(File.OWNER).get());
                            directory.get(File.GROUP).set(addFile.get(File.GROUP).get());
                            directory.get(File.RIGHTS).set(new FileRights(addFile.get(File.RIGHTS).get()));
                            current.get(ParentFile.CHILDREN).add(directory);
                            nextCurrent = directory;
                        } else if (! (nextCurrent instanceof ParentFile)) {
                            throw new InvalidPathException(holder.get(FILE_SYSTEM).get(), path);
                        }

                        current = nextCurrent;
                    }
                }

                return invocation.next(arguments);
            }

        });
        EXECUTE.addExecutor("addFile", FileAddAction.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                FeatureHolder holder = invocation.getHolder();

                String path = holder.get(PATH).get();
                String pathToParent = path.contains(PathUtils.SEPARATOR) ? path.substring(0, path.lastIndexOf(PathUtils.SEPARATOR)) : "";
                File<?> parent = holder.get(FILE_SYSTEM).get().get(FileSystem.GET_FILE).invoke(pathToParent);

                String addFileName = path.substring(path.lastIndexOf(PathUtils.SEPARATOR) + 1);
                if (parent.get(ParentFile.GET_CHILD_BY_NAME).invoke(addFileName) != null) {
                    throw new OccupiedPathException(holder.get(FILE_SYSTEM).get(), path);
                }

                File<ParentFile<?>> addFile = holder.get(FILE).get();
                addFile.get(File.NAME).set(addFileName);
                parent.get(ParentFile.CHILDREN).add(addFile);

                return invocation.next(arguments);
            }

        });

        IS_EXECUTABLE_BY.addExecutor("checkFirstUnexisting", FileAddAction.class, new FunctionExecutor<Boolean>() {

            @Override
            public Boolean invoke(FunctionInvocation<Boolean> invocation, Object... arguments) {

                User executor = (User) arguments[0];
                boolean result = true;

                FeatureHolder holder = invocation.getHolder();
                String[] parts = holder.get(PATH).get().split(PathUtils.SEPARATOR);
                File<?> current = holder.get(FILE_SYSTEM).get().get(FileSystem.ROOT).get();
                for (String part : parts) {
                    File<?> newCurrent = current.get(ParentFile.GET_CHILD_BY_NAME).invoke(part);

                    // Check whether the current file exists
                    if (newCurrent == null) {
                        // Executor user hasn't rights to create the new file
                        if (!FileUtils.hasRight(executor, current, FileRights.WRITE)) {
                            result = false;
                        }
                        break;
                    } else {
                        // Continue on
                        current = newCurrent;
                    }
                }

                invocation.next(arguments);
                return result;
            }

        });

    }

}
