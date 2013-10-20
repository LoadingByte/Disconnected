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

package com.quartercode.disconnected.sim.comp.file;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlTransient;
import org.apache.commons.lang.Validate;
import com.quartercode.disconnected.util.size.SizeObject;
import com.quartercode.disconnected.util.size.SizeUtil;

/**
 * This class represents a file on a media.
 * Every file knows his path and has a content string. Every directory has a list of child files.
 * 
 * @see Media
 */
public class File implements SizeObject {

    /**
     * The file type represents if a file is a content file or a directory.
     */
    public static enum FileType {

        /**
         * A normal file which can hold an object as its content.
         */
        FILE,
        /**
         * A folder which can have child files.
         */
        DIRECTORY;
    }

    /**
     * The seperator which seperates different files in a path string.
     */
    public static final String SEPERATOR = "/";

    @XmlID
    @XmlAttribute
    private String             id;

    private Media              host;
    private String             name;
    @XmlAttribute
    private FileType           type;
    @XmlElement
    private Object             content;
    @XmlElement (name = "child")
    private final List<File>   children  = new ArrayList<File>();

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
    @XmlAttribute
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

        if (equals(host.getRootFile())) {
            return SEPERATOR;
        } else {
            List<File> path = new ArrayList<File>();
            host.getRootFile().generatePathSections(this, path);

            String pathString = "";
            for (File pathEntry : path) {
                pathString += SEPERATOR + pathEntry.getName();
            }

            return pathString.isEmpty() ? null : pathString;
        }
    }

    private boolean generatePathSections(File target, List<File> path) {

        for (File child : children) {
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
    @XmlTransient
    public Object getContent() {

        return type == FileType.FILE ? content : null;
    }

    /**
     * Changes the content to new one (if this file is a content one).
     * This throws an OutOfSpaceException if there isn't enough space on the host drive for the new content.
     * 
     * @param content The new content to write into the file.
     * @throws IllegalArgumentException Can't derive size type from given content.
     * @throws OutOfSpaceException If there isn't enough space on the host drive for the new content.
     */
    public void setContent(Object content) {

        if (type == FileType.FILE) {
            Validate.isTrue(SizeUtil.accept(content), "Size of type " + content.getClass().getName() + " can't be derived");

            Object oldContent = this.content;
            this.content = content;

            if (host.getRootFile() != null && host.getFilled() > host.getFree()) {
                long size = getSize();
                this.content = oldContent;
                throw new OutOfSpaceException(host, size);
            }
        }
    }

    /**
     * Returns the size this file has in bytes (if this file is a content one).
     * Directories have the size of all their children.
     * 
     * @return The size this file has in bytes (if this file is a content one).
     */
    @Override
    public long getSize() {

        if (type == FileType.FILE && content != null) {
            return SizeUtil.getSize(content);
        } else if (type == FileType.DIRECTORY && !children.isEmpty()) {
            long size = 0;
            for (File child : children) {
                size += child.getSize();
            }
            return size;
        } else {
            return 0;
        }
    }

    /**
     * Returns the child files the directory contains (if this file is a directory).
     * If this file isn't a directory, this will return null.
     * 
     * @return The child files the directory contains (if this file is a directory).
     */
    public List<File> getChildFiles() {

        return type == FileType.DIRECTORY ? Collections.unmodifiableList(children) : null;
    }

    /**
     * Looks up the child file with the given name (if this file is a directory).
     * If this file isn't a directory, this will return null.
     * 
     * @param name The name to look for.
     * @return The child file with the given name (if this file is a directory).
     */
    public File getChildFile(String name) {

        if (children != null) {
            for (File child : children) {
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

        if (children != null) {
            if (!children.contains(file)) {
                if (file.getSize() > host.getFree()) {
                    throw new OutOfSpaceException(host, file.getSize());
                } else {
                    children.add(file);
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

        if (children != null) {
            children.remove(file);
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

    /**
     * Resolves the unique serialization id. This should only be called if the host is set.
     */
    protected void resolveId() {

        id = host.getHost().getId() + "-" + getGlobalPath();

        if (children != null) {
            for (File child : children) {
                child.resolveId();
            }
        }
    }

    /**
     * Changes the current hosting media of this file to a new one.
     * 
     * @param host The new media which will host this file.
     */
    protected void changeHost(Media host) {

        this.host = host;

        if (children != null) {
            for (File child : children) {
                child.changeHost(host);
            }
        }
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
        result = prime * result + (children == null ? 0 : children.hashCode());
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
        if (children == null) {
            if (other.children != null) {
                return false;
            }
        } else if (!children.equals(other.children)) {
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
        for (File child : children) {
            childNames.add(child.getName());
        }
        return getClass().getName() + " [name=" + name + ", type=" + type + ", content=" + content + ", children=" + childNames + "]";
    }

}
