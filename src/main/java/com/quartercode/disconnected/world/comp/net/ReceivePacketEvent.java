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

import com.quartercode.classmod.base.FeatureDefinition;
import com.quartercode.classmod.extra.FunctionDefinition;
import com.quartercode.classmod.extra.def.LockableFEWrapper;
import com.quartercode.classmod.extra.def.ObjectProperty;
import com.quartercode.classmod.util.FunctionDefinitionFactory;
import com.quartercode.classmod.util.PropertyAccessorFactory;
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
    protected static final FeatureDefinition<ObjectProperty<Packet>> PACKET;

    static {

        PACKET = ObjectProperty.createDefinition("packet");

    }

    // ----- Properties End -----

    // ----- Functions -----

    /**
     * Returns the received network {@link Packet} that has something to do with the event receiver.
     */
    public static final FunctionDefinition<Packet>                   GET_PACKET;

    /**
     * Changes the received network {@link Packet} that should have something to do with the event receiver.
     * 
     * <table>
     * <tr>
     * <th>Index</th>
     * <th>Type</th>
     * <th>Parameter</th>
     * <th>Description</th>
     * </tr>
     * <tr>
     * <td>0</td>
     * <td>{@link Packet}</td>
     * <td>packet</td>
     * <td>The new received network {@link Packet}.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>                     SET_PACKET;

    static {

        GET_PACKET = FunctionDefinitionFactory.create("getPacket", ReceivePacketEvent.class, PropertyAccessorFactory.createGet(PACKET));
        SET_PACKET = FunctionDefinitionFactory.create("setPacket", ReceivePacketEvent.class, new LockableFEWrapper<Void>(PropertyAccessorFactory.createSet(PACKET)), Packet.class);

        GET_SIZE.addExecutor(ReceivePacketEvent.class, "packet", SizeUtil.createGetSize(PACKET));

    }

    // ----- Functions End -----

    /**
     * Creates a new receive packet event.
     */
    public ReceivePacketEvent() {

    }

}
