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
import javax.xml.bind.annotation.XmlIDREF;
import com.quartercode.disconnected.sim.comp.file.File;
import com.quartercode.disconnected.sim.comp.file.File.FileType;
import com.quartercode.disconnected.sim.comp.file.FileSystem;
import com.quartercode.disconnected.sim.comp.file.FileSystemProvider;
import com.quartercode.disconnected.sim.comp.file.MountException;
import com.quartercode.disconnected.sim.comp.file.NoFileRightException;
import com.quartercode.disconnected.sim.comp.file.OutOfSpaceException;
import com.quartercode.disconnected.sim.comp.program.Process;
import com.quartercode.disconnected.util.InfoString;

/**
 * The file system manager is a subclass the {@link OperatingSystem} uses for holding and modifing file systems.
 * This class only gets used by the {@link OperatingSystem}.
 * 
 * @see FileSystem
 * @see OperatingSystem
 */
public class FileSystemManager implements InfoString {

    private OperatingSystem       host;

    @XmlElement (name = "knownFileSystem")
    private List<KnownFileSystem> knownFileSystems;

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

        knownFileSystems = new ArrayList<KnownFileSystem>();
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
     * Returns a list containing all known file systems which is sorted by the mountpoints.
     * A known file system can be mounted and associated with a set mountpoint (e.g. "system").
     * Only known file systems can be mounted.
     * 
     * @return A list containing all known file systems.
     */
    public List<FileSystem> getKnown() {

        Map<String, FileSystem> bindings = new TreeMap<String, FileSystem>();
        for (KnownFileSystem fileSystem : knownFileSystems) {
            bindings.put(fileSystem.getMountpoint(), fileSystem.getFileSystem());
        }
        return new ArrayList<FileSystem>(bindings.values());
    }

    /**
     * Returns the known file system which is associated with the given mountpoint.
     * A mountpoint is a simple string like "system".
     * If there is no known file system associated with the given mountpoint, this will return null.
     * 
     * @param mountpoint The mountpoint the returned file system is associated to.
     * @return The known file system which is associated with the given mountpoint.
     */
    public FileSystem getKnown(String mountpoint) {

        for (KnownFileSystem fileSystem : knownFileSystems) {
            if (fileSystem.getMountpoint().equals(mountpoint)) {
                return fileSystem.getFileSystem();
            }
        }

        return null;
    }

    /**
     * Returns the known file system object which is associated with the given file system.
     * 
     * @param fileSystem The file system the returned object is associated to.
     * @return The known file system object which is associated with the given file system.
     */
    protected KnownFileSystem getKnown(FileSystem fileSystem) {

        for (KnownFileSystem knownFileSystem : knownFileSystems) {
            if (knownFileSystem.getFileSystem().equals(fileSystem)) {
                return knownFileSystem;
            }
        }

        return null;
    }

    /**
     * Returns a list containing all mounted file systems which is sorted by the mountpoints.
     * A mounted file system is mounted and associated with a mountpoint (e.g. "system").
     * Only mounted file systems can be used.
     * 
     * @return A list containing all mounted file systems.
     */
    public List<FileSystem> getMounted() {

        Map<String, FileSystem> bindings = new TreeMap<String, FileSystem>();
        for (KnownFileSystem fileSystem : knownFileSystems) {
            if (fileSystem.isMounted()) {
                bindings.put(fileSystem.getMountpoint(), fileSystem.getFileSystem());
            }
        }
        return new ArrayList<FileSystem>(bindings.values());
    }

    /**
     * Returns the mounted file system which could hold the file which is stored under the given path.
     * A file system can be mounted and associated with a mountpoint (e.g. "system"). Only mounted file systems can be used.
     * A path is a collection of files seperated by a seperator. This requires a global os path.
     * If there is no file system the file could be stored on, this will return null.
     * 
     * @param path The file represented by this path is stored on the returned file system.
     * @return The mounted file system which holds the file which is stored under the given path.
     */
    public FileSystem getMounted(String path) {

        if (path.startsWith(File.SEPERATOR) && path.split(File.SEPERATOR).length > 1) {
            for (KnownFileSystem fileSystem : knownFileSystems) {
                if (fileSystem.getMountpoint().equals(path.split(File.SEPERATOR)[1])) {
                    return fileSystem.getFileSystem();
                }
            }
        }

        return null;
    }

