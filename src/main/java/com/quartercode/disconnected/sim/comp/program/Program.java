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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.xml.bind.annotation.XmlElement;
import com.quartercode.disconnected.sim.comp.ComputerPart;
import com.quartercode.disconnected.sim.comp.OperatingSystem.RightLevel;
import com.quartercode.disconnected.sim.comp.Version;
import com.quartercode.disconnected.sim.comp.Vulnerability;
import com.quartercode.disconnected.sim.comp.Vulnerability.Vulnerable;
import com.quartercode.disconnected.util.size.SizeObject;

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
public abstract class Program extends ComputerPart implements SizeObject, Vulnerable {

    @XmlElement (name = "vulnerability")
    private List<Vulnerability>         vulnerabilities = new ArrayList<Vulnerability>();
    @XmlElement
    private RightLevel                  rightLevel;
    private final Map<String, Class<?>> parameters      = new HashMap<String, Class<?>>();

    /**
     * Creates a new empty program.
     * This is only recommended for direct field access (e.g. for serialization).
     */
    protected Program() {

        addParameters();
    }

    /**
     * Creates a new program and sets the name, the version, the vulnerabilities and the required right level.
     * 
     * @param name The name the program has.
     * @param version The current version the program has.
     * @param vulnerabilities The vulnerabilities the program has.
     * @param rightLevel The required right level a user need for executing the program.
     */
    public Program(String name, Version version, List<Vulnerability> vulnerabilities, RightLevel rightLevel) {

        super(name, version);

        this.vulnerabilities = vulnerabilities == null ? new ArrayList<Vulnerability>() : vulnerabilities;
        this.rightLevel = rightLevel;

        addParameters();
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
     * Returns a list of required execution parameters with their types an executor uses.
     * 
     * @return A list of required execution parameters with their types an executor uses.
     */
    public Map<String, Class<?>> getParameters() {

        return Collections.unmodifiableMap(parameters);
    }

    /**
     * Adds a new required execution parameter an executor uses.
     * If the parameter already exists, this overwrites the old one.
     * This adding method should only be used by subclasses.
     * 
     * @param name The name for the new parameter.
     * @param type The type an argument which represents the parameter must have.
     */
    protected void addParameter(String name, Class<?> type) {

        if (parameters.containsKey(name)) {
            parameters.remove(name);
        }
        parameters.put(name, type);
    }

    /**
     * This method should be implemented by the extending class and adds its required parameters.
     * Without overriding it, it doesn't do anything.
     */
    protected void addParameters() {

    }

    /**
     * Creates a new program executor instance for this program which takes care of acutally running a program.
     * This also checks the argument map.
     * 
     * @param host The host process which uses the created executor for running the program instance.
     * @param arguments The argument map which contains values for the defined parameters.
     * @return A new program executor instance for this program which takes care of acutally running a program.
     * @throws IllegalArgumentException No or wrong argument type for a specific parameter.
     */
    public ProgramExecutor createExecutor(Process host, Map<String, Object> arguments) {

        for (Entry<String, Class<?>> parameter : parameters.entrySet()) {
            if (arguments == null || !arguments.containsKey(parameter.getKey()) || !parameter.getValue().isAssignableFrom(arguments.get(parameter.getKey()).getClass())) {
                throw new IllegalArgumentException("No or wrong argument type for parameter \"" + parameter.getKey() + "\" (type " + parameter.getValue().getName() + ")");
            }
        }

        return createExecutorInstance(host, arguments);
    }

    /**
     * Creates a new program executor instance for this program which takes care of acutally running a program.
     * This is used internally and shouldn't be called from outside.
     * 
     * @param host The host process which uses the created executor for running the program instance.
     * @param arguments The argument map which contains values for the defined parameters.
     * @return A new program executor instance for this program which takes care of acutally running a program.
     */
    protected abstract ProgramExecutor createExecutorInstance(Process host, Map<String, Object> arguments);

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (parameters == null ? 0 : parameters.hashCode());
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
        if (! (obj instanceof Program)) {
            return false;
        }
        Program other = (Program) obj;
        if (parameters == null) {
            if (other.parameters != null) {
                return false;
            }
        } else if (!parameters.equals(other.parameters)) {
            return false;
        }
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

        return super.toInfoString() + ", " + vulnerabilities.size() + " vulns, requires " + rightLevel + ", " + parameters.size() + " parameters";
    }

    @Override
    public String toString() {

        return getClass().getName() + "[" + toInfoString() + "]";
    }

}
