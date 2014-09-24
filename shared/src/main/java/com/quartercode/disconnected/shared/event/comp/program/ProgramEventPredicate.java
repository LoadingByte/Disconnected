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

import com.quartercode.eventbridge.basic.EventPredicateBase;

/**
 * The program event predicate tests a {@link ProgramEvent} for a specific {@code computerId} and {@code pid}.
 * It can be used to request program events that are fired by a specific process.
 * 
 * @param <T> The further specified type of program event that can be tested by the predicate.
 *        Might be just {@code ProgramEvent}.
 */
public class ProgramEventPredicate<T extends ProgramEvent> extends EventPredicateBase<T> {

    private final String computerId;
    private final int    pid;

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
