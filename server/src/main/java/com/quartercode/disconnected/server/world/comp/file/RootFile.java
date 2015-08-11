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

import com.quartercode.disconnected.server.world.comp.user.User;
import com.quartercode.disconnected.server.world.util.SizeUtils;

/**
 * This class represents the root file of a {@link FileSystem}.
 * Every {@link File} branches off a root file somehow.
 *
 * @see FileSystem
 * @see File
 */
public class RootFile extends ParentFile<FileSystem> {

    /**
     * Creates a new root file.
     * This constructor should only be used by a {@link FileSystem}.
     */
    protected RootFile() {

    }

    @Override
    public String getName() {

        return "root";
    }

    @Override
    public void setName(String name) {

        throw new UnsupportedOperationException("Cannot change the name of a root file");
    }

    @Override
    public String getPath() {

        return "";
    }

    @Override
    public FileSystem getFileSystem() {

        // A root file should only be stored by one file system
        return getSingleParent();
    }

    @Override
    public boolean hasRight(User user, char right) {

        // Only superusers are allowed to manipulate files on the root level
        return user == null || user.isSuperuser();
    }

    @Override
    public long getSize() {

        // The root file only has the size of its children; its metadata (name etc.) doesn't count because the file is only virtual
        return SizeUtils.getSize(getChildFiles());
    }

    @Override
    public FileMoveAction prepareMove(String path) {

        throw new UnsupportedOperationException("Cannot move the root file");
    }

    @Override
    public FileMoveAction prepareMove(String path, FileSystem fileSystem) {

        throw new UnsupportedOperationException("Cannot move the root file");
    }

    @Override
    public FileRemoveAction prepareRemove() {

        throw new UnsupportedOperationException("Cannot remove the root file");
    }

}
