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

package com.quartercode.disconnected.server.world.comp.net;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAttribute;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.lang3.Validate;
import com.quartercode.disconnected.server.world.comp.hardware.NetInterface;
import com.quartercode.disconnected.server.world.comp.net.nodes.ComputerConnectedNode;
import com.quartercode.disconnected.server.world.comp.net.nodes.DeviceNode;
import com.quartercode.disconnected.server.world.util.WorldNode;
import com.quartercode.disconnected.shared.world.comp.net.NetId;
import com.quartercode.jtimber.api.node.Node;

/**
 * This class represents one node inside a {@link Network}.
 * Network {@link Packet}s can be sent from one node to another; in most cases, this will require them to pass some other net nodes on their way.
 * See the JavaDoc of {@link Network} for more information on networks and network nodes.<br>
 * There are different implementations of net nodes available.
 * Each one of them takes on a different task inside the Internet (or any other multi-layer network).
 * However, most of them are {@link ComputerConnectedNode}s, which represent the {@link NetInterface} of a computer inside the world of networks.
 * Therefore, you can say that net nodes are kind of an abstraction of real network interfaces.<br>
 * <br>
 * Each net node is automatically assigned a {@link #getNodeId() node id}, which is the only attribute that is used for equality checks.
 * Moreover, it provides a {@link NetId} that uniquely identifies a single node in the whole Internet.<br>
 * <br>
 * Each net node implementation must provide two methods.
 * Firstly, {@link #getMaxConnections()} returns how many connections to other net nodes the very node is allowed to have inside a network.
 * For example, {@link DeviceNode}s are only allowed to connect to one other node (in most cases, this would be a router or a bridge).<br>
 * Secondly, {@link #process(Packet)} takes a {@link Packet} and handles it in terms of the net node's context.
 * This method could "resend" the packet to the next network node, or it could redirect the packet to the computer the network node is part of.
 * See the method's documentation for some more detailed information about the handling method.
 *
 * @see Network
 * @see Packet
 */
@EqualsAndHashCode (of = "nodeId", callSuper = false)
@ToString (of = "nodeId", callSuper = false)
public abstract class NetNode extends WorldNode<Node<?>> {

    /**
     * This integer value represents "infinity".
     * Internally, however, it just provides {@link Integer#MAX_VALUE}, the largest possible integer value.
     * This constant value can be used to represent infinity when dealing with {@link #getMaxConnections() net node max connection limits}.
     */
    public static final int INFINITY                 = Integer.MAX_VALUE;

    // By default, a net node is not part of a network (nodeId == -1)
    @XmlAttribute
    private int             nodeId                   = -1;

    // Transient
    private NetId           cachedNetId;
    private boolean         cachedNetIdRefreshNeeded = true;

    /**
     * Returns the {@link Network} the net node is currently part of.
     * Note that this method might return {@code null} if the net node isn't part of any network at the moment.<br>
     * <br>
     * Internally, this method just returns the {@link #getParents() net node parent} which is a network.
     *
     * @return The network this net node is part of.
     */
    public Network getNetwork() {

        for (Node<?> parent : getParents()) {
            if (parent instanceof Network) {
                return (Network) parent;
            }
        }

        return null;
    }

    /*
     * Ensure that the net node isn't already part of another network when it is added to a new network.
     */
    @Override
    protected void onAddParent(Node<?> parent) {

        if (parent instanceof Network) {
            for (Node<?> existingParent : getParents()) {
                if (existingParent != parent && existingParent instanceof Network) {
                    throw new IllegalStateException("A net node cannot be part of two networks at the same time");
                }
            }
        }
    }

    /**
     * Returns the internal node id which has been assigned by the node's {@link Network}.
     * A node id of {@code -1} indicates that the this net node isn't actually part of a network.<br>
     * The node id is used as the last component of the node's {@link #getNetId() net id}.
     * Moreover, it is the only attribute that is used for {@link #equals(Object) equality checks}.
     * See {@link Network} for more information on networks and how net nodes (and their node ids) fit into the concept.
     *
     * @return The node id of the net node.
     */
    public int getNodeId() {

        return nodeId;
    }

    /**
     * Assigns a new node id to this net node. See {@link #getNodeId()} for more information on the node id.
     * Note that this method is internal and should only be used by the {@link Network} class.
     *
     * @param nodeId The new node id that should be assigned to this net node.
     *        It must be {@code -1} (node is disconnected) or {@code >= 0} (node is part of a network).
     */
    protected void setNodeId(int nodeId) {

        Validate.isTrue(nodeId >= -1, "Net node node id must be == -1 (disconnected) or >= 0 (connected)");

        this.nodeId = nodeId;

        // Since the node id has changed, we need to refresh the net id
        // However, we can't directly refresh it
        cachedNetIdRefreshNeeded = true;
    }

    /**
     * Returns the {@link NetId} which uniquely identifies this net node in the whole Internet.
     * You can compare it with IP addresses, which are used for the same purpose in the real world.
     * See the JavaDoc of the net id class for more information related to net ids.
     *
     * @return The net id which identifies the net node.
     */
    public NetId getNetId() {

        // This calculation always triggers at least once if a node id has been set at some point in time or if the net node has just been deserialized
        if (cachedNetIdRefreshNeeded) {
            doRefreshNetId();
            cachedNetIdRefreshNeeded = false;
        }

        return cachedNetId;
    }

    private void doRefreshNetId() {

        // If the net node is no longer part of a network (nodeId == -1), it no longer has a net id as well
        if (nodeId == -1) {
            cachedNetId = null;
        }
        // Otherwise, calculate the proper net id of this net node
        else {
            NetId uplinkTargetNetId = getNetwork().getUplinkTargetNetId();
            List<Integer> nodeIds = uplinkTargetNetId == null ? new ArrayList<Integer>() : new ArrayList<>(uplinkTargetNetId.getNodeIdsAtTiers());
            nodeIds.add(nodeId);
            cachedNetId = new NetId(nodeIds);
        }
    }

    /**
     * Orders the net node to refresh its {@link #getNetId() net id} and adapt it to the current circumstances.
     * That might be useful if you change something in the upper-tier networks and want that change to reflect in all net nodes in the networks below.<br>
     * Note that the refreshment is not done instantly.
     * Instead, the net node is refreshed as soon as the next call to {@link #getNetId()} is made.
     */
    public void refreshNetId() {

        cachedNetIdRefreshNeeded = true;
    }

    /**
     * Returns the maximum amount of other net nodes this net node is allowed be connected to inside a {@link Network}.
     * For example, most end-user devices (e.g. personal computers) can only connect to one other node (in most cases a router or a bridge).
     * In order to implement such a limitation, this method can simply be overridden to return {@code 1}.
     * By default, no limit is imposed; this method returns the constant value {@link NetNode#INFINITY}.
     *
     * @return The maximum amount of connections to other net nodes this net node is allowed to have.
     */
    public int getMaxConnections() {

        return INFINITY;
    }

    /**
     * Processes the given {@link Packet} in terms of the network node's context.
     * This method could "resend" the packet to the next network node, or it could redirect the packet to the computer the network node is part of.
     * In some cases, this method might also check for special packet implementations that contain some useful precalculated information (e.g. about {@link RoutedPacket routing}).
     *
     * @param packet The packet that should be handled by the network node.
     *        Note that this argument might be {@code null}, in which case the packet should be ignored.
     */
    public abstract void process(Packet packet);

}
