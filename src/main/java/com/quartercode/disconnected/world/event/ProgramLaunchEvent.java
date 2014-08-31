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

package com.quartercode.disconnected.world.event;

import com.quartercode.disconnected.world.comp.program.ProgramExecutor;
import com.quartercode.eventbridge.basic.EventBase;

/**
 * The program launch event is sent by a program executor when it's started.
 * It contains general information about the type and identity of launched program.
 */
public class ProgramLaunchEvent extends EventBase {

    private static final long                      serialVersionUID = 315320067304169133L;

    private final String                           computerId;
    private final int                              pid;
    private final Class<? extends ProgramExecutor> type;

    /**
     * Creates a new program launch event.
     * 
     * @param computerId The id of the computer which runs the newly launched program.
     * @param pid The process id of the process which runs the newly launched program.
     * @param type The program executor type (class object) that represents which kind of program was launched.
     */
    public ProgramLaunchEvent(String computerId, int pid, Class<? extends ProgramExecutor> type) {

        this.computerId = computerId;
        this.pid = pid;
        this.type = type;
    }

    /**
     * Returns the id of the computer which runs the newly launched program.
     * 
     * @return The computer id.
     */
    public String getComputerId() {

        return computerId;
    }

    /**
     * Returns the process id of the process which runs the newly launched program.
     * 
     * @return The pid (process id).
     */
    public int getPid() {

        return pid;
    }

    /**
     * Returns the program executor type (class object) that represents which kind of program was launched.
     * 
     * @return The type of the program executor.
     */
    public Class<? extends ProgramExecutor> getType() {

        return type;
    }

}
