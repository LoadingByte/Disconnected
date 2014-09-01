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

package com.quartercode.disconnected.test.world.comp.net;

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
import com.quartercode.disconnected.util.ObjArray;
import com.quartercode.disconnected.world.comp.Computer;
import com.quartercode.disconnected.world.comp.hardware.NodeNetInterface;
import com.quartercode.disconnected.world.comp.net.Address;
import com.quartercode.disconnected.world.comp.net.NetID;
import com.quartercode.disconnected.world.comp.net.NetworkModule;
import com.quartercode.disconnected.world.comp.net.Packet;
import com.quartercode.disconnected.world.comp.net.Socket;
import com.quartercode.disconnected.world.comp.net.Socket.PacketHandler;
import com.quartercode.disconnected.world.comp.net.Socket.SocketState;
import com.quartercode.disconnected.world.comp.os.OperatingSystem;

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

        computer.get(Computer.OS).set(operatingSystem);
        operatingSystem.get(OperatingSystem.NET_MODULE).set(netModule);

        computer.get(Computer.HARDWARE).add(netInterface);
    }

    @Test
    public void testCreateSocket() {

        Socket socket = netModule.get(NetworkModule.CREATE_SOCKET).invoke();

        assertEquals("Bound network interface of the socket that was created", netModule, socket.getParent());
        assertTrue("Created socket wasn't added to the network module's socket list", netModule.get(NetworkModule.SOCKETS).get().contains(socket));
    }

    @Test
    public void testDisconnectCreatedSocket() {

        Socket socket = netModule.get(NetworkModule.CREATE_SOCKET).invoke();
        socket.get(Socket.LOCAL_PORT).set(1);
        socket.get(Socket.DISCONNECT).invoke();

        assertTrue("Created socket wasn't removed from the network module's socket list", !netModule.get(NetworkModule.SOCKETS).get().contains(socket));
    }

    @Test
    public void testSetRunningFalseDisconnectCreatedSockets() {

        Socket socket1 = netModule.get(NetworkModule.CREATE_SOCKET).invoke();
        socket1.get(Socket.LOCAL_PORT).set(1);
        Socket socket2 = netModule.get(NetworkModule.CREATE_SOCKET).invoke();
        socket2.get(Socket.LOCAL_PORT).set(2);

        netModule.get(NetworkModule.SET_RUNNING).invoke(false);

        assertTrue("Created socket 1 wasn't removed from the network module's socket list", !netModule.get(NetworkModule.SOCKETS).get().contains(socket1));
        assertTrue("Created socket 2 wasn't removed from the network module's socket list", !netModule.get(NetworkModule.SOCKETS).get().contains(socket2));
    }

    @Test
    public void testSend() {

        int sourcePort = 12345;
        int destinationPort = 54321;
        Address sourceAddress = createAddress(0, 1, sourcePort);
        Address destinationAddress = createAddress(0, 1, destinationPort);

        netInterface.get(NodeNetInterface.NET_ID).set(sourceAddress.get(Address.NET_ID).get());

        Socket socket = netModule.get(NetworkModule.CREATE_SOCKET).invoke();
        socket.get(Socket.LOCAL_PORT).set(sourcePort);
        socket.get(Socket.DESTINATION).set(destinationAddress);
        socket.get(Socket.STATE).set(SocketState.CONNECTED);

        final Packet expectedPacket = new Packet();
        expectedPacket.get(Packet.SOURCE).set(sourceAddress);
        expectedPacket.get(Packet.DESTINATION).set(destinationAddress);
        expectedPacket.get(Packet.DATA).set(new ObjArray("testdata"));

        nodeNetInterfaceProcessHook = context.mock(NodeNetInterfaceProcessHook.class);

        // @formatter:off
        context.checking(new Expectations() {{

            oneOf(nodeNetInterfaceProcessHook).onProcess(expectedPacket);

        }});
        // @formatter:on

        netModule.get(NetworkModule.SEND).invoke(socket, new ObjArray("testdata"));
    }

    @Test
    public void testHandle() {

        int sourcePort = 12345;
        Address sourceAddress = createAddress(0, 1, sourcePort);
        int destinationPort = 54321;

        Packet packet = new Packet();
        packet.get(Packet.SOURCE).set(sourceAddress);
        packet.get(Packet.DESTINATION).set(createAddress(0, 2, destinationPort));
        packet.get(Packet.DATA).set(new ObjArray("testdata"));

        Socket receiverSocket = netModule.get(NetworkModule.CREATE_SOCKET).invoke();
        receiverSocket.get(Socket.LOCAL_PORT).set(destinationPort);
        receiverSocket.get(Socket.DESTINATION).set(sourceAddress);
        receiverSocket.get(Socket.STATE).set(SocketState.CONNECTED);

        final PacketHandler packetHandler = context.mock(PacketHandler.class);
        receiverSocket.get(Socket.PACKET_HANDLERS).add(packetHandler);

        // @formatter:off
        context.checking(new Expectations() {{

            oneOf(packetHandler).handle(new ObjArray("testdata"));

        }});
        // @formatter:on

        netModule.get(NetworkModule.HANDLE).invoke(packet);
    }

    private Address createAddress(int subnet, int id, int port) {

        NetID netId = new NetID();
        netId.get(NetID.SUBNET).set(subnet);
        netId.get(NetID.ID).set(id);

        Address address = new Address();
        address.get(Address.NET_ID).set(netId);
        address.get(Address.PORT).set(port);

        return address;
    }

}
