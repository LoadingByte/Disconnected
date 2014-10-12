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

package com.quartercode.disconnected.server.test.bridge;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import com.quartercode.disconnected.server.bridge.DefaultClientIdentityExtension;
import com.quartercode.disconnected.server.client.ClientIdentityService;
import com.quartercode.disconnected.server.test.bridge.DummyEvents.Event1;
import com.quartercode.disconnected.server.test.bridge.DummyEvents.TestLimitedClientEvent;
import com.quartercode.disconnected.shared.client.ClientIdentity;
import com.quartercode.disconnected.shared.event.LimitedClientEvent;
import com.quartercode.eventbridge.bridge.Bridge;
import com.quartercode.eventbridge.bridge.BridgeConnector;
import com.quartercode.eventbridge.bridge.BridgeConnectorException;
import com.quartercode.eventbridge.bridge.Event;
import com.quartercode.eventbridge.bridge.module.ConnectorSenderModule;
import com.quartercode.eventbridge.bridge.module.ConnectorSenderModule.SpecificConnectorSendInterceptor;
import com.quartercode.eventbridge.channel.ChannelInvocation;
import com.quartercode.eventbridge.def.bridge.DefaultBridge;
import com.quartercode.eventbridge.extra.connector.LocalBridgeConnector;

@SuppressWarnings ("unchecked")
public class DefaultClientIdentityBridgeExtensionTest {

    private static final ClientIdentity      CLIENT_2 = new ClientIdentity("client2");
    private static final ClientIdentity      CLIENT_3 = new ClientIdentity("client3");

    @Rule
    public JUnitRuleMockery                  context  = new JUnitRuleMockery();

    private Bridge                           bridge1;
    private Bridge                           bridge2;
    private Bridge                           bridge3;
    private BridgeConnector                  bridge1To2Connector;
    private BridgeConnector                  bridge1To3Connector;
    @Mock
    private SpecificConnectorSendInterceptor interceptor;

    private DefaultClientIdentityExtension   bridge1Extension;
    private DefaultClientIdentityExtension   bridge2Extension;
    private DefaultClientIdentityExtension   bridge3Extension;

    @Mock
    private ClientIdentityService            clientIdentityService;

    @Before
    public void setUp() {

        // Don't mock because it would be too much work
        bridge1 = new DefaultBridge();
        bridge2 = new DefaultBridge();
        bridge3 = new DefaultBridge();
        bridge1To2Connector = new LocalBridgeConnector(bridge2);
        bridge1To3Connector = new LocalBridgeConnector(bridge3);

        // Add the mocked connector send interceptor to bridge 1
        bridge1.getModule(ConnectorSenderModule.class).getSpecificChannel().addInterceptor(new DummySpecificConnectorSendInterceptor(interceptor), 1);

        // Install the extensions on all bridges
        bridge1Extension = new DefaultClientIdentityExtension();
        bridge2Extension = new DefaultClientIdentityExtension();
        bridge3Extension = new DefaultClientIdentityExtension();
        bridge1Extension.setIdentityService(clientIdentityService);
        bridge2Extension.setIdentityService(clientIdentityService);
        bridge3Extension.setIdentityService(clientIdentityService);
        bridge1.addModule(bridge1Extension);
        bridge2.addModule(bridge2Extension);
        bridge3.addModule(bridge3Extension);

        // Initialize the client identities for the bridge connectors
        // @formatter:off
        context.checking(new Expectations() {{

            allowing(clientIdentityService).getIdentity(bridge1To2Connector);
                will(returnValue(CLIENT_2));
            allowing(clientIdentityService).getIdentity(bridge1To3Connector);
                will(returnValue(CLIENT_3));

        }});
        // @formatter:on
    }

    private void expectClientIdentityServiceConnectionListener() {

        // @formatter:off
        context.checking(new Expectations() {{

            // Connections from bridge1 (for now, dummy identities are used by the extension)
            oneOf(clientIdentityService).putIdentity(bridge1To2Connector, new ClientIdentity("client"));
            oneOf(clientIdentityService).putIdentity(bridge1To3Connector, new ClientIdentity("client"));
            // Reverse connections from bridge2 and bridge3
            exactly(2).of(clientIdentityService).putIdentity(with(any(BridgeConnector.class)), with(new ClientIdentity("client")));

        }});
        // @formatter:on
    }

    private void connect() throws BridgeConnectorException {

        bridge1.addConnector(bridge1To2Connector);
        bridge1.addConnector(bridge1To3Connector);
    }

    private void removeExtensions() {

        bridge1.removeModule(bridge1Extension);
        bridge1Extension = null;
        bridge2.removeModule(bridge2Extension);
        bridge2Extension = null;
        bridge3.removeModule(bridge3Extension);
        bridge3Extension = null;
    }

