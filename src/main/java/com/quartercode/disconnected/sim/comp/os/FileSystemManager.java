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

package com.quartercode.disconnected.sim.comp.os;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;
import org.apache.commons.lang.Validate;
import com.quartercode.disconnected.sim.comp.file.File;
import com.quartercode.disconnected.sim.comp.file.File.FileType;
import com.quartercode.disconnected.sim.comp.file.FileSystem;
import com.quartercode.disconnected.sim.comp.file.FileSystemProvider;
import com.quartercode.disconnected.sim.comp.file.MountException;
import com.quartercode.disconnected.util.InfoString;

/**
 * The file system manager is a subclass the {@link OperatingSystem} uses for holding and modifing file systems.
 * This class only gets used by the {@link OperatingSystem}.
 * 
 * @see FileSystem
 * @see OperatingSystem
 */
public class FileSystemManager implements InfoString {

    private OperatingSystem         host;

    @XmlElementWrapper (name = "mountedFileSystems")
    @XmlElement (name = "fileSystem")
    private List<MountedFileSystem> mountedFileSystems;

    /**
     * Creates a new empty file system manager.
     * This is only recommended for direct field access (e.g. for serialization).
     */
    protected FileSystemManager() {

    }

    /**
     * Creates a new file system manager and sets the host the manager is running for.
     * 
     * @param host The {@link OperatingSystem} this file system manager is running for.
     */
    public FileSystemManager(OperatingSystem host) {

        this.host = host;

        mountedFileSystems = new ArrayList<MountedFileSystem>();
    }

    /**
     * Returns the {@link OperatingSystem} this file system manager is running for.
     * 
     * @return The {@link OperatingSystem} this file system manager is running for.
     */
    public OperatingSystem getHost() {

        return host;
    }

    /**
     * Returns a list containing all avaiable file systems which are connected to this computer.
     * This uses different resources to collect the file systems.
     * 
     * @return A list containing all avaiable file systems which are connected to this computer.
     */
    public List<FileSystem> getAvaiable() {

        List<FileSystem> fileSystems = new ArrayList<FileSystem>();
        for (FileSystemProvider provider : host.getHost().getHardware(FileSystemProvider.class)) {
            fileSystems.add(provider.getFileSystem());
        }
        return fileSystems;
    }

    /**
     * Returns a list containing all mounted file systems which is sorted by the mountpoints.
     * A file system can be mounted and associated with a mountpoint (e.g. "C").
     * Only mounted file systems can be used.
     * 
     * @return A list containing all mounted file systems.
     */
    public List<FileSystem> getMounted() {

        Map<Character, FileSystem> bindings = new TreeMap<Character, FileSystem>();
        for (MountedFileSystem mountedFileSystem : mountedFileSystems) {
            bindings.put(mountedFileSystem.getMountpoint(), mountedFileSystem.getFileSystem());
        }
        return new ArrayList<FileSystem>(bindings.values());
    }

    /**
     * Returns the mounted file system which uses the given mountpoint.
     * If there is no file system using the given mountpoint, this will return null.
     * 
     * @param mountpoint The mountpoint the returned file system needs to use.
     * @return The mounted file system which is using the given mountpoint.
     */
    public FileSystem getMounted(char mountpoint) {

        for (MountedFileSystem mountedFileSystem : mountedFileSystems) {
            if (mountedFileSystem.getMountpoint() == mountpoint) {
                return mountedFileSystem.getFileSystem();
            }
        }

        return null;
    }

    /**
     * Returns the mounted file system which holds the file which is stored under the given path.
     * A file system can be mounted and associated with a mountpoint (e.g. "C"). Only mounted file systems can be used.
     * A path is a collection of files seperated by a seperator. This requires a global os path.
     * If there is no file system the file is stored on, this will return null.
     * 
     * @param path The file represented by this path is stored on the returned file system.
     * @return The mounted file system which holds the file which is stored under the given path.
     */
    public FileSystem getMounted(String path) {

        if (path.contains(":")) {
            return getMounted(path.split(":")[0].charAt(0));
        } else {
            return null;
        }
    }

    /**
     * Returns the mountpoint of the given file system.
     * A file system can be mounted and associated with a mountpoint (e.g. "C").
     * Only mounted file systems can be used.
     * 
     * @param fileSystem The file system which is associated with the returned mountpoint.
     * @return The mountpoint of the given file system.
     */
    public char getMountpoint(FileSystem fileSystem) {

        for (MountedFileSystem mountedFileSystem : mountedFileSystems) {
            if (mountedFileSystem.getFileSystem().equals(fileSystem)) {
                return mountedFileSystem.getMountpoint();
            }
        }
        return '-';
    }

    /**
     * Tries to mount the given file system and binding it to the given mountpoint.
     * 
     * @param fileSystem The file system to mount to the os.
     * @param mountpoint The mountpoint to bind the file system to.
     * @throws MountException Somethign goes wrong while mounting the file system.
     */
    public void mount(FileSystem fileSystem, char mountpoint) {

        Validate.notNull(mountpoint, "Mountpoint can't be null");
        if (getMounted().contains(fileSystem)) {
            throw new MountException(fileSystem, true, "File system already mounted");
        } else {
            mountedFileSystems.add(new MountedFileSystem(fileSystem, mountpoint));
        }
    }

