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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
public class ComputerPart {

    private String              name;
    private List<Vulnerability> vulnerabilities;

    /**
     * Creates a new computer part and sets the name and the vulnerabilities.
     * 
     * @param name The name the part has.
     * @param vulnerabilities The vulnerabilities the part has.
     */
    protected ComputerPart(String name, List<Vulnerability> vulnerabilities) {

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

}
