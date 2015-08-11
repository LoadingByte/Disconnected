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

import java.util.LinkedList;
import java.util.Queue;
import com.quartercode.disconnected.server.world.comp.net.NetNode;
import com.quartercode.disconnected.server.world.comp.net.Network;
import com.quartercode.disconnected.server.world.comp.net.NetworkUtils;
import com.quartercode.disconnected.server.world.comp.net.NetworkUtils.NetNodeMatcher;
import com.quartercode.disconnected.server.world.comp.net.Packet;
import com.quartercode.disconnected.server.world.comp.net.RoutedPacket;
import com.quartercode.disconnected.server.world.comp.net.StandardPacket;
import com.quartercode.disconnected.shared.world.comp.net.NetId;

/**
 * This utility class contains several utilities related to implementing the routing algorithm for {@link NetNode#process(Packet)}.
 * Different {@link NetNode} types can use these utilities as building blocks for building their own {@link Packet} handling algorithms.
 *
 * @see NetNode
 * @see NetNode#process(Packet)
 */
public class RoutingUtils {

    /**
     * Ensures that the given {@link Packet} isn't a {@link RoutedPacket} wrapper, but instead a packet without any routing information (e.g. a {@link StandardPacket}).
     * That is done by returning the {@link RoutedPacket#getWrappedPacket() packet which has been wrapped} if the given input packet is a routed packet.
     * Note that this method is able to recursively unwrap routed packets (e.g. routed packets inside routed packets).
     *
     * @param packet The possible routed packet wrapper which should be unwrapped.
     * @return The unwrapped packet, which definitely is not a routed packet.
     */
    public static Packet unwrapRoutedPacket(Packet packet) {

        if (packet instanceof RoutedPacket) {
            return unwrapRoutedPacket( ((RoutedPacket) packet).getWrappedPacket());
        } else {
            return packet;
        }
    }

    /**
     * If the given {@link Packet} is a {@link RoutedPacket} received by the given {@link NetNode} and the node is connected to the next node on the path, this method returns that next node.
     * Otherwise, {@code null} is returned.
     * If you call this method from inside a {@link NetNode#process(Packet)} method and want to see what to do now, the results can be interpreted like this:
     *
     * <table>
     * <tr>
     * <th>Result</th>
     * <th>Action</th>
     * </tr>
     * <tr>
     * <td>{@code null}</td>
     * <td>A new route needs to be calculated since the packet is not a routed packet or the routing information is invalid.</td>
     * </tr>
     * <tr>
     * <td>{@code NetNode}</td>
     * <td>The packet should be directly handed over to the returned net node by calling its {@link NetNode#process(Packet)} method.</td>
     * </tr>
     * </table>
     * <br>
     * It is important to note that <b>this method modifies the {@link RoutedPacket#getPath() path queue} of the routed packet.
     * Therefore, it should only be called once!</b><br>
     * <br>
     * Also note that the algorithm implemented by this method is described in the first diagram shown in the JavaDoc of the {@link RoutedPacket#getPath()} method.
     *
     * @param netNode The net node which has received the given packet and wants to know the route which might have already been calculated for the packet.
     * @param packet The packet which has been received by the given net node.
     * @return The next valid and possible "hop" in the precomputed path of the given path of the given packet, or {@code null} if it doesn't exist.
     */
    public static NetNode getNextNetNodeWithRoutedPacketInfo(NetNode netNode, Packet packet) {

        if (packet instanceof RoutedPacket) {
            RoutedPacket rpacket = (RoutedPacket) packet;

            if (!rpacket.getPath().isEmpty()) {
                int nextNodeId = rpacket.getPath().poll();

                for (NetNode connectedNetNode : netNode.getNetwork().getConnectedNetNodes(netNode)) {
                    if (connectedNetNode.getNodeId() == nextNodeId) {
                        return connectedNetNode;
                    }
                }
            }
        }

        return null;
    }

