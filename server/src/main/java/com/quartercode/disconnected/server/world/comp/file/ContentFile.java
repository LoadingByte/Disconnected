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

import javax.xml.bind.annotation.XmlAnyElement;
import com.quartercode.disconnected.server.world.comp.os.config.User;
import com.quartercode.disconnected.server.world.util.SizeUtils;

/**
 * This class represents a content file that is able contain and store any object.
 * Note that the stored object must have a {@link SizeUtils derivable size}.
 *
 * @see File
 * @see FileSystem
 */
public class ContentFile extends File<ParentFile<?>> {

    @XmlAnyElement (lax = true)
    private Object content;

    // JAXB constructor
    protected ContentFile() {

    }

    /**
     * Creates a new content file.
     * Note that the file's {@link #getName() name} will be set as soon as the file is added to a {@link FileSystem}.
     *
     * @param owner The owning user of the content file. Note that it may not be {@code null}.
     *        See {@link #getOwner()} for more details.
     */
    public ContentFile(User owner) {

        super(owner);
    }

    /**
     * Returns the object contained by the content file.
     * Note that the returned object must have a {@link SizeUtils derivable size}.
     *
     * @return The stored content object.
     */
    public Object getContent() {

        return content;
    }

    /**
     * Returns the {@link #getContent() object contained by the content file} as an instance of the given class by casting it.
     * If the content is {@code null} or the cast cannot be performed, {@code null} is returned.
     *
     * @param type The type the file content should be casted to.
     * @return The casted file content object.
     */
    public <T> T getContentAs(Class<T> type) {

        if (content != null && type.isInstance(content)) {
            return type.cast(content);
        } else {
            return null;
        }
    }

    /**
     * Stores a new content object in the content file.
     * Note that the new object must have a {@link SizeUtils derivable size}.
     *
     * @param content The new stored content object.
     * @throws IllegalArgumentException If the size of the new file content object cannot be derived.
     * @throws OutOfSpaceException If there is not enough space for the new file content object on the {@link FileSystem}.
     */
    public void setContent(Object content) throws OutOfSpaceException {

        FileSystem fileSystem = getFileSystem();
        if (fileSystem != null) {
            long oldContentSize = SizeUtils.getSize(this.content);
            long newContentSize = SizeUtils.getSize(content);

            if (newContentSize > fileSystem.getFreeSpace() + oldContentSize) {
                throw new OutOfSpaceException(fileSystem, newContentSize - oldContentSize);
            }
        }

        this.content = content;
    }

    @Override
    public long getSize() {

        return super.getSize() + SizeUtils.getSize(content);
    }

}
