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

package com.quartercode.disconnected.world.comp.program.event;

import com.quartercode.disconnected.mocl.base.FeatureDefinition;
import com.quartercode.disconnected.mocl.base.FeatureHolder;
import com.quartercode.disconnected.mocl.base.def.AbstractFeatureDefinition;
import com.quartercode.disconnected.mocl.extra.FunctionDefinition;
import com.quartercode.disconnected.mocl.extra.def.LockableFEWrapper;
import com.quartercode.disconnected.mocl.extra.def.ObjectProperty;
import com.quartercode.disconnected.mocl.util.FunctionDefinitionFactory;
import com.quartercode.disconnected.mocl.util.PropertyAccessorFactory;
import com.quartercode.disconnected.world.comp.SizeUtil;
import com.quartercode.disconnected.world.comp.net.Packet;
import com.quartercode.disconnected.world.comp.program.Process;

/**
 * This class represents the income of a network {@link Packet} which on a port the {@link Process} is listening on.
 * A packet event contains the receiving {@link Process}es and the actual {@link Packet} which was received.
 * 
 * @see ProcessEvent
 */
public class PacketEvent extends ProcessEvent {

    // ----- Properties -----

    /**
     * The {@link Packet} which was received on a port the {@link Process} is listening on.
     */
    protected static final FeatureDefinition<ObjectProperty<Packet>> PACKET;

    static {

        PACKET = new AbstractFeatureDefinition<ObjectProperty<Packet>>("packet") {

            @Override
            public ObjectProperty<Packet> create(FeatureHolder holder) {

                return new ObjectProperty<Packet>(getName(), holder);
            }

        };

    }

    // ----- Properties End -----

    // ----- Functions -----

    /**
     * Returns the {@link Packet} which was received on a port the {@link Process} is listening on.
     */
    public static final FunctionDefinition<Packet>                   GET_PACKET;

    /**
     * Changes the {@link Packet} which was received on a port the {@link Process} is listening on.
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
     * <td>The {@link Packet} which was received for the {@link Process}.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>                     SET_PACKET;

    static {

        GET_PACKET = FunctionDefinitionFactory.create("getPacket", PacketEvent.class, PropertyAccessorFactory.createGet(PACKET));
        SET_PACKET = FunctionDefinitionFactory.create("setPacket", PacketEvent.class, new LockableFEWrapper<Void>(PropertyAccessorFactory.createSet(PACKET)), Packet.class);

        GET_SIZE.addExecutor(ProcessEvent.class, "data", SizeUtil.createGetSize(PACKET));

    }

    // ----- Functions End -----

    /**
     * Creates a new IPC message event.
     */
    public PacketEvent() {

    }

}
