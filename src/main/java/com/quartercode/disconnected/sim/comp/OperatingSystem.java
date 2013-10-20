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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;
import org.apache.commons.lang.Validate;
import com.quartercode.disconnected.sim.comp.Vulnerability.Vulnerable;
import com.quartercode.disconnected.sim.comp.file.File;
import com.quartercode.disconnected.sim.comp.file.File.FileType;
import com.quartercode.disconnected.sim.comp.file.FileSystem;
import com.quartercode.disconnected.sim.comp.file.FileSystemProvider;
import com.quartercode.disconnected.sim.comp.file.MountException;
import com.quartercode.disconnected.sim.comp.net.Address;
import com.quartercode.disconnected.sim.comp.net.Packet;
import com.quartercode.disconnected.sim.comp.program.Process;
import com.quartercode.disconnected.sim.comp.program.Program;

/**
 * This class stores information about an operating system.
 * This also contains a list of all vulnerabilities this operating system has.
 * 
 * @see HostedComputerPart
 * @see Desktop
 * @see Vulnerability
 */
public class OperatingSystem extends HostedComputerPart implements Vulnerable {

    /**
     * This enum represents the right levels a user can has on an operating system.
     * The right level defines what a user can or cannot do. If a user has a right level, he can use every other right level below his one.
     * 
     * @see OperatingSystem
     * @see Program
     */
    public static enum RightLevel {

        /**
         * A guest only has a minimum of rights. You can compare a guest access with a kiosk mode for operating systems.
         */
        GUEST,
        /**
         * A user is typically using installed applications, but he doesn't modify the computer or os in any way.
         */
        USER,
        /**
         * An adiministrator modifies the computer or the os, for example he can install programs or change system properties.
         */
        ADMIN,
        /**
         * The system authority is the superuser on the os and can do everything the os provides.
         */
        SYSTEM;
    }

    @XmlElement (name = "vulnerability")
    private List<Vulnerability>           vulnerabilities    = new ArrayList<Vulnerability>();

    @XmlElement
    private Process                       rootProcess;
    @XmlElementWrapper (name = "mountedFileSystems")
    @XmlElement (name = "fileSystem")
    private final List<MountedFileSystem> mountedFileSystems = new ArrayList<MountedFileSystem>();

    private Desktop                       desktop;

    /**
     * Creates a new empty operating system.
     * This is only recommended for direct field access (e.g. for serialization).
     */
    protected OperatingSystem() {

    }

    /**
     * Creates a new operating system and sets the host computer, the name, the version, the vulnerabilities and the times the os needs for switching on/off.
     * 
     * @param host The host computer this part is built in.
     * @param name The name the operating system has.
     * @param version The current version the operating system has.
     * @param vulnerabilities The vulnerabilities the operating system has.
     */
    public OperatingSystem(Computer host, String name, Version version, List<Vulnerability> vulnerabilities) {

        super(host, name, version);

        this.vulnerabilities = vulnerabilities == null ? new ArrayList<Vulnerability>() : vulnerabilities;
        rootProcess = new Process(this, null, 0, getFile("C:/system/boot/kernel"), null);
        desktop = new Desktop(this);
    }

    @Override
    public List<Vulnerability> getVulnerabilities() {

        return Collections.unmodifiableList(vulnerabilities);
    }

    /**
     * Returns the root process with the pid 0.
     * The root process gets started by the os kernel.
     * 
     * @return The root process with the pid 0.
     */
    public Process getRootProcess() {

        return rootProcess;
    }

    /**
     * Returns a list of all current running processes.
     * 
     * @return A list of all current running processes.
     */
    public List<Process> getAllProcesses() {

        List<Process> processes = new ArrayList<Process>();
        processes.add(rootProcess);
        processes.addAll(rootProcess.getAllChildren());
        return processes;
    }

    /**
     * Returns the process which is bound to the given address.
     * This returns null if there isn't any process with the given binding.
     * 
     * @param binding The address to inspect.
     * @return The process which binds itself to the given address.
     */
    public Process getProcess(Address binding) {

        for (Process process : getAllProcesses()) {
            if (process.getExecutor().getPacketListener(binding) != null) {
                return process;
            }
        }

        return null;
    }

