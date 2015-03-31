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

package com.quartercode.disconnected.server.test.bridge;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import com.quartercode.disconnected.server.bridge.DefaultSBPIdentityExtension;
import com.quartercode.disconnected.server.identity.SBPIdentityService;
import com.quartercode.disconnected.server.test.bridge.DummyEvents.Event1;
import com.quartercode.disconnected.server.test.bridge.DummyEvents.TestLimitedSBPEvent;
import com.quartercode.disconnected.shared.event.LimitedSBPEvent;
import com.quartercode.disconnected.shared.identity.ClientIdentity;
import com.quartercode.disconnected.shared.identity.SBPIdentity;
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
public class DefaultSBPIdentityBridgeExtensionTest {

    private static final SBPIdentity         SBP_2   = new ClientIdentity("sbp2");
    private static final SBPIdentity         SBP_3   = new ClientIdentity("sbp3");

    @Rule
    public JUnitRuleMockery                  context = new JUnitRuleMockery();

    private Bridge                           bridge1;
    private Bridge                           bridge2;
    private Bridge                           bridge3;
    private BridgeConnector                  bridge1To2Connector;
    private BridgeConnector                  bridge1To3Connector;
    @Mock
    private SpecificConnectorSendInterceptor interceptor;

    private DefaultSBPIdentityExtension      bridge1Extension;
    private DefaultSBPIdentityExtension      bridge2Extension;
    private DefaultSBPIdentityExtension      bridge3Extension;

    @Mock
    private SBPIdentityService               sbpIdentityService;

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
        bridge1Extension = new DefaultSBPIdentityExtension();
        bridge2Extension = new DefaultSBPIdentityExtension();
        bridge3Extension = new DefaultSBPIdentityExtension();
        bridge1Extension.setIdentityService(sbpIdentityService);
        bridge2Extension.setIdentityService(sbpIdentityService);
        bridge3Extension.setIdentityService(sbpIdentityService);
        bridge1.addModule(bridge1Extension);
        bridge2.addModule(bridge2Extension);
        bridge3.addModule(bridge3Extension);

        // Initialize the SBP identities for the bridge connectors
        // @formatter:off
        context.checking(new Expectations() {{

            allowing(sbpIdentityService).getIdentity(bridge1To2Connector);
                will(returnValue(SBP_2));
            allowing(sbpIdentityService).getIdentity(bridge1To3Connector);
                will(returnValue(SBP_3));

        }});
        // @formatter:on
    }

    private void expectSBPIdentityServiceConnectionListener() {

        // @formatter:off
        context.checking(new Expectations() {{

            // Connections from bridge1 (for now, dummy identities are used by the extension)
            oneOf(sbpIdentityService).putIdentity(bridge1To2Connector, new ClientIdentity("client"));
            oneOf(sbpIdentityService).putIdentity(bridge1To3Connector, new ClientIdentity("client"));
            // Reverse connections from bridge2 and bridge3
            exactly(2).of(sbpIdentityService).putIdentity(with(any(BridgeConnector.class)), with(new ClientIdentity("client")));

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

        final LimitedSBPEvent event = new TestLimitedSBPEvent(SBP_2);

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

        final LimitedSBPEvent event = new TestLimitedSBPEvent(SBP_2);

        // @formatter:off
        context.checking(new Expectations() {{

            // Extension removed -> should be sent to all connectors
            oneOf(interceptor).send(with(any(ChannelInvocation.class)), with(event), with(bridge1To2Connector));
            oneOf(interceptor).send(with(any(ChannelInvocation.class)), with(event), with(bridge1To3Connector));

        }});
        // @formatter:on

        expectSBPIdentityServiceConnectionListener();
        connect();
        removeExtensions();
        bridge1.send(event);
    }

    @Test
    public void testDisconnect() throws BridgeConnectorException {

        // @formatter:off
        context.checking(new Expectations() {{

            // Connections from bridge1 (for now, dummy identities are used by the extension)
            oneOf(sbpIdentityService).removeIdentity(bridge1To2Connector);
            oneOf(sbpIdentityService).removeIdentity(bridge1To3Connector);
            // Reverse connections from bridge2 and bridge3
            exactly(2).of(sbpIdentityService).removeIdentity(with(any(BridgeConnector.class)));

        }});
        // @formatter:on

        expectSBPIdentityServiceConnectionListener();
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

        expectSBPIdentityServiceConnectionListener();
        connect();
        bridge1.send(event);
    }

    @Test
    public void testSendLimitTo2() throws BridgeConnectorException {

        final LimitedSBPEvent event = new TestLimitedSBPEvent(SBP_2);

        // @formatter:off
        context.checking(new Expectations() {{

            // Limited to SBP 2
            oneOf(interceptor).send(with(any(ChannelInvocation.class)), with(event), with(bridge1To2Connector));

        }});
        // @formatter:on

        expectSBPIdentityServiceConnectionListener();
        connect();
        bridge1.send(event);
    }

    @Test
    public void testSendLimitTo3() throws BridgeConnectorException {

        final LimitedSBPEvent event = new TestLimitedSBPEvent(SBP_3);

        // @formatter:off
        context.checking(new Expectations() {{

            // Limited to SBP 3
            oneOf(interceptor).send(with(any(ChannelInvocation.class)), with(event), with(bridge1To3Connector));

        }});
        // @formatter:on

        expectSBPIdentityServiceConnectionListener();
        connect();
        bridge1.send(event);
    }

    @Test
    public void testSendLimitToBoth() throws BridgeConnectorException {

        final LimitedSBPEvent event = new TestLimitedSBPEvent(SBP_2, SBP_3);

        // @formatter:off
        context.checking(new Expectations() {{

            // Limited to SBPs 2 and 3
            oneOf(interceptor).send(with(any(ChannelInvocation.class)), with(event), with(bridge1To2Connector));
            oneOf(interceptor).send(with(any(ChannelInvocation.class)), with(event), with(bridge1To3Connector));

        }});
        // @formatter:on

        expectSBPIdentityServiceConnectionListener();
        connect();
        bridge1.send(event);
    }

    @Test
    public void testSendLimitToNothing() throws BridgeConnectorException {

        final LimitedSBPEvent event = new TestLimitedSBPEvent();

        // @formatter:off
        context.checking(new Expectations() {{

            // Limited to neither SBP 2 nor SBP 3 -> should not be sent to any connector
            never(interceptor).send(with(any(ChannelInvocation.class)), with(event), with(any(BridgeConnector.class)));

        }});
        // @formatter:on

        expectSBPIdentityServiceConnectionListener();
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
