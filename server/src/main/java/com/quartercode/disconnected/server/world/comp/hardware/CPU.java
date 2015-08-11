/*
 * This file is part of Disconnected.
 * Copyright (c) 2013 QuarterCode <http://quartercode.com/>
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

package com.quartercode.disconnected.server.world.comp.hardware;

import javax.xml.bind.annotation.XmlAttribute;
import org.apache.commons.lang3.Validate;
import com.quartercode.disconnected.server.world.comp.hardware.Mainboard.NeedsMainboardSlot;

/**
 * This class represents the CPU of a computer.
 * A CPU has a count of possible threads running at the same time and a clock rate.
 *
 * @see Hardware
 */
@NeedsMainboardSlot
public class CPU extends Hardware {

    @XmlAttribute
    private int  threads;
    @XmlAttribute
    private long clockRate;

    // JAXB constructor
    protected CPU() {

    }

    /**
     * Creates a new CPU.
     *
     * @param name The "model" name of the new CPU.
     *        See {@link #getName()} for more details.
     * @param threads The amount of possible threads running at the same time (virtual cores).
     * @param clockRate The clock rate (tick frequency) of the CPU in hertz.
     */
    public CPU(String name, int threads, long clockRate) {

        super(name);

        Validate.isTrue(threads > 0, "Max CPU threads must be > 0");
        Validate.isTrue(clockRate > 0, "CPU clock rate must be > 0");

        this.threads = threads;
        this.clockRate = clockRate;
    }

    /**
     * Returns the amount of possible threads running at the same time (virtual cores).
     *
     * @return The amount of threads the CPU can handle.
     */
    public int getThreads() {

        return threads;
    }

    /**
     * Returns the clock rate (tick frequency) of the CPU in hertz.
     *
     * @return The tick frequency of the CPU in hertz.
     */
    public long getClockRate() {

        return clockRate;
    }

}