    /**
     * Returns the mountpoint of the given file system.
     * A file system can be mounted and associated with a mountpoint (e.g. "system").
     * 
     * @param fileSystem The file system which is associated with the returned mountpoint.
     * @return The mountpoint of the given file system.
     */
    public String getMountpoint(FileSystem fileSystem) {

        if (getKnown().contains(fileSystem)) {
            return getKnown(fileSystem).getMountpoint();
        } else {
            return null;
        }
    }

    /**
     * Associates the given file system with the given mountpoint (e.g. "system").
     * A file system can be mounted using such a mountpoint.
     * 
     * @param fileSystem The file system to associate with a mountpoint.
     * @param mountpoint The mountpoint to bind the given file system to.
     */
    public void setMountpoint(FileSystem fileSystem, String mountpoint) {

        if (getKnown().contains(fileSystem)) {
            knownFileSystems.remove(getKnown(fileSystem));
        }
        if (mountpoint != null && getMounted(File.SEPERATOR + mountpoint) == null) {
            knownFileSystems.add(new KnownFileSystem(fileSystem, mountpoint));
        }
    }

    /**
     * Returns true if the given file system is currently mounted.
     * 
     * @param fileSystem The file system to check.
     * @return True if the given file system is currently mounted.
     */
    public boolean isMounted(FileSystem fileSystem) {

        if (getKnown().contains(fileSystem)) {
            return getKnown(fileSystem).isMounted();
        } else {
            return false;
        }
    }

