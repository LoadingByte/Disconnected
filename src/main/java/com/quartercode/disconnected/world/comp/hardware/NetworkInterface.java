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

package com.quartercode.disconnected.world.comp.hardware;

import java.util.Queue;
import com.quartercode.classmod.base.FeatureDefinition;
import com.quartercode.classmod.base.FeatureHolder;
import com.quartercode.classmod.base.def.AbstractFeatureDefinition;
import com.quartercode.classmod.extra.FunctionDefinition;
import com.quartercode.classmod.extra.def.ObjectProperty;
import com.quartercode.classmod.util.CollectionPropertyAccessorFactory;
import com.quartercode.classmod.util.FunctionDefinitionFactory;
import com.quartercode.classmod.util.PropertyAccessorFactory;
import com.quartercode.disconnected.world.comp.hardware.Mainboard.NeedsMainboardSlot;
import com.quartercode.disconnected.world.comp.net.IP;
import com.quartercode.disconnected.world.comp.net.Packet;

/**
 * This class represents a network interface of a computer.
 * Every network interface has a speed a byte needs to get transfered through it.
 * Network interfaces are a central compontent of the internet infrastructure as they provide a connection to the internet.
 * 
 * @see Hardware
 * @see Packet
 */
@NeedsMainboardSlot
public class NetworkInterface extends Hardware {

    // ----- Properties -----

    /**
     * The {@link IP} this interface can be found under.
     */
    protected static final FeatureDefinition<ObjectProperty<IP>>            IP;

    /**
     * A {@link Queue} of remaining {@link Packet}s that should be sent soon.
     */
    protected static final FeatureDefinition<ObjectProperty<Queue<Packet>>> REMAINING_PACKETS;

    static {

        IP = new AbstractFeatureDefinition<ObjectProperty<IP>>("ip") {

            @Override
            public ObjectProperty<IP> create(FeatureHolder holder) {

                return new ObjectProperty<IP>(getName(), holder);
            }

        };

        REMAINING_PACKETS = new AbstractFeatureDefinition<ObjectProperty<Queue<Packet>>>("remainingPackets") {

            @Override
            public ObjectProperty<Queue<Packet>> create(FeatureHolder holder) {

                return new ObjectProperty<Queue<Packet>>(getName(), holder);
            }

        };

    }

    // ----- Properties End -----

    // ----- Functions -----

    /**
     * Returns the {@link IP} this interface can be found under.
     */
    public static final FunctionDefinition<IP>                              GET_IP;

    /**
     * Changes the {@link IP} this interface can be found under.
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
     * <td>{@link IP}</td>
     * <td>ip</td>
     * <td>The new {@link IP}.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>                            SET_IP;

    /**
     * Retrieves the next {@link Packet} in the sending queue and removes it ({@link Queue} poll).
     */
    public static final FunctionDefinition<Packet>                          NEXT_PACKET;

    /**
     * Sends some {@link Packet}s through the network interface by putting them on the internal {@link Packet} {@link Queue}.
     * 
     * <table>
     * <tr>
     * <th>Index</th>
     * <th>Type</th>
     * <th>Parameter</th>
     * <th>Description</th>
     * </tr>
     * <tr>
     * <td>0...</td>
     * <td>{@link Packet}...</td>
     * <td>packets</td>
     * <td>The {@link Packet}s to send through the interface.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>                            SEND_PACKETS;

    static {

        GET_IP = FunctionDefinitionFactory.create("getIp", NetworkInterface.class, PropertyAccessorFactory.createGet(IP));
        SET_IP = FunctionDefinitionFactory.create("setIp", NetworkInterface.class, PropertyAccessorFactory.createSet(IP), IP.class);

        NEXT_PACKET = FunctionDefinitionFactory.create("nextPacket", NetworkInterface.class, CollectionPropertyAccessorFactory.createPoll(REMAINING_PACKETS));
        SEND_PACKETS = FunctionDefinitionFactory.create("sendPacket", NetworkInterface.class, CollectionPropertyAccessorFactory.createAdd(REMAINING_PACKETS), Packet[].class);

    }

    // ----- Functions End -----

    /**
     * Creates a new network interface.
     */
    public NetworkInterface() {

    }

}
