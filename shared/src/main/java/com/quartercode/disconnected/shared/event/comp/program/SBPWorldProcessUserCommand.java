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

import com.quartercode.disconnected.shared.event.LimitedSBPEvent;
import com.quartercode.disconnected.shared.identity.SBPIdentity;
import com.quartercode.disconnected.shared.world.comp.program.SBPWorldProcessUserId;
import com.quartercode.eventbridge.basic.EventBase;

/**
 * An event that is sent to the user (server bridge partner) of a world process.
 * That user could be a client that runs a graphical process on the client side for rendering the program, or it could a an AI that interprets the command.
 */
public abstract class SBPWorldProcessUserCommand extends EventBase implements LimitedSBPEvent {

    private static final long serialVersionUID = 5578483002721162314L;

    @Override
    public SBPIdentity[] getSBPs() {

        return new SBPIdentity[] { getWorldProcessUserId().getSBP() };
    }

    /**
     * Returns the {@link SBPWorldProcessUserId} that should receive the event.
     * If this event is sent by a world process, that world process user has launched the world process.
     * Note that the SBP mentioned in that object is the one returned by {@link #getSBPs()}.
     * 
     * @return The world process user the event is sent to.
     */
    public abstract SBPWorldProcessUserId getWorldProcessUserId();

}
