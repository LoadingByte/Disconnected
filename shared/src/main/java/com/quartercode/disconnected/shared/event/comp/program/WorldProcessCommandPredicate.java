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

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import com.quartercode.disconnected.shared.world.comp.program.WorldProcessId;
import com.quartercode.eventbridge.basic.EventPredicateBase;

/**
 * An event predicate that filters out {@link WorldProcessCommand}s sent to a specific server-side world process.
 * The world process is identified with a {@link WorldProcessId}.
 * 
 * @param <T> The type of world process command that can be tested by the predicate.
 * @see WorldProcessId
 */
@RequiredArgsConstructor
@Getter
public class WorldProcessCommandPredicate<T extends WorldProcessCommand> extends EventPredicateBase<T> {

    private static final long    serialVersionUID = 7884175850606958107L;

    /**
     * The {@link WorldProcessId} each {@link WorldProcessCommand} must have to be accepted by the predicate.
     * That means that the accepted commands are sent to this server-side world process.
     */
    private final WorldProcessId worldProcessId;

    @Override
    public boolean test(WorldProcessCommand event) {

        return event.getWorldProcessId().equals(worldProcessId);
    }

}
