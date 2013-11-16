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

package com.quartercode.disconnected.world.comp.os;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import com.quartercode.disconnected.world.comp.Computer;
import com.quartercode.disconnected.world.comp.HostedComputerPart;
import com.quartercode.disconnected.world.comp.Version;
import com.quartercode.disconnected.world.comp.Vulnerability;
import com.quartercode.disconnected.world.comp.Vulnerability.Vulnerable;
import com.quartercode.disconnected.world.comp.program.Process.ProcessState;
import com.quartercode.disconnected.world.comp.session.Desktop;

/**
 * This class stores information about an operating system.
 * This also contains a list of all vulnerabilities this operating system has.
 * 
 * @see HostedComputerPart
 * @see Desktop
 * @see Vulnerability
 * @see ProcessManager
 * @see UserManager
 * @see FileSystemManager
 * @see NetworkManager
 */
public class OperatingSystem extends HostedComputerPart implements Vulnerable {

    @XmlElement (name = "vulnerability")
    private List<Vulnerability> vulnerabilities = new ArrayList<Vulnerability>();

    @XmlElement
    private ProcessManager      processManager;
    @XmlElement
    private UserManager         userManager;
    @XmlElement
    private FileSystemManager   fileSystemManager;
    private NetworkManager      networkManager;

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

        processManager = new ProcessManager(this);
        userManager = new UserManager(this);
        fileSystemManager = new FileSystemManager(this);
        networkManager = new NetworkManager(this);
    }

    @Override
    public List<Vulnerability> getVulnerabilities() {

        return Collections.unmodifiableList(vulnerabilities);
    }

    /**
     * Returns the process manager which is used for holding and modifing processes.
     * 
     * @return The process manager which is used for holding and modifing processes.
     */
    public ProcessManager getProcessManager() {

        return processManager;
    }

    /**
     * Returns the user manager which is used for holding and modifing users and groups.
     * 
     * @return The user manager which is used for holding and modifing users and groups.
     */
    public UserManager getUserManager() {

        return userManager;
    }

    /**
     * Returns the file system manager which is used for holding and modifing file systems.
     * 
     * @return The file system manager which is used for holding and modifing file systems.
     */
    public FileSystemManager getFileSystemManager() {

        return fileSystemManager;
    }

    /**
     * Returns the file system manager which is used for storing and delivering packets.
     * 
     * @return The file system manager which is used for storing and delivering packets.
     */
    public NetworkManager getNetworkManager() {

        return networkManager;
    }

    /**
     * Returns if the operating system is running.
     * 
     * @return True if the operating system is running, false if not.
     */
    @XmlTransient
    public boolean isRunning() {

        return processManager.getRootProcess() != null && processManager.getRootProcess().getState() != ProcessState.STOPPED;
    }

    /**
     * Changes the running state of the operating system.
     * 
     * @param running True if the operating system is running, false if not.
     */
    public void setRunning(boolean running) {

        if (running) {
            fileSystemManager.setRunning(true);
            processManager.setRunning(true);
        } else {
            processManager.setRunning(false);
            fileSystemManager.setRunning(false);
        }
    }

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (fileSystemManager == null ? 0 : fileSystemManager.hashCode());
        result = prime * result + (processManager == null ? 0 : processManager.hashCode());
        result = prime * result + (userManager == null ? 0 : userManager.hashCode());
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
        if (fileSystemManager == null) {
            if (other.fileSystemManager != null) {
                return false;
            }
        } else if (!fileSystemManager.equals(other.fileSystemManager)) {
            return false;
        }
        if (processManager == null) {
            if (other.processManager != null) {
                return false;
            }
        } else if (!processManager.equals(other.processManager)) {
            return false;
        }
        if (userManager == null) {
            if (other.userManager != null) {
                return false;
            }
        } else if (!userManager.equals(other.userManager)) {
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

        String info = super.toInfoString();
        info += ", " + vulnerabilities.size() + " vulns, " + processManager.toInfoString();
        info += ", " + userManager.toInfoString() + ", " + fileSystemManager.toInfoString();
        return info;
    }

    @Override
    public String toString() {

        return getClass().getName() + "[" + toInfoString() + "]";
    }

}