    /**
     * Tries to unmount the given file system from the computer.
     * 
     * @param fileSystem The file system to unmount from the os.
     * @throws MountException Somethign goes wrong while unmounting the file system.
     */
    public void unmount(FileSystem fileSystem) {

        if (!getMounted().contains(fileSystem)) {
            throw new MountException(fileSystem, false, "File system not mounted");
        } else {
            mountedFileSystems.remove(fileSystem);
        }
    }

    /**
     * Returns the file which is stored on a mounted file system under the given path.
     * A path is a collection of files seperated by a seperator.
     * This will look up the file using a global os path.
     * 
     * @param path The path the returned file is stored under.
     * @return The file which is stored on a mounted file system under the given path.
     */
    public File getFile(String path) {

        FileSystem fileSystem = getMounted(path);
        if (fileSystem != null) {
            return fileSystem.getFile(path.split(":")[1]);
        } else {
            return null;
        }
    }

    /**
     * Creates a new file using the given path and type on the associated file system mounted on this computer and returns it.
     * If the file already exists, the existing file will be returned.
     * A path is a collection of files seperated by a seperator.
     * This will create the file location using a global os path.
     * 
     * @param path The path the new file will be located under.
     * @param type The file type the new file should has.
     * @param user The user who owns the new file.
     * @return The new file (or the existing one, if the file already exists).
     */
    public File addFile(String path, FileType type, User user) {

        FileSystem fileSystem = getMounted(path);
        if (fileSystem != null) {
            return fileSystem.addFile(path.split(":")[1], type, user);
        } else {
            return null;
        }
    }

    /**
     * Changes the running state of the file system manager.
     * 
     * @param running True if the file system manager is running, false if not.
     */
    public void setRunning(boolean running) {

        if (running) {
            // Mount every avaiable file system (temp)
            for (FileSystem fileSystem : getAvaiable()) {
                mount(fileSystem, (char) ('C' + getMounted().size()));
            }
        } else {
            mountedFileSystems.clear();
        }
    }

    public void beforeUnmarshal(Unmarshaller unmarshaller, Object parent) {

        host = (OperatingSystem) parent;
    }

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = prime * result + (mountedFileSystems == null ? 0 : mountedFileSystems.hashCode());
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
        FileSystemManager other = (FileSystemManager) obj;
        if (mountedFileSystems == null) {
            if (other.mountedFileSystems != null) {
                return false;
            }
        } else if (!mountedFileSystems.equals(other.mountedFileSystems)) {
            return false;
        }
        return true;
    }

    @Override
    public String toInfoString() {

        return getMounted().size() + " mounted";
    }

    @Override
    public String toString() {

        return getClass().getName() + " [" + toInfoString() + "]";
    }

    /**
     * The mounted file system represents a file system which is mounted into an os.
     * This is used in a datastructure of the os class to store the mountpoints of file systems.
     * 
     * @see FileSystem
     */
    protected static class MountedFileSystem implements InfoString {

        @XmlIDREF
        @XmlAttribute (name = "name")
        private FileSystem fileSystem;
        @XmlAttribute
        private char       mountpoint;

        /**
         * Creates a new empty mounted file system representation object.
         * This is only recommended for direct field access (e.g. for serialization).
         */
        public MountedFileSystem() {

        }

        /**
         * Creates a new mounted file system representation object and sets the file system and the mountpoint.
         * 
         * @param fileSystem The file system which is represented by this ds object.
         * @param mountpoint The mountpoint the given file system is using.
         */
        public MountedFileSystem(FileSystem fileSystem, char mountpoint) {

            this.fileSystem = fileSystem;
            this.mountpoint = mountpoint;
        }

        /**
         * Returns the file system which is represented by this ds object.
         * 
         * @return The file system which is represented by this ds object.
         */
        public FileSystem getFileSystem() {

            return fileSystem;
        }

        /**
         * Returns the mountpoint the represented file system is using.
         * 
         * @return The mountpoint the represented file system is using.
         */
        public char getMountpoint() {

            return mountpoint;
        }

        @Override
        public int hashCode() {

            final int prime = 31;
            int result = 1;
            result = prime * result + (fileSystem == null ? 0 : fileSystem.hashCode());
            result = prime * result + mountpoint;
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
            MountedFileSystem other = (MountedFileSystem) obj;
            if (fileSystem == null) {
                if (other.fileSystem != null) {
                    return false;
                }
            } else if (!fileSystem.equals(other.fileSystem)) {
                return false;
            }
            if (mountpoint != other.mountpoint) {
                return false;
            }
            return true;
        }

        @Override
        public String toInfoString() {

            return fileSystem.toInfoString() + ", mounted on " + mountpoint;
        }

        @Override
        public String toString() {

            return getClass().getName() + " [" + toInfoString() + "]";
        }
    }

}
