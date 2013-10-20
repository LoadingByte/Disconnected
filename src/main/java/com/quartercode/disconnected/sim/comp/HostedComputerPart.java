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

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlID;
import com.quartercode.disconnected.sim.comp.hardware.Hardware;

/**
 * This class stores information about a computer part which is directly hosted on a computer, like a hardware part or an operating system.
 * A hosted part stores the name, the version and the hosting computer. Everything else is defined by subclasses.
 * 
 * @see Computer
 * @see Version
 * 
 * @see Hardware
 * @see OperatingSystem
 */
public class HostedComputerPart extends ComputerPart {

    private Computer host;

    /**
     * Creates a new empty hosted computer part.
     * This is only recommended for direct field access (e.g. for serialization).
     */
    protected HostedComputerPart() {

    }

    /**
     * Creates a new hosted computer part and sets the host computer, the name and the version.
     * 
     * @param host The host computer this part is built in.
     * @param name The name the part has.
     * @param version The current version the part has.
     */
    protected HostedComputerPart(Computer host, String name, Version version) {

        super(name, version);

        this.host = host;
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
     * Generates an unique id which is used to reference to this computer part.
     * 
     * @return An unique id which is used to reference to this computer part.
     */
    @XmlID
    @XmlAttribute
    public String getId() {

        return host.getId() + "-" + host.getParts().indexOf(this);
    }

    public void beforeUnmarshal(Unmarshaller unmarshaller, Object parent) {

        host = (Computer) parent;
    }

    @Override
    public String toString() {

        return getClass().getName() + "[" + toInfoString() + "]";
    }

}
