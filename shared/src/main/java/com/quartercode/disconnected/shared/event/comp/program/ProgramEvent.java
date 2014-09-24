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
 * Program events are used to communicate with program executors.
 * They may be fired by a program executor or sent to a program executor.
 * Following from that, such events could transport state or other program-related information from or to the program executor.
 * For requesting these program events, {@link ProgramEventPredicate}s can be used.
 */
public class ProgramEvent extends EventBase {

    private final String computerId;
    private final int    pid;

    /**
     * Creates a new program event.
     * 
     * @param computerId The id of the computer which runs the program the event is fired by.
     * @param pid The process id of the process which runs the program the event is fired by.
     */
    public ProgramEvent(String computerId, int pid) {

        this.computerId = computerId;
        this.pid = pid;
    }

    /**
     * Returns the id of the computer which runs the program this event was fired by.
     * 
     * @return The computer id.
     */
    public String getComputerId() {

        return computerId;
    }

    /**
     * Returns the process id of the process which runs the program this event was fired by.
     * 
     * @return The process id (pid).
     */
    public int getPid() {

        return pid;
    }

}
