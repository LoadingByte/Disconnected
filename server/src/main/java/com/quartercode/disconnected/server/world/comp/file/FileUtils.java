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

import org.apache.commons.lang3.tuple.Triple;
import com.quartercode.disconnected.server.registry.ServerRegistries;
import com.quartercode.disconnected.server.world.comp.file.FileSystemModule.KnownFileSystem;
import com.quartercode.disconnected.shared.util.registry.Registries;
import com.quartercode.disconnected.shared.world.comp.file.FilePlaceholder;
import com.quartercode.disconnected.shared.world.comp.file.FileRights;
import com.quartercode.disconnected.shared.world.comp.file.PathUtils;

/**
 * This file utility contains methods related to {@link File}s and {@link FileSystem}s.
 *
 * @see File
 * @see FileSystem
 */
public class FileUtils {

    /**
     * Creates a new {@link FilePlaceholder} that represents the given {@link KnownFileSystem}.
     *
     * @param knownFs The known file system that should be represented by the placeholder.
     * @return The new file placeholder.
     */
    public static FilePlaceholder createFilePlaceholder(KnownFileSystem knownFs) {

        FileSystem fileSystem = knownFs.getFileSystem();
        RootFile root = fileSystem.getRootFile();

        String path = PathUtils.SEPARATOR + knownFs.getMountpoint();
        String type = Registries.get(ServerRegistries.FILE_TYPES).getLeft(RootFile.class);
        long size = fileSystem.getSize();
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

        String path = PathUtils.resolve(PathUtils.normalize(fileSystemMountpoint), file.getPath());

        String type = Registries.get(ServerRegistries.FILE_TYPES).getLeft(file.getClass());
        long size = file.getSize();
        Triple<FileRights, String, String> commonData = getCommonFilePlaceholderData(file);

        return new FilePlaceholder(path, type, size, commonData.getLeft(), commonData.getMiddle(), commonData.getRight());
    }

    private static Triple<FileRights, String, String> getCommonFilePlaceholderData(File<?> file) {

        String owner = file.getOwner() == null ? null : file.getOwner().getName();
        return Triple.of(file.getRights(), owner, file.getGroup());
    }

    private FileUtils() {

    }

}
