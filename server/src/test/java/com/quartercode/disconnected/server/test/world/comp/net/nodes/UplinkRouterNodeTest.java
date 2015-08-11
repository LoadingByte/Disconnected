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

import static org.junit.Assert.assertSame;
import org.junit.Before;
import org.junit.Test;
import com.quartercode.disconnected.server.world.comp.net.Network;
import com.quartercode.disconnected.server.world.comp.net.nodes.DeviceNode;
import com.quartercode.disconnected.server.world.comp.net.nodes.DownlinkRouterNode;
import com.quartercode.disconnected.server.world.comp.net.nodes.UplinkRouterNode;

public class UplinkRouterNodeTest {

    private Network          network;
    private UplinkRouterNode uplinkNode;

    @Before
    public void setUp() {

        network = new Network();
        uplinkNode = new UplinkRouterNode();

        // The device node is just a dummy so that the network can exist
        network.addConnection(uplinkNode, new DeviceNode());
    }

    @Test
    public void testGetUplinkTarget() {

        // Create a downlink node which stores the new network
        DownlinkRouterNode downlinkNode = new DownlinkRouterNode(network);

        // Assert that the method functions as expected
        assertSame("Uplink target", downlinkNode, uplinkNode.getUplinkTarget());
    }

    @Test
    public void testGetUplinkTargetWhileUnlinked() {

        // No downlink has been set up
        assertSame("Uplink target while unlinked", null, uplinkNode.getUplinkTarget());
    }

    @Test (expected = IllegalStateException.class)
    public void testOneUplinkNodePerNetworkLimit() {

        Network network = new Network();

        // Try to add a second uplink node
        network.addConnection(uplinkNode, new UplinkRouterNode());
    }

}
