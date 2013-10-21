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
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import com.quartercode.disconnected.sim.comp.net.Address;
import com.quartercode.disconnected.sim.comp.program.Process;
import com.quartercode.disconnected.util.InfoString;

/**
 * The process manager is a subclass the {@link OperatingSystem} uses for holding and modifing processes.
 * This class only gets used by the {@link OperatingSystem}.
 * 
 * @see Process
 * @see OperatingSystem
 */
public class ProcessManager implements InfoString {

    private OperatingSystem host;

    @XmlElement (name = "process")
    private Process         rootProcess;

    /**
     * Creates a new empty process manager.
     * This is only recommended for direct field access (e.g. for serialization).
     */
    protected ProcessManager() {

    }

    /**
     * Creates a new process manager and sets the host the manager is running for.
     * 
     * @param host The {@link OperatingSystem} this process manager is running for.
     */
    public ProcessManager(OperatingSystem host) {

        this.host = host;

        rootProcess = new Process(host, null, 0, host.getFileSystemManager().getFile("C:/system/boot/kernel"), null);
    }

    /**
     * Returns the {@link OperatingSystem} this process manager is running for.
     * 
     * @return The {@link OperatingSystem} this process manager is running for.
     */
    public OperatingSystem getHost() {

        return host;
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

    public void beforeUnmarshal(Unmarshaller unmarshaller, Object parent) {

        host = (OperatingSystem) parent;
    }

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = prime * result + (rootProcess == null ? 0 : rootProcess.hashCode());
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
        ProcessManager other = (ProcessManager) obj;
        if (rootProcess == null) {
            if (other.rootProcess != null) {
                return false;
            }
        } else if (!rootProcess.equals(other.rootProcess)) {
            return false;
        }
        return true;
    }

    @Override
    public String toInfoString() {

        return getAllProcesses().size() + " processes";
    }

    @Override
    public String toString() {

        return getClass().getName() + " [" + toInfoString() + "]";
    }

}
