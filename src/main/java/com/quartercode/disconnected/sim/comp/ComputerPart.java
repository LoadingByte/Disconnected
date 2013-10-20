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

import javax.xml.bind.annotation.XmlElement;
import org.apache.commons.lang.Validate;
import com.quartercode.disconnected.sim.comp.program.Program;
import com.quartercode.disconnected.util.InfoString;

/**
 * This class stores information about a generic computer part, like a hardware part or an operating system.
 * A part stores the name and version, everything else is defined by subclasses.
 * 
 * @see Computer
 * @see Version
 * 
 * @see HostedComputerPart
 * @see Program
 */
public class ComputerPart implements InfoString {

    @XmlElement
    private String  name;
    @XmlElement
    private Version version;

    /**
     * Creates a new empty computer part.
     * This is only recommended for direct field access (e.g. for serialization).
     */
    protected ComputerPart() {

    }

    /**
     * Creates a new computer part and sets the name and the version.
     * 
     * @param name The name the part has.
     * @param version The current version the part has.
     */
    protected ComputerPart(String name, Version version) {

        Validate.notNull(name, "Name can't be null");

        this.name = name;
        this.version = version;
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
     * Returns the current version the part has.
     * 
     * @return The current version the part has.
     */
    public Version getVersion() {

        return version;
    }

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = prime * result + (name == null ? 0 : name.hashCode());
        result = prime * result + (version == null ? 0 : version.hashCode());
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
        if (version == null) {
            if (other.version != null) {
                return false;
            }
        } else if (!version.equals(other.version)) {
            return false;
        }
        return true;
    }

    @Override
    public String toInfoString() {

        return name + " " + version;
    }

    @Override
    public String toString() {

        return getClass().getName() + "[" + toInfoString() + "]";
    }

}