    /**
     * Calculates and returns a new pid for a new process.
     * 
     * @return The calculated pid.
     */
    public synchronized int requestPid() {

        List<Integer> pids = new ArrayList<Integer>();
        for (Process process : getAllProcesses()) {
            pids.add(process.getPid());
        }
        int pid = 0;
        while (pids.contains(pid)) {
            pid++;
        }
        return pid;
    }

    /**
     * Returns the desktop the os displays.
     * The desktop displays windows which can be opened by programs.
     * 
     * @return The desktop the os displays.
     */
    public Desktop getDesktop() {

        return desktop;
    }

    /**
     * Returns a list containing all avaiable file systems which are connected to this computer.
     * This uses different resources to collect the file systems.
     * 
     * @return A list containing all avaiable file systems which are connected to this computer.
     */
    public List<FileSystem> getAvaiableFileSystems() {

        List<FileSystem> fileSystems = new ArrayList<FileSystem>();
        for (FileSystemProvider provider : getHost().getHardware(FileSystemProvider.class)) {
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
    public List<FileSystem> getMountedFileSystems() {

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
    public FileSystem getMountedFileSystem(char mountpoint) {

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
    public FileSystem getMountedFileSystem(String path) {

        if (path.contains(":")) {
            return getMountedFileSystem(path.split(":")[0].charAt(0));
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
    public char getFileSystemMountpoint(FileSystem fileSystem) {

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
    public void mountFileSystem(FileSystem fileSystem, char mountpoint) {

        Validate.notNull(mountpoint, "Mountpoint can't be null");
        if (getMountedFileSystems().contains(fileSystem)) {
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
    public void unmountFileSystem(FileSystem fileSystem) {

        if (!getMountedFileSystems().contains(fileSystem)) {
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

        FileSystem fileSystem = getMountedFileSystem(path);
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
     * @return The new file (or the existing one, if the file already exists).
     */
    public File addFile(String path, FileType type) {

        FileSystem fileSystem = getMountedFileSystem(path);
        if (fileSystem != null) {
            return fileSystem.addFile(path.split(":")[1], type);
        } else {
            return null;
        }
    }

    /**
     * Sends a new packet from the sender to the receiver address of the given packet.
     * 
     * @param packet The packet to send.
     */
    public void sendPacket(Packet packet) {

        packet.getSender().getIp().getHost().sendPacket(packet);
    }

    /**
     * This method takes an incoming packet and distributes it to the target port.
     * 
     * @param packet The packet which came in and called the method.
     */
    public void handlePacket(Packet packet) {

        if (getProcess(packet.getReceiver()) != null) {
            getProcess(packet.getReceiver()).getExecutor().receivePacket(packet);
        }
    }

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (rootProcess == null ? 0 : rootProcess.hashCode());
        result = prime * result + (vulnerabilities == null ? 0 : vulnerabilities.hashCode());
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
        OperatingSystem other = (OperatingSystem) obj;
        if (rootProcess == null) {
            if (other.rootProcess != null) {
                return false;
            }
        } else if (!rootProcess.equals(other.rootProcess)) {
            return false;
        }
        if (vulnerabilities == null) {
            if (other.vulnerabilities != null) {
                return false;
            }
        } else if (!vulnerabilities.equals(other.vulnerabilities)) {
            return false;
        }
        return true;
    }

    @Override
    public String toInfoString() {

        return super.toInfoString() + ", " + vulnerabilities.size() + " vulns";
    }

    @Override
    public String toString() {

        return getClass().getName() + "[" + toInfoString() + "]";
    }

    /**
     * The mounted file system represents a file system which is mounted into an os.
     * This is used in a datastructure of the os class to store the mountpoints of file systems.
     * 
     * @see FileSystem
     */
    @XmlAccessorType (XmlAccessType.FIELD)
    protected static class MountedFileSystem {

        @XmlIDREF
        private FileSystem fileSystem;
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

    }

}
