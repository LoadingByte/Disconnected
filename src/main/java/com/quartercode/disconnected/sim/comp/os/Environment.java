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
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import com.quartercode.disconnected.util.InfoString;
import com.quartercode.disconnected.util.size.SizeObject;
import com.quartercode.disconnected.util.size.SizeUtil;

/**
 * The environment of an operating contains the environment variables which are currently set.
 * Environment variables are like global variables on the level of the os.
 * 
 * @see EnvironmentVariable
 */
public class Environment implements SizeObject {

    @XmlValue
    @XmlJavaTypeAdapter (value = Environment.EnvironmentAdapter.class)
    private final List<EnvironmentVariable> variables = new ArrayList<EnvironmentVariable>();

    /**
     * Creates a new empty environment container.
     */
    public Environment() {

    }

    /**
     * Creates a new environment container and parses the given variable content string.
     * The string should be in this format:
     * 
     * <pre>
     * VAR1=AValue\nVAR2=AnotherValue
     * </pre>
     * 
     * The \n is equals to a new line:
     * 
     * <pre>
     * VAR1=AValue
     * VAR2=AnotherValue
     * </pre>
     * 
     * @param variables A string containing all variables in the format described above.
     */
    public Environment(String variables) {

        for (String line : variables.split("\n")) {
            String[] parts = line.split("=");
            if (parts.length == 2) {
                addVariable(new EnvironmentVariable(parts[0], parts[1]));
            }
        }
    }

    /**
     * Returns a list of all environment variables which are currently set in the environment.
     * Environment variables are like global variables on the level of the os.
     * 
     * @return A list of all environment variables.
     */
    public List<EnvironmentVariable> getVariables() {

        return Collections.unmodifiableList(variables);
    }

    /**
     * Returns the variable in the environment which has the given name.
     * Environment variables are like global variables on the level of the os.
     * 
     * @param name The name the returned variable has.
     * @return The variable in the environment which has the given name.
     */
    public EnvironmentVariable getVariable(String name) {

        for (EnvironmentVariable variable : variables) {
            if (variable.getName().equals(name)) {
                return variable;
            }
        }
        return null;
    }

    /**
     * Adds the given variable to the environment.
     * This overwrites any existing variable with the same name as the new one.
     * 
     * @param variable The variable to add to the environment.
     */
    public void addVariable(EnvironmentVariable variable) {

        if (getVariable(variable.getName()) != null) {
            removeVariable(getVariable(variable.getName()));
        }
        variables.add(variable);
    }

    /**
     * Removes the given variable from the environment.
     * This removes any variable with the same name as the given one.
     * 
     * @param variable The variable to remove from the environment.
     */
    public void removeVariable(EnvironmentVariable variable) {

        variables.remove(getVariable(variable.getName()));
    }

    @Override
    public long getSize() {

        return SizeUtil.getSize(variables);
    }

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = prime * result + (variables == null ? 0 : variables.hashCode());
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
        Environment other = (Environment) obj;
        if (variables == null) {
            if (other.variables != null) {
                return false;
            }
        } else if (!variables.equals(other.variables)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {

        String content = "";
        for (EnvironmentVariable variable : variables) {
            content += variable.getName() + "=" + variable.getValue() + "\n";
        }
        return content;
    }

    /**
     * Environment variables are used by different programs.
     * They are like global variables on the level of the os.
     * Every environment variable has a name and an associated value string.
     * 
     * @see Environment
     */
    public static class EnvironmentVariable implements SizeObject, InfoString {

        private String name;
        private String value;

        /**
         * Creates a new empty environment variable.
         * This is only recommended for direct field access (e.g. for serialization).
         */
        public EnvironmentVariable() {

        }

        /**
         * Creates a new environment variable and sets its name and its value string.
         * 
         * @param name The name of the environment variable. This name is final.
         * @param value The value string which is assigned to the variable. This value can be changed.
         */
        public EnvironmentVariable(String name, String value) {

            this.name = name;
            this.value = value;
        }

        /**
         * Returns the name of the environment variable.
         * The name of a variable name is final.
         * 
         * @return The name of the environment variable.
         */
        public String getName() {

            return name;
        }

        /**
         * Returns the value string which is assigned to the environment variable.
         * The value of a variable can be changed.
         * 
         * @return The value string which is assigned to the environment variable.
         */
        public String getValue() {

            return value;
        }

        /**
         * Changes the value string which is assigned to the environment variable.
         * 
         * @param value The new value string.
         */
        public void setValue(String value) {

            this.value = value;
        }

        @Override
        public long getSize() {

            return SizeUtil.getSize(name) + SizeUtil.getSize(value);
        }

        @Override
        public int hashCode() {

            final int prime = 31;
            int result = 1;
            result = prime * result + (name == null ? 0 : name.hashCode());
            result = prime * result + (value == null ? 0 : value.hashCode());
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
            EnvironmentVariable other = (EnvironmentVariable) obj;
            if (name == null) {
                if (other.name != null) {
                    return false;
                }
            } else if (!name.equals(other.name)) {
                return false;
            }
            if (value == null) {
                if (other.value != null) {
                    return false;
                }
            } else if (!value.equals(other.value)) {
                return false;
            }
            return true;
        }

        @Override
        public String toInfoString() {

            return name + " = " + value;
        }

        @Override
        public String toString() {

            return getClass().getName() + " [" + toInfoString() + "]";
        }

    }

    /**
     * This environment container adapter stores an environment container with all it's variables in a string.
     * It uses the format:
     * 
     * <pre>
     * VAR1=AValue\nVAR2=AnotherValue
     * </pre>
     * 
     * The \n is equals to a new line:
     * 
     * <pre>
     * VAR1=AValue
     * VAR2=AnotherValue
     * </pre>
     */
    public static class EnvironmentAdapter extends XmlAdapter<String, List<EnvironmentVariable>> {

        /**
         * Creates a new environment container adapter.
         */
        public EnvironmentAdapter() {

        }

        @Override
        public List<EnvironmentVariable> unmarshal(String v) {

            return new Environment(v).getVariables();
        }

        @Override
        public String marshal(List<EnvironmentVariable> v) {

            Environment environment = new Environment();
            for (EnvironmentVariable variable : v) {
                environment.addVariable(variable);
            }
            return environment.toString().substring(0, environment.toString().length() - 1);
        }

    }

}
