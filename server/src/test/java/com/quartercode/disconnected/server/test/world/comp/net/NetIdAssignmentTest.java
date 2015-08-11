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

package com.quartercode.disconnected.server.test.world.comp.net;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import com.quartercode.disconnected.server.world.comp.net.Network;
import com.quartercode.disconnected.server.world.comp.net.nodes.BridgeNode;
import com.quartercode.disconnected.server.world.comp.net.nodes.DeviceNode;
import com.quartercode.disconnected.server.world.comp.net.nodes.DownlinkRouterNode;
import com.quartercode.disconnected.server.world.comp.net.nodes.UplinkRouterNode;
import com.quartercode.disconnected.shared.world.comp.net.NetId;

public class NetIdAssignmentTest {

    @Test
    public void test() {

        DeviceNode deviceNode = new DeviceNode();

        assertEquals("Net id assigned to the disconnected device node", null, deviceNode.getNetId());

        // Tier 3
        Network tier3 = new Network();
        tier3.addConnection(deviceNode, new UplinkRouterNode());

        // Tier 2
        Network tier2 = new Network();
        BridgeNode tier2BridgeNode = new BridgeNode();
        tier2.addConnection(tier2BridgeNode, new UplinkRouterNode());
        tier2.addConnection(tier2BridgeNode, new DownlinkRouterNode(tier3));

        // Tier 1
        Network tier1 = new Network();
        tier1.addConnection(new UplinkRouterNode(), new DownlinkRouterNode(tier2));

        // Tier 0
        Network tier0 = new Network();
        DownlinkRouterNode tier0Downlink = new DownlinkRouterNode(tier1);
        // This device node is just a dummy so that the network can exist
        tier0.addConnection(tier0Downlink, new DeviceNode());

        assertEquals("Net id assigned to the connected device node", new NetId(0, 1, 2, 0), deviceNode.getNetId());

        // Change the downlink router in tier 0 which leads down to the tier 1 network
        // The net id of the device node should change as a result of this
        tier0Downlink.setSubNetwork(new Network());
        tier0.addConnection(tier0Downlink, new DownlinkRouterNode(tier1));

        assertEquals("Net id assigned to the connected device node after the change", new NetId(2, 1, 2, 0), deviceNode.getNetId());
    }
}
