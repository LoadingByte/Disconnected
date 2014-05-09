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
import com.quartercode.disconnected.util.RunnableInvocationProvider;

/**
 * The local bridge connector is a simple {@link BridgeConnector} that connects two {@link Bridge}s that run on the same vm.
 * It also allows the event handlers of a bridge to be called by certain {@link RunnableInvocationProvider}s.
 * 
 * @see BridgeConnector
 */
public class LocalBridgeConnector extends AbstractBridgeConnector {

    private Bridge                     remote;
    private RunnableInvocationProvider localInvocProvider;
    private RunnableInvocationProvider remoteInvocProvider;

    private LocalBridgeConnector       remoteConnector;

    /**
     * Creates a new local bridge connector that connects to the given remote {@link Bridge}.
     * Also sets the {@link RunnableInvocationProvider} the event handlers of the bridges are called by.
     * 
     * @param remote The second {@link Bridge} the local bridge connector connects to.
     * @param localInvocProvider The runnable invocation provider that executes the event handler calls of all received events.
     * @param remoteInvocProvider The runnable invocation provider that executes the event handler calls of all sent events.
     */
    public LocalBridgeConnector(Bridge remote, RunnableInvocationProvider localInvocProvider, RunnableInvocationProvider remoteInvocProvider) {

        this.remote = remote;
        this.localInvocProvider = localInvocProvider;
        this.remoteInvocProvider = remoteInvocProvider;
    }

    private LocalBridgeConnector(RunnableInvocationProvider localInvocProvider, LocalBridgeConnector remoteConnector) {

        this.localInvocProvider = localInvocProvider;
        this.remoteConnector = remoteConnector;
    }

    @Override
    public void start(Bridge localBridge) throws BridgeConnectorException {

        super.start(localBridge);

        // Connect the remote bridge with the local bridge (add reverse connection)
        if (remote != null && remoteInvocProvider != null) {
            remoteConnector = new LocalBridgeConnector(remoteInvocProvider, this);
            remote.connect(remoteConnector);
        }
    }

    @Override
    public void stop() throws BridgeConnectorException {

        super.stop();

        // Disconnect the remote bridge from the local bridge (remove reverse connection)
        if (remote != null && remoteInvocProvider != null) {
            remote.disconnect(remoteConnector);
        }

        remote = null;
        localInvocProvider = null;
        remoteInvocProvider = null;
        remoteConnector = null;
    }

    @Override
    public void send(Event event) {

        remoteConnector.handle(event);
    }

    private void handle(final Event event) {

        localInvocProvider.invoke(new Runnable() {

            @Override
            public void run() {

                getLocalBridge().handle(LocalBridgeConnector.this, event);
            }

        });
    }

}
