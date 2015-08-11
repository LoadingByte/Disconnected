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

package com.quartercode.disconnected.server.world.comp.net.nodes;

import com.quartercode.disconnected.server.world.comp.net.NetworkUtils.NetNodeMatcher;
import com.quartercode.disconnected.server.world.comp.net.Packet;
import com.quartercode.disconnected.server.world.comp.net.RoutedPacket;
import com.quartercode.disconnected.server.world.comp.net.nodes.RoutingUtils.IdNodeMatcher;
import com.quartercode.disconnected.server.world.comp.net.nodes.RoutingUtils.TypeNodeMatcher;

/**
 * An abstract {@link ComputerConnectedNode} which implements the default {@link RoutedPacket#getPath() packet handling algorithm} without further extensions.
 * Therefore, it is able to send, forward and receive {@link Packet}s.
 * This class is extended by the two unspecialized node versions {@link DeviceNode} and {@link BridgeNode}.
 */
public abstract class UnspecializedNode extends TemplateNode {

    @Override
    protected void computeRouteAndExecute(Packet packet) {

        // Compute the node the packet should be sent to next
        int nextTarget = RoutingUtils.computeNextRoutingTarget(getNetId(), packet);

        // The packet's destination is the computer which is assigned to this node
        if (nextTarget == -1) {
            deliverPacketToNetInterface(packet);
        }
        // Precompute the shortest path to the next routing target and send the packet on its way
        else {
            NetNodeMatcher targetMatcher;

            // The next target is the uplink of the current network
            if (nextTarget == -2) {
                targetMatcher = new TypeNodeMatcher(UplinkRouterNode.class);
            }
            // The next target is located inside the network this node is located in as well
            else if (nextTarget >= 0) {
                targetMatcher = new IdNodeMatcher(nextTarget);
            }
            // Drop the packet because the target is unknown
            else {
                return;
            }

            process(RoutingUtils.precomputeShortestPath(this, targetMatcher, packet));
        }
    }

}
