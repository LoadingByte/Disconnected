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

package com.quartercode.disconnected.server.test.world.comp.net.socket;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import java.util.List;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.jmock.Expectations;
import org.jmock.Sequence;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import com.quartercode.disconnected.server.util.ObjArray;
import com.quartercode.disconnected.server.world.comp.net.socket.Socket;
import com.quartercode.disconnected.server.world.comp.net.socket.SocketRegistry;
import com.quartercode.disconnected.server.world.comp.net.socket.SocketState;
import com.quartercode.disconnected.shared.world.comp.net.Address;

@SuppressWarnings ("unchecked")
public class SocketConnectionTest {

    private static final Address SOCKET_1_ADDRESS = new Address("1:1");
    private static final Address SOCKET_2_ADDRESS = new Address("2:2");

    @Rule
    public JUnitRuleMockery      context          = new JUnitRuleMockery();

    private SocketRegistry       socketRegistry;
    @Mock
    private NetModuleSendHook    mockNetModuleSendHook;

    private CustomTestSocket     socket1;
    private CustomTestSocket     socket2;

    @Before
    public void setUp() {

        socketRegistry = new SocketRegistry() {

            /*
             * Apply a simple routing:
             * socket1 -> socket2
             * socket2 -> socket1
             */
            @Override
            protected void sendPacket(Socket socket, Object data, boolean instantly) {

                // Invoke the custom hook
                mockNetModuleSendHook.onSend(socket, data);

                if (socket == socket1) {
                    socket2.handle(data);
                } else if (socket == socket2) {
                    socket1.handle(data);
                }
            }

        };

        // Create the test sockets
        socket1 = createSocket(SOCKET_2_ADDRESS, SOCKET_1_ADDRESS.getPort());
        socket2 = createSocket(SOCKET_1_ADDRESS, SOCKET_2_ADDRESS.getPort());
    }

