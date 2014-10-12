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
import com.quartercode.eventbridge.basic.AbstractBridgeModule;
import com.quartercode.eventbridge.bridge.Bridge;
import com.quartercode.eventbridge.bridge.Bridge.ModifyConnectorListListener;
import com.quartercode.eventbridge.bridge.BridgeConnector;
import com.quartercode.eventbridge.bridge.Event;
import com.quartercode.eventbridge.bridge.module.ConnectorSenderModule;
import com.quartercode.eventbridge.bridge.module.ConnectorSenderModule.SpecificConnectorSendInterceptor;
import com.quartercode.eventbridge.channel.ChannelInvocation;
import com.quartercode.eventbridge.factory.Factory;

/**
 * The default default implementation of the {@link ClientIdentityExtension} interface.
 * 
 * @see ClientIdentityExtension
 */
public class DefaultClientIdentityExtension extends AbstractBridgeModule implements ClientIdentityExtension {

    private final CIBEModifyConnectorListListener      modifyConnectorListListener      = new CIBEModifyConnectorListListener();
    private final CIBESpecificConnectorSendInterceptor specificConnectorSendInterceptor = new CIBESpecificConnectorSendInterceptor();

    private ClientIdentityService                      identityService;

    @Override
    public void add(Bridge bridge) {

        super.add(bridge);

        bridge.addModifyConnectorListListener(modifyConnectorListListener);
        bridge.getModule(ConnectorSenderModule.class).getSpecificChannel().addInterceptor(specificConnectorSendInterceptor, 50);
    }

    @Override
    public void remove() {

        getBridge().removeModifyConnectorListListener(modifyConnectorListListener);
        getBridge().getModule(ConnectorSenderModule.class).getSpecificChannel().removeInterceptor(specificConnectorSendInterceptor);

        super.remove();
    }

    @Override
    public ClientIdentityService getIdentityService() {

        return identityService;
    }

    @Override
    public void setIdentityService(ClientIdentityService identityService) {

        this.identityService = identityService;
    }

    private class CIBEModifyConnectorListListener implements ModifyConnectorListListener {

        @Override
        public void onAddConnector(BridgeConnector connector, Bridge bridge) {

            // TODO: Identify connector

            // This code just adds a dummy identity for a single player
            identityService.putIdentity(connector, new ClientIdentity("client"));
        }

        @Override
        public void onRemoveConnector(BridgeConnector connector, Bridge bridge) {

            identityService.removeIdentity(connector);
        }

    }

    private class CIBESpecificConnectorSendInterceptor implements SpecificConnectorSendInterceptor {

        @Override
        public void send(ChannelInvocation<SpecificConnectorSendInterceptor> invocation, Event event, BridgeConnector connector) {

            if (isAllowed(event, connector)) {
                invocation.next().send(invocation, event, connector);
            }
        }

        private boolean isAllowed(Event event, BridgeConnector connector) {

            if (! (event instanceof LimitedClientEvent)) {
                return true;
            } else {
                ClientIdentity connectorIdentity = identityService.getIdentity(connector);

                for (ClientIdentity eventIdentity : ((LimitedClientEvent) event).getClients()) {
                    if (eventIdentity.equals(connectorIdentity)) {
                        return true;
                    }
                }

                return false;
            }
        }

    }

    /**
     * A {@link Factory} for the {@link DefaultClientIdentityExtension} object.
     */
    public static class DefaultClientIdentityExtensionFactory implements Factory {

        @Override
        public Object create() {

            return new DefaultClientIdentityExtension();
        }

    }

}
