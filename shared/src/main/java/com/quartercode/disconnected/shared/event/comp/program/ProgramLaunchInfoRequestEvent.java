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
 * This event can be used to retrieve needed information before sending a {@link ProgramLaunchCommandEvent}.
 * The returned information is related to the computer of the client that sends the request event.
 * Every return event contains a newly generated pid which can be input into a program launch command event.
 * 
 * @see ProgramLaunchInfoReturnEvent
 */
public class ProgramLaunchInfoRequestEvent extends EventBase {

    /**
     * This event is the return event for the {@link ProgramLaunchInfoRequestEvent}.
     * It contains a newly generated pid which can be input into a {@link ProgramLaunchCommandEvent}.
     * 
     * @see ProgramLaunchInfoRequestEvent
     */
    public static class ProgramLaunchInfoReturnEvent extends EventBase {

        private final String computerId;
        private final int    pid;

        /**
         * Creates a new program launch info return event.
         * 
         * @param computerId The id of the computer which represents the client that sent the request event.
         * @param pid A newly generated process id that might be used to launch a new program.
         */
        public ProgramLaunchInfoReturnEvent(String computerId, int pid) {

            this.computerId = computerId;
            this.pid = pid;
        }

        /**
         * Returns the id of the computer which represents the client that sent the request event.
         * 
         * @return The computer id.
         */
        public String getComputerId() {

            return computerId;
        }

        /**
         * Returns a newly generated process id that might be used to launch a new program.
         * 
         * @return A new pid.
         */
        public int getPid() {

            return pid;
        }

    }

}
