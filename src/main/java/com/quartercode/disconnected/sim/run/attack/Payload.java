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

package com.quartercode.disconnected.sim.run.attack;

import java.util.ArrayList;
import java.util.List;
import com.quartercode.disconnected.sim.comp.OperatingSystem;

/**
 * This class represents a payload which can be executed on a specified operating system.
 * In most of the cases, a payload gets executed after exploiting a vulnerability.
 * 
 * @see Exploit
 */
public class Payload {

    private final OperatingSystem operatingSystem;
    private final List<String>    scripts;

    /**
     * Creates a new payload and sets the operating system the payload is written for and some scripts which define what happens while execution.
     * 
     * @param operatingSystem The operating system the payload is written for.
     * @param scripts Some scripts which define what happens while executing the payload.
     */
    public Payload(OperatingSystem operatingSystem, List<String> scripts) {

        this.operatingSystem = operatingSystem;
        this.scripts = scripts == null ? new ArrayList<String>() : scripts;
    }

    /**
     * Returns the operating system the payload is written for.
     * 
     * @return The operating system the payload is written for.
     */
    public OperatingSystem getOperatingSystem() {

        return operatingSystem;
    }

    /**
     * Returns the scripts which define what happens while executing the payload.
     * 
     * @return The scripts which define what happens while executing the payload.
     */
    public List<String> getScripts() {

        return scripts;
    }

}
