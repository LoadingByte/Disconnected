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
import static org.junit.Assert.assertTrue;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.junit.Before;
import org.junit.Test;
import com.quartercode.disconnected.server.world.comp.net.NetNode;
import com.quartercode.disconnected.server.world.comp.net.Network;
import com.quartercode.disconnected.server.world.comp.net.NetworkUtils;
import com.quartercode.disconnected.server.world.comp.net.NetworkUtils.NetNodeMatcher;

public class NetworkUtilsRoutingTest {

    private Network network;

    @Before
    public void setUp() {

        network = generateNetwork(123456, 20);
    }

    private Network generateNetwork(int seed, int nodes) {

        // Generate the basic network with two connected nodes
        Network network = new Network();
        network.addConnection(new DummyNetNode(), new DummyNetNode());

        int existingNodes = 2;

        // Generate new nodes and connect each node to a randomly selected node that already exists
        Random random = new Random(seed);
        for (int counter = 0; counter < nodes - 2; counter++) {
            network.addConnection(network.getNetNodeByNodeId(random.nextInt(existingNodes)), new DummyNetNode());
            existingNodes++;
        }

        return network;
    }

    // Use a timeout to catch endless cycle errors
    @Test (timeout = 10000)
    public void test() {

        for (NetNode netNode1 : network.getNetNodes()) {
            // Calculate the minimum distance from netNode1 to each other network node
            Map<NetNode, Integer> minDistances = getMinimumDistances(network, netNode1);

            for (NetNode netNode2 : network.getNetNodes()) {
                // Run a test between the two net nodes (the two nodes might actually be the same node, that case is tested as well)
                new TestExecutor(network, netNode1, netNode2, minDistances).run();
            }
        }
    }

    // See NetworkUtils for more explanation on the code
    private Map<NetNode, Integer> getMinimumDistances(Network network, NetNode sourceNode) {

        Map<NetNode, Integer> minDistances = new HashMap<>();
        Queue<NetNode> nextNodes = new LinkedList<>();
        Set<NetNode> markedNodes = new HashSet<>();

        // The source node has no distance from itself
        minDistances.put(sourceNode, 0);

        nextNodes.offer(sourceNode);
        markedNodes.add(sourceNode);

        while (!nextNodes.isEmpty()) {
            NetNode currentNode = nextNodes.poll();

            for (NetNode connectedNode : network.getConnectedNetNodes(currentNode)) {
                if (!markedNodes.contains(connectedNode)) {
                    markedNodes.add(connectedNode);
                    nextNodes.add(connectedNode);
                    // Calculate the minimum distance to the current node and store it
                    minDistances.put(connectedNode, getMinimumDistance(minDistances, currentNode) + 1);
                }
            }
        }

        return minDistances;
    }

    private int getMinimumDistance(Map<NetNode, Integer> minDistances, NetNode node) {

        if (!minDistances.containsKey(node)) {
            return Integer.MAX_VALUE;
        } else {
            return minDistances.get(node);
        }
    }

    @RequiredArgsConstructor
    private static class TestExecutor {

        private final Network               network;
        private final NetNode               sourceNode;
        private final NetNode               targetNode;
        private final Map<NetNode, Integer> minDistances;

        public void run() {

            // Calculate the path
            List<NetNode> path = NetworkUtils.getShortestPath(sourceNode, new NetNodeMatcher() {

                @Override
                public boolean matches(NetNode netNode) {

                    return targetNode.equals(netNode);
                }

            });

            // Check whether the path actually works
            checkValidPath(path);

            // Check whether the path is really one of the shortest possible paths (or the one and only shortest possible path)
            checkShortestPath(path);
        }

        /*
         * Checks whether the path takes you from the source node to the target node.
         * Note that this method does *not* validate whether the path is the shortest one possible.
         */
        private void checkValidPath(List<NetNode> path) {

            assertEquals("First element of the path (source node) on the path " + path, sourceNode, path.get(0));
            assertEquals("Last element of the path (target node) on the path " + path, targetNode, path.get(path.size() - 1));

            // Check whether the path exists
            ListIterator<NetNode> pathIter = path.listIterator();
            while (pathIter.nextIndex() < path.size() - 1) {
                NetNode currentNode = pathIter.next();
                NetNode nextNode = pathIter.next();
                pathIter.previous();

                assertTrue("Invalid segment in path: Cannot get from node '" + currentNode + "' to node '" + nextNode + "' (complete path " + path + ")",
                        network.getConnectedNetNodes(currentNode).contains(nextNode));
            }
        }

        /*
         * Checks optimality (shortest path) conditions for the set path.
         * Taken from "Algorithms, 4th Edition, 4.1 Undirected Graphs" (http://algs4.cs.princeton.edu/41undirected/), modified.
         */
        private void checkShortestPath(List<NetNode> path) {

            // Check that "minDist(sourceNode -- sourceNode)" is 0
            assertEquals("Minimum distance from source to source on the path " + path, 0, (int) minDistances.get(sourceNode));

            // Check for each edge "node -> adjNode": minDist(sourceNode -- adjNode) <= minDist(sourceNode -- node) + 1
            // This is actually part of the optimality check
            for (NetNode node : network.getNetNodes()) {
                for (NetNode adjNode : network.getConnectedNetNodes(node)) {
                    assertTrue("Shortest path test failed for edge '" + adjNode + " -- " + node + "' (path " + path + ")",
                            minDistances.get(adjNode) <= minDistances.get(node) + 1);
                }
            }

            // Check that the distance between two nodes on the path is always one using minDistances
            ListIterator<NetNode> pathIter = path.listIterator();
            while (pathIter.nextIndex() < path.size() - 1) {
                NetNode currentNode = pathIter.next();
                NetNode nextNode = pathIter.next();
                pathIter.previous();

                assertEquals("Distance between two nodes ('" + currentNode + " -- " + nextNode + "') on the path " + path,
                        1, minDistances.get(nextNode) - minDistances.get(currentNode));
            }
        }

    }

}
