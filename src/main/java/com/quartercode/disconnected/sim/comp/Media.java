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

package com.quartercode.disconnected.sim.comp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlTransient;
import com.quartercode.disconnected.sim.comp.Media.File.FileType;

/**
 * This class represents a media of a computer.
 * The media has a letter (e.g. "C") and stores files which can be accessed like regular files.
 * 
 * @see File
 */
@XmlAccessorType (XmlAccessType.FIELD)
public class Media implements MediaProvider {

    @XmlIDREF
    private Computer host;
    private long     size;

    private char     letter;
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
        return addFile(new File(this, parts[parts.length - 1], type), path);
    }

    private File addFile(File file, String path) {

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
                        current.addChildFile(new File(this, part, FileType.DIRECTORY));
                    }
                }
                current = current.getChildFile(part);
            }
        }

        return current;
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

        return "Media [size=" + size + ", letter=" + letter + ", getFilled()=" + getFilled() + "]";
    }

    /**
     * This class represents a file on a media.
     * Every file knows his path and has a content string. Every directory has a list of child files.
     * 
     * @see Media
     */
    @XmlAccessorType (XmlAccessType.FIELD)
    public static class File implements Serializable {

        /**
         * The file type represents if a file is a content file or a directory.
         */
        public static enum FileType {

            FILE, DIRECTORY;
        }

        /**
         * The seperator which seperates different files in a path string.
         */
        public static final String SEPERATOR        = "/";

        private static final long  serialVersionUID = 1L;

        @XmlTransient
        private Media              host;
        @XmlAttribute
        private String             name;
        @XmlAttribute
        private FileType           type;
        private String             content;
        @XmlElement (name = "child")
        private final List<File>   childs           = new ArrayList<File>();

        /**
         * Creates a new empty file.
         * This is only recommended for direct field access (e.g. for serialization).
         */
        protected File() {

        }

        /**
         * Creates a new root file for a given media host.
         * 
         * @param host The host which will use this file.
         */
        protected File(Media host) {

            this.host = host;
            name = "root";
            type = FileType.DIRECTORY;
        }

        /**
         * Creates a new file for the given media host using the given name and file type.
         * 
         * @param host The host which will use this file.
         * @param name The name the new file will have.
         * @param type The type the new file will have.
         */
        protected File(Media host, String name, FileType type) {

            this.host = host;
            this.name = name;
            this.type = type;
        }

        /**
         * Returns the media which hosts this file.
         * 
         * @return The media which hosts this file.
         */
        public Media getHost() {

            return host;
        }

        /**
         * Returns the name the file has.
         * 
         * @return The name the file has.
         */
        public String getName() {

            return name;
        }

        /**
         * Changes the name of the file.
         * 
         * @param name The new name for the file.
         */
        protected void setName(String name) {

            this.name = name;
        }

        /**
         * Returns the global path the file has.
         * A path is a collection of files seperated by a seperator.
         * The global path also contains the drive letter and can be used on the os level.
         * 
         * @return The path the file has.
         */
        public String getGlobalPath() {

            return host.getLetter() + ":" + getLocalPath();
        }

        /**
         * Returns the local the path the file has.
         * A path is a collection of files seperated by a seperator.
         * The local path can be used on the hardware level to look up a file on a given hard drive.
         * 
         * @return The path the file has.
         */
        public String getLocalPath() {

            List<File> path = new ArrayList<File>();
            host.getRootFile().generatePathSections(this, path);

            String pathString = "";
            for (File pathEntry : path) {
                pathString += SEPERATOR + pathEntry.getName();
            }

            return pathString.isEmpty() ? null : pathString;
        }

        private boolean generatePathSections(File target, List<File> path) {

            for (File child : childs) {
                path.add(child);
                if (target.equals(child) || child.generatePathSections(target, path)) {
                    return true;
                } else {
                    path.remove(child);
                }
            }

            return false;
        }

        /**
         * Returns the type the file has.
         * The type sets if a file is a content one or a directory.
         * 
         * @return The type the file has.
         */
        public FileType getType() {

            return type;
        }

        /**
         * Returns the content the file has (if this file is a content one).
         * If this file isn't a content one, this will return null.
         * 
         * @return The content the file has (if this file is a content one).
         */
        public String getContent() {

            return type == FileType.FILE ? content == null ? "" : content : null;
        }

        /**
         * Changes the content to new one (if this file is a content one).
         * This throws an OutOfSpaceException if there isn't enough space on the host drive for the new content.
         * 
         * @param content The new content to write into the file.
         * @throws OutOfSpaceException If there isn't enough space on the host drive for the new content.
         */
        public void setContent(String content) {

            if (type == FileType.FILE) {
                String oldContent = this.content;
                this.content = content == null ? "" : content;

                if (host.getFilled() > host.getFree()) {
                    long size = getSize();
                    this.content = oldContent;
                    throw new OutOfSpaceException(host, size);
                }
            }
        }

        /**
         * Returns the size this file has in bytes (if this file is a content one).
         * Directories don't have a size.
         * 
         * @return The size this file has in bytes (if this file is a content one).
         */
        public long getSize() {

            return type == FileType.FILE ? (content == null ? "" : content).length() : 0;
        }

        /**
         * Returns the child files the directory contains (if this file is a directory).
         * If this file isn't a directory, this will return null.
         * 
         * @return The child files the directory contains (if this file is a directory).
         */
        public List<File> getChildFiles() {

            return type == FileType.DIRECTORY ? Collections.unmodifiableList(childs) : null;
        }

        /**
         * Looks up the child file with the given name (if this file is a directory).
         * If this file isn't a directory, this will return null.
         * 
         * @param name The name to look for.
         * @return The child file with the given name (if this file is a directory).
         */
        public File getChildFile(String name) {

            if (childs != null) {
                for (File child : childs) {
                    if (child.getName().equals(name)) {
                        return child;
                    }
                }
            }

            return null;
        }

        /**
         * Adds a new child file to this directory (if this file is a directory).
         * If this file isn't a directory, nothign will happen.
         * This throws an OutOfSpaceException if there isn't enough space on the host drive for the new file.
         * 
         * @param file The file to add to this directory.
         * @throws OutOfSpaceException If there isn't enough space on the host drive for the new file.
         */
        protected void addChildFile(File file) {

            if (childs != null) {
                if (!childs.contains(file)) {
                    if (file.getSize() > host.getFree()) {
                        throw new OutOfSpaceException(host, file.getSize());
                    } else {
                        childs.add(file);
                    }
                }
            }
        }

        /**
         * Removes a child file from this directory (if this file is a directory).
         * If this file isn't a directory, nothign will happen.
         * 
         * @param file The file to remove from this directory.
         */
        protected void removeChildFile(File file) {

            if (childs != null) {
                childs.remove(file);
            }
        }

        /**
         * Returns the parent directory which contains this file.
         * 
         * @return The parent directory which contains this file.
         */
        public File getParent() {

            String path = getLocalPath();
            return host.getFile(path.substring(0, path.lastIndexOf(SEPERATOR)));
        }

        /**
         * Changes the name of the file to a new one.
         * After the renaming, the file can be used like before.
         * 
         * @param name The new name for the file.
         */
        public void rename(String name) {

            setName(name);
        }

        /**
         * Moves the file to a new location under the given path.
         * After the movement, the file can be used like before.
         * This throws an OutOfSpaceException if there isn't enough space on the new host drive for the file.
         * 
         * @param path The new location for the file.
         * @throws OutOfSpaceException If there isn't enough space on the new host drive for the file.
         */
        public void move(String path) {

            remove();

            if (path.contains(":")) {
                host = host.getHost().getOperatingSystem().getMedia(path).resolveMedia();
                host.addFile(this, path.split(":")[1]);
            } else {
                host.addFile(this, path);
            }
        }

        /**
         * Removes this file from the hard drive.
         * If this file is a directory, all child files will also be removed.
         */
        public void remove() {

            getParent().removeChildFile(this);
        }

        public void beforeUnmarshal(Unmarshaller unmarshaller, Object parent) {

            if (parent instanceof Media) {
                host = (Media) parent;
            } else {
                host = ((File) parent).getHost();
            }
        }

        @Override
        public int hashCode() {

            final int prime = 31;
            int result = 1;
            result = prime * result + (childs == null ? 0 : childs.hashCode());
            result = prime * result + (content == null ? 0 : content.hashCode());
            result = prime * result + (name == null ? 0 : name.hashCode());
            result = prime * result + (type == null ? 0 : type.hashCode());
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
            File other = (File) obj;
            if (childs == null) {
                if (other.childs != null) {
                    return false;
                }
            } else if (!childs.equals(other.childs)) {
                return false;
            }
            if (content == null) {
                if (other.content != null) {
                    return false;
                }
            } else if (!content.equals(other.content)) {
                return false;
            }
            if (name == null) {
                if (other.name != null) {
                    return false;
                }
            } else if (!name.equals(other.name)) {
                return false;
            }
            if (type != other.type) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {

            List<String> childNames = new ArrayList<String>();
            for (File child : childs) {
                childNames.add(child.getName());
            }
            return getClass().getName() + " [name=" + name + ", type=" + type + ", content=" + content + ", childs=" + childNames + "]";
        }

    }

    /**
     * This runtime exception occures if there is not enough space on a hard drive for handling some new bytes (e.g. from a file).
     */
    public static class OutOfSpaceException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        private final Media       host;
        private final long        size;

        /**
         * Creates a new out of space exception and sets the host which should have handled the new bytes and the amount of new bytes.
         * 
         * @param host The hard drive host which should have handled the new bytes,
         * @param size The amount of new bytes.
         */
        public OutOfSpaceException(Media host, long size) {

            super("Out of space on " + host.getLetter() + ": " + host.getFilled() + "b/" + host.getSize() + "b filled, can't handle " + size + "b");
            this.host = host;
            this.size = size;
        }

        /**
         * Returns the hard drive host which should have handled the new bytes,
         * 
         * @return The hard drive host which should have handled the new bytes,
         */
        public Media getHost() {

            return host;
        }

        /**
         * Returns the amount of new bytes.
         * 
         * @return The amount of new bytes.
         */
        public long getSize() {

            return size;
        }

    }

}
