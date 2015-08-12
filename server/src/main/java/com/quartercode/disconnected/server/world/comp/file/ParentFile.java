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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlElementRef;
import com.quartercode.disconnected.server.world.comp.os.config.User;
import com.quartercode.disconnected.server.world.util.SizeUtils;
import com.quartercode.jtimber.api.node.Node;
import com.quartercode.jtimber.api.node.wrapper.SubstituteWithWrapper;
import com.quartercode.jtimber.api.node.wrapper.collection.ListWrapper;

/**
 * This class represents a parent file that is able to contain and hold other {@link File}s.
 * An example of an actual parent file is the {@link Directory}.
 *
 * @param <P> The type of {@link Node}s that are able to be the parent of this parent file.
 *        Apart from the special case {@link RootFile}, this parameter should always reference another file class.
 * @see File
 * @see FileSystem
 */
public abstract class ParentFile<P extends Node<?>> extends File<P> {

    @XmlElementRef
    @SubstituteWithWrapper (ListWrapper.class)
    private final List<File<ParentFile<?>>> childFiles = new ArrayList<>();

    // JAXB constructor
    protected ParentFile() {

    }

    /**
     * Creates a new parent file.
     * Note that the file's {@link #getName() name} will be set as soon as the file is added to a {@link FileSystem}.
     *
     * @param owner The owning user of the parent file. Note that it may not be {@code null}.
     *        See {@link #getOwner()} for more details.
     */
    protected ParentFile(User owner) {

        super(owner);
    }

    /**
     * Returns the child {@link File}s the parent file contains.
     * For example, a {@link Directory} would store the files it holds in this list.
     *
     * @return The child files of the directory.
     */
    public List<File<ParentFile<?>>> getChildFiles() {

        return Collections.unmodifiableList(childFiles);
    }

    /**
     * Returns the {@link #getChildFiles() child file} which has the given name.
     *
     * @param name The name of the child {@link File} to return.
     * @return The child file with the given name.
     */
    public File<ParentFile<?>> getChildFileByName(String name) {

        for (File<ParentFile<?>> child : childFiles) {
            if (child.getName().equals(name)) {
                return child;
            }
        }

        return null;
    }

    /**
     * Adds the given {@link #getChildFiles() child file} to the parent file.
     * This operation can be compared with adding a {@link File} to a {@link Directory}.
     *
     * @param childFile The new child file which should be added to the parent file.
     * @throws OutOfSpaceException If there is not enough space for the new child files on the {@link FileSystem}.
     */
    public void addChildFile(File<ParentFile<?>> childFile) throws OutOfSpaceException {

        FileSystem fileSystem = getFileSystem();
        if (fileSystem != null) {
            long childFileSize = childFile.getSize();
            if (childFileSize > fileSystem.getFreeSpace()) {
                throw new OutOfSpaceException(fileSystem, childFileSize);
            }
        }

        childFiles.add(childFile);
    }

    /**
     * Removes the given {@link #getChildFiles() child file} from the parent file.
     * This operation can be compared with removing a {@link File} from a {@link Directory}.
     *
     * @param childFile The child file which should be removed from the parent file.
     */
    public void removeChildFile(File<ParentFile<?>> childFile) {

        childFiles.remove(childFile);
    }

    @Override
    public long getSize() {

        return super.getSize() + SizeUtils.getSize(childFiles);
    }

}
