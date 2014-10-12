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

package com.quartercode.disconnected.server.bridge;

import com.quartercode.disconnected.server.client.ClientIdentityService;
import com.quartercode.disconnected.shared.client.ClientIdentity;
import com.quartercode.disconnected.shared.event.LimitedClientEvent;
import com.quartercode.eventbridge.bridge.Bridge;
import com.quartercode.eventbridge.bridge.BridgeConnector;
import com.quartercode.eventbridge.bridge.BridgeModule;

/**
 * An internal bridge extension that adds hooks to a {@link Bridge} for implementing the functionality of a {@link ClientIdentityService}.
 * It adds the following actions:
 * 
 * <ul>
 * <li>It adds the correct {@link ClientIdentity} if a {@link BridgeConnector} is added and a new connection with a client is established.</li>
 * <li>It removes a {@link ClientIdentity} if the associated {@link BridgeConnector} is removed.</li>
 * <li>It limits {@link LimitedClientEvent}s to the clients which are allowed to receive the event.</li>
 * </ul>
 */
public interface ClientIdentityExtension extends BridgeModule {

    /**
     * Returns the {@link ClientIdentityService} the extension is linked with.
     * All actions are performed with that service.
     * 
     * @return The linked client identity service.
     */
    public ClientIdentityService getIdentityService();

    /**
     * Changes the {@link ClientIdentityService} the extension is linked with.
     * All actions will be performed with that service.
     * 
     * @param identityService The new linked client identity service.
     */
    public void setIdentityService(ClientIdentityService identityService);

}
