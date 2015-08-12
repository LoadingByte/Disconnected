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

package com.quartercode.disconnected.server.world.comp.net.socket;

import com.quartercode.disconnected.server.world.comp.net.Packet;

/**
 * A packet handle is a simple functional class which is called when a {@link Packet} arrives at a {@link Socket}.
 * However, the handler only receives the carried data object and no further metadata.<br>
 * <br>
 * Actually, the {@link Socket#getPacketHandlers() packet handlers of a socket} are called when scheduler invokes the group {@code computer.processUpdate}.
 * When that happens, all buffered packets are polled from the socket's internal incoming packet buffer and are handed over to the handlers.
 *
 * @see Socket
 * @see Packet
 */
public interface PacketHandler {

    /**
     * This method is called when a {@link Packet} with the given {@link Packet#getData() data payload object} arrives at the given {@link Socket}.
     *
     * @param socket The socket which received the given packet.
     *        Note that this is always the socket (or one of the sockets) the packet handler was added to originally.
     * @param data The data payload object that was carried by the received packet.
     *        It should be processed by the handler.
     */
    public void handle(Socket socket, Object data);

}
