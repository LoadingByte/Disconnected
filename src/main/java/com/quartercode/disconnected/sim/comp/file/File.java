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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlTransient;
import org.apache.commons.lang.Validate;
import com.quartercode.disconnected.sim.comp.file.FileRights.FileRight;
import com.quartercode.disconnected.sim.comp.os.Group;
import com.quartercode.disconnected.sim.comp.os.OperatingSystem;
import com.quartercode.disconnected.sim.comp.os.User;
import com.quartercode.disconnected.sim.comp.program.Process;
import com.quartercode.disconnected.util.InfoString;
import com.quartercode.disconnected.util.size.SizeObject;
import com.quartercode.disconnected.util.size.SizeUtil;

/**
 * This class represents a file on a file system.
 * Every file knows his path and has a content string. Every directory has a list of child files.
 * 
 * @see FileSystem
 */
public class File implements SizeObject, InfoString {

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
     * The path seperator which seperates different files in a path string.
     */
    public static final String SEPERATOR = "/";

    /**
     * Creates an absolute path out of the given one.
     * The algorithm starts at the given start path and changes the path according to the "change" path.
     * The "change" path also can be absolute. This will ignore the start path.
     * 
     * Here's an example:
     * 
     * <pre>
     * Start:  /user/homes/test/
     * Change: ../test2/docs
     * Result: /user/home/test2/docs
     * </pre>
     * 
     * @param start The absolute path the algorithm starts at.
     * @param path The "change" path which defines where the start path should change (see above).
     * @return The resolved absolute path.
     */
    public static String resolvePath(String start, String path) {

        if (!start.startsWith(File.SEPERATOR)) {
            throw new IllegalArgumentException("Start path must be absolute (it has to start with " + File.SEPERATOR + "): " + start);
        } else {
            List<String> current = new ArrayList<String>();
            if (!path.startsWith(File.SEPERATOR)) {
                current.addAll(Arrays.asList(start.split(File.SEPERATOR)));
                // Remove first entry ([this]/...), it's empty
                current.remove(0);
            }

            for (String pathChange : path.split(File.SEPERATOR)) {
                if (!pathChange.equals(".") && !pathChange.isEmpty()) {
                    if (pathChange.equals("..")) {
                        current.remove(current.size() - 1);
                    } else {
                        current.add(pathChange);
                    }
                }
            }

            if (current.isEmpty()) {
                return File.SEPERATOR;
            } else {
                String resolvedPath = "";
                for (String part : current) {
                    resolvedPath += File.SEPERATOR + part;
                }
                return resolvedPath;
            }
        }
    }

    private FileSystem       host;
    private String           name;
    @XmlAttribute
    private FileType         type;
    private FileRights       rights;
    private User             owner;
    private Group            group;

    @XmlElement
    private Object           content;
    @XmlElement (name = "file")
    private final List<File> children = new ArrayList<File>();

    /**
     * Creates a new empty file.
     * This is only recommended for direct field access (e.g. for serialization).
     */
    protected File() {

    }

    /**
     * Creates a new root file for a given file system host.
     * 
     * @param host The host which will use this file.
     */
    protected File(FileSystem host) {

        this.host = host;
        name = "root";
        type = FileType.DIRECTORY;
    }

    /**
     * Creates a new file for the given file system host using the given name and file type.
     * 
     * @param host The host which will use this file.
     * @param name The name the new file will have.
     * @param type The type the new file will have.
     * @param rights The file rights object which stores the UNIX-like file right attributes.
     * @param owner The user who owns the file. This is important for the rights system.
     * @param group The group which partly owns the file. This is important for the rights system.
     */
    protected File(FileSystem host, String name, FileType type, FileRights rights, User owner, Group group) {

        this.host = host;
        this.name = name;
        this.type = type;
        setRights(rights);
        setOwner(owner);
        setGroup(group);
    }

