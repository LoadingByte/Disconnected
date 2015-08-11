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

package com.quartercode.disconnected.server.bridge;

import com.quartercode.disconnected.server.identity.SBPIdentityService;
import com.quartercode.disconnected.shared.event.LimitedSBPEvent;
import com.quartercode.disconnected.shared.identity.SBPIdentity;
import com.quartercode.eventbridge.bridge.Bridge;
import com.quartercode.eventbridge.bridge.BridgeConnector;
import com.quartercode.eventbridge.bridge.BridgeModule;

/**
 * An internal bridge extension that adds hooks to a {@link Bridge} for implementing the functionality of an {@link SBPIdentityService}.
 * It adds the following actions:
 *
 * <ul>
 * <li>It adds the correct {@link SBPIdentity} if a {@link BridgeConnector} is added and a new connection with a server bridge partner is established.</li>
 * <li>It removes an {@link SBPIdentity} if the associated {@link BridgeConnector} is removed.</li>
 * <li>It limits {@link LimitedSBPEvent}s to the server bridge partners which are allowed to receive the event.</li>
 * </ul>
 */
public interface SBPIdentityExtension extends BridgeModule {

    /**
     * Returns the {@link SBPIdentityService} the extension is linked with.
     * All actions are performed with that service.
     *
     * @return The linked SBP identity service.
     */
    public SBPIdentityService getIdentityService();

    /**
     * Changes the {@link SBPIdentityService} the extension is linked with.
     * All actions will be performed with that service.
     *
     * @param identityService The new linked SBP identity service.
     */
    public void setIdentityService(SBPIdentityService identityService);

}
