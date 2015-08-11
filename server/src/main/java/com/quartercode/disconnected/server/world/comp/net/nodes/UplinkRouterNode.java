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

import com.quartercode.disconnected.server.world.comp.net.NetNode;
import com.quartercode.disconnected.server.world.comp.net.Network;
import com.quartercode.disconnected.server.world.comp.net.Packet;
import com.quartercode.disconnected.server.world.comp.net.nodes.RoutingUtils.IdNodeMatcher;
import com.quartercode.disconnected.shared.world.comp.net.NetId;
import com.quartercode.jtimber.api.node.Node;

/**
 * An uplink router {@link NetNode} is located in the lower-tier {@link Network} when connecting two network layers together.
 * Together with a {@link DownlinkRouterNode} in the upper-tier network, the two nodes automatically create a link where {@link Packet}s can flow through.
 * However, if the network containing the uplink node doesn't have a downlink node as parent, no packets can be sent between the two network layers.
 * It is quite important to note that each network is only allowed to contain <b>one</b> uplink router node.<br>
 * <br>
 * If you wonder what network tiers are, you might want to check out the JavaDoc of {@link NetId} and {@link Network}.
 *
 * @see DownlinkRouterNode
 */
public class UplinkRouterNode extends TemplateNode {

    /**
     * Returns the linked {@link DownlinkRouterNode} this uplink connects with in order to form a link.
     * It is used for sending packets between the two networks.
     * If the network containing this node doesn't have a downlink node as parent, {@code null} is returned.
     *
     * @return The downlink router node which is associated with this uplink router node, or {@code null} if this node is unlinked.
     */
    public DownlinkRouterNode getUplinkTarget() {

        Node<?> presumableUplinkTarget = getSingleParent().getSingleParent();

        if (presumableUplinkTarget instanceof DownlinkRouterNode) {
            return (DownlinkRouterNode) presumableUplinkTarget;
        } else {
            return null;
        }
    }

    /*
     * Ensure that the network which contains the uplink router doesn't already contain another uplink router.
     */
    @Override
    protected void onAddParent(Node<?> parent) {

        super.onAddParent(parent);

        if (parent instanceof Network) {
            for (NetNode netNode : ((Network) parent).getNetNodes()) {
                if (netNode instanceof UplinkRouterNode && !netNode.equals(this)) {
                    throw new IllegalStateException("Cannot add two uplink router nodes to one network");
                }
            }
        }
    }

    @Override
    protected void computeRouteAndExecute(Packet packet) {

        // See UnspecializedNode for documentation on this first part (up to where other comments start)

        int nextTarget = RoutingUtils.computeNextRoutingTarget(getNetId(), packet);

        if (nextTarget == -1) {
            deliverPacketToNetInterface(packet);
        }
        // The next target is the uplink of the current network -> send the packet up this uplink
        else if (nextTarget == -2) {
            getUplinkTarget().process(packet);
        } else if (nextTarget >= 0) {
            // The next target is located inside the network this node is located in as well
            process(RoutingUtils.precomputeShortestPath(this, new IdNodeMatcher(nextTarget), packet));
        }

        // Otherwise: Drop the packet because the target is unknown
    }

}
