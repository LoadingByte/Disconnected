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
 * An enumeration which describes the different states a {@link Socket} can be in.
 *
 * @see Socket#getState()
 */
public enum SocketState {

    /**
     * The socket was just created and hasn't been connected yet.
     */
    INACTIVE,
    /**
     * The socket sent its {@code syn} or {@code syn-ack} {@link Packet} as part of the handshake.
     * The handshake is triggered by the {@link Socket#CONNECT} method.
     */
    HANDSHAKE_SYN,
    /**
     * The socket is ready for sending and receiving packets.
     * That means that it received the other socket's {@code ack} or {@code syn-ack} {@link Packet} as part of the handshake.
     */
    CONNECTED,
    /**
     * The socket received a teardown {@link Packet} and is about to disconnect by calling its own {@link Socket#DISCONNECT} method.
     */
    RECEIVED_TEARDOWN,
    /**
     * The socket is fully disconnected and can no longer be used for anything.
     */
    DISCONNECTED;

}
