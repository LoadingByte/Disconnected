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
import javax.xml.bind.annotation.XmlElement;
import com.quartercode.disconnected.sim.comp.ComputerPart;
import com.quartercode.disconnected.sim.comp.OperatingSystem.RightLevel;
import com.quartercode.disconnected.sim.comp.Version;
import com.quartercode.disconnected.sim.comp.Vulnerability;
import com.quartercode.disconnected.sim.comp.Vulnerability.Vulnerable;
import com.quartercode.disconnected.sim.comp.media.FileContent;

/**
 * This class stores information about a program.
 * A program object can be stored in a file. The execution is done by a program executor. To run an executor, you need to create a new process.
 * This also contains a list of all vulnerabilities this program has and the required right level.
 * 
 * @see ComputerPart
 * @see Vulnerability
 * 
 * @see ProgramExecutor
 * @see Process
 */
public class Program extends ComputerPart implements FileContent, Vulnerable {

    private static final long                serialVersionUID = 1L;

    private Class<? extends ProgramExecutor> executor;

    @XmlElement (name = "vulnerability")
    private List<Vulnerability>              vulnerabilities  = new ArrayList<Vulnerability>();
    private RightLevel                       rightLevel;

    /**
     * Creates a new empty program.
     * This is only recommended for direct field access (e.g. for serialization).
     */
    protected Program() {

    }

    /**
     * Creates a new program and sets the name, the version, the vulnerabilities, the executor and the required right level.
     * 
     * @param name The name the program has.
     * @param version The current version the program has.
     * @param vulnerabilities The vulnerabilities the program has.
     * @param executor The program executor which takes care of acutally running a program.
     * @param rightLevel The required right level a user need for executing the program.
     */
    public Program(String name, Version version, List<Vulnerability> vulnerabilities, Class<? extends ProgramExecutor> executor, RightLevel rightLevel) {

        super(name, version);

        this.vulnerabilities = vulnerabilities == null ? new ArrayList<Vulnerability>() : vulnerabilities;
        this.executor = executor;
        this.rightLevel = rightLevel;
    }

    @Override
    public List<Vulnerability> getVulnerabilities() {

        return Collections.unmodifiableList(vulnerabilities);
    }

    /**
     * Returns the required right level a user need for executing the program.
     * 
     * @return The required right level a user need for executing the program.
     */
    public RightLevel getRightLevel() {

        return rightLevel;
    }

    /**
     * Creates a new program executor instance for this program which takes care of acutally running a program.
     * 
     * @return A new program executor instance for this program which takes care of acutally running a program.
     * @throws RuntimeException An exception occurred while initalizing the new program executor instance
     */
    public ProgramExecutor createExecutor() {

        try {
            return executor.newInstance();
        }
        catch (Exception e) {
            throw new RuntimeException("Exception while initalizing new program executor instance", e);
        }
    }

    @Override
    public long getSize() {

        return 0;
    }

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (rightLevel == null ? 0 : rightLevel.hashCode());
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
        Program other = (Program) obj;
        if (rightLevel != other.rightLevel) {
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

        return super.toInfoString() + ", " + vulnerabilities.size() + " vulns, requires " + rightLevel;
    }

    @Override
    public String toString() {

        return getClass().getName() + "[" + toInfoString() + "]";
    }

}
