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

package com.quartercode.disconnected.shared.world.comp.file;

import java.io.Serializable;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * A file placeholder represents a file object by storing commonly used data about it.
 * File systems are represented by their root files.
 */
public class FilePlaceholder implements Serializable {

    private static final long  serialVersionUID = -6387807415977338194L;

    private final String       path;
    private final String       type;
    private final long         size;
    private final FileRights   rights;
    private final String       owner;
    private final String       group;

    private transient String[] pathArray;

    /**
     * Creates a new file placeholder using the provided data.
     * 
     * @param path The path of the represented file.
     * @param type The string that describes the type of the represented file.
     *        By default, possible strings are {@code rootFile}, {@code directory}, and {@code contentFile}.
     * @param size The total size of the represented file.
     *        If the placeholder represents a file system, the size of that file system should be used.
     * @param rights A {@link FileRights} object that stores the file rights of the represented file.
     * @param owner The name of the user that owns the represented file.
     * @param group The name of the group that is assigned to the represented file.
     */
    public FilePlaceholder(String path, String type, long size, FileRights rights, String owner, String group) {

        Validate.notNull(path, "File placeholder path cannot be null");
        Validate.notNull(type, "File placeholder type cannot be null");
        Validate.isTrue(size >= 0, "File placeholder size must be >= 0");
        Validate.notNull(rights, "File placeholder rights cannot be null");

        this.path = path;
        this.type = type;
        this.size = size;
        this.rights = rights;
        this.owner = owner;
        this.group = group;
    }

    private String[] getPathArray() {

        if (pathArray == null) {
            pathArray = PathUtils.split(path);
        }

        return pathArray;
    }

    /**
     * Returns whether the placeholder represents a file system.
     * This returns true if {@link #getPath()} only has one segment.
     * 
     * @return Whether a file system is represented.
     */
    public boolean isFileSystem() {

        return getPathArray().length == 1;
    }

    /**
     * Returns the path of the represented file.
     * 
     * @return The file path.
     * @see #getFileSystemMountpoint()
     * @see #getName()
     */
    public String getPath() {

        return path;
    }

    /**
     * Returns the mountpoint of the file system the file file is stored on.
     * It is derived from the first segment of the path ({@link #getPath()}).
     * 
     * @return The file system mountpoint.
     */
    public String getFileSystemMountpoint() {

        return getPathArray()[0];
    }

    /**
     * Returns the name of the represented file.
     * It is derived from the last element of the path ({@link #getPath()}).
     * 
     * @return The file name.
     */
    public String getName() {

        return getPathArray()[getPathArray().length - 1];
    }

    /**
     * Returns a string that describes the type of the represented file.
     * By default, possible types are:
     * 
     * <ul>
     * <li>rootFile</li>
     * <li>directory</li>
     * <li>contentFile</li>
     * </ul>
     * 
     * @return The file type.
     */
    public String getType() {

        return type;
    }

    /**
     * Returns the total size of the represented file.
     * If the placeholder represents a file system, the size of that file system is used.
     * 
     * @return The file/file system size.
     */
    public long getSize() {

        return size;
    }

    /**
     * Returns a {@link FileRights} object that stores the file rights of the represented file.
     * 
     * @return The file rights.
     */
    public FileRights getRights() {

        return rights;
    }

    /**
     * Returns the name of the user which owns the represented file.
     * 
     * @return The name of the file owner.
     */
    public String getOwner() {

        return owner;
    }

    /**
     * Returns The name of the group which is assigned to the represented file.
     * 
     * @return The name of the file group.
     */
    public String getGroup() {

        return group;
    }

    @Override
    public int hashCode() {

        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(Object obj) {

        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public String toString() {

        return ToStringBuilder.reflectionToString(this);
    }

}
