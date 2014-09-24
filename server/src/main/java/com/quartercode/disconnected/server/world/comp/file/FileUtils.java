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
import com.quartercode.disconnected.server.world.comp.file.FileRights.FileAccessor;
import com.quartercode.disconnected.server.world.comp.file.FileRights.FileRight;
import com.quartercode.disconnected.server.world.comp.file.FileSystemModule.KnownFileSystem;
import com.quartercode.disconnected.server.world.comp.os.Group;
import com.quartercode.disconnected.server.world.comp.os.User;
import com.quartercode.disconnected.shared.event.util.FilePlaceholder;
import com.quartercode.disconnected.shared.util.PathUtils;

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

    /**
     * Creates a new {@link FilePlaceholder} that represents the given {@link KnownFileSystem}.
     * 
     * @param fileSystem The known file system that should be represented by the placeholder.
     * @return The new file placeholder.
     */
    public static FilePlaceholder createFilePlaceholder(KnownFileSystem fileSystem) {

        FileSystem actualFs = fileSystem.get(KnownFileSystem.FILE_SYSTEM).get();
        RootFile root = actualFs.get(FileSystem.ROOT).get();

        String path = PathUtils.SEPARATOR + fileSystem.get(KnownFileSystem.MOUNTPOINT).get();
        String type = StringFileTypeMapper.classToString(RootFile.class);
        long size = actualFs.get(FileSystem.GET_SIZE).invoke();
        Triple<String, String, String> commonData = getCommonFilePlaceholderData(root);

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

        String path = PathUtils.resolve(PathUtils.normalize(fileSystemMountpoint), file.get(File.GET_PATH).invoke());

        String type = StringFileTypeMapper.classToString(file.getClass());
        long size = file.get(File.GET_SIZE).invoke();
        Triple<String, String, String> commonData = getCommonFilePlaceholderData(file);

        return new FilePlaceholder(path, type, size, commonData.getLeft(), commonData.getMiddle(), commonData.getRight());
    }

    private static Triple<String, String, String> getCommonFilePlaceholderData(File<?> file) {

        String rights = file.get(File.RIGHTS).get().get(FileRights.TO_STRING).invoke();

        User ownerObject = file.get(File.OWNER).get();
        String owner = ownerObject == null ? null : ownerObject.get(User.NAME).get();

        Group groupObject = file.get(File.GROUP).get();
        String group = groupObject == null ? null : groupObject.get(Group.NAME).get();

        return Triple.of(rights, owner, group);
    }

    private FileUtils() {

    }

}
