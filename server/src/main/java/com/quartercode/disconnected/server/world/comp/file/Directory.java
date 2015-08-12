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

import com.quartercode.disconnected.server.world.comp.os.config.User;

/**
 * This class represents a directory that is able to contain and hold other {@link File}s.
 * It actually extends the {@link ParentFile} class.
 *
 * @see File
 * @see ParentFile
 * @see FileSystem
 */
public class Directory extends ParentFile<ParentFile<?>> {

    // JAXB constructor
    protected Directory() {

    }

    /**
     * Creates a new directory.
     * Note that the file's {@link #getName() name} will be set as soon as the file is added to a {@link FileSystem}.
     *
     * @param owner The owning user of the directory. Note that it may not be {@code null}.
     *        See {@link #getOwner()} for more details.
     */
    public Directory(User owner) {

        super(owner);
    }

}
