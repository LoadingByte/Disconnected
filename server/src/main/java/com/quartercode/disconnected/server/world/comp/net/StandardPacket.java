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

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import org.apache.commons.lang3.Validate;
import com.quartercode.disconnected.server.world.comp.net.socket.Socket;
import com.quartercode.disconnected.server.world.util.SizeUtils;
import com.quartercode.disconnected.server.world.util.WorldNode;
import com.quartercode.disconnected.shared.world.comp.net.Address;
import com.quartercode.jtimber.api.node.Node;

/**
 * This class represents the basic {@link Packet} which is constructed by a program/OS and sent on its way.
 * Therefore, it contains no routing information apart from the destination {@link Address}.
 * Instead, other network nodes add that routing information by wrapping the packet in other packet implementations (e.g. {@link RoutedPacket}).
 *
 * @see Packet
 */
public class StandardPacket extends WorldNode<Node<?>> implements Packet {

    @XmlAttribute
    private Address source;
    @XmlAttribute
    private Address destination;
    @XmlAnyElement (lax = true)
    private Object  data;

    // JAXB constructor
    protected StandardPacket() {

    }

    /**
     * Creates a new standard packet.
     *
     * @param source The network {@link Address} of the {@link Socket} which should send the packet.
     * @param destination The network {@link Address} of the {@link Socket} the packet is sent to.
     * @param data The data payload {@link Object} which is sent using the packet.
     *        If the payload object is mutable for some reason, it shouldn't be modified after the packet has been constructed.
     */
    public StandardPacket(Address source, Address destination, Object data) {

        Validate.notNull(source, "Packet source address cannot be null");
        Validate.notNull(destination, "Packet destination address cannot be null");
        Validate.notNull(data, "Packet data payload object cannot be null");

        this.source = source;
        this.destination = destination;
        this.data = data;
    }

    @Override
    public Address getSource() {

        return source;
    }

    @Override
    public Address getDestination() {

        return destination;
    }

    @Override
    public Object getData() {

        return data;
    }

    @Override
    public long getSize() {

        return SizeUtils.getSize(data);
    }

}
