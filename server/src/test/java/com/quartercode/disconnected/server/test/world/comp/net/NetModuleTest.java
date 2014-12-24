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

import static com.quartercode.classmod.extra.func.Priorities.LEVEL_5;
import static com.quartercode.classmod.extra.func.Priorities.LEVEL_9;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import com.quartercode.classmod.def.extra.conv.DefaultCFeatureHolder;
import com.quartercode.classmod.extra.func.FunctionExecutor;
import com.quartercode.classmod.extra.func.FunctionInvocation;
import com.quartercode.classmod.util.test.JUnitRuleModMockery;
import com.quartercode.disconnected.server.world.comp.Computer;
import com.quartercode.disconnected.server.world.comp.hardware.NodeNetInterface;
import com.quartercode.disconnected.server.world.comp.net.NetModule;
import com.quartercode.disconnected.server.world.comp.net.Packet;
import com.quartercode.disconnected.server.world.comp.net.PacketHandler;
import com.quartercode.disconnected.server.world.comp.net.Socket;
import com.quartercode.disconnected.server.world.comp.net.Socket.SocketState;
import com.quartercode.disconnected.server.world.comp.os.OS;
import com.quartercode.disconnected.shared.CommonBootstrap;
import com.quartercode.disconnected.shared.world.comp.net.Address;
import com.quartercode.disconnected.shared.world.comp.net.NetID;

@SuppressWarnings ("unchecked")
public class NetModuleTest {

    @BeforeClass
    public static void setUpBeforeClass() {

        CommonBootstrap.bootstrap();
    }

    @Rule
    public JUnitRuleMockery        context      = new JUnitRuleMockery();
    @Rule
    public JUnitRuleModMockery     modmock      = new JUnitRuleModMockery();

    private final Computer         computer     = new Computer();
    private final OS               os           = new OS();
    private final NetModule        netModule    = new NetModule();
    private final NodeNetInterface netInterface = new NodeNetInterface();

    @Before
    public void setUp() {

        netInterface.setObj(NodeNetInterface.NET_ID, new NetID());

        computer.setObj(Computer.OS, os);
        os.setObj(OS.NET_MODULE, netModule);

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

        Socket socket = netModule.invoke(NetModule.CREATE_SOCKET);

        assertEquals("Bound network interface of the socket that was created", netModule, socket.getParent());
        assertTrue("Created socket wasn't added to the network module's socket list", netModule.getColl(NetModule.SOCKETS).contains(socket));
    }

    @Test
    public void testDisconnectCreatedSocket() {

        Socket socket = netModule.invoke(NetModule.CREATE_SOCKET);
        socket.setObj(Socket.LOCAL_PORT, 1);
        socket.invoke(Socket.DISCONNECT);

        assertTrue("Created socket wasn't removed from the network module's socket list", !netModule.getColl(NetModule.SOCKETS).contains(socket));
    }

    @Test
    public void testSetRunningFalseDisconnectCreatedSockets() {

        Socket socket1 = netModule.invoke(NetModule.CREATE_SOCKET);
        socket1.setObj(Socket.LOCAL_PORT, 1);
        Socket socket2 = netModule.invoke(NetModule.CREATE_SOCKET);
        socket2.setObj(Socket.LOCAL_PORT, 2);

        netModule.invoke(NetModule.SET_RUNNING, false);

        assertTrue("Created socket 1 wasn't removed from the network module's socket list", !netModule.getColl(NetModule.SOCKETS).contains(socket1));
        assertTrue("Created socket 2 wasn't removed from the network module's socket list", !netModule.getColl(NetModule.SOCKETS).contains(socket2));
    }

    @Test
    public void testSend() {

        int destinationPort = 54321;
        Address destinationAddress = new Address(new NetID(0, 1), destinationPort);

        final Packet packet = new Packet();
        packet.setObj(Packet.DESTINATION, destinationAddress);
        packet.setObj(Packet.DATA, "testdata");

        final FunctionExecutor<Void> processHook = context.mock(FunctionExecutor.class, "processHook");
        modmock.addFuncExec(NodeNetInterface.PROCESS, "processHook", NodeNetInterface.class, processHook, LEVEL_9);

        // @formatter:off
        context.checking(new Expectations() {{

            oneOf(processHook).invoke(with(any(FunctionInvocation.class)), with(new Object[] { packet }));

        }});
        // @formatter:on

        netModule.invoke(NetModule.SEND, packet);
    }

    @Test
    public void testSendTcp() {

        int sourcePort = 12345;
        int destinationPort = 54321;
        Address sourceAddress = new Address(new NetID(0, 1), sourcePort);
        Address destinationAddress = new Address(new NetID(0, 1), destinationPort);

        netInterface.setObj(NodeNetInterface.NET_ID, sourceAddress.getNetId());

        Socket socket = netModule.invoke(NetModule.CREATE_SOCKET);
        socket.setObj(Socket.LOCAL_PORT, sourcePort);
        socket.setObj(Socket.DESTINATION, destinationAddress);
        socket.setObj(Socket.STATE, SocketState.CONNECTED);

        final Packet expectedPacket = new Packet();
        expectedPacket.setObj(Packet.SOURCE, sourceAddress);
        expectedPacket.setObj(Packet.DESTINATION, destinationAddress);
        expectedPacket.setObj(Packet.PROTOCOL, "tcp");
        expectedPacket.setObj(Packet.DATA, "testdata");

        final FunctionExecutor<Void> processHook = context.mock(FunctionExecutor.class, "processHook");
        modmock.addFuncExec(NodeNetInterface.PROCESS, "processHook", NodeNetInterface.class, processHook, LEVEL_9);

        // @formatter:off
        context.checking(new Expectations() {{

            oneOf(processHook).invoke(with(any(FunctionInvocation.class)), with(new Object[] { expectedPacket }));

        }});
        // @formatter:on

        netModule.invoke(NetModule.SEND_TCP, socket, "testdata");
    }

    @Test
    public void testHandleTcp() {

        int sourcePort = 12345;
        Address sourceAddress = new Address(new NetID(0, 1), sourcePort);
        int destinationPort = 54321;

        Packet packet = new Packet();
        packet.setObj(Packet.SOURCE, sourceAddress);
        packet.setObj(Packet.DESTINATION, new Address(new NetID(0, 2), destinationPort));
        packet.setObj(Packet.PROTOCOL, "tcp");
        packet.setObj(Packet.DATA, "testdata");

        final Socket receiverSocket = netModule.invoke(NetModule.CREATE_SOCKET);
        receiverSocket.setObj(Socket.LOCAL_PORT, destinationPort);
        receiverSocket.setObj(Socket.DESTINATION, sourceAddress);
        receiverSocket.setObj(Socket.STATE, SocketState.CONNECTED);

        // Add a packet handler
        final FunctionExecutor<Void> packetHandlerHook = context.mock(FunctionExecutor.class, "packetHandlerHook");
        modmock.addFuncExec(PacketHandler.HANDLE, "packetHandlerHook", HookedPacketHandler.class, packetHandlerHook);
        receiverSocket.addToColl(Socket.PACKET_HANDLERS, new HookedPacketHandler());

        // @formatter:off
        context.checking(new Expectations() {{

            oneOf(packetHandlerHook).invoke(with(any(FunctionInvocation.class)), with(new Object[] { receiverSocket, "testdata" }));

        }});
        // @formatter:on

        netModule.invoke(NetModule.HANDLE, packet);
        receiverSocket.get(Socket.SCHEDULER).update("computerProgramUpdate");
    }

    private static class HookedPacketHandler extends DefaultCFeatureHolder implements PacketHandler {

    }

}
