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

package com.quartercode.disconnected.server.identity;

import com.quartercode.disconnected.shared.identity.SBPIdentity;
import com.quartercode.eventbridge.bridge.BridgeConnector;

/**
 * This service assigns {@link SBPIdentity} objects (server bridge partners, e.g. clients) to {@link BridgeConnector}s and makes both accessible.
 * For this to work properly, an SBP identity bridge extension must be added to exactly one bridge.
 * 
 * @see SBPIdentity
 */
public interface SBPIdentityService {

    /**
     * Returns the {@link SBPIdentity} of the given {@link BridgeConnector}.
     * That means that the returned SBP uses the given connector.
     * 
     * @param connector The bridge connector whose identity should be returned.
     * @return The SBP identity of the given connector.
     */
    public SBPIdentity getIdentity(BridgeConnector connector);

    /**
     * Returns the {@link BridgeConnector} the given {@link SBPIdentity} uses to communicate with the server.
     * 
     * @param identity The SBP identity whose bridge connector should be returned.
     * @return The bridge connector that uses the given identity.
     */
    public BridgeConnector getConnector(SBPIdentity identity);

    /**
     * Assigns the given {@link SBPIdentity} to the given {@link BridgeConnector}.
     * Any previous entry with that connector will be removed.
     * 
     * @param connector The bridge connector that is used by the given SBP.
     * @param identity The identity of the SBP that uses the given bridge connector.
     */
    public void putIdentity(BridgeConnector connector, SBPIdentity identity);

    /**
     * Removes the mapping between the given {@link BridgeConnector} and some {@link SBPIdentity}.
     * That means that the connector will no longer be identifiable.
     * 
     * @param connector The bridge connector whose mapping should be removed.
     */
    public void removeIdentity(BridgeConnector connector);

    /**
     * Removes the mapping between the given {@link SBPIdentity} and some {@link BridgeConnector}.
     * That means that the bridge connector the identity was assigned to will no longer be identifiable.
     * 
     * @param identity The SBP identity whose mapping should be removed.
     */
    public void removeIdentity(SBPIdentity identity);

}
