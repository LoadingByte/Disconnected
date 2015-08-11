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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import org.apache.commons.lang3.Validate;

/**
 * This class contains utility methods related to {@link Network}s and the {@link NetNode}s they contain.
 */
public class NetworkUtils {

    /**
     * Calculates the <b>shortest</b> valid path from the given start ("source") {@link NetNode} to the given end ("target") node inside the same {@link Network}.
     * The returned path list contains the net nodes you would need to "jump" to in order to get from the source to the target node in the correct order.
     * It includes both the source node (first element) and the target node (last element).<br>
     * The target net node is detected by calling the given {@link NetNodeMatcher} on each visited net node.
     * The target node is the node it returns {@code true} for.<br>
     * <br>
     * Note that the internal BFS algorithm is taken from <a href="http://algs4.cs.princeton.edu/41undirected/">Algorithms, 4th Edition, 4.1 Undirected Graphs</a>.
     * See that website for further documentation.
     *
     * @param sourceNode The start net node the returned path originates from.
     * @param targetNodeMatcher A {@link NetNodeMatcher} that returns {@code true} for the net node the path should end at.
     * @return A path through the network that goes from the given source node to the (yet to be discovered) target node.
     *         It includes both the source node (first element) and the target node (last element).
     */
    public static List<NetNode> getShortestPath(NetNode sourceNode, NetNodeMatcher targetNodeMatcher) {

        Network network = sourceNode.getNetwork();

        // Ensure that the source node is actually part of the network
        Validate.validState(network.containsNetNode(sourceNode), "Cannot calculate shortest path from a source node unknown to the network");

        // Special case for the path from the source node to that exact source node
        // This case wouldn't be properly processed by the code below
        if (targetNodeMatcher.matches(sourceNode)) {
            return Arrays.asList(sourceNode);
        }

        /*
         * The actual BFS algorithm:
         */

        // If you look at one key node, the value node is the next node you have to go to if you want to reach the source node through the shortest path
        Map<NetNode, NetNode> previousNodes = new HashMap<>();

        // Stores the nodes that should be processed next by the algorithm below
        Queue<NetNode> nextNodes = new LinkedList<>();
        // Stores the nodes that have already been processed
        Set<NetNode> markedNodes = new HashSet<>();

        // The source node should be processed next
        nextNodes.offer(sourceNode);
        markedNodes.add(sourceNode);

        // This variable will later store the target node as soon as it has been found
        // If it is null after the loop ended, the target node matcher can't indentify any node of the network as its target
        NetNode targetNode = null;

        searchLoop:
        while (!nextNodes.isEmpty()) {
            NetNode currentNode = nextNodes.poll();

            for (NetNode connectedNode : network.getConnectedNetNodes(currentNode)) {
                if (!markedNodes.contains(connectedNode)) {
                    previousNodes.put(connectedNode, currentNode);

                    if (targetNodeMatcher.matches(connectedNode)) {
                        // Stop the algorithm if the target node has been discovered
                        // Because this is BFS and not DFS, the available path must be the shortest one
                        targetNode = connectedNode;
                        break searchLoop;
                    } else {
                        // Otherwise, continue with the search by feeding the "connectedNode" into the search loop again
                        markedNodes.add(connectedNode);
                        nextNodes.add(connectedNode);
                    }
                }
            }
        }

        // Ensure that the target node has been discovered
        Validate.validState(targetNode != null, "Cannot calculate shortest path to a target node unknown to the network");

        // Reconstruct and return the path using the recorded "previousNodes"
        List<NetNode> path = new ArrayList<>();
        for (NetNode node = targetNode; node != null; node = previousNodes.get(node)) {
            path.add(node);
        }
        Collections.reverse(path);
        return path;
    }

    /**
     * A net node matcher is used to discover the target {@link NetNode} for {@link NetworkUtils#getShortestPath(Network, NetNode, NetNodeMatcher) calculated paths through networks}.
     * It provides a single method that takes one net node and returns whether that net node is the searched target net node.
     */
    public static interface NetNodeMatcher {

        /**
         * Returns whether the net node matcher matches the given {@link NetNode}.
         * This is used by the {@link NetworkUtils#getShortestPath(Network, NetNode, NetNodeMatcher) path calculation method} to discover the target net node.
         * For example, this method could use the {@link NetNode#getNetId() net ID} of the net node to check for a match.
         *
         * @param netNode The net node that should be checked for a match.
         * @return Whether the given net node is the node the matcher "searches" for.
         */
        public boolean matches(NetNode netNode);

    }

}
