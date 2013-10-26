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

package com.quartercode.disconnected.sim.comp.program;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import org.apache.commons.lang.Validate;
import com.quartercode.disconnected.sim.comp.file.File;
import com.quartercode.disconnected.sim.comp.os.OperatingSystem;
import com.quartercode.disconnected.sim.comp.os.User;
import com.quartercode.disconnected.sim.comp.program.SessionProgram.Session;
import com.quartercode.disconnected.util.InfoString;

/**
 * This class represents a process which is basically a running instance of a program.
 * On the creation, the program object will be backed up, as well as a new program executor instance.
 * 
 * @see Program
 * @see ProgramExecutor
 * @see File
 */
public class Process implements InfoString {

    /**
     * The process state defines the global state of the process the os can see.
     * It stores if the process is running, interrupted etc.
     */
    public static enum ProcessState {

        /**
         * The process is running and the update executes every tick.
         * This is the default state of a process.
         */
        RUNNING,
        /**
         * The execution is suspended, tick updates will be ignored.
         */
        SUSPENDED,
        /**
         * The execution is interrupted friendly and should be stopped soon.
         * If a process notes this state, it should try to execute last activities and the stop the execution.
         */
        INTERRUPTED,
        /**
         * The execution is permanently stopped.
         * If a process is stopped, it wont be able to start again.
         */
        STOPPED;

    }

    @XmlIDREF
    @XmlAttribute
    private OperatingSystem     host;
    private Process             parent;
    @XmlAttribute
    private int                 pid;
    @XmlIDREF
    private File                file;
    @XmlElement
    private ProgramExecutor     executor;

    @XmlElement
    private ProcessState        state    = ProcessState.RUNNING;
    @XmlElement (name = "process")
    private final List<Process> children = new ArrayList<Process>();

    /**
     * Creates a new empty process.
     * This is only recommended for direct field access (e.g. for serialization).
     */
    protected Process() {

    }

    /**
     * Creates a new process using the program stored in the given file.
     * This should only be used directly by the operating system.
     * 
     * @param host The host operating system the process will be ran on.
     * @param parent The parent process which created this process.
     * @param pid A unique process id the process has This is used to identify the process.
     * @param file The process launch file which contains the program for the process.
     * @param arguments The argument map which contains values for the defined parameters.
     * @throws IllegalArgumentException No or wrong argument type for a specific parameter.
     */
    public Process(OperatingSystem host, Process parent, int pid, File file, Map<String, Object> arguments) {

        Validate.isTrue(file.getContent() instanceof Program, "Process launch file must contain a program");

        this.host = host;
        this.parent = parent;
        this.pid = pid;
        this.file = file;

        Program program = (Program) file.getContent();
        executor = program.createExecutor(this, arguments);
    }

    /**
     * Returns the host operating system the process will be ran on.
     * 
     * @return The host operating system the process will be ran on.
     */
    public OperatingSystem getHost() {

        return host;
    }

    /**
     * Returns the parent process which created this process.
     * 
     * @return The parent process which created this process.
     */
    public Process getParent() {

        return parent;
    }

    /**
     * Returns a unique process id the process has This is used to identify the process.
     * 
     * @return A unique process id the process has. This is used to identify the process.
     */
    public int getPid() {

        return pid;
    }

    /**
     * Returns the process launch file which contains the program for the process.
     * 
     * @return The process launch file which contains the program for the process.
     */
    public File getFile() {

        return file;
    }

    /**
     * Returns the program executor which takes care of running the process.
     * 
     * @return The program executor which takes care of running the process.
     */
    public ProgramExecutor getExecutor() {

        return executor;
    }

    /**
     * Returns the process state which defines the global state of the process the os can see.
     * It stores if the process is running, interrupted etc.
     * 
     * @return The process state which defines the global state of the process the os can see.
     */
    public ProcessState getState() {

        return state;
    }

