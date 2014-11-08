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

import static com.quartercode.classmod.extra.Priorities.LEVEL_5;
import static com.quartercode.classmod.extra.Priorities.LEVEL_9;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import com.quartercode.classmod.extra.FunctionExecutor;
import com.quartercode.classmod.extra.FunctionInvocation;
import com.quartercode.classmod.util.test.JUnitRuleModMockery;
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

@SuppressWarnings ("unchecked")
public class NetworkModuleTest {

    @Rule
    // @formatter:off
    public JUnitRuleMockery context = new JUnitRuleMockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
        setThreadingPolicy(new Synchroniser());
    }};
    // @formatter:on

    @Rule
    public JUnitRuleModMockery     modmock         = new JUnitRuleModMockery();

    private final Computer         computer        = new Computer();
    private final OperatingSystem  operatingSystem = new OperatingSystem();
    private final NetworkModule    netModule       = new NetworkModule();
    private final NodeNetInterface netInterface    = new NodeNetInterface();

    @Before
    public void setUp() {

        netInterface.setObj(NodeNetInterface.NET_ID, new NetID());

        computer.setObj(Computer.OS, operatingSystem);
        operatingSystem.setObj(OperatingSystem.NET_MODULE, netModule);

        computer.addToColl(Computer.HARDWARE, netInterface);

        // Add a function executor for stopping the normal NodeNetInterface.PROCESS method
        final FunctionExecutor<Void> processInvocationStopper = context.mock(FunctionExecutor.class, "processInvocationStopper");
        modmock.addFuncExec(NodeNetInterface.PROCESS, "invocationStopper", NodeNetInterface.class, processInvocationStopper, LEVEL_5);

        // @formatter:off
        context.checking(new Expectations() {{

            // Invocation stopper
            allowing(processInvocationStopper).invoke(with(any(FunctionInvocation.class)), with(any(Object[].class)));

        }});
        // @formatter:on
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

        final FunctionExecutor<Void> processHook = context.mock(FunctionExecutor.class, "processHook");
        modmock.addFuncExec(NodeNetInterface.PROCESS, "processHook", NodeNetInterface.class, processHook, LEVEL_9);

        // @formatter:off
        context.checking(new Expectations() {{

            oneOf(processHook).invoke(with(any(FunctionInvocation.class)), with(new Object[] { packet }));

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

        final FunctionExecutor<Void> processHook = context.mock(FunctionExecutor.class, "processHook");
        modmock.addFuncExec(NodeNetInterface.PROCESS, "processHook", NodeNetInterface.class, processHook, LEVEL_9);

        // @formatter:off
        context.checking(new Expectations() {{

            oneOf(processHook).invoke(with(any(FunctionInvocation.class)), with(new Object[] { expectedPacket }));

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
