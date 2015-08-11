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
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jmock.Expectations;
import org.jmock.Sequence;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Rule;
import org.junit.Test;
import com.quartercode.disconnected.server.world.comp.net.socket.PacketHandler;
import com.quartercode.disconnected.server.world.comp.net.socket.Socket;
import com.quartercode.disconnected.server.world.comp.net.socket.SocketState;

public class SocketTest {

    @Rule
    public JUnitRuleMockery  context = new JUnitRuleMockery();

    private final TestSocket socket  = new TestSocket();

    @Test
    public void testHandle() {

        socket.sneakyStateChange(SocketState.CONNECTED);

        socket.handle("testdata");
        assertArrayEquals("Incoming packet buffer of socket after packet handling", new Object[] { "testdata" }, socket.getIncomingPacketBuffer().toArray());
    }

    @Test
    public void testHandleNotConnected() {

        socket.handle("testdata");
        assertArrayEquals("Incoming packet buffer of socket after packet handling", new Object[0], socket.getIncomingPacketBuffer().toArray());
    }

    @Test
    public void testPacketHandlers() {

        // Required to initialize the scheduler tasks
        socket.setState(SocketState.CONNECTED);

        // Add a packet handler
        final PacketHandler packetHandler = context.mock(PacketHandler.class);
        socket.addPacketHandler(packetHandler);

        // @formatter:off
        context.checking(new Expectations() {{

            final Sequence handlings = context.sequence("handlings");

            oneOf(packetHandler).handle(socket, "testdata1"); inSequence(handlings);
            oneOf(packetHandler).handle(socket, "testdata2"); inSequence(handlings);
            oneOf(packetHandler).handle(socket, "testdata3"); inSequence(handlings);
            oneOf(packetHandler).handle(socket, "testdata4"); inSequence(handlings);

        }});
        // @formatter:on

        // Add test packets to the packet buffer and trigger the packet handling
        socket.getIncomingPacketBuffer().add("testdata1");
        socket.getIncomingPacketBuffer().add("testdata2");
        socket.getIncomingPacketBuffer().add("testdata3");
        socket.getIncomingPacketBuffer().add("testdata4");
        socket.getScheduler().update("computer.programUpdate");
    }

    @Test
    public void testDisconnectWhileHandling() {

        socket.sneakyStateChange(SocketState.CONNECTED);

        // Add a packet handler
        final MutableBoolean invokedPacketHandlerOnce = new MutableBoolean();
        socket.addPacketHandler(new PacketHandler() {

            @Override
            public void handle(Socket socket, Object data) {

                assertFalse("Packet handler has been invoked twice", invokedPacketHandlerOnce.getValue());
                invokedPacketHandlerOnce.setTrue();

                assertEquals("Socket which sent packet", SocketTest.this.socket, socket);
                assertEquals("Packet content", "testdata1", data);

                // Disconnect; after this call, the next packer handlers shouldn't be invoked
                SocketTest.this.socket.sneakyStateChange(SocketState.DISCONNECTED);
            }

        });

        // Add test packets to the packet buffer and trigger the packet handling
        socket.getIncomingPacketBuffer().add("testdata1");
        socket.getIncomingPacketBuffer().add("testdata2");
        socket.getScheduler().update("computer.programUpdate");
    }

}
