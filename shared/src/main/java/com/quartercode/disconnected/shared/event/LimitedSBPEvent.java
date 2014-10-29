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

package com.quartercode.disconnected.shared.event;

import com.quartercode.disconnected.shared.identity.SBPIdentity;
import com.quartercode.eventbridge.bridge.Event;

/**
 * An event that is only sent to a limited set of server bridge partners.
 * Those SBPs are identified with {@link SBPIdentity} objects.
 */
public interface LimitedSBPEvent extends Event {

    /**
     * Returns the {@link SBPIdentity} objects that identify the server bridge partners who are allowed to receive the event.
     * It is permitted to provide only one SBP.
     * 
     * @return The SBPs who are allowed to receive the event.
     */
    public SBPIdentity[] getSBPs();

}
