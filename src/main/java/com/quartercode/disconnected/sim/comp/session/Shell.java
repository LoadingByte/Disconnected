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
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import com.quartercode.disconnected.graphics.session.ShellWidget;
import com.quartercode.disconnected.sim.comp.file.File;
import com.quartercode.disconnected.sim.comp.session.ShellSessionProgram.ShellSession;

/**
 * A shell can run commands and holds the latest output messages.
 * 
 * @see ShellSession
 */
public class Shell {

    private ShellSession       host;
    private File               currentDirectory;
    @XmlElementWrapper (name = "output")
    @XmlElement (name = "line")
    private final List<String> output = new ArrayList<String>();

    /**
     * Creates a new empty shell.
     * This is only recommended for direct field access (e.g. for serialization).
     */
    protected Shell() {

    }

    /**
     * Creates a new shell in the root directory.
     * 
     * @param host The hosting shell session which uses this shell.
     */
    public Shell(ShellSession host) {

        this.host = host;
    }

    /**
     * Creates a new shell in the given directory.
     * 
     * @param host The hosting shell session which uses this shell.
     * @param currentDirectory The directory the shell will start in.
     */
    public Shell(ShellSession host, File currentDirectory) {

        this.host = host;
        this.currentDirectory = currentDirectory;
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
     * Returns the directory the shell is currently in.
     * This directory is used for all relative paths used in the shell.
     * The object is null if the shell is currently in the root directory.
     * 
     * @return The directory the shell is currently in.
     */
    public File getCurrentDirectory() {

        return currentDirectory;
    }

    /**
     * Changes the directory the shell is currently in.
     * This directory is used for all relative paths used in the shell.
     * The object is null if the shell is currently in the root directory.
     * 
     * @param currentDirectory The new directory the shell will be in after the change.
     */
    public void setCurrentDirectory(File currentDirectory) {

        this.currentDirectory = currentDirectory;
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

    private void printOutput(String line) {

        if (output.size() == 50) {
            output.remove(0);
        }
        output.add(line);
    }

    /**
     * Runs the given command on the shell.
     * This will print the output of the command to the shell.
     * 
     * @param command The command to run on the shell.
     */
    public void run(String command) {

        // TODO: Run command

        printOutput("Input: " + command);
        for (ShellWidget widget : host.getWidgets()) {
            widget.update();
        }
    }

    public void beforeUnmarshal(Unmarshaller unmarshaller, Object parent) {

        host = (ShellSession) parent;
    }

}
