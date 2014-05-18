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

package com.quartercode.disconnected.world.comp.program;

import com.quartercode.disconnected.bridge.Event;
import com.quartercode.disconnected.bridge.EventPredicate;
import com.quartercode.disconnected.util.DataObjectBase;

/**
 * Program events are fired by program executors and transport state or other program-related information.
 * For requesting these program events, {@link ProgramEventPredicate}s can be used.
 */
public class ProgramEvent extends DataObjectBase implements Event {

    private static final long serialVersionUID = -799460291560821563L;

    private final String      computerId;
    private final int         pid;

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

    /**
     * The program event predicate tests a {@link ProgramEvent} for a specific {@code computerId} and {@code pid}.
     * It can be used to request program events that are fired by a specific process.
     * 
     * @param <T> The further specified type of program event that can be tested by the predicate.
     *        Might be just {@code ProgramEvent}.
     */
    public static class ProgramEventPredicate<T extends ProgramEvent> extends DataObjectBase implements EventPredicate<T> {

        private static final long serialVersionUID = -8481054597885149089L;

        private final String      computerId;
        private final int         pid;

        /**
         * Creates a new program event predicate which tests {@link ProgramEvent}s for the given criteria.
         * 
         * @param computerId The computer id to check all program events for.
         * @param pid The process id to check all program events for.
         */
        public ProgramEventPredicate(String computerId, int pid) {

            this.computerId = computerId;
            this.pid = pid;
        }

        @Override
        public boolean test(ProgramEvent event) {

            return event.getComputerId().equals(computerId) && event.getPid() == pid;
        }

    }

}
