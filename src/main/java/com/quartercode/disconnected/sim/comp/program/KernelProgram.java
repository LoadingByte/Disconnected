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

package com.quartercode.disconnected.sim.comp.program;

import java.util.List;
import java.util.Map;
import javax.xml.bind.annotation.XmlElement;
import com.quartercode.disconnected.sim.comp.Version;
import com.quartercode.disconnected.sim.comp.Vulnerability;
import com.quartercode.disconnected.sim.comp.os.OperatingSystem;
import com.quartercode.disconnected.sim.comp.program.Process.ProcessState;
import com.quartercode.disconnected.sim.run.Ticker;
import com.quartercode.disconnected.util.size.ByteUnit;

/**
 * The main kernel which runs the central functions of an operating system.
 * 
 * @see OperatingSystem
 */
public class KernelProgram extends Program {

    /**
     * Creates a new empty kernel program.
     * This is only recommended for direct field access (e.g. for serialization).
     */
    protected KernelProgram() {

    }

    /**
     * Creates a new kernel program and sets the name, the version and the vulnerabilities.
     * 
     * @param name The name the program has.
     * @param version The current version the program has.
     * @param vulnerabilities The vulnerabilities the program has.
     */
    public KernelProgram(String name, Version version, List<Vulnerability> vulnerabilities) {

        super(name, version, vulnerabilities);
    }

    @Override
    public long getSize() {

        return ByteUnit.BYTE.convert(10, ByteUnit.MEGABYTE);
    }

    @Override
    protected ProgramExecutor createExecutorInstance(final Process host, Map<String, Object> arguments) {

        return new ProgramExecutor(host) {

            @XmlElement
            private int elapsedSinceInterrupt = -1;

            @Override
            public void update() {

                if (getHost().getState() == ProcessState.INTERRUPTED) {
                    elapsedSinceInterrupt = 0;
                }
                if (elapsedSinceInterrupt >= 0) {
                    elapsedSinceInterrupt++;
                }

                // Force stop after 5 seconds
                if (elapsedSinceInterrupt > Ticker.DEFAULT_TICKS_PER_SECOND * 5) {
                    getHost().stop();
                    elapsedSinceInterrupt = -1;
                }
            }
        };
    }

}
