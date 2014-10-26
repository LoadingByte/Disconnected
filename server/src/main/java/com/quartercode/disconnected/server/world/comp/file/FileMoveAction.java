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
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.reflect.TypeLiteral;
import com.quartercode.classmod.extra.CFeatureHolder;
import com.quartercode.classmod.extra.FunctionDefinition;
import com.quartercode.classmod.extra.FunctionExecutor;
import com.quartercode.classmod.extra.FunctionInvocation;
import com.quartercode.classmod.extra.Prioritized;
import com.quartercode.classmod.extra.PropertyDefinition;
import com.quartercode.classmod.extra.storage.ReferenceStorage;
import com.quartercode.classmod.extra.storage.StandardStorage;
import com.quartercode.disconnected.server.world.comp.os.User;
import com.quartercode.disconnected.shared.comp.file.PathUtils;

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

        FILE_SYSTEM = create(new TypeLiteral<PropertyDefinition<FileSystem>>() {}, "name", "fileSystem", "storage", new ReferenceStorage<>());

        FILE = create(new TypeLiteral<PropertyDefinition<File<ParentFile<?>>>>() {}, "name", "file", "storage", new ReferenceStorage<>());
        FILE.addSetterExecutor("adjustFileSystem", FileMoveAction.class, new FunctionExecutor<Void>() {

            @Override
            @Prioritized (Prioritized.LEVEL_6)
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                CFeatureHolder holder = invocation.getCHolder();

                if (holder.getObj(FILE_SYSTEM) == null) {
                    File<?> file = (File<?>) arguments[0];
                    holder.setObj(FILE_SYSTEM, file.invoke(File.GET_FILE_SYSTEM));
                }

                return invocation.next(arguments);
            }

        });

        PATH = create(new TypeLiteral<PropertyDefinition<String>>() {}, "name", "path", "storage", new StandardStorage<>());
        PATH.addSetterExecutor("normalize", FileMoveAction.class, new FunctionExecutor<Void>() {

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
     * Moves the set {@link #FILE} from the file system it's currently located on.
     * If the file for removal is a {@link ParentFile}, all child files are also going to be removed.
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

                CFeatureHolder holder = invocation.getCHolder();
                FileSystem targetFileSystem = holder.getObj(FILE_SYSTEM);
                File<ParentFile<?>> moveFile = holder.getObj(FILE);
                String targetPath = holder.getObj(PATH);

                // Retrieve the old parent file before the movement
                ParentFile<?> oldParent = moveFile.getParent();

                // Add the file to the target file system under the target path
                targetFileSystem.invoke(FileSystem.CREATE_ADD_FILE, moveFile, targetPath).invoke(FileAddAction.EXECUTE);
                // Retrieve the new parent file after the file was added under the new location
                ParentFile<?> newParent = moveFile.getParent();

                // Manually remove the file from its old parent file
                oldParent.removeCol(ParentFile.CHILDREN, moveFile);
                // Set the new parent file again because the removal automatically setthe parent object to null
                moveFile.setParent(newParent);

                return invocation.next(arguments);
            }

        });

        GET_MISSING_RIGHTS.addExecutor("checkSplitActions", FileMoveAction.class, new FunctionExecutor<Map<File<?>, Character[]>>() {

            @Override
            public Map<File<?>, Character[]> invoke(FunctionInvocation<Map<File<?>, Character[]>> invocation, Object... arguments) {

                User executor = (User) arguments[0];
                CFeatureHolder holder = invocation.getCHolder();

                Map<File<?>, Character[]> missingRights = new HashMap<>();

                FileRemoveAction action1 = new FileRemoveAction();
                action1.setObj(FileRemoveAction.FILE, holder.getObj(FILE));
                missingRights.putAll(action1.invoke(GET_MISSING_RIGHTS, executor));

                FileAddAction action2 = new FileAddAction();
                action2.setObj(FileAddAction.FILE_SYSTEM, holder.getObj(FILE_SYSTEM));
                action2.setObj(FileAddAction.FILE, holder.getObj(FILE));
                action2.setObj(FileAddAction.PATH, holder.getObj(PATH));
                missingRights.putAll(action2.invoke(GET_MISSING_RIGHTS, executor));

                invocation.next(arguments);
                return missingRights;
            }

        });

    }

}
