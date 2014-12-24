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
import static org.junit.Assert.assertFalse;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jmock.Expectations;
import org.jmock.Sequence;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import com.quartercode.classmod.def.extra.conv.DefaultCFeatureHolder;
import com.quartercode.classmod.extra.func.FunctionExecutor;
import com.quartercode.classmod.extra.func.FunctionInvocation;
import com.quartercode.classmod.util.test.JUnitRuleModMockery;
import com.quartercode.disconnected.server.world.comp.net.Packet;
import com.quartercode.disconnected.server.world.comp.net.PacketHandler;
import com.quartercode.disconnected.server.world.comp.net.Socket;
import com.quartercode.disconnected.server.world.comp.net.Socket.SocketState;
import com.quartercode.disconnected.shared.CommonBootstrap;

@SuppressWarnings ("unchecked")
public class SocketTest {

    @BeforeClass
    public static void setUpBeforeClass() {

        CommonBootstrap.bootstrap();
    }

    @Rule
    public JUnitRuleMockery    context = new JUnitRuleMockery();
    @Rule
    public JUnitRuleModMockery modmock = new JUnitRuleModMockery();

    private final Socket       socket  = new Socket();

    @Test
    public void testPacketHandlers() {

        socket.setObj(Socket.STATE, SocketState.CONNECTED);

        // Add test packets to the packet queue
        socket.addToColl(Socket.INCOMING_PACKET_QUEUE, "testdata1");
        socket.addToColl(Socket.INCOMING_PACKET_QUEUE, "testdata2");
        socket.addToColl(Socket.INCOMING_PACKET_QUEUE, "testdata3");
        socket.addToColl(Socket.INCOMING_PACKET_QUEUE, "testdata4");

        // Add a packet handler
        final FunctionExecutor<Void> packetHandlerHook = context.mock(FunctionExecutor.class, "packetHandlerHook");
        modmock.addFuncExec(PacketHandler.HANDLE, "packetHandlerHook", HookedPacketHandler.class, packetHandlerHook);
        socket.addToColl(Socket.PACKET_HANDLERS, new HookedPacketHandler());

        // @formatter:off
        context.checking(new Expectations() {{

            final Sequence handlings = context.sequence("handlings");

            oneOf(packetHandlerHook).invoke(with(any(FunctionInvocation.class)), with(new Object[] { socket, "testdata1" })); inSequence(handlings);
            oneOf(packetHandlerHook).invoke(with(any(FunctionInvocation.class)), with(new Object[] { socket, "testdata2" })); inSequence(handlings);
            oneOf(packetHandlerHook).invoke(with(any(FunctionInvocation.class)), with(new Object[] { socket, "testdata3" })); inSequence(handlings);
            oneOf(packetHandlerHook).invoke(with(any(FunctionInvocation.class)), with(new Object[] { socket, "testdata4" })); inSequence(handlings);

        }});
        // @formatter:on

        // Trigger the packet handling
        socket.get(Socket.SCHEDULER).update("computerProgramUpdate");
    }

    @Test
    public void testHandle() {

        socket.setObj(Socket.STATE, SocketState.CONNECTED);

        // Add a packet handler
        final FunctionExecutor<Void> packetHandlerHook = context.mock(FunctionExecutor.class, "packetHandlerHook");
        modmock.addFuncExec(PacketHandler.HANDLE, "packetHandlerHook", HookedPacketHandler.class, packetHandlerHook);
        socket.addToColl(Socket.PACKET_HANDLERS, new HookedPacketHandler());

        // @formatter:off
        context.checking(new Expectations() {{

            oneOf(packetHandlerHook).invoke(with(any(FunctionInvocation.class)), with(new Object[] { socket, "testdata" }));

        }});
        // @formatter:on

        socket.invoke(Socket.HANDLE, createPacket("testdata"));
        socket.get(Socket.SCHEDULER).update("computerProgramUpdate");
    }

    @Test
    public void testHandleNotConnected() {

        // Add a packet handler
        final FunctionExecutor<Void> packetHandlerHook = context.mock(FunctionExecutor.class, "packetHandlerHook");
        modmock.addFuncExec(PacketHandler.HANDLE, "packetHandlerHook", HookedPacketHandler.class, packetHandlerHook);
        socket.addToColl(Socket.PACKET_HANDLERS, new HookedPacketHandler());

        // @formatter:off
        context.checking(new Expectations() {{

            never(packetHandlerHook).invoke(with(any(FunctionInvocation.class)), with(new Object[] { socket, "testdata" }));

        }});
        // @formatter:on

        socket.invoke(Socket.HANDLE, createPacket("testdata"));
        socket.get(Socket.SCHEDULER).update("computerProgramUpdate");
    }

    @Test
    public void testDisconnectWhileHandling() {

        socket.setObj(Socket.STATE, SocketState.CONNECTED);

        // Add a packet handler
        final MutableBoolean invokedPacketHandlerOnce = new MutableBoolean();
        final FunctionExecutor<Void> packetHandlerHook = new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                assertFalse("Packet handler has been invoked twice", invokedPacketHandlerOnce.getValue());
                invokedPacketHandlerOnce.setTrue();

                assertEquals("Socket which sent packet", socket, arguments[0]);
                assertEquals("Packet content", "testdata1", arguments[1]);

                // Disconnect; after this call, the next packer handlers shouldn't be invoked
                socket.setObj(Socket.STATE, SocketState.DISCONNECTED);

                return invocation.next(arguments);
            }

        };
        modmock.addFuncExec(PacketHandler.HANDLE, "packetHandlerHook", HookedPacketHandler.class, packetHandlerHook);
        socket.addToColl(Socket.PACKET_HANDLERS, new HookedPacketHandler());

        socket.invoke(Socket.HANDLE, createPacket("testdata1"));
        socket.invoke(Socket.HANDLE, createPacket("testdata2"));
        socket.get(Socket.SCHEDULER).update("computerProgramUpdate");
    }

    private Packet createPacket(Object data) {

        Packet packet = new Packet();
        packet.setObj(Packet.DATA, data);
        return packet;
    }

    private static class HookedPacketHandler extends DefaultCFeatureHolder implements PacketHandler {

    }

}
