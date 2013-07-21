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
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import com.quartercode.disconnected.sim.comp.ComputerPart.ComputerPartAdapter;

/**
 * This class stores information about a program.
 * This also contains a list of all vulnerabilities this program has, the operating system the program is written for and the required right level.
 * 
 * @see ComputerPart
 */
@XmlJavaTypeAdapter (value = ComputerPartAdapter.class)
public class Program extends ComputerPart {

    private static final long serialVersionUID = 1L;

    private String            operatingSystem;
    private String            rightLevel;

    /**
     * Creates a new program and sets the name, the vulnerabilities, the operating system the program is written for and the required right level.
     * 
     * @param name The name the program has.
     * @param vulnerabilities The vulnerabilities the program has.
     * @param operatingSystem The operating system the program is written for.
     * @param rightLevel The required right level a user need for executing the program.
     */
    protected Program(String name, List<Vulnerability> vulnerabilities, String operatingSystem, String rightLevel) {

        super(name, vulnerabilities);

        this.operatingSystem = operatingSystem;
        this.rightLevel = rightLevel;
    }

    /**
     * Returns the operating system the program is written for.
     * 
     * @return The operating system the program is written for.
     */
    public String getOperatingSystem() {

        return operatingSystem;
    }

    /**
     * Returns the required right level a user need for executing the program.
     * 
     * @return The required right level a user need for executing the program.
     */
    public String getRightLevel() {

        return rightLevel;
    }

}
