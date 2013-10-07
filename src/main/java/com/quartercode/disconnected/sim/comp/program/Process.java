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

import java.io.Serializable;
import java.util.Map;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import com.quartercode.disconnected.sim.comp.OperatingSystem;
import com.quartercode.disconnected.sim.comp.media.File;
import com.quartercode.disconnected.util.InfoString;

/**
 * This class represents a process which is basically a running instance of a program.
 * On the creation, the program object will be backed up, as well as a new program executor instance.
 * 
 * @see Program
 * @see ProgramExecutor
 * @see File
 */
public class Process implements Serializable, InfoString {

    private static final long serialVersionUID = 1L;

    @XmlIDREF
    @XmlAttribute
    private OperatingSystem   host;
    @XmlAttribute
    private int               pid;
    @XmlIDREF
    private File              file;

    @XmlElement
    private ProgramExecutor   executor;

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
     * @param pid A unique process id the process has This is used to identify the process.
     * @param file The process launch file which contains the program for the process.
     * @param arguments The argument map which contains values for the defined parameters.
     * @throws IllegalArgumentException No or wrong argument type for a specific parameter.
     */
    public Process(OperatingSystem host, int pid, File file, Map<String, Object> arguments) {

        if (! (file.getContent() instanceof Program)) {
            throw new IllegalArgumentException("Process launch file must contain a program");
        }

        this.host = host;
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
     * Returns the unique id the process has.
     * 
     * @return The unique id the process has.
     */
    @XmlID
    @XmlAttribute
    public String getId() {

        return host.getHost().getId() + "-" + pid;
    }

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
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
        if (! (obj instanceof Process)) {
            return false;
        }
        Process other = (Process) obj;
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

        return "pid " + pid + " on " + host.getId() + ", created from " + file.getGlobalPath() + ", ran by " + executor.getClass().getName();
    }

    @Override
    public String toString() {

        return getClass().getName() + " [" + toInfoString() + "]";
    }

}
