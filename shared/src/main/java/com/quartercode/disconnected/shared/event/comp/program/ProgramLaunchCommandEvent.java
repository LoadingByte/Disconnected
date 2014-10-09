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

package com.quartercode.disconnected.shared.event.comp.program;

import com.quartercode.eventbridge.basic.EventBase;

/**
 * This event starts a program on the computer of the client that sends it.
 * Such an event must be sent to a server bridge which handles it.<br>
 * <br>
 * Before a program launch command is sent, the user probably retrieves some data using a {@link ProgramLaunchInfoRequestEvent}.
 * It is used to get a new pid for the new program instance.
 * Here's an example for the combination of both events:
 * 
 * <pre>
 * // Get launch information
 * bridge.getModule(ReturnEventExtensionRequester.class).sendRequest(new ProgramLaunchInfoRequestEvent(),
 *         new AbstractEventHandler&lt;ProgramLaunchInfoResponseEvent&gt;(ProgramLaunchInfoResponseEvent.class) {
 * 
 *             public void handle(ProgramLaunchInfoResponseEvent event) {
 * 
 *                 // Launch program
 *                 bridge.send(new ProgramLaunchCommandEvent(event.getPid(), &quot;pathToProgram&quot;));
 *             }
 * 
 *         });
 * </pre>
 */
public class ProgramLaunchCommandEvent extends EventBase {

    private final int    pid;
    private final String filePath;

    /**
     * Creates a new program launch command event.
     * 
     * @param pid The pid the newly launched program will have.
     * @param filePath The path under which the program file, which will be used for the new program instance, can be found.
     */
    public ProgramLaunchCommandEvent(int pid, String filePath) {

        this.pid = pid;
        this.filePath = filePath;
    }

    /**
     * Returns the pid the newly launched program will have.
     * It is checked for uniqueness before it's actually used.
     * 
     * @return The pid for the new program.
     */
    public int getPid() {

        return pid;
    }

    /**
     * Returns the path under which the program file, which will be used for the new program instance, can be found.
     * If the path doesn't point to a valid program file, the launch process is stopped.
     * 
     * @return The source file path for the new program.
     */
    public String getFilePath() {

        return filePath;
    }

}
