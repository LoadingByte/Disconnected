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
import java.util.ResourceBundle;
import javax.xml.bind.annotation.XmlElement;
import com.quartercode.disconnected.sim.comp.ComputerPart;
import com.quartercode.disconnected.sim.comp.Version;
import com.quartercode.disconnected.sim.comp.Vulnerability;
import com.quartercode.disconnected.sim.comp.Vulnerability.Vulnerable;
import com.quartercode.disconnected.sim.comp.program.ArgumentException.MissingArgumentException;
import com.quartercode.disconnected.sim.comp.program.ArgumentException.MissingParameterException;
import com.quartercode.disconnected.sim.comp.program.ArgumentException.WrongArgumentTypeException;
import com.quartercode.disconnected.util.size.SizeObject;

/**
 * This class stores information about a program.
 * A program object can be stored in a file. The execution is done by a program executor. To run an executor, you need to create a new process.
 * This also contains a list of all vulnerabilities this program has.
 * 
 * @see ComputerPart
 * @see Vulnerability
 * 
 * @see ProgramExecutor
 * @see Process
 */
public abstract class Program extends ComputerPart implements SizeObject, Vulnerable {

    @XmlElement (name = "vulnerability")
    private List<Vulnerability>   vulnerabilities = new ArrayList<Vulnerability>();
    private final List<Parameter> parameters      = new ArrayList<Parameter>();

    /**
     * Creates a new empty program.
     * This is only recommended for direct field access (e.g. for serialization).
     */
    protected Program() {

        addParameters();
    }

    /**
     * Creates a new program and sets the name, the version and the vulnerabilities.
     * 
     * @param name The name the program has.
     * @param version The current version the program has.
     * @param vulnerabilities The vulnerabilities the program has.
     */
    public Program(String name, Version version, List<Vulnerability> vulnerabilities) {

        super(name, version);

        this.vulnerabilities = vulnerabilities == null ? new ArrayList<Vulnerability>() : vulnerabilities;

        addParameters();
    }

    @Override
    public List<Vulnerability> getVulnerabilities() {

        return Collections.unmodifiableList(vulnerabilities);
    }

    /**
     * Returns a list of possible/required execution parameters.
     * For more detail on the parameters, see the {@link Parameter} class.
     * 
     * @return A list of possible/required execution parameters.
     */
    public List<Parameter> getParameters() {

        return parameters;
    }

    /**
     * Returns the execution parameter with the given name (or null if there's no parameter with the given name).
     * For more detail on the parameters, see the {@link Parameter} class.
     * 
     * @param name The name the returned parameter has to have.
     * @return The execution parameter with the given name
     */
    public Parameter getParameter(String name) {

        for (Parameter parameter : parameters) {
            if (parameter.getName().equals(name)) {
                return parameter;
            }
        }

        return null;
    }

    /**
     * Adds a new execution parameter an executor uses.
     * This adding method should only be used by subclasses.
     * For more detail on the parameters, see the {@link Parameter} class.
     * 
     * @param parameter The new parameter to add to the list.
     */
    protected void addParameter(Parameter parameter) {

        parameters.add(parameter);
    }

    /**
     * This method should be implemented by the extending class and adds its required parameters.
     * Without overriding it, it doesn't do anything.
     */
    protected void addParameters() {

    }

    /**
     * Returns the resource bundle the implementing program uses.
     * 
     * @return The resource bundle the implementing program uses.
     */
    public abstract ResourceBundle getResourceBundle();

    /**
     * Creates a new program executor instance for this program which takes care of acutally running a program.
     * This also checks the argument map.
     * 
     * @param host The host process which uses the created executor for running the program instance.
     * @param arguments The argument map which contains values for the defined parameters.
     * @return A new program executor instance for this program which takes care of acutally running a program.
     * @throws ArgumentException Some parameters/arguments are not set correctly.
     */
    public ProgramExecutor createExecutor(Process host, Map<String, Object> arguments) throws ArgumentException {

        // Create a new hash map with the contents of the old one (it will get modified)
        arguments = arguments == null ? new HashMap<String, Object>() : new HashMap<String, Object>(arguments);

        for (Parameter parameter : parameters) {
            if (parameter.isSwitch()) {
                // Put switch object if it's not set
                if (!arguments.containsKey(parameter.getName()) || ! (arguments.get(parameter.getName()) instanceof Boolean)) {
                    arguments.put(parameter.getName(), false);
                }
            } else if (parameter.isArgument()) {
                // Throw exception if argument parameter is required, but not set
                if (parameter.isRequired() && !arguments.containsKey(parameter.getName())) {
                    throw new MissingParameterException(parameter);
                }
                // Throw exception if argument is required, but not set
                else if (parameter.isArgumentRequired() && arguments.get(parameter.getName()) == null) {
                    throw new MissingArgumentException(parameter);
                }
                // Throw exception if argument has the wrong type
                else if (arguments.get(parameter.getName()) != null && !parameter.getType().getType().isAssignableFrom(arguments.get(parameter.getName()).getClass())) {
                    throw new WrongArgumentTypeException(parameter, arguments.get(parameter.getName()).toString());
                }
            } else if (parameter.isRest()) {
                // Throw exception if rest is required, but not set
                if (parameter.isRequired() && (!arguments.containsKey(parameter.getName()) || ((String[]) arguments.get(parameter.getName())).length == 0)) {
                    throw new MissingParameterException(parameter);
                }
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
     * @throws ArgumentException Some parameters/arguments are not set correctly.
     */
    protected abstract ProgramExecutor createExecutorInstance(Process host, Map<String, Object> arguments) throws ArgumentException;

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = super.hashCode();
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

        return super.toInfoString() + ", " + vulnerabilities.size() + " vulns, " + parameters.size() + " parameters";
    }

    @Override
    public String toString() {

        return getClass().getName() + "[" + toInfoString() + "]";
    }

}
