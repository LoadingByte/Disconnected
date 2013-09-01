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

package com.quartercode.disconnected.sim.comp.media;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import com.quartercode.disconnected.sim.comp.Computer;
import com.quartercode.disconnected.sim.comp.media.File.FileType;

/**
 * This class represents a media of a computer.
 * The media has a letter (e.g. "C") and stores files which can be accessed like regular files.
 * 
 * @see File
 */
public class Media implements MediaProvider {

    @XmlIDREF
    @XmlAttribute
    private Computer host;
    @XmlElement
    private long     size;

    private char     letter;
    @XmlElement
    private File     rootFile;

    /**
     * Creates a new empty media.
     * This is only recommended for direct field access (e.g. for serialization).
     */
    protected Media() {

    }

    /**
     * Creates a new media and sets the host computer and the size in bytes.
     * 
     * @param host The computer this media is hosted on.
     * @param size The size of the media, given in bytes.
     */
    public Media(Computer host, long size) {

        this.host = host;
        this.size = size;

        rootFile = new File(this);
    }

    @Override
    public Media resolveMedia() {

        return this;
    }

    @Override
    public Computer getHost() {

        return host;
    }

    @Override
    public long getSize() {

        return size;
    }

    @Override
    public char getLetter() {

        return letter;
    }

    @Override
    public void setLetter(char letter) {

        this.letter = letter;

        if (rootFile != null) {
            rootFile.resolveId();
        }
    }

    @Override
    public File getRootFile() {

        return rootFile;
    }

    @Override
    public File getFile(String path) {

        String[] parts = path.split(File.SEPERATOR);

        File current = rootFile;
        for (String part : parts) {
            if (!part.isEmpty()) {
                current = current.getChildFile(part);
                if (current == null) {
                    break;
                }
            }
        }

        return current;
    }

    @Override
    public File addFile(String path, FileType type) {

        String[] parts = path.split(File.SEPERATOR);
        File file = new File(this, parts[parts.length - 1], type);
        addFile(file, path);
        file.resolveId();
        return file;
    }

    /**
     * Adds an existing file to the media.
     * If the given path doesn't exist, this creates a new one.
     * 
     * @param file The existing file object to add to the media.
     * @param path The path the file will be located under.
     */
    protected void addFile(File file, String path) {

        String[] parts = path.split(File.SEPERATOR);

        File current = rootFile;
        for (int counter = 0; counter < parts.length; counter++) {
            String part = parts[counter];
            if (!part.isEmpty()) {
                if (current.getChildFile(part) == null) {
                    if (counter == parts.length - 1) {
                        current.addChildFile(file);
                        file.setName(part);
                    } else {
                        File dir = new File(this, part, FileType.DIRECTORY);
                        current.addChildFile(dir);
                        dir.resolveId();
                    }
                }
                current = current.getChildFile(part);
            }
        }
    }

    @Override
    public long getFilled() {

        return getFilled(rootFile);
    }

    private long getFilled(File file) {

        long filled = file.getSize();
        if (file.getChildFiles() != null) {
            for (File childFile : file.getChildFiles()) {
                filled += getFilled(childFile);
            }
        }
        return filled;
    }

    @Override
    public long getFree() {

        return size - getFilled();
    }

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = prime * result + letter;
        result = prime * result + (rootFile == null ? 0 : rootFile.hashCode());
        result = prime * result + (int) (size ^ size >>> 32);
        return result;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Media other = (Media) obj;
        if (letter != other.letter) {
            return false;
        }
        if (rootFile == null) {
            if (other.rootFile != null) {
                return false;
            }
        } else if (!rootFile.equals(other.rootFile)) {
            return false;
        }
        if (size != other.size) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {

        return getClass().getName() + " [" + size + " bytes size, drive letter " + letter + ", " + getFilled() + " bytes filled]";
    }

}
