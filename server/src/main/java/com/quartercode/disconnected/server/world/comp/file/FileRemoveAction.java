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

import static com.quartercode.classmod.factory.ClassmodFactory.factory;
import java.util.HashMap;
import java.util.Map;
import com.quartercode.classmod.extra.conv.CFeatureHolder;
import com.quartercode.classmod.extra.func.FunctionDefinition;
import com.quartercode.classmod.extra.func.FunctionExecutor;
import com.quartercode.classmod.extra.func.FunctionInvocation;
import com.quartercode.classmod.extra.prop.PropertyDefinition;
import com.quartercode.classmod.extra.storage.ReferenceStorage;
import com.quartercode.classmod.factory.PropertyDefinitionFactory;
import com.quartercode.disconnected.server.world.comp.os.user.User;
import com.quartercode.disconnected.shared.world.comp.file.FileRights;

/**
 * The file remove action is a simple file action that defines the process of removing a {@link File} from its file system.
 * For doing that, the action only takes the file to remove and resolves the rest of the required data automatically.<br>
 * <br>
 * See {@link FileAction} for more detail on what file actions actually are.
 * 
 * @see FileAction
 * @see File
 */
public class FileRemoveAction extends FileAction {

    // ----- Properties -----

    /**
     * The {@link File} that should be removed from the file system it's currently stored on.
     */
    public static final PropertyDefinition<File<ParentFile<?>>> FILE;

    static {

        FILE = factory(PropertyDefinitionFactory.class).create("file", new ReferenceStorage<>());

    }

    // ----- Functions -----

    /**
     * Removes the set {@link #FILE} from the file system it's currently located on.
     * If the file for removal is a {@link ParentFile}, all child files are also going to be removed.
     * 
     * <table>
     * <tr>
     * <th>Exception</th>
     * <th>When?</th>
     * </tr>
     * <tr>
     * <td>{@link IllegalStateException}</td>
     * <td>The file for removal is not stored on any file system (it has no parent file).</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>                EXECUTE = FileAction.EXECUTE;

    static {

        EXECUTE.addExecutor("removeFile", FileRemoveAction.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                CFeatureHolder holder = invocation.getCHolder();
                File<ParentFile<?>> removeFile = holder.getObj(FILE);

                if (removeFile.getParent() != null) {
                    removeFile.getParent().removeFromColl(ParentFile.CHILDREN, removeFile);
                } else {
                    throw new IllegalStateException("File for removal is not stored on any file system (parent file == null)");
                }

                return invocation.next(arguments);
            }

        });

        GET_MISSING_RIGHTS.addExecutor("checkForDeleteRight", FileRemoveAction.class, new FunctionExecutor<Map<File<?>, Character[]>>() {

            @Override
            public Map<File<?>, Character[]> invoke(FunctionInvocation<Map<File<?>, Character[]>> invocation, Object... arguments) {

                User executor = (User) arguments[0];
                File<ParentFile<?>> removeFile = invocation.getCHolder().getObj(FILE);

                Map<File<?>, Character[]> missingRights = new HashMap<>();
                checkFile(executor, removeFile, missingRights);

                invocation.next(arguments);
                return missingRights;
            }

            private void checkFile(User executor, File<?> file, Map<File<?>, Character[]> target) {

                if (!file.invoke(File.HAS_RIGHT, executor, FileRights.DELETE)) {
                    target.put(file, new Character[] { FileRights.DELETE });
                }

                if (file instanceof ParentFile) {
                    for (File<?> childFile : file.getColl(ParentFile.CHILDREN)) {
                        checkFile(executor, childFile, target);
                    }
                }
            }

        });

    }

}
