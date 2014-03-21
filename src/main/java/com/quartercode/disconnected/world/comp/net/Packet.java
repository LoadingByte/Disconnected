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
import com.quartercode.classmod.base.def.DefaultFeatureHolder;
import com.quartercode.classmod.extra.FunctionDefinition;
import com.quartercode.classmod.extra.def.LockableFEWrapper;
import com.quartercode.classmod.extra.def.ObjectProperty;
import com.quartercode.classmod.util.FunctionDefinitionFactory;
import com.quartercode.classmod.util.PropertyAccessorFactory;
import com.quartercode.disconnected.world.comp.SizeUtil;
import com.quartercode.disconnected.world.comp.SizeUtil.DerivableSize;

/**
 * This class represents a packet which can be sent between network interfaces.
 * A packet contains a sender, a receiver (both represented by addresses) and a data payload {@link Object} which should be sent.
 */
public class Packet extends DefaultFeatureHolder implements DerivableSize {

    // ----- Properties -----

    /**
     * The network {@link Address} which sends the packet.
     */
    protected static final FeatureDefinition<ObjectProperty<Address>> SENDER;

    /**
     * The network {@link Address} which should receive the packet.
     */
    protected static final FeatureDefinition<ObjectProperty<Address>> RECEIVER;

    /**
     * The data payload {@link Object} which should be sent.
     * The payload can't be modified after construction.
     */
    protected static final FeatureDefinition<ObjectProperty<Object>>  DATA;

    static {

        SENDER = ObjectProperty.createDefinition("sender");
        RECEIVER = ObjectProperty.createDefinition("receiver");
        DATA = ObjectProperty.createDefinition("data");

    }

    // ----- Properties End -----

    // ----- Functions -----

    /**
     * Returns the network {@link Address} which sends the packet.
     */
    public static final FunctionDefinition<Address>                   GET_SENDER;

    /**
     * Changes the network {@link Address} which sends the packet.
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
     * <td>{@link Address}</td>
     * <td>sender</td>
     * <td>The new sending network {@link Address}.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>                      SET_SENDER;

    /**
     * Returns the network {@link Address} which should receive the packet.
     */
    public static final FunctionDefinition<Address>                   GET_RECEIVER;

    /**
     * Changes the network {@link Address} which should receive the packet.
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
     * <td>{@link Address}</td>
     * <td>receiver</td>
     * <td>The new receiving network {@link Address}.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>                      SET_RECEIVER;

    /**
     * Returns the data payload {@link Object} which should be sent.
     * The payload can't be modified after construction.
     */
    public static final FunctionDefinition<Object>                    GET_DATA;

    /**
     * Changes the data payload {@link Object} which should be sent.
     * The payload can't be modified after construction.
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
     * <td>{@link Object}</td>
     * <td>data</td>
     * <td>The new data payload {@link Object}.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>                      SET_DATA;

    static {

        GET_SENDER = FunctionDefinitionFactory.create("getSender", Packet.class, PropertyAccessorFactory.createGet(SENDER));
        SET_SENDER = FunctionDefinitionFactory.create("setSender", Packet.class, new LockableFEWrapper<Void>(PropertyAccessorFactory.createSet(SENDER)), Address.class);

        GET_RECEIVER = FunctionDefinitionFactory.create("getReceiver", Packet.class, PropertyAccessorFactory.createGet(RECEIVER));
        SET_RECEIVER = FunctionDefinitionFactory.create("setReceiver", Packet.class, new LockableFEWrapper<Void>(PropertyAccessorFactory.createSet(RECEIVER)), Address.class);

        GET_DATA = FunctionDefinitionFactory.create("getData", Packet.class, PropertyAccessorFactory.createGet(DATA));
        SET_DATA = FunctionDefinitionFactory.create("setData", Packet.class, new LockableFEWrapper<Void>(PropertyAccessorFactory.createSet(DATA)), Object.class);

        GET_SIZE.addExecutor(Packet.class, "data", SizeUtil.createGetSize(DATA));

    }

    // ----- Functions End -----

    /**
     * Creates a new packet.
     */
    public Packet() {

    }

}
