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

package com.quartercode.disconnected.server.test.world.comp.net;

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
import com.quartercode.disconnected.shared.world.comp.net.Address;
import com.quartercode.disconnected.shared.world.comp.net.NetId;

public class NetworkModuleTest {

    private static final NetId   LOCAL_NET_ID                   = new NetId(0);
    private static final Address EMPTY_ADDRESS                  = new Address("0:1");

    @Rule
    public JUnitRuleMockery      context                        = new JUnitRuleMockery();

    private final NetworkModule  netModule                      = new TestNetworkModule();
    private final NetInterface   netInterface                   = new NetInterface("");

    private final List<Packet>   packetsDeliveredToNetInterface = new ArrayList<>();

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

        Socket socket = netModule.createSocket(EMPTY_ADDRESS, 1);

        assertTrue("Socket wasn't added to the network module's socket list", netModule.getSockets().contains(socket));
    }

    @Test
    public void testDisconnectCreatedSocket() {

        Socket socket = netModule.createSocket(EMPTY_ADDRESS, 1);
        socket.disconnect();

        assertFalse("Socket wasn't removed from the network module's socket list", netModule.getSockets().contains(socket));
    }

    @Test
    public void testSendPacket() {

        Address sourceAddress = new Address(LOCAL_NET_ID, 12345);
        Address destinationAddress = new Address(new NetId(1), 54321);

        Packet packet = new StandardPacket(sourceAddress, destinationAddress, "testdata");
        netModule.sendPacket(packet, true);

        assertArrayEquals("Packets delivered to the net interface", new Packet[] { packet }, packetsDeliveredToNetInterface.toArray());
    }

    private class TestNetworkModule extends NetworkModule {

        @Override
        public NetInterface getNetInterface() {

            return netInterface;
        }

        @Override
        protected void deliverPacketToNetInterface(Packet packet) {

            packetsDeliveredToNetInterface.add(packet);
        }

    }

}
