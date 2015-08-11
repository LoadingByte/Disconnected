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

package com.quartercode.disconnected.server.world.comp.hardware;

import javax.xml.bind.annotation.XmlElement;
import org.apache.commons.lang3.Validate;
import com.quartercode.disconnected.server.world.comp.file.File;
import com.quartercode.disconnected.server.world.comp.file.FileSystem;
import com.quartercode.disconnected.server.world.comp.hardware.Mainboard.NeedsMainboardSlot;

/**
 * This class represents a hard drive of a computer.
 * It references a {@link FileSystem} which stores {@link File}s that can be accessed like regular files on a real computer.
 *
 * @see Hardware
 * @see FileSystem
 */
@NeedsMainboardSlot
public class HardDrive extends Hardware {

    @XmlElement
    private FileSystem fileSystem;

    // JAXB constructor
    protected HardDrive() {

    }

    /**
     * Creates a new hard drive.
     *
     * @param name The "model" name of the new hard drive.
     *        See {@link #getName()} for more details.
     * @param fileSystem The {@link FileSystem} the hard drive should contain.
     */
    public HardDrive(String name, FileSystem fileSystem) {

        super(name);

        Validate.notNull(fileSystem, "Cannot use null as hard drive file system");

        this.fileSystem = fileSystem;
    }

    /**
     * Returns the {@link FileSystem} contained by the hard drive.
     * It has been set on construction.
     *
     * @return The file system of the hard drive.
     */
    public FileSystem getFileSystem() {

        return fileSystem;
    }

}