    /**
     * Computes the <b>next target</b> of a possible {@link RoutedPacket#getPath() route} for the given {@link Packet} through the current {@link Network},
     * using the {@link NetNode} with the given {@link NetId} as source.
     * That means that the current net node should try to route the packet to the returned next target.
     * That target then knows what to do next.<br>
     * <br>
     * You might wonder why the target of a packet route doesn't have to be the actual final {@link Packet#getDestination() destination} of the packet.
     * The packet often needs to jump through various network layers (or tiers) to get to its destination.
     * However, a precomputed route is only possible for one network layer and cannot describe jumps between two different tiers.
     * Instead, the route takes the packet to the next {@link UplinkRouterNode uplink}/{@link DownlinkRouterNode downlink} router, which in turn executes the layer change.
     * See the JavaDoc of {@link NetId} and {@link Network} for more information.<br>
     * <br>
     * This method is able to return one the following results, directing the net node to execute a specific action.
     * For performance reasons, simple integer codes are used as results.
     *
     * <table>
     * <tr>
     * <th>Result</th>
     * <th>Action</th>
     * </tr>
     * <tr>
     * <td>{@code -1}</td>
     * <td>The current net node is the final destination of the packet. Therefore, the net node should try to deliver the packet to the OS of its computer.</td>
     * </tr>
     * <tr>
     * <td>{@code -2}</td>
     * <td>The next target is the one and only {@link UplinkRouterNode} of my network. If the current node is the uplink router, it should send the packet up through its uplink.</td>
     * </tr>
     * <tr>
     * <td>{@code -3}</td>
     * <td>The current node seems to be a {@link DownlinkRouterNode}. It should send the packet down through its downlink. If the node is not a downlink router, the packet should be dropped.</td>
     * </tr>
     * <tr>
     * <td>{@code >= 0}</td>
     * <td>The next target is the net node with the returned node id inside my network.</td>
     * </tr>
     * </table>
     * <br>
     * Note that the algorithm implemented by this method is described in the second diagram shown in the JavaDoc of the {@link RoutedPacket#getPath()} method.
     *
     * @param sourceNetId The net id of the net node which currently processes the packet and therefore wants to compute its next target.
     * @param packet The packet which is currently processed.
     * @return The next target of the given packet. See the table above for an explanation of the different integer codes.
     */
    public static int computeNextRoutingTarget(NetId sourceNetId, Packet packet) {

        NetId destNetId = packet.getDestination().getNetId();

        if (sourceNetId.equals(destNetId)) {
            // To me
            return -1;
        } else {
            if (sourceNetId.getTier() > destNetId.getTier()) {
                // To uplink
                return -2;
            } else {
                int penultimateTier = sourceNetId.getTier() - 1;
                // We need the "+ 1" here because the second sublist index is exclusive
                if (sourceNetId.getNodeIdsAtTiers().subList(0, penultimateTier + 1).equals(destNetId.getNodeIdsAtTiers().subList(0, penultimateTier + 1))) {
                    if (sourceNetId.getTier() < destNetId.getTier()) {
                        // To downlink
                        int downlinkNodeId = destNetId.getNodeIdAtTier(sourceNetId.getTier());
                        if (sourceNetId.getNodeIdAtLowestTier() == downlinkNodeId) {
                            return -3;
                        } else {
                            return downlinkNodeId;
                        }
                    } else {
                        // To node in my network
                        return destNetId.getNodeIdAtLowestTier();
                    }
                } else {
                    // To uplink
                    return -2;
                }
            }
        }
    }

    /**
     * Creates a new {@link RoutedPacket} using the path returned by {@link NetworkUtils#getShortestPath(NetNode, NetNodeMatcher)} for the given arguments.
     * This method should be used because it's automatically executing some important steps:
     *
     * <ul>
     * <li>Convert the net node list, which was returned by {@code getShortestPath()}, to a node id list.</li>
     * <li>Remove the first entry from the list because that's the source node itself (unnecessary).</li>
     * <li>Catch exceptions thrown by {@code getShortestPath()} and completely return a {@code null} packet instead. Such a result indicates that finding a path is impossible.</li>
     * </ul>
     *
     * See {@link NetworkUtils#getShortestPath(NetNode, NetNodeMatcher)} for more information on the shortest path algorithm.
     *
     * @param sourceNode The start net node the used path originates from.
     *        This node probably wants to send the packet through the network.
     * @param targetNodeMatcher A {@link NetNodeMatcher} that returns {@code true} for the net node the path should end at.
     * @param packet The packet which should be wrapped into a routed packet.
     * @return The routed packet with a path through the network that goes from the given source node to the (yet to be discovered) target node.
     *         This might also be {@code null} if the path is not computable for some reason.
     */
    public static RoutedPacket precomputeShortestPath(NetNode sourceNode, NetNodeMatcher targetNodeMatcher, Packet packet) {

        try {
            Queue<Integer> path = new LinkedList<>();

            for (NetNode pathNode : NetworkUtils.getShortestPath(sourceNode, targetNodeMatcher)) {
                path.add(pathNode.getNodeId());
            }

            // Since the computed shortest path contains the starting node as well, we have to remove it
            // Otherwise, the starting node would try to route the packet to itself
            path.poll();

            return new RoutedPacket(packet, path);
        } catch (IllegalStateException e) {
            // The path is not computable because of an invalid destination or network state
            // Therefore, null is returned
            return null;
        }
    }

    private RoutingUtils() {

    }

    /**
     * A {@link NetNodeMatcher} which matches all {@link NetNode}s that are an instance of a provided net node class (similar to the {@code instanceof} operator).
     */
    public static class TypeNodeMatcher implements NetNodeMatcher {

        private final Class<? extends NetNode> type;

        /**
         * Creates a new type node matcher.
         *
         * @param type The {@link NetNode} type all matched net nodes must be an {@link Class#isInstance(Object) instance} of.
         *        The comparison is similar to the {@code instanceof} operator.
         */
        public TypeNodeMatcher(Class<? extends NetNode> type) {

            this.type = type;
        }

        @Override
        public boolean matches(NetNode netNode) {

            return type.isInstance(netNode);
        }

    }

    /**
     * A {@link NetNodeMatcher} which matches all {@link NetNode}s that have a specific {@link NetNode#getNodeId() node id} assigned to them.
     */
    public static class IdNodeMatcher implements NetNodeMatcher {

        private final int nodeId;

        /**
         * Creates a new id node matcher.
         *
         * @param nodeId The {@link NetNode#getNodeId() node id} all matched net nodes must have assigned to them.
         */
        public IdNodeMatcher(int nodeId) {

            this.nodeId = nodeId;
        }

        @Override
        public boolean matches(NetNode netNode) {

            return netNode.getNodeId() == nodeId;
        }

    }

}
