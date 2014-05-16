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

import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import com.quartercode.disconnected.bridge.Event;
import com.quartercode.disconnected.bridge.EventPredicate;

/**
 * Program events are fired by program executors and transport state or other program-related information.
 * For requesting these program events, {@link ProgramEventPredicate}s can be used.
 */
@Data
public class ProgramEvent implements Event {

    private static final long serialVersionUID = 8231224050193263229L;

    /**
     * The id of the computer that runs the program this event was fired by.
     */
    private final String      computerId;

    /**
     * The process id of the process that runs the program this event was fired by.
     */
    private final int         pid;

    @Override
    public String toString() {

        return ToStringBuilder.reflectionToString(this);
    }

    /**
     * The program event predicate tests a {@link ProgramEvent} for a specific {@code computerId} and {@code pid}.
     * It can be used to request program events that are fired by a specific process.
     * 
     * @param <T> The further specified type of program event that can be tested by the predicate.
     *        Might be just {@code ProgramEvent}.
     */
    @Data
    public static class ProgramEventPredicate<T extends ProgramEvent> implements EventPredicate<T> {

        private static final long serialVersionUID = 7617552432313374155L;

        /**
         * The computer id to check all {@link ProgramEvent}s for.
         * This id is the id of the computer that runs the program which fires the program events.
         */
        private final String      computerId;

        /**
         * The process id to check all {@link ProgramEvent}s for.
         * The pid is an identifier for the process that runs the program which fires the program events.
         */
        private final int         pid;

        @Override
        public boolean test(ProgramEvent event) {

            return event.getComputerId().equals(computerId) && event.getPid() == pid;
        }

    }

}