    /**
     * Returns the file system which hosts this file.
     * 
     * @return The file system which hosts this file.
     */
    public FileSystem getHost() {

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
     * Returns the global path the file has on the given operating system.
     * A path is a collection of files seperated by a seperator.
     * The global path is made out of the mountpoint of the file system and the local file path
     * It can be used on the os level.
     * 
     * @return The global path the file has on the given operating system.
     */
    public String getGlobalPath(OperatingSystem operatingSystem) {

        return SEPERATOR + operatingSystem.getFileSystemManager().getMountpoint(host) + SEPERATOR + getLocalPath();
    }

    /**
     * Returns the global path the file has on the hosting operating system.
     * A path is a collection of files seperated by a seperator.
     * The global path is made out of the local mountpoint of the file system and the local file path
     * It can be used on the os level.
     * 
     * @return The path the file has.
     */
    public String getGlobalHostPath() {

        return getGlobalPath(host.getHost().getOperatingSystem());
    }

    /**
     * Returns the local the path the file has.
     * A path is a collection of files seperated by a seperator.
     * The local path can be used on the hardware level to look up a file on a given file system.
     * 
     * @return The path the file has.
     */
    public String getLocalPath() {

        if (equals(host.getRootFile())) {
            return "";
        } else {
            List<File> path = new ArrayList<File>();
            host.getRootFile().generatePathSections(this, path);

            String pathString = "";
            for (File pathEntry : path) {
                pathString += SEPERATOR + pathEntry.getName();
            }

            return pathString.isEmpty() ? null : pathString.substring(1);
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
     * Returns the file rights object which stores the UNIX-like file right attributes.
     * For more documentation on how it works, see the {@link FileRights} class.
     * 
     * @return The file rights storage.
     */
    @XmlAttribute
    public FileRights getRights() {

        return rights;
    }

    /**
     * Changes the file rights storage which stores the UNIX-like file right attributes to a new one.
     * For more documentation on how it works, see the {@link FileRights} class.
     * 
     * @param rights The new file rights storage.
     */
    public void setRights(FileRights rights) {

        Validate.notNull(rights, "A file can't have no right attributes");

        this.rights = rights;
    }

    /**
     * Returns the user who owns the file.
     * This is important for the rights system.
     * 
     * @return The user who owns the file.
     */
    @XmlIDREF
    @XmlAttribute
    public User getOwner() {

        return owner;
    }

    /**
     * Changes the user who owns the file.
     * This is important for the rights system.
     * 
     * @param owner The new owner of the file.
     */
    public void setOwner(User owner) {

        Validate.notNull(owner, "A file can't have no owner");

        this.owner = owner;
    }

    /**
     * Returns the group which partly owns the file.
     * This is important for the rights system.
     * 
     * @return The group which partly owns the file.
     */
    @XmlIDREF
    @XmlAttribute
    public Group getGroup() {

        return group;
    }

    /**
     * Changes the group who partly owns the file to a new one.
     * This is important for the rights system.
     * 
     * @param group The new partly owning group of the file.
     */
    public void setGroup(Group group) {

        this.group = group;
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
     * Returns the content the file has (if this file is a content one).
     * If this file isn't a content one, this will return null.
     * 
     * @param process The process which wants to read from this file.
     * @return The content the file has (if this file is a content one).
     * @throws NoFileRightException The given process hasn't the right to read from this file.
     */
    public Object read(Process process) throws NoFileRightException {

        FileRights.checkRight(process, this, FileRight.READ);
        return getContent();
    }

    /**
     * Changes the content to new one (if this file is a content one).
     * This throws an OutOfSpaceException if there isn't enough space on the host drive for the new content.
     * 
     * @param content The new content to write into the file.
     * @throws IllegalArgumentException Can't derive size type from given content.
     * @throws OutOfSpaceException If there isn't enough space on the host drive for the new content.
     */
    public void setContent(Object content) throws OutOfSpaceException {

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
     * Changes the content to new one (if this file is a content one).
     * This throws an OutOfSpaceException if there isn't enough space on the host drive for the new content.
     * 
     * @param process The process which wants to write to this file.
     * @param content The new content to write into the file.
     * @throws NoFileRightException The given process hasn't the right to write into this file.
     * @throws OutOfSpaceException If there isn't enough space on the host drive for the new content.
     */
    public void write(Process process, Object content) throws NoFileRightException, OutOfSpaceException {

        FileRights.checkRight(process, this, FileRight.WRITE);
        setContent(content);
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
    protected void addChildFile(File file) throws OutOfSpaceException {

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
     * Changes the name of the file to a new one.
     * After the renaming, the file can be used like before.
     * 
     * @param process The process which wants to rename this file.
     * @param name The new name for the file.
     * @throws NoFileRightException The given process hasn't the rights to delete this file and write into the parent directory.
     */
    public void rename(Process process, String name) throws NoFileRightException {

        FileRights.checkRight(process, this, FileRight.DELETE);
        FileRights.checkRight(process, getParent(), FileRight.WRITE);
        rename(name);
    }

    /**
     * Moves the file to a new location under the given path.
     * After the movement, the file can be used like before.
     * This throws an OutOfSpaceException if there isn't enough space on the new host drive for the file.
     * 
     * @param path The new location for the file.
     * @throws OutOfSpaceException If there isn't enough space on the new host drive for the file.
     */
    public void move(String path) throws OutOfSpaceException {

        try {
            move(null, path);
        }
        catch (NoFileRightException e) {
            // Wont ever happen
        }
    }

    /**
     * Moves the file to a new location under the given path.
     * After the movement, the file can be used like before.
     * This throws an OutOfSpaceException if there isn't enough space on the new host drive for the file.
     * 
     * @param process The process which wants to move this file.
     * @param path The new location for the file.
     * @throws NoFileRightException The given process hasn't the right to delete the file or write into a directory where the algorithm needs to write.
     * @throws OutOfSpaceException If there isn't enough space on the new host drive for the file.
     */
    public void move(Process process, String path) throws NoFileRightException, OutOfSpaceException {

        if (process != null) {
            FileRights.checkRight(process, this, FileRight.DELETE);
        }

        File oldParent = getParent();
        path = File.resolvePath(getGlobalHostPath(), path);
        host = host.getHost().getOperatingSystem().getFileSystemManager().getMounted(path);
        host.addFile(process, this, path.substring(path.indexOf(SEPERATOR, 1)));
        oldParent.removeChildFile(this);
    }

    /**
     * Removes this file from the file system.
     * If this file is a directory, all child files will also be removed.
     */
    public void remove() {

        getParent().removeChildFile(this);
    }

    /**
     * Removes this file from the file system.
     * If this file is a directory, all child files will also be removed.
     * 
     * @param process The process which wants to remove the file.
     * @throws NoFileRightException The process hasn't the right to delete this file.
     */
    public void remove(Process process) throws NoFileRightException {

        FileRights.checkRight(process, this, FileRight.DELETE);
        remove();
    }

    /**
     * Changes the current hosting file system of this file to a new one.
     * 
     * @param host The new file system which will host this file.
     */
    protected void changeHost(FileSystem host) {

        this.host = host;

        if (children != null) {
            for (File child : children) {
                child.changeHost(host);
            }
        }
    }

    /**
     * Returns the unique serialization id for the file.
     * The id is a combination of the host file system's id and the local path of the file.
     * It should only be used by a serialization algorithm.
     * 
     * @return The unique serialization id for the file.
     */
    @XmlAttribute
    @XmlID
    protected String getId() {

        return host.getId() + "-" + getLocalPath();
    }

    protected void beforeUnmarshal(Unmarshaller unmarshaller, Object parent) {

        if (parent instanceof FileSystem) {
            host = (FileSystem) parent;
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
        result = prime * result + (group == null ? 0 : group.hashCode());
        result = prime * result + (name == null ? 0 : name.hashCode());
        result = prime * result + (owner == null ? 0 : owner.hashCode());
        result = prime * result + (rights == null ? 0 : rights.hashCode());
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
        if (group == null) {
            if (other.group != null) {
                return false;
            }
        } else if (!group.equals(other.group)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (owner == null) {
            if (other.owner != null) {
                return false;
            }
        } else if (!owner.equals(other.owner)) {
            return false;
        }
        if (rights == null) {
            if (other.rights != null) {
                return false;
            }
        } else if (!rights.equals(other.rights)) {
            return false;
        }
        if (type != other.type) {
            return false;
        }
        return true;
    }

    @Override
    public String toInfoString() {

        return type.name().toLowerCase() + " " + name + ", " + rights.toString() + " (o " + owner.getName() + ", g " + group.getName() + "), " + children.size() + " child files";
    }

    @Override
    public String toString() {

        return getClass().getName() + " [" + toInfoString() + "]";
    }

}
