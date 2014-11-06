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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import com.quartercode.classmod.extra.FunctionExecutor;
import com.quartercode.classmod.extra.FunctionInvocation;
import com.quartercode.classmod.extra.Prioritized;
import com.quartercode.disconnected.server.util.ObjArray;
import com.quartercode.disconnected.server.world.comp.Computer;
import com.quartercode.disconnected.server.world.comp.hardware.NodeNetInterface;
import com.quartercode.disconnected.server.world.comp.net.NetworkModule;
import com.quartercode.disconnected.server.world.comp.net.Packet;
import com.quartercode.disconnected.server.world.comp.net.Socket;
import com.quartercode.disconnected.server.world.comp.net.Socket.PacketHandler;
import com.quartercode.disconnected.server.world.comp.net.Socket.SocketState;
import com.quartercode.disconnected.server.world.comp.net.TCPPacket;
import com.quartercode.disconnected.server.world.comp.os.OperatingSystem;
import com.quartercode.disconnected.shared.world.comp.net.Address;
import com.quartercode.disconnected.shared.world.comp.net.NetID;

public class NetworkModuleTest {

    /*
     * This field is changed by the test at every run.
     */
    private static NodeNetInterfaceProcessHook nodeNetInterfaceProcessHook;

    private static interface NodeNetInterfaceProcessHook {

        public void onProcess(Packet packet);

    }

    @BeforeClass
    public static void installHook() {

        NodeNetInterface.PROCESS.addExecutor("hook", NodeNetInterface.class, new FunctionExecutor<Void>() {

            @Override
            @Prioritized (Prioritized.LEVEL_9)
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                if (nodeNetInterfaceProcessHook != null) {
                    nodeNetInterfaceProcessHook.onProcess((Packet) arguments[0]);
                }

                // Stop the invocation
                return null;
            }

        });
    }

    @AfterClass
    public static void uninstallHook() {

        NodeNetInterface.PROCESS.removeExecutor("hook", NodeNetInterface.class);
    }

    @Rule
    // @formatter:off
    public JUnitRuleMockery context = new JUnitRuleMockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
        setThreadingPolicy(new Synchroniser());
    }};
    // @formatter:on

    private final Computer         computer        = new Computer();
    private final OperatingSystem  operatingSystem = new OperatingSystem();
    private final NetworkModule    netModule       = new NetworkModule();
    private final NodeNetInterface netInterface    = new NodeNetInterface();

    @Before
    public void setUp() {

        nodeNetInterfaceProcessHook = null;

        netInterface.setObj(NodeNetInterface.NET_ID, new NetID());

        computer.setObj(Computer.OS, operatingSystem);
        operatingSystem.setObj(OperatingSystem.NET_MODULE, netModule);

        computer.addToColl(Computer.HARDWARE, netInterface);
    }

    @Test
    public void testCreateSocket() {

        Socket socket = netModule.invoke(NetworkModule.CREATE_SOCKET);

        assertEquals("Bound network interface of the socket that was created", netModule, socket.getParent());
        assertTrue("Created socket wasn't added to the network module's socket list", netModule.getColl(NetworkModule.SOCKETS).contains(socket));
    }

    @Test
    public void testDisconnectCreatedSocket() {

        Socket socket = netModule.invoke(NetworkModule.CREATE_SOCKET);
        socket.setObj(Socket.LOCAL_PORT, 1);
        socket.invoke(Socket.DISCONNECT);

        assertTrue("Created socket wasn't removed from the network module's socket list", !netModule.getColl(NetworkModule.SOCKETS).contains(socket));
    }

    @Test
    public void testSetRunningFalseDisconnectCreatedSockets() {

        Socket socket1 = netModule.invoke(NetworkModule.CREATE_SOCKET);
        socket1.setObj(Socket.LOCAL_PORT, 1);
        Socket socket2 = netModule.invoke(NetworkModule.CREATE_SOCKET);
        socket2.setObj(Socket.LOCAL_PORT, 2);

        netModule.invoke(NetworkModule.SET_RUNNING, false);

        assertTrue("Created socket 1 wasn't removed from the network module's socket list", !netModule.getColl(NetworkModule.SOCKETS).contains(socket1));
        assertTrue("Created socket 2 wasn't removed from the network module's socket list", !netModule.getColl(NetworkModule.SOCKETS).contains(socket2));
    }

    @Test
    public void testSend() {

        int destinationPort = 54321;
        Address destinationAddress = new Address(new NetID(0, 1), destinationPort);

        final Packet packet = new Packet();
        packet.setObj(Packet.DESTINATION, destinationAddress);
        packet.setObj(Packet.DATA, new ObjArray("testdata"));

        nodeNetInterfaceProcessHook = context.mock(NodeNetInterfaceProcessHook.class);

        // @formatter:off
        context.checking(new Expectations() {{

            oneOf(nodeNetInterfaceProcessHook).onProcess(packet);

        }});
        // @formatter:on

        netModule.invoke(NetworkModule.SEND, packet);
    }

    @Test
    public void testSendTcp() {

        int sourcePort = 12345;
        int destinationPort = 54321;
        Address sourceAddress = new Address(new NetID(0, 1), sourcePort);
        Address destinationAddress = new Address(new NetID(0, 1), destinationPort);

        netInterface.setObj(NodeNetInterface.NET_ID, sourceAddress.getNetId());

        Socket socket = netModule.invoke(NetworkModule.CREATE_SOCKET);
        socket.setObj(Socket.LOCAL_PORT, sourcePort);
        socket.setObj(Socket.DESTINATION, destinationAddress);
        socket.setObj(Socket.STATE, SocketState.CONNECTED);

        final TCPPacket expectedPacket = new TCPPacket();
        expectedPacket.setObj(TCPPacket.SOURCE, sourceAddress);
        expectedPacket.setObj(TCPPacket.DESTINATION, destinationAddress);
        expectedPacket.setObj(TCPPacket.DATA, new ObjArray("testdata"));

        nodeNetInterfaceProcessHook = context.mock(NodeNetInterfaceProcessHook.class);

        // @formatter:off
        context.checking(new Expectations() {{

            oneOf(nodeNetInterfaceProcessHook).onProcess(expectedPacket);

        }});
        // @formatter:on

        netModule.invoke(NetworkModule.SEND_TCP, socket, new ObjArray("testdata"));
    }

    @Test
    public void testHandleTcp() {

        int sourcePort = 12345;
        Address sourceAddress = new Address(new NetID(0, 1), sourcePort);
        int destinationPort = 54321;

        TCPPacket packet = new TCPPacket();
        packet.setObj(TCPPacket.SOURCE, sourceAddress);
        packet.setObj(TCPPacket.DESTINATION, new Address(new NetID(0, 2), destinationPort));
        packet.setObj(TCPPacket.DATA, new ObjArray("testdata"));

        final Socket receiverSocket = netModule.invoke(NetworkModule.CREATE_SOCKET);
        receiverSocket.setObj(Socket.LOCAL_PORT, destinationPort);
        receiverSocket.setObj(Socket.DESTINATION, sourceAddress);
        receiverSocket.setObj(Socket.STATE, SocketState.CONNECTED);

        final PacketHandler packetHandler = context.mock(PacketHandler.class);
        receiverSocket.addToColl(Socket.PACKET_HANDLERS, packetHandler);

        // @formatter:off
        context.checking(new Expectations() {{

            oneOf(packetHandler).handle(receiverSocket, new ObjArray("testdata"));

        }});
        // @formatter:on

        netModule.invoke(NetworkModule.HANDLE, packet);
    }

}