    /**
     * Returns if this process and every child is stopped.
     * This acts recursively and checks every child and their childs etc.
     * 
     * @return If this process and every child is stopped.
     */
    public boolean isCompletelyStopped() {

        if (state != ProcessState.STOPPED) {
            return false;
        } else {
            for (Process child : children) {
                if (!child.isCompletelyStopped()) {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * Changes the process state which defines the global state of the process the os can see.
     * This also applies the new state to every child process.
     * 
     * @param state The new process state.
     * @param recursive True if the state change should affect the child processes.
     */
    protected void setState(ProcessState state, boolean recursive) {

        this.state = state;

        if (recursive) {
            for (Process child : children) {
                child.setState(state, recursive);
            }
        }
    }

    /**
     * Suspends the execution temporarily, tick updates will be ignored.
     * Suspension only works if the execution is running. During the interruption, an execution can't be suspended.
     * 
     * @param recursive True if the state change should affect the child processes.
     */
    public void suspend(boolean recursive) {

        if (state == ProcessState.RUNNING) {
            setState(ProcessState.SUSPENDED, recursive);
        }
    }

    /**
     * Suspends a suspended process.
     * Resuming only works if the execution is suspended.
     * 
     * @param recursive True if the state change should affect the child processes.
     */
    public void resume(boolean recursive) {

        if (state == ProcessState.SUSPENDED) {
            setState(ProcessState.RUNNING, recursive);
        }
    }

    /**
     * Interrupts the execution friendly which should be stopped soon.
     * If the process notes the interruption, it should try to execute last activities and the stop the execution.
     * Interruption only works if the execution is running.
     * 
     * @param recursive True if the state change should affect the child processes.
     */
    public void interrupt(boolean recursive) {

        if (state == ProcessState.RUNNING) {
            setState(ProcessState.INTERRUPTED, recursive);
        }
    }

    /**
     * Forces the process to stop the execution.
     * This will act like {@link #suspend(boolean)}, apart from the fact that a stopped program wont ever be able to resume.
     * The forced stopping action should only be used if the further execution of the program must be stopped or if the interruption finished.
     * 
     * @param recursive True if the state change should affect the child processes.
     */
    public void stop(boolean recursive) {

        if (state != ProcessState.STOPPED) {
            setState(ProcessState.STOPPED, recursive);
        }
    }

    /**
     * Returns the child processes this process started.
     * 
     * @return The child processes this process started.
     */
    public List<Process> getChildren() {

        return Collections.unmodifiableList(children);
    }

    /**
     * Returns all child processes and their child processes etc. recursively.
     * 
     * @return All child processes and their child processes etc.
     */
    public List<Process> getAllChildren() {

        List<Process> allChildren = new ArrayList<Process>();
        for (Process child : children) {
            allChildren.add(child);
            allChildren.addAll(child.getAllChildren());
        }
        return allChildren;
    }

    /**
     * Creates a new child process using the program stored in the given file.
     * The new process will be a child of this process.
     * 
     * @param file The process launch file which contains the program for the process.
     * @param arguments The argument map which contains values for the defined parameters.
     * @throws IllegalArgumentException No or wrong argument type for a specific parameter.
     */
    public Process createChild(File file, Map<String, Object> arguments) {

        return createChild(file, arguments, host.getProcessManager().requestPid());
    }

    /**
     * Creates a new child process using the program stored in the given file using the given pid.
     * The new process will be a child of this process and will run under the same session.
     * 
     * @param file The process launch file which contains the program for the process.
     * @param arguments The argument map which contains values for the defined parameters.
     * @param pid A unique process id the process has. This is used to identify the process.
     * @throws IllegalArgumentException No or wrong argument type for a specific parameter.
     */
    public Process createChild(File file, Map<String, Object> arguments, int pid) {

        Process process = new Process(host, this, pid, file, arguments);
        children.add(process);
        return process;
    }

    /**
     * Resolves the session which started the process.
     * The process is running with the rights of that session.
     * 
     * @return The session which started the process.
     */
    public Session getSession() {

        if (parent == null) {
            return null;
        } else if (parent.getFile().getContent() instanceof SessionProgram) {
            return (Session) parent.getExecutor();
        } else {
            return parent.getSession();
        }
    }

    /**
     * Resolves the user the process is running under.
     * This uses the {@link #getSession()} method for resolving the session object.
     * 
     * @return The user the process is running under.
     */
    public User getUser() {

        return getSession().getUser();
    }

    /**
     * Unregisters a process from the operating system.
     * This should only be used by the process object.
     * 
     * @param process The process to unregister from this operating system.
     */
    public void unregisterChild(Process process) {

        children.remove(process);
    }

    /**
     * Returns the unique id the process has.
     * 
     * @return The unique id the process has.
     */
    @XmlID
    @XmlAttribute
    protected String getId() {

        return host.getHost().getId() + "-" + pid;
    }

    public void beforeUnmarshal(Unmarshaller unmarshaller, Object parent) {

        if (parent instanceof Process) {
            this.parent = (Process) parent;
        }
    }

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = prime * result + (children == null ? 0 : children.hashCode());
        result = prime * result + (executor == null ? 0 : executor.hashCode());
        result = prime * result + (file == null ? 0 : file.hashCode());
        result = prime * result + pid;
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
        Process other = (Process) obj;
        if (children == null) {
            if (other.children != null) {
                return false;
            }
        } else if (!children.equals(other.children)) {
            return false;
        }
        if (executor == null) {
            if (other.executor != null) {
                return false;
            }
        } else if (!executor.equals(other.executor)) {
            return false;
        }
        if (file == null) {
            if (other.file != null) {
                return false;
            }
        } else if (!file.equals(other.file)) {
            return false;
        }
        if (pid != other.pid) {
            return false;
        }
        return true;
    }

    @Override
    public String toInfoString() {

        return "pid " + pid + " on " + host.getId() + ", source " + file.getGlobalPath(host) + ", " + state.name().toLowerCase() + ", " + children.size() + " children";
    }

    @Override
    public String toString() {

        return getClass().getName() + " [" + toInfoString() + "]";
    }

}
