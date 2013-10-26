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

package com.quartercode.disconnected.sim.comp.session;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlIDREF;
import com.quartercode.disconnected.sim.comp.session.ShellSessionProgram.ShellSession;

/**
 * A shell can run commands and holds the latest output messages.
 * 
 * @see ShellSession
 */
public class Shell {

    @XmlIDREF
    private ShellSession       host;
    private final List<String> output = new ArrayList<String>();

    /**
     * Creates a new empty shell.
     * This is only recommended for direct field access (e.g. for serialization).
     */
    protected Shell() {

    }

    /**
     * Creates a new shell.
     * 
     * @param host The hosting shell session which uses this shell.
     */
    public Shell(ShellSession host) {

        this.host = host;
    }

    /**
     * Returns the hosting shell session which uses this shell.
     * 
     * @return The hosting shell session which uses this shell.
     */
    public ShellSession getHost() {

        return host;
    }

    /**
     * Returns the last messages the shell printed out.
     * Those messages wont get serialized.
     * 
     * @return The last messages the shell printed out.
     */
    public List<String> getOutput() {

        return Collections.unmodifiableList(output);
    }

    /**
     * Runs the given command on the shell.
     * This will print the output of the command to the shell.
     * 
     * @param command The command to run on the shell.
     */
    public void run(String command) {

        // TODO: Run command
    }

}
