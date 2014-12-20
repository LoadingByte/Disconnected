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

package com.quartercode.disconnected.server.test.world.comp.net;

import static com.quartercode.classmod.extra.func.Priorities.LEVEL_9;
import static org.junit.Assert.assertEquals;
import org.jmock.Expectations;
import org.jmock.Sequence;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import com.quartercode.classmod.def.extra.conv.DefaultCFeatureHolder;
import com.quartercode.classmod.extra.func.FunctionExecutor;
import com.quartercode.classmod.extra.func.FunctionInvocation;
import com.quartercode.classmod.util.test.JUnitRuleModMockery;
import com.quartercode.disconnected.server.util.ObjArray;
import com.quartercode.disconnected.server.world.comp.net.NetModule;
import com.quartercode.disconnected.server.world.comp.net.Packet;
import com.quartercode.disconnected.server.world.comp.net.PacketHandler;
import com.quartercode.disconnected.server.world.comp.net.Socket;
import com.quartercode.disconnected.server.world.comp.net.Socket.SocketState;
import com.quartercode.disconnected.shared.world.comp.net.Address;
import com.quartercode.disconnected.shared.world.comp.net.NetID;

@SuppressWarnings ("unchecked")
public class SocketConnectionTest {

    @Rule
    public JUnitRuleMockery    context = new JUnitRuleMockery();
    @Rule
    public JUnitRuleModMockery modmock = new JUnitRuleModMockery();

    private NetModule          netModule;
    @Mock
    private NetModuleSendHook  mockNetModuleSendHook;

    private Address            socket1Address;
    private Address            socket2Address;
    private Socket             socket1;
    private Socket             socket2;

