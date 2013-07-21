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

import java.util.List;
import com.quartercode.disconnected.sim.comp.Hardware;
import com.quartercode.disconnected.sim.comp.Vulnerability;

/**
 * This class represents a ram module of a computer.
 * A ram module has a size (given in bytes) and an access frequency (given in hertz).
 */
public class RAM extends Hardware {

    private static final long serialVersionUID = 7876933474996321380L;

    private long              size;
    private long              frequency;

    /**
     * Creates a new ram module and sets the name, the vulnerabilities, the size and the access frequency.
     * 
     * @param name The name the cpu has.
     * @param vulnerabilities The vulnerabilities the cpu has.
     * @param size The size of the ram module, given in bytes.
     * @param frequency The access frequency of the ram module, given in hertz.
     */
    public RAM(String name, List<Vulnerability> vulnerabilities, long size, long frequency) {

        super(name, vulnerabilities);

        this.size = size;
        this.frequency = frequency;
    }

    /**
     * Returns the size of the ram module, given in bytes.
     * 
     * @return The size of the ram module, given in bytes.
     */
    public long getSize() {

        return size;
    }

    /**
     * Returns the access frequency of the ram module, given in hertz.
     * 
     * @return The access frequency of the ram module, given in hertz.
     */
    public long getFrequency() {

        return frequency;
    }

}
