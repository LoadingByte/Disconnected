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

package com.quartercode.disconnected.shared.event.program;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import com.quartercode.disconnected.shared.comp.program.ClientProcessId;
import com.quartercode.eventbridge.basic.EventPredicateBase;

/**
 * An event predicate that filters out {@link ClientProcessCommand}s sent to a specific client process.
 * The client process is identified with a {@link ClientProcessId}.
 * 
 * @param <T> The type of client process command that can be tested by the predicate.
 * @see ClientProcessId
 */
@RequiredArgsConstructor
@Getter
public class ClientProcessCommandPredicate<T extends ClientProcessCommand> extends EventPredicateBase<T> {

    /**
     * The {@link ClientProcessId} each {@link ClientProcessCommand} must have to be accepted by the predicate.
     * That means that the accepted commands are sent to this client process.
     */
    private final ClientProcessId clientProcessId;

    @Override
    public boolean test(ClientProcessCommand event) {

        return event.getClientProcessId().equals(clientProcessId);
    }

}
