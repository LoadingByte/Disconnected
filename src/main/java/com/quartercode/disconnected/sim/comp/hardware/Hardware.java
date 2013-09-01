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

package com.quartercode.disconnected.sim.comp.hardware;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import com.quartercode.disconnected.sim.comp.Computer;
import com.quartercode.disconnected.sim.comp.HostedComputerPart;
import com.quartercode.disconnected.sim.comp.Version;
import com.quartercode.disconnected.sim.comp.Vulnerability;
import com.quartercode.disconnected.sim.comp.Vulnerability.Vulnerable;

/**
 * This class stores information about a part of hardware, like a mainboard, a cpu or a ram module.
 * This also contains a list of all vulnerabilities this hardware part has.
 * 
 * @see HostedComputerPart
 * @see Vulnerability
 */
public class Hardware extends HostedComputerPart implements Vulnerable {

    private static final long   serialVersionUID = 1L;

    @XmlElement (name = "vulnerability")
    private List<Vulnerability> vulnerabilities  = new ArrayList<Vulnerability>();

    /**
     * Creates a new empty hardware part.
     * This is only recommended for direct field access (e.g. for serialization).
     */
    protected Hardware() {

    }

    /**
     * Creates a new hardware part and sets the host computer, the name, the version and the vulnerabilities.
     * 
     * @param host The host computer this part is built in.
     * @param name The name the hardware part has.
     * @param version The current version hardware the part has.
     * @param vulnerabilities The vulnerabilities the hardware part has.
     */
    public Hardware(Computer host, String name, Version version, List<Vulnerability> vulnerabilities) {

        super(host, name, version);

        this.vulnerabilities = vulnerabilities == null ? new ArrayList<Vulnerability>() : vulnerabilities;
    }

    @Override
    public List<Vulnerability> getVulnerabilities() {

        return Collections.unmodifiableList(vulnerabilities);
    }

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
        Hardware other = (Hardware) obj;
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

        return super.toInfoString() + ", " + vulnerabilities.size() + " vulns";
    }

    @Override
    public String toString() {

        return getClass().getName() + "[" + toInfoString() + "]";
    }

}
