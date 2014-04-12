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
import com.quartercode.classmod.extra.ExecutorInvocationException;
import com.quartercode.classmod.extra.FunctionDefinition;
import com.quartercode.classmod.extra.FunctionExecutor;
import com.quartercode.classmod.extra.FunctionInvocation;
import com.quartercode.classmod.extra.Prioritized;
import com.quartercode.classmod.extra.PropertyDefinition;
import com.quartercode.classmod.extra.def.ReferenceProperty;
import com.quartercode.disconnected.world.comp.file.FileRights.FileRight;
import com.quartercode.disconnected.world.comp.os.User;

/**
 * The file remove action is a simple file action that defines the process of removing a {@link File} from its file system.
 * For doing that, the action only takes the file to remove and resolves the rest of the required data automatically.<br>
 * <br>
 * See {@link FileAction} for more detail on what file actions actually are.
 * 
 * @see FileAction
 * @see File
 */
public class FileRemoveAction extends DefaultFeatureHolder implements FileAction {

    // ----- Properties -----

    /**
     * The {@link File} that should be removed from the file system it's currently stored on.
     */
    public static final PropertyDefinition<File<ParentFile<?>>> FILE;

    static {

        FILE = ReferenceProperty.createDefinition("file");

    }

    // ----- Properties End -----

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
            @Prioritized (Prioritized.LEVEL_5)
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) throws ExecutorInvocationException {

                FeatureHolder holder = invocation.getHolder();
                File<ParentFile<?>> removeFile = holder.get(FILE).get();

                if (removeFile.getParent() != null) {
                    removeFile.getParent().get(ParentFile.CHILDREN).remove(removeFile);
                } else {
                    throw new IllegalStateException("File for removal is not stored on any file system (parent file == null)");
                }

                return invocation.next(arguments);
            }

        });

        IS_EXECUTABLE_BY.addExecutor("checkRemoveRight", FileRemoveAction.class, new FunctionExecutor<Boolean>() {

            @Override
            public Boolean invoke(FunctionInvocation<Boolean> invocation, Object... arguments) throws ExecutorInvocationException {

                User executor = (User) arguments[0];
                File<ParentFile<?>> removeFile = invocation.getHolder().get(FILE).get();
                boolean result = checkFile(executor, removeFile);

                invocation.next(arguments);
                return result;
            }

            private boolean checkFile(User executor, File<?> file) throws ExecutorInvocationException {

                if (!FileUtils.hasRight(executor, file, FileRight.DELETE)) {
                    return false;
                } else if (file instanceof ParentFile) {
                    for (File<?> childFile : file.get(ParentFile.CHILDREN).get()) {
                        if (!checkFile(executor, childFile)) {
                            return false;
                        }
                    }
                }

                return true;
            }

        });

    }

    // ----- Functions End -----

    /**
     * Creates a new file remove action.
     */
    public FileRemoveAction() {

    }

}
