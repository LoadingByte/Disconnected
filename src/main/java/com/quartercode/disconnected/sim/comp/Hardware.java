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

import java.util.List;
import javax.xml.bind.annotation.XmlSeeAlso;
import com.quartercode.disconnected.sim.comp.hardware.CPU;
import com.quartercode.disconnected.sim.comp.hardware.HardDrive;
import com.quartercode.disconnected.sim.comp.hardware.RAM;

/**
 * This class stores information about a part of hardware, like a cpu or a ram module.
 * This also contains a list of all vulnerabilities this hardware part has.
 * The mainboard is not classified as a hardware part.
 * 
 * @see ComputerPart
 * 
 * @see CPU
 * @see RAM
 * @see HardDrive
 */
@XmlSeeAlso ({ CPU.class, RAM.class, HardDrive.class })
public class Hardware extends ComputerPart {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a new empty hardware part.
     * This is only recommended for direct field access (e.g. for serialization).
     */
    public Hardware() {

    }

    /**
     * Creates a new hardware part and sets the computer, the name, the version and the vulnerabilities.
     * 
     * @param computer The computer this part is built in.
     * @param name The name the hardware part has.
     * @param version The current version hardware the part has.
     * @param vulnerabilities The vulnerabilities the hardware part has.
     */
    public Hardware(Computer computer, String name, Version version, List<Vulnerability> vulnerabilities) {

        super(computer, name, version, vulnerabilities);
    }

}
