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

import org.apache.commons.lang3.tuple.Triple;
import com.quartercode.disconnected.server.world.comp.file.FileSystemModule.KnownFileSystem;
import com.quartercode.disconnected.server.world.comp.os.Group;
import com.quartercode.disconnected.server.world.comp.os.User;
import com.quartercode.disconnected.shared.util.PathUtils;
import com.quartercode.disconnected.shared.world.comp.file.FilePlaceholder;
import com.quartercode.disconnected.shared.world.comp.file.FileRights;

/**
 * This file utility contains methods related to {@link File}s and {@link FileSystem}s.
 * 
 * @see File
 * @see FileSystem
 */
public class FileUtils {

    /**
     * Returns if the given {@link User} has the given {@link FileRight} on the given {@link File}.
     * 
     * @param user The {@link User} who may have the given {@link FileRight} on the given {@link File}.
     * @param file The {@link File} the given {@link User} may have access to.
     * @param right The {@link FileRight} the given {@link User} may have.
     * @return True if the given {@link User} has the given {@link FileRight} on the given {@link File}.
     */
    public static boolean hasRight(User user, File<?> file, char right) {

        if (user == null || user.invoke(User.IS_SUPERUSER)) {
            return true;
        } else if (file instanceof RootFile) {
            // Only superusers (filtered out by the previous check) can add files to the root file
            return false;
        } else if (checkRight(file, FileRights.OWNER, right) && file.getObj(File.OWNER).equals(user)) {
            return true;
        } else if (checkRight(file, FileRights.GROUP, right) && user.getCol(User.GROUPS).contains(file.getObj(File.GROUP))) {
            return true;
        } else if (checkRight(file, FileRights.OTHERS, right)) {
            return true;
        }

        return false;
    }

    private static boolean checkRight(File<?> file, char accessor, char right) {

        return file.getObj(File.RIGHTS).isRightSet(accessor, right);
    }

    /**
     * Returns if the given {@link User} can change the {@link FileRights} attributes of the given {@link File}.
     * 
     * @param user The {@link User} who may can change the {@link FileRights} attributes.
     * @param file The {@link File} the given {@link User} may have access to.
     * @return True if the given {@link User} can change the {@link FileRights} attributes of the given {@link File}.
     */
    public static boolean canChangeRights(User user, File<?> file) {

        return file.getObj(File.OWNER).equals(user) || user.invoke(User.IS_SUPERUSER);
    }

    /**
     * Creates a new {@link FilePlaceholder} that represents the given {@link KnownFileSystem}.
     * 
     * @param fileSystem The known file system that should be represented by the placeholder.
     * @return The new file placeholder.
     */
    public static FilePlaceholder createFilePlaceholder(KnownFileSystem fileSystem) {

        FileSystem actualFs = fileSystem.getObj(KnownFileSystem.FILE_SYSTEM);
        RootFile root = actualFs.getObj(FileSystem.ROOT);

        String path = PathUtils.SEPARATOR + fileSystem.getObj(KnownFileSystem.MOUNTPOINT);
        String type = StringFileTypeMapper.classToString(RootFile.class);
        long size = actualFs.invoke(FileSystem.GET_SIZE);
        Triple<FileRights, String, String> commonData = getCommonFilePlaceholderData(root);

        return new FilePlaceholder(path, type, size, commonData.getLeft(), commonData.getMiddle(), commonData.getRight());
    }

    /**
     * Creates a new {@link FilePlaceholder} that represents the given {@link File} which is stored on a file system that is mounted under the given mountpoint.
     * 
     * @param fileSystemMountpoint The mountpoint of the file system the file is stored on.
     * @param file The file that should be represented by the placeholder.
     * @return The new file placeholder.
     */
    public static FilePlaceholder createFilePlaceholder(String fileSystemMountpoint, File<?> file) {

        String path = PathUtils.resolve(PathUtils.normalize(fileSystemMountpoint), file.invoke(File.GET_PATH));

        String type = StringFileTypeMapper.classToString(file.getClass());
        long size = file.invoke(File.GET_SIZE);
        Triple<FileRights, String, String> commonData = getCommonFilePlaceholderData(file);

        return new FilePlaceholder(path, type, size, commonData.getLeft(), commonData.getMiddle(), commonData.getRight());
    }

    private static Triple<FileRights, String, String> getCommonFilePlaceholderData(File<?> file) {

        FileRights rights = file.getObj(File.RIGHTS);

        User ownerObject = file.getObj(File.OWNER);
        String owner = ownerObject == null ? null : ownerObject.getObj(User.NAME);

        Group groupObject = file.getObj(File.GROUP);
        String group = groupObject == null ? null : groupObject.getObj(Group.NAME);

        return Triple.of(rights, owner, group);
    }

    private FileUtils() {

    }

}
