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

package com.quartercode.disconnected.shared.event.comp.prog;

import com.quartercode.disconnected.shared.world.comp.prog.WorldProcessId;
import com.quartercode.eventbridge.bridge.Event;

/**
 * An event that is sent to a simulation process on the server side.
 * For example, such an event could be fired by a client-side graphical process in order to provide some input to the server-side program logic.
 */
public interface WorldProcessCommand extends Event {

    /**
     * Returns the {@link WorldProcessId} that points to the server simulation process that should receive the event.
     * 
     * @return The world process the event is sent to.
     */
    public WorldProcessId getWorldProcessId();

}
