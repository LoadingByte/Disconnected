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

import java.util.LinkedList;
import java.util.Queue;
import com.quartercode.classmod.extra.CollectionPropertyDefinition;
import com.quartercode.classmod.extra.FunctionDefinition;
import com.quartercode.classmod.extra.PropertyDefinition;
import com.quartercode.classmod.extra.def.ObjectCollectionProperty;
import com.quartercode.classmod.extra.def.ObjectProperty;
import com.quartercode.classmod.util.CollectionPropertyAccessorFactory;
import com.quartercode.classmod.util.FunctionDefinitionFactory;
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
    public static final PropertyDefinition<IP>                                 IP;

    /**
     * A {@link Queue} of remaining {@link Packet}s that should be sent soon.
     */
    protected static final CollectionPropertyDefinition<Packet, Queue<Packet>> REMAINING_PACKETS;

    static {

        IP = ObjectProperty.createDefinition("ip");
        REMAINING_PACKETS = ObjectCollectionProperty.createDefinition("remainingPackets", new LinkedList<Packet>(), true);

    }

    // ----- Functions -----

    /**
     * Retrieves the next {@link Packet} in the sending queue and removes it ({@link Queue} poll).
     */
    public static final FunctionDefinition<Packet>                             NEXT_PACKET;

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
    public static final FunctionDefinition<Void>                               SEND_PACKETS;

    static {

        NEXT_PACKET = FunctionDefinitionFactory.create("nextPacket", NetworkInterface.class, CollectionPropertyAccessorFactory.createPoll(REMAINING_PACKETS));
        SEND_PACKETS = FunctionDefinitionFactory.create("sendPacket", NetworkInterface.class, CollectionPropertyAccessorFactory.createAdd(REMAINING_PACKETS), Packet[].class);

    }

    /**
     * Creates a new network interface.
     */
    public NetworkInterface() {

    }

}
