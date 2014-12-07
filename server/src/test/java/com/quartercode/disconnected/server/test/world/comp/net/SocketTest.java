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

import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
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

@SuppressWarnings ("unchecked")
public class SocketTest {

    @Rule
    public JUnitRuleMockery    context = new JUnitRuleMockery();
    @Rule
    public JUnitRuleModMockery modmock = new JUnitRuleModMockery();

    @Test
    public void testHandle() {

        final Socket socket = new Socket();
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

        Packet packet = new Packet();
        packet.setObj(Packet.DATA, "testdata");
        socket.invoke(Socket.HANDLE, packet);
    }

    @Test
    public void testHandleNotConnected() {

        final Socket socket = new Socket();

        // Add a packet handler
        final FunctionExecutor<Void> packetHandlerHook = context.mock(FunctionExecutor.class, "packetHandlerHook");
        modmock.addFuncExec(PacketHandler.HANDLE, "packetHandlerHook", HookedPacketHandler.class, packetHandlerHook);
        socket.addToColl(Socket.PACKET_HANDLERS, new HookedPacketHandler());

        // @formatter:off
        context.checking(new Expectations() {{

            never(packetHandlerHook).invoke(with(any(FunctionInvocation.class)), with(new Object[] { socket, "testdata" }));

        }});
        // @formatter:on

        Packet packet = new Packet();
        packet.setObj(Packet.DATA, "testdata");
        socket.invoke(Socket.HANDLE, packet);
    }

    private static class HookedPacketHandler extends DefaultCFeatureHolder implements PacketHandler {

    }

}
