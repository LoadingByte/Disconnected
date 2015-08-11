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

import static org.junit.Assert.*;
import java.util.ArrayList;
import java.util.List;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import com.quartercode.disconnected.server.world.comp.hardware.NetInterface;
import com.quartercode.disconnected.server.world.comp.net.NetworkModule;
import com.quartercode.disconnected.server.world.comp.net.Packet;
import com.quartercode.disconnected.server.world.comp.net.StandardPacket;
import com.quartercode.disconnected.server.world.comp.net.nodes.ComputerConnectedNode;
import com.quartercode.disconnected.server.world.comp.net.socket.Socket;
import com.quartercode.disconnected.server.world.comp.net.socket.SocketRegistry;
import com.quartercode.disconnected.server.world.comp.net.socket.SocketState;
import com.quartercode.disconnected.shared.world.comp.net.Address;
import com.quartercode.disconnected.shared.world.comp.net.NetId;

public class SocketRegistryTest {

    private static final NetId       LOCAL_NET_ID                    = new NetId(0);
    private static final Address     EMPTY_ADDRESS                   = new Address("0:1");

    @Rule
    public JUnitRuleMockery          context                         = new JUnitRuleMockery();

    private final TestSocketRegistry socketRegistry                  = new TestSocketRegistry();
    private final NetworkModule      networkModule                   = new MockNetworkModule();
    private final NetInterface       netInterface                    = new NetInterface("");

    private final List<Packet>       packetsDeliveredToNetworkModule = new ArrayList<>();

    @Before
    public void setUp() {

        netInterface.setNetNode(new ComputerConnectedNode() {

            @Override
            public NetId getNetId() {

                return LOCAL_NET_ID;
            }

            @Override
            public void process(Packet packet) {

                // Empty
            }

        });
    }

    @Test
    public void testCreateSocket() {

        Socket socket = socketRegistry.createSocket(EMPTY_ADDRESS, 1);

        assertTrue("Socket wasn't added to the socket registry's socket list", socketRegistry.getSockets().contains(socket));
    }

    @Test
    public void testDisconnectCreatedSocket() {

        Socket socket = socketRegistry.createSocket(EMPTY_ADDRESS, 1);
        socket.disconnect();

        assertFalse("Socket wasn't removed from the socket registry's socket list", socketRegistry.getSockets().contains(socket));
    }

    @Test
    public void testSetRunningFalseDisconnectCreatedSockets() {

        Socket socket1 = socketRegistry.createSocket(EMPTY_ADDRESS, 1);
        Socket socket2 = socketRegistry.createSocket(EMPTY_ADDRESS, 2);

        socketRegistry.setRunning(false);

        assertEquals("State of socket 1", SocketState.DISCONNECTED, socket1.getState());
        assertEquals("State of socket 2", SocketState.DISCONNECTED, socket1.getState());

        assertFalse("Socket 1 wasn't removed from the socket registry's socket list", socketRegistry.getSockets().contains(socket1));
        assertFalse("Socket 2 wasn't removed from the socket registry's socket list", socketRegistry.getSockets().contains(socket2));
    }

    @Test
    public void testSendPacket() {

        int localPort = 12345;
        Address sourceAddress = new Address(LOCAL_NET_ID, localPort);
        Address destinationAddress = new Address(new NetId(1), 54321);

        Socket socket = socketRegistry.createSocket(destinationAddress, localPort);
        socketRegistry.sendPacket(socket, "testdata", true);

        Packet expectedPacket = new StandardPacket(sourceAddress, destinationAddress, "testdata");
        assertArrayEquals("Packets delivered to the net interface", new Packet[] { expectedPacket }, packetsDeliveredToNetworkModule.toArray());
    }

    @Test
    public void testHandlePacket() {

        Address sourceAddress = new Address(new NetId(1), 12345);
        int destinationPort = 54321;
        Address destinationAddress = new Address(LOCAL_NET_ID, destinationPort);

        Socket receiverSocket = socketRegistry.createSocket(sourceAddress, destinationPort);
        // "Connect" the socket
        TestSocket.sneakyStateChange(receiverSocket, SocketState.CONNECTED);

        // Construct and send the test packet
        Packet packet = new StandardPacket(sourceAddress, destinationAddress, "testdata");
        socketRegistry.handlePacket(packet);

        // Check that the packet has arrived at the correct socket
        assertArrayEquals("Incoming packet buffer of socket after packet handling", new Object[] { "testdata" }, TestSocket.getIncomingPacketBuffer(receiverSocket).toArray());
    }

    private class TestSocketRegistry extends SocketRegistry {

        public TestSocketRegistry() {

        }

        @Override
        protected NetworkModule getNetworkModule() {

            return networkModule;
        }

        // Make method accessible
        @Override
        protected void sendPacket(Socket socket, Object data, boolean instantly) {

            super.sendPacket(socket, data, instantly);
        }

    }

    private class MockNetworkModule extends NetworkModule {

        @Override
        public NetInterface getNetInterface() {

            return netInterface;
        }

        @Override
        public void sendPacket(Packet packet, boolean instantly) {

            packetsDeliveredToNetworkModule.add(packet);
        }

    }

}