    @Before
    public void setUp() {

        netModule = new NetModule();

        // Create the test sockets
        socket1Address = new Address(new NetID(0, 1), 10);
        socket2Address = new Address(new NetID(0, 2), 20);
        socket1 = createSocket(netModule, socket1Address.getPort(), socket2Address);
        socket2 = createSocket(netModule, socket2Address.getPort(), socket1Address);

        // Apply a simple routing:
        // socket1 -> socket2
        // socket2 -> socket1
        modmock.addFuncExec(NetModule.SEND_TCP, "hook", NetModule.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                Socket socket = (Socket) arguments[0];
                Object data = arguments[1];

                // Invoke the custom hook
                mockNetModuleSendHook.onSend(socket, data);

                Packet packet = new Packet();
                packet.setObj(Packet.PROTOCOL, "tcp");
                packet.setObj(Packet.DATA, data);

                if (socket == socket1) {
                    packet.setObj(Packet.SOURCE, socket1Address);
                    packet.setObj(Packet.DESTINATION, socket2Address);
                    socket2.invoke(Socket.HANDLE, packet);
                } else if (socket == socket2) {
                    packet.setObj(Packet.SOURCE, socket2Address);
                    packet.setObj(Packet.DESTINATION, socket1Address);
                    socket1.invoke(Socket.HANDLE, packet);
                }

                // Stop the invocation
                return null;
            }

        }, LEVEL_9);
    }

    @Before
    public void setUpCurrentSeqNumberGenerator() {

        /*
         * Change the sequence number generator of the Socket class in order to generate predictable numbers.
         * Each socket always gets a sequence number which is equal to the socket's local port.
         * Socket 1 always gets the sequence number 10 while socket 2 always gets the sequence number 20.
         */
        modmock.addPropSetter(Socket.CURRENT_SEQ_NUMBER, "testGenerate", Socket.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                Socket holder = (Socket) invocation.getCHolder();

                if ((int) arguments[0] < 0) {
                    int newSequenceNumber = holder.getObj(Socket.LOCAL_PORT);
                    holder.setObj(Socket.CURRENT_SEQ_NUMBER, newSequenceNumber);

                    // Cancel the invocation chain; the next generator should not be invoked
                    return null;
                } else {
                    return invocation.next(arguments);
                }
            }

        }, LEVEL_9);
    }

    @Test
    public void testSendAndHandle() {

        // Connect the two test sockets
        socket1.setObj(Socket.STATE, SocketState.CONNECTED);
        socket2.setObj(Socket.STATE, SocketState.CONNECTED);

        // Add some packet handlers for checking that the correct packets arrive
        final FunctionExecutor<Void> packetHandler1Hook = context.mock(FunctionExecutor.class, "packetHandler1Hook");
        final FunctionExecutor<Void> packetHandler2Hook = context.mock(FunctionExecutor.class, "packetHandler2Hook");
        modmock.addFuncExec(PacketHandler.HANDLE, "packetHandler1Hook", HookedPacketHandler1.class, packetHandler1Hook);
        modmock.addFuncExec(PacketHandler.HANDLE, "packetHandler2Hook", HookedPacketHandler2.class, packetHandler2Hook);

        // @formatter:off
        context.checking(new Expectations() {{

            final Sequence sequence = context.sequence("sequence");

            oneOf(mockNetModuleSendHook).onSend(socket1, "testdata1"); inSequence(sequence);
            oneOf(packetHandler2Hook).invoke(with(any(FunctionInvocation.class)), with(new Object[] { socket2, "testdata1" })); inSequence(sequence);
            oneOf(mockNetModuleSendHook).onSend(socket2, "testdata2"); inSequence(sequence);
            oneOf(packetHandler1Hook).invoke(with(any(FunctionInvocation.class)), with(new Object[] { socket1, "testdata2" })); inSequence(sequence);

        }});
        // @formatter:on

        socket1.addToColl(Socket.PACKET_HANDLERS, new HookedPacketHandler1());
        socket2.addToColl(Socket.PACKET_HANDLERS, new HookedPacketHandler2());

        // Send test packets

        socket1.invoke(Socket.SEND, "testdata1");
        socket1.get(Socket.SCHEDULER).update("computerNetworkUpdate"); // Send
        socket2.get(Socket.SCHEDULER).update("computerProgramUpdate"); // Handle

        socket2.invoke(Socket.SEND, "testdata2");
        socket2.get(Socket.SCHEDULER).update("computerNetworkUpdate"); // Send
        socket1.get(Socket.SCHEDULER).update("computerProgramUpdate"); // Handle
    }

    @Test
    public void testHandshake() {

        // @formatter:off
        context.checking(new Expectations() {{

            final Sequence handshake = context.sequence("handshake");

            // The sequence numbers are predictable because the test replaces the generator:
            // Socket 1: 10
            // Socket 2: 20
            oneOf(mockNetModuleSendHook).onSend(socket1, new ObjArray("$_handshake", "syn", 10)); inSequence(handshake);
            oneOf(mockNetModuleSendHook).onSend(socket2, new ObjArray( "$_handshake", "syn-ack", 20, 10 + 1)); inSequence(handshake);
            oneOf(mockNetModuleSendHook).onSend(socket1, new ObjArray( "$_handshake", "ack", 20 + 1)); inSequence(handshake);

        }});
        // @formatter:on

        // Connect the two sockets
        socket1.invoke(Socket.CONNECT);

        // Check the socket states
        assertEquals("State of socket 1 after the handshake", SocketState.CONNECTED, socket1.getObj(Socket.STATE));
        assertEquals("State of socket 2 after the handshake", SocketState.CONNECTED, socket2.getObj(Socket.STATE));

        // Send some packets in order to ensure that the connection functions
        // Note that this test is very basic and not very reliable

        // @formatter:off
        context.checking(new Expectations() {{

            final Sequence packetSending = context.sequence("packetSending");

            oneOf(mockNetModuleSendHook).onSend(socket1,  "testdata1"); inSequence(packetSending);
            oneOf(mockNetModuleSendHook).onSend(socket2,  "testdata2"); inSequence(packetSending);

        }});
        // @formatter:on

        socket1.invoke(Socket.SEND, "testdata1");
        socket1.get(Socket.SCHEDULER).update("computerNetworkUpdate"); // Send
        socket2.get(Socket.SCHEDULER).update("computerProgramUpdate"); // Handle

        socket2.invoke(Socket.SEND, "testdata2");
        socket2.get(Socket.SCHEDULER).update("computerNetworkUpdate"); // Send
        socket1.get(Socket.SCHEDULER).update("computerProgramUpdate"); // Handle
    }

    @Test
    public void testConnectionTimeout() {

        // Disconnect socket 2 so it can't send or receive any packets
        socket2.setObj(Socket.STATE, SocketState.DISCONNECTED);

        // @formatter:off
        context.checking(new Expectations() {{

            final Sequence packetSending = context.sequence("packetSending");

            oneOf(mockNetModuleSendHook).onSend(socket1, new ObjArray("$_handshake", "syn", 10)); inSequence(packetSending);
            oneOf(mockNetModuleSendHook).onSend(socket1, "$_teardown"); inSequence(packetSending);

        }});
        // @formatter:on

        // Connect the two sockets
        socket1.invoke(Socket.CONNECT);

        // Let socket1 time out
        for (int update = 0; update < Socket.CONNECTION_TIMEOUT; update++) {
            socket1.get(Socket.SCHEDULER).update("computerNetworkUpdate");
        }

        // Check the socket states
        assertEquals("State of socket 1 after the connection timeout", SocketState.DISCONNECTED, socket1.getObj(Socket.STATE));
        assertEquals("State of socket 2 after the connection timeout", SocketState.DISCONNECTED, socket2.getObj(Socket.STATE));
    }

    @Test
    public void testKeepalive() {

        // Connect the two test sockets
        socket1.setObj(Socket.STATE, SocketState.CONNECTED);
        socket2.setObj(Socket.STATE, SocketState.CONNECTED);

        // @formatter:off
        context.checking(new Expectations() {{

            final Sequence keepalive = context.sequence("keepalive");

            oneOf(mockNetModuleSendHook).onSend(socket1, new ObjArray("$_keepalive", "req")); inSequence(keepalive);
            oneOf(mockNetModuleSendHook).onSend(socket2, new ObjArray("$_keepalive", "rsp")); inSequence(keepalive);

        }});
        // @formatter:on

        // Let socket1 send a keepalive request and check whether a response has arrived
        for (int update = 0; update < Socket.KEEPALIVE_PERIOD + Socket.KEEPALIVE_REPONSE_TIMEOUT; update++) {
            socket1.get(Socket.SCHEDULER).update("computerNetworkUpdate");
        }

        // Check the socket states
        assertEquals("State of socket 1 after the successful keepalive", SocketState.CONNECTED, socket1.getObj(Socket.STATE));
        assertEquals("State of socket 2 after the successful keepalive", SocketState.CONNECTED, socket2.getObj(Socket.STATE));
    }

    @Test
    public void testKeepaliveNoResponse() {

        // Connect socket 1
        socket1.setObj(Socket.STATE, SocketState.CONNECTED);
        // Disconnect socket 2 so it can't send or receive any packets
        socket2.setObj(Socket.STATE, SocketState.DISCONNECTED);

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
            socket1.get(Socket.SCHEDULER).update("computerNetworkUpdate");
        }

        // Check the socket states
        assertEquals("State of socket 1 after the unsuccessful handshake", SocketState.DISCONNECTED, socket1.getObj(Socket.STATE));
        assertEquals("State of socket 2 after the unsuccessful handshake", SocketState.DISCONNECTED, socket2.getObj(Socket.STATE));
    }

    @Test
    public void testTeardown() {

        // Connect the two test sockets
        socket1.setObj(Socket.STATE, SocketState.CONNECTED);
        socket2.setObj(Socket.STATE, SocketState.CONNECTED);

        // @formatter:off
        context.checking(new Expectations() {{

            oneOf(mockNetModuleSendHook).onSend(socket1, "$_teardown");

        }});
        // @formatter:on

        // Disconnect the two sockets
        socket1.invoke(Socket.DISCONNECT);

        // Check the socket states
        assertEquals("State of socket 1 after the handshake", SocketState.DISCONNECTED, socket1.getObj(Socket.STATE));
        assertEquals("State of socket 2 after the handshake", SocketState.DISCONNECTED, socket2.getObj(Socket.STATE));
    }

    private Socket createSocket(NetModule netModule, int localPort, Address destination) {

        Socket socket = new Socket();
        socket.setParent(netModule);
        socket.setObj(Socket.LOCAL_PORT, localPort);
        socket.setObj(Socket.DESTINATION, destination);

        return socket;
    }

    private static interface NetModuleSendHook {

        public void onSend(Socket socket, Object data);

    }

    private static class HookedPacketHandler1 extends DefaultCFeatureHolder implements PacketHandler {

    }

    private static class HookedPacketHandler2 extends DefaultCFeatureHolder implements PacketHandler {

    }

}
