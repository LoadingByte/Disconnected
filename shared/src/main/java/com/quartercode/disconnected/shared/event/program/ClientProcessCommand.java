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

import com.quartercode.disconnected.shared.client.ClientIdentity;
import com.quartercode.disconnected.shared.event.LimitedClientEvent;
import com.quartercode.disconnected.shared.program.ClientProcessId;
import com.quartercode.eventbridge.basic.EventBase;

/**
 * An event that is sent to a graphical process on the client side.
 * For example, such an event could be fired by a server-side process in order to update the client-side rendering.
 */
public abstract class ClientProcessCommand extends EventBase implements LimitedClientEvent {

    @Override
    public ClientIdentity[] getClients() {

        return new ClientIdentity[] { getClientProcessId().getClient() };
    }

    /**
     * Returns the {@link ClientProcessId} that points to the graphical client process that should receive the event.
     * Note that the client mentioned in that object is the one returned by {@link #getClients()}.
     * 
     * @return The client process the event is sent to.
     */
    public abstract ClientProcessId getClientProcessId();

}
