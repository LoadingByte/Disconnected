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
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlTransient;
import org.apache.commons.lang.Validate;
import com.quartercode.disconnected.util.CloneUtil;
import com.quartercode.disconnected.util.InfoString;

/**
 * This class stores information about a spefific computer part, like a hardware part or an operating system.
 * This also contains a list of all vulnerabilities this part has.
 * 
 * @see Computer
 * @see Version
 * @see Vulnerability
 * 
 * @see Hardware
 * @see OperatingSystem
 * @see Program
 */
@XmlAccessorType (XmlAccessType.FIELD)
public class ComputerPart implements InfoString, Serializable {

    private static final long   serialVersionUID = 1L;

    @XmlTransient
    private Computer            host;

    private String              name;
    private Version             version;
    @XmlElement (name = "vulnerability")
    private List<Vulnerability> vulnerabilities  = new ArrayList<Vulnerability>();

    /**
     * Creates a new empty computer part.
     * This is only recommended for direct field access (e.g. for serialization).
     */
    protected ComputerPart() {

    }

    /**
     * Creates a new computer part and sets the host computer, the name, the version and the vulnerabilities.
     * 
     * @param host The host computer this part is built in.
     * @param name The name the part has.
     * @param version The current version the part has.
     * @param vulnerabilities The vulnerabilities the part has.
     */
    protected ComputerPart(Computer host, String name, Version version, List<Vulnerability> vulnerabilities) {

        Validate.notNull(name, "Name can't be null");

        this.host = host;
        this.name = name;
        this.version = version;
        this.vulnerabilities = vulnerabilities == null ? new ArrayList<Vulnerability>() : vulnerabilities;
    }

    /**
     * Returns the host computer this part is built in.
     * 
     * @return The host computer this part is built in.
     */
    public Computer getHost() {

        return host;
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

    /**
     * Returns the vulnerabilities the part has.
     * 
     * @return The vulnerabilities the part has.
     */
    public List<Vulnerability> getVulnerabilities() {

        return Collections.unmodifiableList(vulnerabilities);
    }

    /**
     * Generates an unique id which is used to reference to this computer part.
     * 
     * @return An unique id which is used to reference to this computer part.
     */
    @XmlID
    @XmlAttribute
    public String getId() {

        return host.getId() + "." + host.getParts().indexOf(this);
    }

    public void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {

        host = (Computer) parent;
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
        result = prime * result + (version == null ? 0 : version.hashCode());
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
        if (version == null) {
            if (other.version != null) {
                return false;
            }
        } else if (!version.equals(other.version)) {
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

        return name + " " + version + ", " + vulnerabilities.size() + " vulns";
    }

    @Override
    public String toString() {

        return getClass().getName() + "[" + toInfoString() + "]";
    }

}
