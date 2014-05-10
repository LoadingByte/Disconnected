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

package com.quartercode.disconnected.bridge.connector;

import com.quartercode.disconnected.bridge.AbstractBridgeConnector;
import com.quartercode.disconnected.bridge.Bridge;
import com.quartercode.disconnected.bridge.BridgeConnector;
import com.quartercode.disconnected.bridge.BridgeConnectorException;
import com.quartercode.disconnected.bridge.Event;

/**
 * The local bridge connector is a simple {@link BridgeConnector} that connects two {@link Bridge}s that run on the same vm.
 * 
 * @see BridgeConnector
 */
public class LocalBridgeConnector extends AbstractBridgeConnector {

    private Bridge               remote;
    private LocalBridgeConnector remoteConnector;

    /**
     * Creates a new local bridge connector that connects to the given remote {@link Bridge}.
     * 
     * @param remote The second {@link Bridge} the local bridge connector connects to.
     */
    public LocalBridgeConnector(Bridge remote) {

        this.remote = remote;
    }

    private LocalBridgeConnector(LocalBridgeConnector remoteConnector) {

        this.remoteConnector = remoteConnector;
    }

    @Override
    public void start(Bridge localBridge) throws BridgeConnectorException {

        super.start(localBridge);

        // Connect the remote bridge with the local bridge (add reverse connection)
        if (remote != null) {
            remoteConnector = new LocalBridgeConnector(this);
            remote.connect(remoteConnector);
        }
    }

    @Override
    public void stop() throws BridgeConnectorException {

        super.stop();

        // Disconnect the remote bridge from the local bridge (remove reverse connection)
        if (remote != null) {
            remote.disconnect(remoteConnector);
        }

        remote = null;
        remoteConnector = null;
    }

    @Override
    public void send(Event event) {

        remoteConnector.handle(event);
    }

    private void handle(Event event) {

        getLocalBridge().handle(this, event);
    }

}
