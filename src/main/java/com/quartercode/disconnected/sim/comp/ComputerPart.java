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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import org.apache.commons.lang.Validate;
import com.quartercode.disconnected.Disconnected;
import com.quartercode.disconnected.util.CloneUtil;

/**
 * This class stores information about a spefific computer part, like a mainboard or an operating system.
 * This also contains a list of all vulnerabilities this part has.
 * 
 * @see Computer
 * @see Vulnerability
 * 
 * @see Mainboard
 * @see Hardware
 * @see OperatingSystem
 * @see Program
 */
public class ComputerPart implements Serializable {

    private static final long   serialVersionUID = 1L;

    private String              name;
    private List<Vulnerability> vulnerabilities;

    /**
     * Creates a new computer part and sets the name and the vulnerabilities.
     * 
     * @param name The name the part has.
     * @param vulnerabilities The vulnerabilities the part has.
     */
    protected ComputerPart(String name, List<Vulnerability> vulnerabilities) {

        Validate.notNull(name, "Name can't be null");

        this.name = name;
        this.vulnerabilities = vulnerabilities == null ? new ArrayList<Vulnerability>() : vulnerabilities;
    }

    /**
     * Returns the name the part has.
     * 
     * @return The name the part has.
     */
    public String getName() {

        return name;
    }

    /**
     * Returns the vulnerabilities the part has.
     * 
     * @return The vulnerabilities the part has.
     */
    public List<Vulnerability> getVulnerabilities() {

        return Collections.unmodifiableList(vulnerabilities);
    }

    @Override
    public ComputerPart clone() {

        return (ComputerPart) CloneUtil.clone(this);
    }

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = prime * result + (name == null ? 0 : name.hashCode());
        result = prime * result + (vulnerabilities == null ? 0 : vulnerabilities.hashCode());
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
        ComputerPart other = (ComputerPart) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
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
    public String toString() {

        return getClass().getName() + " [name=" + name + ", vulnerabilities=" + vulnerabilities + "]";
    }

    /**
     * This computer part name adapter is for storing a computer part in a profile xml file using his name.
     * While unmarshalling, the adapter loads the resource store resource for the given name.
     */
    public static class ComputerPartAdapter extends XmlAdapter<String, ComputerPart> {

        /**
         * Creates a new computer part adapter.
         */
        public ComputerPartAdapter() {

        }

        @Override
        public ComputerPart unmarshal(String v) throws Exception {

            return Disconnected.getResoureStore().getComputerPart(v);
        }

        @Override
        public String marshal(ComputerPart v) throws Exception {

            return v.getName();
        }

    }

}
