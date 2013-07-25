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
 * This class represents a cpu module of a computer.
 * A cpu has a count of possible threads running at the same time and a frequency (given in hertz).
 */
public class CPU extends Hardware {

    private static final long serialVersionUID = -6953965174736837113L;

    private final int         threads;
    private final long        frequency;

    /**
     * Creates a new cpu and sets the name, the vulnerabilities, the count of possible threads and the frequency.
     * 
     * @param name The name the cpu has.
     * @param vulnerabilities The vulnerabilities the cpu has.
     * @param threads The count of possible threads running at the same time.
     * @param frequency The frequency of the cpu, given in hertz.
     */
    public CPU(String name, List<Vulnerability> vulnerabilities, int threads, long frequency) {

        super(name, vulnerabilities);

        this.threads = threads;
        this.frequency = frequency;
    }

    /**
     * Returns the count of possible threads running at the same time.
     * 
     * @return The count of possible threads running at the same time.
     */
    public int getThreads() {

        return threads;
    }

    /**
     * Returns the frequency of the cpu, given in hertz.
     * 
     * @return The frequency of the cpu, given in hertz.
     */
    public long getFrequency() {

        return frequency;
    }

}
