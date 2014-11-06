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
import org.jmock.lib.concurrent.Synchroniser;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Rule;
import org.junit.Test;
import com.quartercode.disconnected.server.world.comp.net.Packet;
import com.quartercode.disconnected.server.world.comp.net.Socket;
import com.quartercode.disconnected.server.world.comp.net.Socket.PacketHandler;
import com.quartercode.disconnected.server.world.comp.net.Socket.SocketState;

public class SocketTest {

    @Rule
    // @formatter:off
    public JUnitRuleMockery context = new JUnitRuleMockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
        setThreadingPolicy(new Synchroniser());
    }};
    // @formatter:on

    @Test
    public void testHandle() {

        final PacketHandler packetHandler = context.mock(PacketHandler.class);
        final Socket socket = new Socket();
        socket.setObj(Socket.STATE, SocketState.CONNECTED);
        socket.addToColl(Socket.PACKET_HANDLERS, packetHandler);

        // @formatter:off
        context.checking(new Expectations() {{

            oneOf(packetHandler).handle(socket, "testdata");

        }});
        // @formatter:on

        Packet packet = new Packet();
        packet.setObj(Packet.DATA, "testdata");
        socket.invoke(Socket.HANDLE, packet);
    }

    @Test
    public void testHandleNotConnected() {

        final PacketHandler packetHandler = context.mock(PacketHandler.class);
        final Socket socket = new Socket();
        socket.addToColl(Socket.PACKET_HANDLERS, packetHandler);

        // @formatter:off
        context.checking(new Expectations() {{

            never(packetHandler).handle(socket, "testdata");

        }});
        // @formatter:on

        Packet packet = new Packet();
        packet.setObj(Packet.DATA, "testdata");
        socket.invoke(Socket.HANDLE, packet);
    }

}