    @Test
    public void testRemoveBeforeConnect() throws BridgeConnectorException {

        final LimitedClientEvent event = new TestLimitedClientEvent(CLIENT_2);

        // @formatter:off
        context.checking(new Expectations() {{

            // Extension removed -> should be sent to all connectors
            oneOf(interceptor).send(with(any(ChannelInvocation.class)), with(event), with(bridge1To2Connector));
            oneOf(interceptor).send(with(any(ChannelInvocation.class)), with(event), with(bridge1To3Connector));

        }});
        // @formatter:on

        removeExtensions();
        connect();
        bridge1.send(event);
    }

    @Test
    public void testRemoveAfterConnect() throws BridgeConnectorException {

        final LimitedClientEvent event = new TestLimitedClientEvent(CLIENT_2);

        // @formatter:off
        context.checking(new Expectations() {{

            // Extension removed -> should be sent to all connectors
            oneOf(interceptor).send(with(any(ChannelInvocation.class)), with(event), with(bridge1To2Connector));
            oneOf(interceptor).send(with(any(ChannelInvocation.class)), with(event), with(bridge1To3Connector));

        }});
        // @formatter:on

        expectClientIdentityServiceConnectionListener();
        connect();
        removeExtensions();
        bridge1.send(event);
    }

    @Test
    public void testDisconnect() throws BridgeConnectorException {

        // @formatter:off
        context.checking(new Expectations() {{

            // Connections from bridge1 (for now, dummy identities are used by the extension)
            oneOf(clientIdentityService).removeIdentity(bridge1To2Connector);
            oneOf(clientIdentityService).removeIdentity(bridge1To3Connector);
            // Reverse connections from bridge2 and bridge3
            exactly(2).of(clientIdentityService).removeIdentity(with(any(BridgeConnector.class)));

        }});
        // @formatter:on

        expectClientIdentityServiceConnectionListener();
        connect();
        bridge1.removeConnector(bridge1To2Connector);
        bridge1.removeConnector(bridge1To3Connector);
    }

    @Test
    public void testSendNoLimitEvent() throws BridgeConnectorException {

        final Event event = new Event1();

        // @formatter:off
        context.checking(new Expectations() {{

            // No limit -> should be sent to all connectors
            oneOf(interceptor).send(with(any(ChannelInvocation.class)), with(event), with(bridge1To2Connector));
            oneOf(interceptor).send(with(any(ChannelInvocation.class)), with(event), with(bridge1To3Connector));

        }});
        // @formatter:on

        expectClientIdentityServiceConnectionListener();
        connect();
        bridge1.send(event);
    }

    @Test
    public void testSendLimitTo2() throws BridgeConnectorException {

        final LimitedClientEvent event = new TestLimitedClientEvent(CLIENT_2);

        // @formatter:off
        context.checking(new Expectations() {{

            // Limited to client 2
            oneOf(interceptor).send(with(any(ChannelInvocation.class)), with(event), with(bridge1To2Connector));

        }});
        // @formatter:on

        expectClientIdentityServiceConnectionListener();
        connect();
        bridge1.send(event);
    }

    @Test
    public void testSendLimitTo3() throws BridgeConnectorException {

        final LimitedClientEvent event = new TestLimitedClientEvent(CLIENT_3);

        // @formatter:off
        context.checking(new Expectations() {{

            // Limited to client 3
            oneOf(interceptor).send(with(any(ChannelInvocation.class)), with(event), with(bridge1To3Connector));

        }});
        // @formatter:on

        expectClientIdentityServiceConnectionListener();
        connect();
        bridge1.send(event);
    }

    @Test
    public void testSendLimitToBoth() throws BridgeConnectorException {

        final LimitedClientEvent event = new TestLimitedClientEvent(CLIENT_2, CLIENT_3);

        // @formatter:off
        context.checking(new Expectations() {{

            // Limited to client 2 and 3
            oneOf(interceptor).send(with(any(ChannelInvocation.class)), with(event), with(bridge1To2Connector));
            oneOf(interceptor).send(with(any(ChannelInvocation.class)), with(event), with(bridge1To3Connector));

        }});
        // @formatter:on

        expectClientIdentityServiceConnectionListener();
        connect();
        bridge1.send(event);
    }

    @Test
    public void testSendLimitToNothing() throws BridgeConnectorException {

        final LimitedClientEvent event = new TestLimitedClientEvent();

        // @formatter:off
        context.checking(new Expectations() {{

            // Limited to neither client 2 nor client 3 -> should not be sent to any connector
            never(interceptor).send(with(any(ChannelInvocation.class)), with(event), with(any(BridgeConnector.class)));

        }});
        // @formatter:on

        expectClientIdentityServiceConnectionListener();
        connect();
        bridge1.send(event);
    }

    private static class DummySpecificConnectorSendInterceptor implements SpecificConnectorSendInterceptor {

        private final SpecificConnectorSendInterceptor dummy;

        private DummySpecificConnectorSendInterceptor(SpecificConnectorSendInterceptor dummy) {

            this.dummy = dummy;
        }

        @Override
        public void send(ChannelInvocation<SpecificConnectorSendInterceptor> invocation, Event event, BridgeConnector connector) {

            dummy.send(invocation, event, connector);
            invocation.next().send(invocation, event, connector);
        }

    }

}