    /**
     * Tries to mount the given file system and binding it to the set mountpoint or unmount it.
     * The mountpoint can be set by using {@link #setMountpoint(FileSystem, String)}.
     * 
     * @param fileSystem The file system to mount or into the os or unmount from it.
     * @param mounted True if the given file system should be mounted, false if it should be unmounted.
     * @throws MountException Something goes wrong while mounting or unmounting the file system.
     */
    public void setMounted(FileSystem fileSystem, boolean mounted) throws MountException {

        if (!getKnown().contains(fileSystem)) {
            throw new MountException(fileSystem, true, "File system not known");
        }

        if (mounted) {
            if (getKnown(fileSystem).isMounted()) {
                throw new MountException(fileSystem, true, "File system already mounted");
            } else {
                getKnown(fileSystem).setMounted(true);
            }
        } else {
            if (!getMounted().contains(fileSystem)) {
                throw new MountException(fileSystem, false, "File system not mounted");
            } else {
                getKnown(fileSystem).setMounted(false);
            }
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
        if (fileSystem != null && path.lastIndexOf(File.SEPERATOR) > 0) {
            return fileSystem.getFile(path.substring(path.indexOf(File.SEPERATOR, 1)));
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
     * @throws OutOfSpaceException If there isn't enough space on the target file system for the new file.
     */
    public File addFile(String path, FileType type, User user) throws OutOfSpaceException {

        FileSystem fileSystem = getMounted(path);
        if (fileSystem != null) {
            return fileSystem.addFile(path.substring(path.indexOf(File.SEPERATOR, 1)), type, user);
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
     * @param process The process which wants to add the file.
     * @param path The path the new file will be located under.
     * @param type The file type the new file should has.
     * @param user The user who owns the new file.
     * @return The new file (or the existing one, if the file already exists).
     * @throws NoFileRightException The given process hasn't the right to write into a directory where the algorithm needs to write
     * @throws OutOfSpaceException If there isn't enough space on the target file system for the new file.
     */
    public File addFile(Process process, String path, FileType type, User user) throws NoFileRightException, OutOfSpaceException {

        FileSystem fileSystem = getMounted(path);
        if (fileSystem != null) {
            return fileSystem.addFile(process, path.substring(path.indexOf(File.SEPERATOR, 1)), type, user);
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
            // Mount every known file system (temp)
            for (FileSystem fileSystem : getKnown()) {
                try {
                    setMounted(fileSystem, true);
                }
                catch (MountException e) {
                    // TODO: Notify the user
                }
            }
        } else {
            for (FileSystem mountedFileSystem : getMounted()) {
                try {
                    setMounted(mountedFileSystem, false);
                }
                catch (MountException e) {
                    // TODO: Flush and force unmount
                }
            }
        }
    }

    protected void beforeUnmarshal(Unmarshaller unmarshaller, Object parent) {

        host = (OperatingSystem) parent;
    }

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = prime * result + (knownFileSystems == null ? 0 : knownFileSystems.hashCode());
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
        if (knownFileSystems == null) {
            if (other.knownFileSystems != null) {
                return false;
            }
        } else if (!knownFileSystems.equals(other.knownFileSystems)) {
            return false;
        }
        return true;
    }

    @Override
    public String toInfoString() {

        return getAvaiable().size() + " avaiable fs, " + getKnown().size() + "/" + getMounted().size() + " known fs mounted";
    }

    @Override
    public String toString() {

        return getClass().getName() + " [" + toInfoString() + "]";
    }

    /**
     * The known file system represents a file system which can mounted into an os because it's mountpoint is known.
     * This is used in a datastructure of the os class to store the mountpoints of file systems.
     * A mountpoint is a string like "system". To get the root file of a mounted fs with that mountpoint, you can use "system".
     * 
     * @see FileSystem
     */
    protected static class KnownFileSystem implements InfoString {

        @XmlIDREF
        @XmlAttribute (name = "name")
        private FileSystem fileSystem;
        @XmlAttribute
        private String     mountpoint;
        @XmlAttribute
        private boolean    mounted;

        /**
         * Creates a new empty known file system representation object.
         * This is only recommended for direct field access (e.g. for serialization).
         */
        public KnownFileSystem() {

        }

        /**
         * Creates a new known file system representation object and sets the file system and the mountpoint.
         * A mountpoint is a string like "system".
         * 
         * @param fileSystem The file system which is represented by this ds object.
         * @param mountpoint The mountpoint the given file system is using.
         */
        public KnownFileSystem(FileSystem fileSystem, String mountpoint) {

            this.fileSystem = fileSystem;
            this.mountpoint = mountpoint;
        }

        /**
         * Returns the file system which is represented by this data structure object.
         * 
         * @return The file system which is represented by this data structure object.
         */
        public FileSystem getFileSystem() {

            return fileSystem;
        }

        /**
         * Returns the mountpoint the represented file system is using.
         * A mountpoint is a string like "system".
         * 
         * @return The mountpoint the represented file system is using.
         */
        public String getMountpoint() {

            return mountpoint;
        }

        /**
         * Returns true if the known file system is actually mounted.
         * You can mount known file systems using {@link FileSystemManager#setMounted(FileSystem, boolean)}.
         * 
         * @return True if the known file system is actually mounted.
         */
        public boolean isMounted() {

            return mounted;
        }

        /**
         * Sets if this known file system is currently mounted into the os.
         * 
         * @param mounted True if this file system is currently mounted.
         */
        protected void setMounted(boolean mounted) {

            this.mounted = mounted;
        }

        @Override
        public int hashCode() {

            final int prime = 31;
            int result = 1;
            result = prime * result + (fileSystem == null ? 0 : fileSystem.hashCode());
            result = prime * result + (mountpoint == null ? 0 : mountpoint.hashCode());
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
            KnownFileSystem other = (KnownFileSystem) obj;
            if (fileSystem == null) {
                if (other.fileSystem != null) {
                    return false;
                }
            } else if (!fileSystem.equals(other.fileSystem)) {
                return false;
            }
            if (mountpoint == null) {
                if (other.mountpoint != null) {
                    return false;
                }
            } else if (!mountpoint.equals(other.mountpoint)) {
                return false;
            }
            return true;
        }

        @Override
        public String toInfoString() {

            return fileSystem.toInfoString() + ", assigned to " + File.SEPERATOR + mountpoint;
        }

        @Override
        public String toString() {

            return getClass().getName() + " [" + toInfoString() + "]";
        }

    }

}
