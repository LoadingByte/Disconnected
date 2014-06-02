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

package com.quartercode.disconnected.world.comp.net;

import com.quartercode.eventbridge.basic.EventBase;

/**
 * This event represents the income of a network {@link Packet} on a computer.
 * 
 * @see Packet
 */
public class ReceivePacketEvent extends EventBase {

    private static final long serialVersionUID = 4232201098390515542L;

    private final Packet      packet;

    /**
     * Creates a new receive packet event.
     * 
     * @param packet The received network {@link Packet}.
     */
    public ReceivePacketEvent(Packet packet) {

        this.packet = packet;
    }

    /**
     * Returns the received network {@link Packet}.
     * 
     * @return The network {@link Packet}.
     */
    public Packet getPacket() {

        return packet;
    }

}
