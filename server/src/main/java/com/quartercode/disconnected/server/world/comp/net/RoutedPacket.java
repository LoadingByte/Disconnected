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

package com.quartercode.disconnected.server.world.comp.net;

import static com.quartercode.classmod.factory.ClassmodFactory.factory;
import java.util.ArrayList;
import java.util.List;
import com.quartercode.classmod.extra.prop.CollectionPropertyDefinition;
import com.quartercode.classmod.extra.prop.PropertyDefinition;
import com.quartercode.classmod.extra.storage.StandardStorage;
import com.quartercode.classmod.extra.valuefactory.CloneValueFactory;
import com.quartercode.classmod.factory.CollectionPropertyDefinitionFactory;
import com.quartercode.classmod.factory.PropertyDefinitionFactory;
import com.quartercode.disconnected.server.world.util.DerivableSize;
import com.quartercode.disconnected.server.world.util.SizeUtils;
import com.quartercode.disconnected.server.world.util.WorldFeatureHolder;

/**
 * This class represents a packet which is routed between routers.
 * It wraps a real {@link Packet} and stores some important routing information.
 * 
 * @see #PATH
 */
public class RoutedPacket extends WorldFeatureHolder implements DerivableSize {

    // ----- Properties -----

    /**
     * The network {@link Packet} that is wrapped by the routed packet.
     * It should be extracted when the routed packet reaches its destination.
     */
    public static final PropertyDefinition<Packet>                           PACKET;

    /**
     * An integer queue that contains the subnet ids that belong to the routers which should transport the routed packet.
     * When a router receives the packet, it should apply the following algorithm:
     * 
     * <table border="1">
     * <tr>
     * <td colspan="6">Is the path queue empty?</td>
     * </tr>
     * <tr>
     * <td colspan="2">Yes</td>
     * <td colspan="4">No</td>
     * </tr>
     * <tr>
     * <td colspan="2">Is the packet destination a node in my subnet?</td>
     * <td colspan="4">Poll the next entry from the queue.</td>
     * </tr>
     * <tr>
     * <td>Yes</td>
     * <td>No</td>
     * <td colspan="4">Is the next entry a negative integer?</td>
     * </tr>
     * <tr>
     * <td>Extract the packet and hand it over to the destination node.</td>
     * <td>Calculate a new route to the destination of the packet.</td>
     * <td colspan="2">Yes</td>
     * <td colspan="2">No</td>
     * </tr>
     * <tr>
     * <td colspan="2"></td>
     * <td colspan="2">Am I connected to the backbone?</td>
     * <td colspan="2">Is the entry equal to the subnet of a neighbour router?</td>
     * </tr>
     * <tr>
     * <td colspan="2"></td>
     * <td>Yes</td>
     * <td>No</td>
     * <td>Yes</td>
     * <td>No</td>
     * </tr>
     * <tr>
     * <td colspan="2"></td>
     * <td>Extract the packet and hand it over to the backbone.</td>
     * <td>Calculate a new route to the destination of the packet.</td>
     * <td>Hand the routed packet over to the determined neighbour router.</td>
     * <td>Calculate a new route to the destination of the packet.</td>
     * </tr>
     * </table>
     * 
     * Note that this cannot be an actual queue since Classmod cannot handle queues in collection properties.
     */
    public static final CollectionPropertyDefinition<Integer, List<Integer>> PATH;

    static {

        PACKET = factory(PropertyDefinitionFactory.class).create("packet", new StandardStorage<>());
        PATH = factory(CollectionPropertyDefinitionFactory.class).create("path", new StandardStorage<>(), new CloneValueFactory<>(new ArrayList<>()));

    }

    // ----- Functions -----

    static {

        GET_SIZE.addExecutor("data", RoutedPacket.class, SizeUtils.createGetSize(PACKET));

    }

}