    private CustomTestSocket createSocket(Address destination, int localPort) {

        try {
            CustomTestSocket socket = new CustomTestSocket();
            ((List<Socket>) FieldUtils.readField(socketRegistry, "sockets", true)).add(socket);
            socket.initialize(localPort, destination);
            return socket;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testSendAndHandle() {

        // "Connect" the two test sockets
        socket1.sneakyStateChange(SocketState.CONNECTED);
        socket2.sneakyStateChange(SocketState.CONNECTED);

        // Check whether the two sockets are "connected" properly
        sendAndAssertTestPackets();
    }

    private void sendAndAssertTestPackets() {

        // Assert that the following packets are sent correctly
        // @formatter:off
        context.checking(new Expectations() {{

            final Sequence sequence = context.sequence("sequence");

            oneOf(mockNetModuleSendHook).onSend(socket1, "testdata1"); inSequence(sequence);
            oneOf(mockNetModuleSendHook).onSend(socket2, "testdata2"); inSequence(sequence);

        }});
        // @formatter:on

        // Send test packets
        socket1.send("testdata1");
        socket2.send("testdata2");

        // Assert that the packets have arrived correctly
        assertArrayEquals("Packets received by socket 1", new Object[] { "testdata2" }, socket1.getIncomingPacketBuffer().toArray());
        assertArrayEquals("Packets received by socket 2", new Object[] { "testdata1" }, socket2.getIncomingPacketBuffer().toArray());
    }

    @Test
    public void testHandshake() {

        // @formatter:off
        context.checking(new Expectations() {{

            final Sequence handshake = context.sequence("handshake");

            // The sequence numbers are predictable because the test replaces the generator (see the class at the bottom of this test file):
            // Socket 1: 10
            // Socket 2: 20
            oneOf(mockNetModuleSendHook).onSend(socket1, new ObjArray("$_handshake", "syn", 10)); inSequence(handshake);
            oneOf(mockNetModuleSendHook).onSend(socket2, new ObjArray("$_handshake", "syn-ack", 20, 10 + 1)); inSequence(handshake);
            oneOf(mockNetModuleSendHook).onSend(socket1, new ObjArray("$_handshake", "ack", 20 + 1)); inSequence(handshake);

        }});
        // @formatter:on

        // Connect the two sockets via the regular handshake protocol
        socket1.connect();

        // Check the socket states
        assertEquals("State of socket 1 after the handshake", SocketState.CONNECTED, socket1.getState());
        assertEquals("State of socket 2 after the handshake", SocketState.CONNECTED, socket2.getState());

        // Send some packets in order to ensure that the connection functions
        // Note that this test is very basic and not very reliable

        sendAndAssertTestPackets();
    }

    @Test
    public void testConnectionTimeout() {

        // "Disconnect" socket 2 so it can neither send nor receive packets
        socket2.sneakyStateChange(SocketState.DISCONNECTED);

        // @formatter:off
        context.checking(new Expectations() {{

            final Sequence packetSending = context.sequence("packetSending");

            oneOf(mockNetModuleSendHook).onSend(socket1, new ObjArray("$_handshake", "syn", 10)); inSequence(packetSending);
            oneOf(mockNetModuleSendHook).onSend(socket1, "$_teardown"); inSequence(packetSending);

        }});
        // @formatter:on

        // Try to connect the two sockets
        socket1.connect();

        // Let socket1 time out
        for (int update = 0; update < Socket.CONNECTION_TIMEOUT; update++) {
            socket1.getScheduler().update("networkUpdate");
        }

        // Check the socket states
        assertEquals("State of socket 1 after the connection timeout", SocketState.DISCONNECTED, socket1.getState());
        assertEquals("State of socket 2 after the connection timeout", SocketState.DISCONNECTED, socket2.getState());
    }

    @Test
    public void testKeepalive() {

        // "Connect" the two test sockets
        socket1.setState(SocketState.CONNECTED);
        socket2.setState(SocketState.CONNECTED);

        // @formatter:off
        context.checking(new Expectations() {{

            final Sequence keepalive = context.sequence("keepalive");

            oneOf(mockNetModuleSendHook).onSend(socket1, new ObjArray("$_keepalive", "req")); inSequence(keepalive);
            oneOf(mockNetModuleSendHook).onSend(socket2, new ObjArray("$_keepalive", "rsp")); inSequence(keepalive);

        }});
        // @formatter:on

        // Let socket1 send a keepalive request and check whether a response has arrived
        for (int update = 0; update < Socket.KEEPALIVE_PERIOD + Socket.KEEPALIVE_REPONSE_TIMEOUT; update++) {
            socket1.getScheduler().update("networkUpdate");
        }

        // Check the socket states
        assertEquals("State of socket 1 after the successful keepalive", SocketState.CONNECTED, socket1.getState());
        assertEquals("State of socket 2 after the successful keepalive", SocketState.CONNECTED, socket2.getState());
    }

    @Test
    public void testKeepaliveNoResponse() {

        // "Connect" socket 1
        socket1.setState(SocketState.CONNECTED);
        // "Disconnect" socket 2 so it can neither send nor receive packets
        socket2.sneakyStateChange(SocketState.DISCONNECTED);

        // @formatter:off
        context.checking(new Expectations() {{

            final Sequence packetSending = context.sequence("packetSending");

            oneOf(mockNetModuleSendHook).onSend(socket1, new ObjArray("$_keepalive", "req")); inSequence(packetSending);
            never(mockNetModuleSendHook).onSend(socket2, new ObjArray("$_keepalive", "rsp"));
            oneOf(mockNetModuleSendHook).onSend(socket1, "$_teardown"); inSequence(packetSending);

        }});
        // @formatter:on

        // Let socket1 send a keepalive request and check whether a response has arrived
        for (int update = 0; update < Socket.KEEPALIVE_PERIOD + Socket.KEEPALIVE_REPONSE_TIMEOUT; update++) {
            socket1.getScheduler().update("networkUpdate");
        }

        // Check the socket states
        assertEquals("State of socket 1 after the unsuccessful keepalive", SocketState.DISCONNECTED, socket1.getState());
        assertEquals("State of socket 2 after the unsuccessful keepalive", SocketState.DISCONNECTED, socket2.getState());
    }

    @Test
    public void testTeardown() {

        // "Connect" the two test sockets
        socket1.sneakyStateChange(SocketState.CONNECTED);
        socket2.sneakyStateChange(SocketState.CONNECTED);

        // @formatter:off
        context.checking(new Expectations() {{

            oneOf(mockNetModuleSendHook).onSend(socket1, "$_teardown");

        }});
        // @formatter:on

        // Disconnect the two sockets
        socket1.disconnect();

        // Check the socket states
        assertEquals("State of socket 1 after the teardown", SocketState.DISCONNECTED, socket1.getState());
        assertEquals("State of socket 2 after the teardown", SocketState.DISCONNECTED, socket2.getState());
    }

    private static interface NetModuleSendHook {

        public void onSend(Socket socket, Object data);

    }

    private static class CustomTestSocket extends TestSocket {

        /*
         * Change the sequence number generator of the Socket class in order to generate predictable numbers.
         * Each socket always gets a sequence number which is equal to the socket's local port.
         * Socket 1 always gets the sequence number 10 while socket 2 always gets the sequence number 20.
         */
        @Override
        protected void generateSeqNumber() {

            try {
                FieldUtils.writeField(this, "currentSeqNumber", getLocalPort() * 10, true);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

    }

}
