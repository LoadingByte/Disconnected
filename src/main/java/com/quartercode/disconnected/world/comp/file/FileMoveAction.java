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

import com.quartercode.classmod.base.FeatureHolder;
import com.quartercode.classmod.base.def.DefaultFeatureHolder;
import com.quartercode.classmod.extra.FunctionDefinition;
import com.quartercode.classmod.extra.FunctionExecutor;
import com.quartercode.classmod.extra.FunctionInvocation;
import com.quartercode.classmod.extra.Prioritized;
import com.quartercode.classmod.extra.PropertyDefinition;
import com.quartercode.classmod.extra.def.ObjectProperty;
import com.quartercode.classmod.extra.def.ReferenceProperty;
import com.quartercode.disconnected.world.comp.os.User;

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
public class FileMoveAction extends DefaultFeatureHolder implements FileAction {

    // ----- Properties -----

    /**
     * The {@link FileSystem} where the set {@link #FILE} should be moved to.
     * The target {@link #PATH} is related to that file system.<br>
     * Note that this property is automatically adjusted to the current file system when setting {@link #FILE}.
     * However, the current value of the property must be {@code null} in order for that to happen.
     */
    public static final PropertyDefinition<FileSystem>          FILE_SYSTEM;

    /**
     * The {@link File} that should be moved to the set {@link #PATH}.
     * The name of the file is changed to the last entry of the path on execution.<br>
     * Note that setting this property automatically adjusts the {@link #FILE_SYSTEM} to the current system of the new file.
     * However, the current value of the file system property must be {@code null} in order for that to happen.
     */
    public static final PropertyDefinition<File<ParentFile<?>>> FILE;

    /**
     * The path on the {@link #FILE_SYSTEM} where the set {@link #FILE} should be moved to.
     * Any directories in this path that do not yet exist are created on execution.
     * The name of the set {@link #FILE} is also changed to the last entry of this path.
     */
    public static final PropertyDefinition<String>              PATH;

    static {

        FILE_SYSTEM = ReferenceProperty.createDefinition("fileSystem");

        FILE = ReferenceProperty.createDefinition("file");
        FILE.addSetterExecutor("adjustFileSystem", FileMoveAction.class, new FunctionExecutor<Void>() {

            @Override
            @Prioritized (Prioritized.LEVEL_6)
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                FeatureHolder holder = invocation.getHolder();

                if (holder.get(FILE_SYSTEM).get() == null) {
                    File<?> file = (File<?>) arguments[0];
                    holder.get(FILE_SYSTEM).set(file.get(File.GET_FILE_SYSTEM).invoke());
                }

                return invocation.next(arguments);
            }

        });

        PATH = ObjectProperty.createDefinition("path");
        PATH.addSetterExecutor("normalize", FileMoveAction.class, new FunctionExecutor<Void>() {

            @Override
            @Prioritized (Prioritized.LEVEL_6)
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                String normalizedPath = FileUtils.normalizePath((String) arguments[0]);
                if (!normalizedPath.isEmpty()) {
                    normalizedPath = normalizedPath.substring(1);
                }
                return invocation.next(normalizedPath);
            }

        });

    }

    // ----- Functions -----

    /**
     * Moves the set {@link #FILE} from the file system it's currently located on.
     * If the file for removal is a {@link ParentFile}, all child files are also going to be removed.
     * 
     * <table>
     * <tr>
     * <th>Exception</th>
     * <th>When?</th>
     * </tr>
     * <tr>
     * <td>{@link IllegalArgumentException}</td>
     * <td>The set file path isn't valid (for example, a file along the path is not a parent file).</td>
     * </tr>
     * <tr>
     * <td>{@link OutOfSpaceException}</td>
     * <td>There is not enough space for the file or a required directory on the new file system.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>                EXECUTE = FileAction.EXECUTE;

    static {

        EXECUTE.addExecutor("moveFile", FileMoveAction.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                FeatureHolder holder = invocation.getHolder();
                FileSystem targetFileSystem = holder.get(FILE_SYSTEM).get();
                File<ParentFile<?>> moveFile = holder.get(FILE).get();
                String targetPath = holder.get(PATH).get();

                // Retrieve the old parent file before the movement
                ParentFile<?> oldParent = moveFile.getParent();

                // Add the file to the target file system under the target path
                targetFileSystem.get(FileSystem.CREATE_ADD_FILE).invoke(moveFile, targetPath).get(FileAddAction.EXECUTE).invoke();
                // Retrieve the new parent file after the file was added under the new location
                ParentFile<?> newParent = moveFile.getParent();

                // Manually remove the file from its old parent file
                oldParent.get(ParentFile.CHILDREN).remove(moveFile);
                // Set the new parent file again because the removal automatically setthe parent object to null
                moveFile.setParent(newParent);

                return invocation.next(arguments);
            }

        });

        IS_EXECUTABLE_BY.addExecutor("checkSplitActions", FileMoveAction.class, new FunctionExecutor<Boolean>() {

            @Override
            public Boolean invoke(FunctionInvocation<Boolean> invocation, Object... arguments) {

                User executor = (User) arguments[0];
                boolean result = true;
                FeatureHolder holder = invocation.getHolder();

                FileRemoveAction action1 = new FileRemoveAction();
                action1.get(FileRemoveAction.FILE).set(holder.get(FILE).get());
                if (!action1.get(FileAction.IS_EXECUTABLE_BY).invoke(executor)) {
                    result = false;
                }

                FileAddAction action2 = new FileAddAction();
                action2.get(FileAddAction.FILE_SYSTEM).set(holder.get(FILE_SYSTEM).get());
                action2.get(FileAddAction.FILE).set(holder.get(FILE).get());
                action2.get(FileAddAction.PATH).set(holder.get(PATH).get());
                if (!action2.get(FileAction.IS_EXECUTABLE_BY).invoke(executor)) {
                    result = false;
                }

                invocation.next(arguments);
                return result;
            }

        });

    }

    /**
     * Creates a new file move action.
     */
    public FileMoveAction() {

    }

}
