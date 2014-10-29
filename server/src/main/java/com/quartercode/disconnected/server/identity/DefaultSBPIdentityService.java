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

package com.quartercode.disconnected.server.identity;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.lang3.Validate;
import com.quartercode.disconnected.shared.identity.SBPIdentity;
import com.quartercode.eventbridge.bridge.BridgeConnector;

/**
 * This is the default implementation of the {@link SBPIdentityService}.
 * 
 * @see SBPIdentityService
 */
public class DefaultSBPIdentityService implements SBPIdentityService {

    private final Map<BridgeConnector, SBPIdentity> identities = new HashMap<>();

    @Override
    public SBPIdentity getIdentity(BridgeConnector connector) {

        return identities.get(connector);
    }

    @Override
    public BridgeConnector getConnector(SBPIdentity identity) {

        for (Entry<BridgeConnector, SBPIdentity> entry : identities.entrySet()) {
            if (entry.getValue().equals(identity)) {
                return entry.getKey();
            }
        }

        return null;
    }

    @Override
    public void putIdentity(BridgeConnector connector, SBPIdentity identity) {

        Validate.notNull(connector, "Cannot set identity of null connector");
        Validate.notNull(identity, "Cannot set null identity");

        identities.put(connector, identity);
    }

    @Override
    public void removeIdentity(BridgeConnector connector) {

        identities.remove(connector);
    }

    @Override
    public void removeIdentity(SBPIdentity identity) {

        identities.remove(getConnector(identity));
    }

}
