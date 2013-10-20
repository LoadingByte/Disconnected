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
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import com.quartercode.disconnected.sim.comp.Computer;
import com.quartercode.disconnected.sim.comp.Version;
import com.quartercode.disconnected.sim.comp.Vulnerability;
import com.quartercode.disconnected.sim.comp.file.File;
import com.quartercode.disconnected.sim.comp.file.File.FileType;
import com.quartercode.disconnected.sim.comp.file.Media;
import com.quartercode.disconnected.sim.comp.file.MediaProvider;
import com.quartercode.disconnected.sim.comp.hardware.Mainboard.NeedsMainboardSlot;

/**
 * This class represents a hard drive of a computer.
 * A hard drive only has it's size stored (given in bytes).
 * The hard drive has a letter (e.g. "C") and stores files which can be accessed like regular files.
 * 
 * @see Hardware
 * @see File
 */
@XmlAccessorType (XmlAccessType.FIELD)
@NeedsMainboardSlot
public class HardDrive extends Hardware implements MediaProvider {

    private static final long serialVersionUID = 1L;

    private Media             media;

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

        media = new Media(host, size);
    }

    @Override
    public Media resolveMedia() {

        return media;
    }

    @Override
    public long getSize() {

        return media.getSize();
    }

    @Override
    public char getLetter() {

        return media.getLetter();
    }

    @Override
    public void setLetter(char letter) {

        media.setLetter(letter);
    }

    @Override
    public File getRootFile() {

        return media.getRootFile();
    }

    @Override
    public File getFile(String path) {

        return media.getFile(path);
    }

    @Override
    public File addFile(String path, FileType type) {

        return media.addFile(path, type);
    }

    @Override
    public long getFilled() {

        return media.getFilled();
    }

    @Override
    public long getFree() {

        return media.getFree();
    }

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (media == null ? 0 : media.hashCode());
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
        if (media == null) {
            if (other.media != null) {
                return false;
            }
        } else if (!media.equals(other.media)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {

        return getClass().getName() + " [" + toInfoString() + ", media: " + media + "]";
    }

}
