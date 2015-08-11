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

package com.quartercode.disconnected.server.test.world.comp.net.nodes;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import com.quartercode.disconnected.server.world.comp.net.Network;
import com.quartercode.disconnected.server.world.comp.net.nodes.DeviceNode;
import com.quartercode.disconnected.server.world.comp.net.nodes.DownlinkRouterNode;
import com.quartercode.disconnected.server.world.comp.net.nodes.UplinkRouterNode;

public class DownlinkRouterNodeTest {

    @Test
    public void testGetDownlinkTarget() {

        Network network = new Network();
        UplinkRouterNode uplinkNode = new UplinkRouterNode();
        // The device node is just a dummy so that the network can exist
        network.addConnection(uplinkNode, new DeviceNode());

        // Create a downlink node which stores the new network
        DownlinkRouterNode downlinkNode = new DownlinkRouterNode(network);

        // Assert that the method functions as expected
        assertEquals("Downlink target", uplinkNode, downlinkNode.getDownlinkTarget());
    }

    @Test
    public void testGetDownlinkTargetWhileUnlinked() {

        // Create a downlink node which stores a network *that doesn't contain an uplink node*
        DownlinkRouterNode downlinkNode = new DownlinkRouterNode();

        // Assert that the method functions as expected
        assertEquals("Downlink target while unlinked", null, downlinkNode.getDownlinkTarget());
    }

}
