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

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import com.quartercode.disconnected.sim.comp.Computer;
import com.quartercode.disconnected.sim.comp.file.File.FileType;
import com.quartercode.disconnected.sim.comp.file.FileRights.FileRight;
import com.quartercode.disconnected.sim.comp.os.User;
import com.quartercode.disconnected.sim.comp.program.Process;
import com.quartercode.disconnected.util.InfoString;
import com.quartercode.disconnected.util.size.SizeObject;

/**
 * This class represents a file system.
 * The system stores files which can be accessed like regular file objects.
 * A file system can be virtual or physical.
 * 
 * @see File
 */
public class FileSystem implements SizeObject, InfoString {

    @XmlIDREF
    @XmlAttribute
    private Computer host;
    @XmlElement
    private long     size;

    @XmlElement (name = "file")
    private File     rootFile;

    /**
     * Creates a new empty file system.
     * This is only recommended for direct field access (e.g. for serialization).
     */
    protected FileSystem() {

    }

    /**
     * Creates a new file system and sets the hosting computer and the size in bytes.
     * 
     * @param host The computer this media is hosted on.
     * @param size The size of the media, given in bytes.
     */
    public FileSystem(Computer host, long size) {

        this.host = host;
        this.size = size;

        rootFile = new File(this);
    }

    /**
     * Returns the computer this media is hosted on.
     * This will only return the sotring computer, not any accessing computers.
     * 
     * @return The computer this media is hosted to.
     */
    public Computer getHost() {

        return host;
    }

    /**
     * Returns the size of the media, given in bytes.
     * 
     * @return The size of the media, given in bytes.
     */
    @Override
    public long getSize() {

        return size;
    }

    /**
     * Returns the root file which every other file path branches of.
     * 
     * @return The root file which every other file path branches of.
     */
    public File getRootFile() {

        return rootFile;
    }

    /**
     * Returns the file which is stored on the media under the given path.
     * A path is a collection of files seperated by a seperator.
     * This will look up the file using a local media path.
     * 
     * @param path The path to look in for the file.
     * @return The file which is stored on the media under the given path.
     */
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

    /**
     * Creates a new file using the given path and type on this media and returns it.
     * If the file already exists, the existing file will be returned.
     * A path is a collection of files seperated by a seperator.
     * This will get the file location using a local media path.
     * 
     * @param path The path the new file will be located under.
     * @param type The file type the new file should has.
     * @param user The user who owns the new file.
     * @return The new file (or the existing one, if the file already exists).
     */
    public File addFile(String path, FileType type, User user) {

        try {
            return addFile(null, path, type, user);
        }
        catch (NoFileRightException e) {
            // Wont ever happen
            return null;
        }
    }

    /**
     * Creates a new file using the given path and type on this file system and returns it.
     * If the file already exists, the existing file will be returned.
     * A path is a collection of files seperated by a seperator.
     * This will get the file location using a local file system path.
     * 
     * @param process The process which wants to add the file.
     * @param path The path the new file will be located under.
     * @param type The file type the new file should has.
     * @param user The user who owns the new file.
     * @return The new file (or the existing one, if the file already exists).
     * @throws NoFileRightException The given process hasn't the right to write into a directory where the algorithm needs to write
     */
    public File addFile(Process process, String path, FileType type, User user) throws NoFileRightException {

        String[] parts = path.split(File.SEPERATOR);
        File file = new File(this, parts[parts.length - 1], type, new FileRights("rwd-r---r---"), user, user.getPrimaryGroup());
        addFile(process, file, path);
        return file;
    }

    /**
     * Adds an existing file to the media.
     * If the given path doesn't exist, this creates a new one.
     * 
     * @param file The existing file object to add to the media.
     * @param path The path the file will be located under.
     * @throws IllegalStateException There's a non-directory file in the path.
     */
    protected void addFile(File file, String path) {

        try {
            addFile(null, file, path);
        }
        catch (NoFileRightException e) {
            // Wont ever happen
        }
    }

    /**
     * Adds an existing file to the file system.
     * If the given path doesn't exist, this creates a new one.
     * 
     * @param process The process which wants to add the given file.
     * @param file The existing file object to add to the file system.
     * @param path The path the file will be located under.
     * @throws NoFileRightException The given process hasn't the right to write into a directory where the algorithm needs to write
     * @throws IllegalStateException There's a non-directory file in the path.
     */
    protected void addFile(Process process, File file, String path) throws NoFileRightException {

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
                        if (process != null) {
                            FileRights.checkRight(process, current, FileRight.WRITE);
                        }
                        File dir = new File(this, part, FileType.DIRECTORY, new FileRights("rwd-r---r---"), file.getOwner(), file.getGroup());
                        current.addChildFile(dir);
                    }
                } else if (current.getChildFile(part).getType() != FileType.DIRECTORY) {
                    throw new IllegalStateException("File path '" + path + " isn't valid: File '" + current.getChildFile(part).getLocalPath() + "' isn't a directory");
                }
                current = current.getChildFile(part);
            }
        }
    }

    /**
     * Returns the total amount of bytes which are occupied by files.
     * 
     * @return The total amount of bytes which are occupied by files.
     */
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

    /**
     * Returns the total amount of bytes which are not occupied by any files.
     * 
     * @return The total amount of bytes which are not occupied by any files.
     */
    public long getFree() {

        return size - getFilled();
    }

    /**
     * Returns the unique serialization id for the file system.
     * The id is the identy hash code of the fs object.
     * 
     * @return The unique serialization id for the file system.
     */
    @XmlAttribute
    @XmlID
    protected String getId() {

        return Integer.toHexString(System.identityHashCode(this));
    }

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
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
        FileSystem other = (FileSystem) obj;
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
    public String toInfoString() {

        return getFilled() + "/" + size + "b filled, host computer " + host.getId();
    }

    @Override
    public String toString() {

        return getClass().getName() + " [" + toInfoString() + "]";
    }

}
