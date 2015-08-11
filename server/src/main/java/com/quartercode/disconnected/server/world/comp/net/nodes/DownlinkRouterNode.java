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

import javax.xml.bind.annotation.XmlElement;
import org.apache.commons.lang3.Validate;
import com.quartercode.disconnected.server.world.comp.net.NetNode;
import com.quartercode.disconnected.server.world.comp.net.Network;
import com.quartercode.disconnected.server.world.comp.net.NetworkUtils.NetNodeMatcher;
import com.quartercode.disconnected.server.world.comp.net.Packet;
import com.quartercode.disconnected.server.world.comp.net.nodes.RoutingUtils.IdNodeMatcher;
import com.quartercode.disconnected.server.world.comp.net.nodes.RoutingUtils.TypeNodeMatcher;
import com.quartercode.disconnected.shared.world.comp.net.NetId;

/**
 * A downlink router {@link NetNode} is located in the upper-tier {@link Network} when connecting two network layers together.
 * Such a node also stores the lower-tier network it links with the upper-tier network it is located in itself.
 * Together with an {@link UplinkRouterNode} in the lower-tier network, the two nodes automatically create a link where {@link Packet}s can flow through.
 * However, if no uplink router node is available, no packets can be sent between the two network layers.<br>
 * <br>
 * If you wonder what network tiers are, you might want to check out the JavaDoc of {@link NetId} and {@link Network}.
 *
 * @see UplinkRouterNode
 */
public class DownlinkRouterNode extends TemplateNode {

    @XmlElement
    private Network subNetwork;

    /**
     * Creates a new downlink router node and creates a totally new and empty {@link #getSubNetwork() subnetwork}.
     * The new subnetwork doesn't even contain an {@link UplinkRouterNode}, which is required for establishing the link.
     * However, the link activates as soon as such an uplink router node is added to the subnetwork (use {@link #getSubNetwork()} ro retrieve the subnetwork).
     */
    public DownlinkRouterNode() {

        subNetwork = new Network();
    }

    /**
     * Creates a new downlink router node and sets the {@link #getSubNetwork() subnetwork} to the given one.
     * As long as (or as soon as) the new subnetwork contains an {@link UplinkRouterNode}, the link is automatically established.
     * No further steps are required.
     * However, if no uplink router node is available, no packets can be sent between the two network layers.
     *
     * @param subNetwork The subnetwork which should be linked with the network of this node.
     */
    public DownlinkRouterNode(Network subNetwork) {

        setSubNetwork(subNetwork);
    }

    /**
     * Returns the sub{@link Network network} which is linked with the network this downlink router node is part of.
     * As long as the set subnetwork contains an {@link UplinkRouterNode}, the link is established.
     * However, if no uplink router node is available, this router is unlinked and no packets can be sent between the two network layers.
     * In order to change the linked network, you can simply call the {@link #setSubNetwork(Network)} method.
     *
     * @return The subnetwork which is linked with the network of this node.
     */
    public Network getSubNetwork() {

        return subNetwork;
    }

    /**
     * Changes the sub{@link Network network} which is linked with the network this downlink router node is part of.
     * As long as (or as soon as) the new subnetwork contains an {@link UplinkRouterNode}, the link is automatically established.
     * No further steps are required.
     * However, if no uplink router node is available, no packets can be sent between the two network layers.
     *
     * @param subNetwork The subnetwork which should be linked with the network of this node.
     */
    public void setSubNetwork(Network subNetwork) {

        Validate.notNull(subNetwork, "Subnetwork of a downlink router node cannot be null");

        this.subNetwork = subNetwork;
        refreshNetIds(subNetwork);
    }

    private void refreshNetIds(Network network) {

        for (NetNode netNode : network.getNetNodes()) {
            netNode.refreshNetId();

            if (netNode instanceof DownlinkRouterNode) {
                refreshNetIds( ((DownlinkRouterNode) netNode).getSubNetwork());
            }
        }
    }

    /**
     * Returns the linked {@link UplinkRouterNode} inside the {@link #getSubNetwork() subnetwork}.
     * It is used for sending packets between the two networks.
     * If the subnetwork doesn't contain an uplink router node, {@code null} is returned.
     *
     * @return The uplink router node which is associated with this downlink router node, or {@code null} if this node is unlinked.
     */
    public UplinkRouterNode getDownlinkTarget() {

        return subNetwork.getNetNodeByType(UplinkRouterNode.class);
    }

    @Override
    protected void computeRouteAndExecute(Packet packet) {

        // See UnspecializedNode for documentation on this first part (up to where other comments start)

        int nextTarget = RoutingUtils.computeNextRoutingTarget(getNetId(), packet);

        if (nextTarget == -1) {
            deliverPacketToNetInterface(packet);
        }
        // The next target is this downlink -> send the packet down this downlink
        else if (nextTarget == -3) {
            UplinkRouterNode downlinkTarget = getDownlinkTarget();

            // If the downlink network doesn't have an uplink router node, drop the packet
            if (downlinkTarget != null) {
                downlinkTarget.process(packet);
            }
        }
        // Precompute the shortest path to the next routing target and send the packet on its way (see UnspecializedNode for documentation)
        else {
            NetNodeMatcher targetMatcher;
            if (nextTarget == -2) {
                targetMatcher = new TypeNodeMatcher(UplinkRouterNode.class);
            } else if (nextTarget >= 0) {
                targetMatcher = new IdNodeMatcher(nextTarget);
            } else {
                return;
            }
            process(RoutingUtils.precomputeShortestPath(this, targetMatcher, packet));
        }
    }

}
