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

package com.quartercode.disconnected.shared.event.comp.prog.control;

import com.quartercode.eventbridge.basic.EventBase;

/**
 * This event interrupts a world process on the computer of the server bridge partner that sends it.
 * Such an event must be sent to a server bridge which handles it.<br>
 * <br>
 * Note that interrupting a process is not the same as stopping a process.
 * When a process is interrupted, it is given the chance to execute some final activities before being stopped.
 */
public class WorldProcessInterruptCommand extends EventBase {

    private static final long serialVersionUID = 1367286829833127727L;

    private final int         worldPid;
    private final boolean     recursive;

    /**
     * Creates a new world process interrupt command.
     * 
     * @param worldPid The pid of the world process which should be interrupted.
     *        That world process must run on the computer of the server bridge partner that sends the command.
     * @param recursive Whether all child processes of the given process should also be interrupted.
     *        If this is not true, all direct child processes of the process will eventually be promoted to child processes of the parent process.
     */
    public WorldProcessInterruptCommand(int worldPid, boolean recursive) {

        this.worldPid = worldPid;
        this.recursive = recursive;
    }

    /**
     * Returns the pid of the world process which should be interrupted.
     * That world process runs on the computer of the server bridge partner that sent the command.
     * 
     * @return The pid of the world process for interruption.
     */
    public int getWorldPid() {

        return worldPid;
    }

    /**
     * Returns whether all child processes of the defined process should also be interrupted.
     * If this is not true, all direct child processes of the interrupted process will eventually be promoted to child processes of the parent process.
     * 
     * @return Whether all child processes should also be interrupted.
     */
    public boolean isRecursive() {

        return recursive;
    }

}
