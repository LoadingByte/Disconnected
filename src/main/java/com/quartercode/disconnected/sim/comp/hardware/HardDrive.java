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

package com.quartercode.disconnected.sim.comp.hardware;

import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import com.quartercode.disconnected.sim.comp.Computer;
import com.quartercode.disconnected.sim.comp.Version;
import com.quartercode.disconnected.sim.comp.Vulnerability;
import com.quartercode.disconnected.sim.comp.file.File;
import com.quartercode.disconnected.sim.comp.file.FileSystem;
import com.quartercode.disconnected.sim.comp.file.FileSystemProvider;
import com.quartercode.disconnected.sim.comp.hardware.Mainboard.NeedsMainboardSlot;
import com.quartercode.disconnected.util.InfoString;

/**
 * This class represents a hard drive of a computer.
 * A hard drive only has it's size stored (given in bytes).
 * The hard drive stores files which can be accessed like regular files.
 * 
 * @see Hardware
 * @see File
 */
@NeedsMainboardSlot
public class HardDrive extends Hardware implements FileSystemProvider, InfoString {

    @XmlElement
    private FileSystem fileSystem;

    /**
     * Creates a new empty hard drive.
     * This is only recommended for direct field access (e.g. for serialization).
     */
    protected HardDrive() {

    }

    /**
     * Creates a new hard drive and sets the host computer, the name, the version, the vulnerabilities and the size.
     * 
     * @param host The host computer this part is built in.
     * @param name The name the hard drive has.
     * @param version The current version the hard drive has.
     * @param vulnerabilities The vulnerabilities the hard drive has.
     * @param size The size of the hard drive module, given in bytes.
     */
    public HardDrive(Computer host, String name, Version version, List<Vulnerability> vulnerabilities, long size) {

        super(host, name, version, vulnerabilities);

        fileSystem = new FileSystem(host, size);
    }

    @Override
    public FileSystem getFileSystem() {

        return fileSystem;
    }

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (fileSystem == null ? 0 : fileSystem.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        HardDrive other = (HardDrive) obj;
        if (fileSystem == null) {
            if (other.fileSystem != null) {
                return false;
            }
        } else if (!fileSystem.equals(other.fileSystem)) {
            return false;
        }
        return true;
    }

    @Override
    public String toInfoString() {

        return super.toInfoString() + ", fs " + fileSystem.toInfoString();
    }

    @Override
    public String toString() {

        return getClass().getName() + " [" + toInfoString() + "]";
    }

}
