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

package com.quartercode.disconnected.server.world.comp.net;

import com.quartercode.disconnected.server.world.comp.net.socket.Socket;
import com.quartercode.disconnected.server.world.util.DerivableSize;
import com.quartercode.disconnected.shared.world.comp.net.Address;

/**
 * This class represents a packet which can be sent from one to another network interface.
 * Each packet contains the source and destination {@link Address}es, as well as the data payload {@link Object} which is sent using the packet.<br>
 * <br>
 * The most basic implementation of this interface is the {@link StandardPacket}.
 * Other implementations, for example the {@link RoutedPacket}, wrap around a standard packet and add more routing information to the packet in order to deliver it properly.
 * When the packet is delivered, it can then be unwrapped again, or its original information can be accessed through the interface methods.
 *
 * @see Address
 */
public interface Packet extends DerivableSize {

    /**
     * Returns the network {@link Address} of the {@link Socket} which sent the packet.
     *
     * @return The sender address.
     */
    public Address getSource();

    /**
     * Returns the network {@link Address} of the {@link Socket} the packet is sent to.
     *
     * @return The receiver address.
     */
    public Address getDestination();

    /**
     * Returns the data payload {@link Object} which is sent using the packet.
     * If the payload object is mutable for some reason, it shouldn't be modified after the packet has been constructed.
     *
     * @return The actual data payload sent from the sender to the receiver.
     */
    public Object getData();

}
