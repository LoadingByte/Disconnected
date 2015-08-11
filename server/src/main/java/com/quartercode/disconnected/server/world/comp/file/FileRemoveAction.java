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
import com.quartercode.disconnected.shared.world.comp.file.FileRights;
import com.quartercode.jtimber.api.node.Weak;

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

    @Weak
    private final File<ParentFile<?>> file;

    /**
     * Creates a new file remove action.
     *
     * @param file The {@link File} that should be removed from the {@link FileSystem} it's currently stored on.
     */
    public FileRemoveAction(File<ParentFile<?>> file) {

        Validate.notNull(file, "Cannot use null file for file remove action");

        this.file = file;
    }

    /**
     * Returns the {@link File} that should be removed from the {@link FileSystem} it's currently stored on.
     *
     * @return The file that should be removed.
     */
    public File<ParentFile<?>> getFile() {

        return file;
    }

    /**
     *
     * Removes the set {@link #getFile()} from the file system it's currently stored on.
     * If the file for removal is a {@link ParentFile}, all child files are also going to be removed.<br>
     * <br>
     * Note that no right checks or anything like that are done by this method.
     * If you need such permission checks, use {@link #isExecutableBy(User)} or {@link #getMissingRights(User)}.
     *
     * @throws IllegalStateException If the file for removal is not stored on any file system (true if it has no parent file).
     */
    @Override
    public void execute() {

        Validate.validState(file.getSingleParent() != null, "File for removal is not stored on any file system (parent file is null)");
        file.getSingleParent().removeChildFile(file);
    }

    @Override
    public Map<File<?>, Character[]> getMissingRights(User user) {

        Map<File<?>, Character[]> missingRights = new HashMap<>();
        checkFile(user, file, missingRights);

        return missingRights;
    }

    private void checkFile(User user, File<?> file, Map<File<?>, Character[]> target) {

        if (!file.hasRight(user, FileRights.DELETE)) {
            target.put(file, new Character[] { FileRights.DELETE });
        }

        if (file instanceof ParentFile) {
            for (File<?> childFile : ((ParentFile<?>) file).getChildFiles()) {
                checkFile(user, childFile, target);
            }
        }
    }

}
