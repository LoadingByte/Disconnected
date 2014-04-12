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

import com.quartercode.classmod.extra.PropertyDefinition;
import com.quartercode.classmod.extra.def.ObjectProperty;
import com.quartercode.disconnected.world.comp.SizeUtil;
import com.quartercode.disconnected.world.comp.SizeUtil.DerivableSize;
import com.quartercode.disconnected.world.event.Event;

/**
 * This {@link Event} represents the income of a network {@link Packet}.
 * he receiver is selected based on some basic information, for example open ports or connections.
 * A packet event just contains the received {@link Packet}.
 * 
 * @see Packet
 * @see Event
 */
public class ReceivePacketEvent extends Event implements DerivableSize {

    // ----- Properties -----

    /**
     * The received network {@link Packet} that has something to do with the event receiver.
     */
    public static final PropertyDefinition<Packet> PACKET;

    static {

        PACKET = ObjectProperty.createDefinition("packet");

    }

    // ----- Functions -----

    static {

        GET_SIZE.addExecutor("packet", ReceivePacketEvent.class, SizeUtil.createGetSize(PACKET));

    }

    /**
     * Creates a new receive packet event.
     */
    public ReceivePacketEvent() {

    }

}
