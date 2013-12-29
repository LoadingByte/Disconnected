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
import java.util.HashMap;
import java.util.List;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import com.quartercode.disconnected.mocl.extra.FunctionExecutionException;
import com.quartercode.disconnected.world.comp.file.ContentFile;
import com.quartercode.disconnected.world.comp.net.Address;
import com.quartercode.disconnected.world.comp.program.Process;
import com.quartercode.disconnected.world.comp.program.RootProcess;

/**
 * The process manager is a subclass the {@link OperatingSystem} uses for holding and modifying processes.
 * This class only gets used by the {@link OperatingSystem}.
 * 
 * @see Process
 * @see OperatingSystem
 */
public class ProcessManager {

    private OperatingSystem host;

    @XmlElement (name = "process")
    private RootProcess     rootProcess;

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
    public RootProcess getRootProcess() {

        return rootProcess;
    }

    /**
     * Returns a list of all current running processes.
     * 
     * @return A list of all current running processes.
     * @throws FunctionExecutionException Something goes wrong while resolving some data.
     */
    public List<Process<?>> getAllProcesses() throws FunctionExecutionException {

        List<Process<?>> processes = new ArrayList<Process<?>>();
        processes.add(rootProcess);
        processes.addAll(rootProcess.get(Process.GET_ALL_CHILDREN).invoke());
        return processes;
    }

    /**
     * Returns the process which is bound to the given address.
     * This returns null if there isn't any process with the given binding.
     * 
     * @param binding The address to inspect.
     * @return The process which binds itself to the given address.
     * @throws FunctionExecutionException Something goes wrong while resolving some data.
     */
    public Process<?> getProcess(Address binding) throws FunctionExecutionException {

        // TODO: Reimplement this when the new network system is created
        // for (Process<?> process : getAllProcesses()) {
        // if (process.get(Process.GET_EXECUTOR).invoke().getPacketListener(binding) != null) {
        // return process;
        // }
        // }

        return null;
    }

    /**
     * Changes the running state of the process manager.
     * 
     * @param running True if the process manager is running, false if not.
     * @throws FunctionExecutionException Something goes wrong while interrupting the root process on shutdown.
     */
    public void setRunning(boolean running) throws FunctionExecutionException {

        if (running) {
            try {
                Environment environment = new Environment(host.getFileSystemManager().getFile("/system/etc/environment.cfg").get(ContentFile.GET_CONTENT).toString());
                rootProcess = new RootProcess();
                rootProcess.setParent(host);
                rootProcess.setLocked(false);
                rootProcess.get(Process.SET_SOURCE).invoke(host.getFileSystemManager().getFile("/system/boot/kernel"));
                rootProcess.get(Process.SET_ENVIRONMENT).invoke(environment);
                rootProcess.get(Process.LAUNCH).invoke(new HashMap<String, Object>());
                rootProcess.setLocked(true);
            }
            catch (FunctionExecutionException e) {
                // Won't ever happen
            }
        } else {
            rootProcess.get(Process.INTERRUPT).invoke(true);
        }
    }

    protected void beforeUnmarshal(Unmarshaller unmarshaller, Object parent) {

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
    public String toString() {

        try {
            return getClass().getName() + " [" + getAllProcesses().size() + " processes]";
        }
        catch (FunctionExecutionException e) {
            // Ignore
            return null;
        }
    }

}
