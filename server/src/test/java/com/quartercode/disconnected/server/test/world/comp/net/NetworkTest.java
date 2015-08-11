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

import static org.junit.Assert.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import org.junit.Before;
import org.junit.Test;
import com.quartercode.disconnected.server.world.comp.net.ConnectionLimitExceededException;
import com.quartercode.disconnected.server.world.comp.net.Network;

public class NetworkTest {

    /*
     * Network topology:
     *
     * _________ Node2
     * ___________ |
     * Node3 --- Node1
     * ___________ |
     * _________ Node4 --- Node5
     */
    private final Network      network  = new Network();

    private final DummyNetNode netNode1 = new DummyNetNode();
    private final DummyNetNode netNode2 = new DummyNetNode();
    private final DummyNetNode netNode3 = new DummyNetNode();
    private final DummyNetNode netNode4 = new DummyNetNode(2); // max of 2 connections
    private final DummyNetNode netNode5 = new DummyNetNode();

    @Before
    public void setUp() {

        network.addConnection(netNode1, netNode2);
        network.addConnection(netNode1, netNode3);
        network.addConnection(netNode1, netNode4);
        network.addConnection(netNode4, netNode5);
    }

    @Test
    public void testAssignedNodeIds() {

        assertEquals("Node id assigned to net node 1", 0, netNode1.getNodeId());
        assertEquals("Node id assigned to net node 2", 1, netNode2.getNodeId());
        assertEquals("Node id assigned to net node 3", 2, netNode3.getNodeId());
        assertEquals("Node id assigned to net node 4", 3, netNode4.getNodeId());
        assertEquals("Node id assigned to net node 5", 4, netNode5.getNodeId());
    }

    @Test
    public void testGetNetNodes() {

        assertEquals("Net nodes of network", new HashSet<>(Arrays.asList(netNode1, netNode2, netNode3, netNode4, netNode5)), network.getNetNodes());
    }

    @Test
    public void testContainsNetNode() {

        assertTrue("Network returned wrong answer on whether it contains net node 1", network.containsNetNode(netNode1));
        assertTrue("Network returned wrong answer on whether it contains net node 2", network.containsNetNode(netNode2));
        assertTrue("Network returned wrong answer on whether it contains net node 3", network.containsNetNode(netNode3));
        assertTrue("Network returned wrong answer on whether it contains net node 4", network.containsNetNode(netNode4));
        assertTrue("Network returned wrong answer on whether it contains net node 5", network.containsNetNode(netNode5));

        assertFalse("Network says it contains a net node it actually doesn't contain", network.containsNetNode(new DummyNetNode(6)));
    }

    @Test
    public void testGetConnectedNetNodes() {

        assertEquals("Nodes connected to net node 1", new HashSet<>(Arrays.asList(netNode2, netNode3, netNode4)), network.getConnectedNetNodes(netNode1));
        assertEquals("Nodes connected to net node 2", new HashSet<>(Arrays.asList(netNode1)), network.getConnectedNetNodes(netNode2));
        assertEquals("Nodes connected to net node 3", new HashSet<>(Arrays.asList(netNode1)), network.getConnectedNetNodes(netNode3));
        assertEquals("Nodes connected to net node 4", new HashSet<>(Arrays.asList(netNode1, netNode5)), network.getConnectedNetNodes(netNode4));
        assertEquals("Nodes connected to net node 5", new HashSet<>(Arrays.asList(netNode4)), network.getConnectedNetNodes(netNode5));

        assertEquals("Nodes connected to a net node the network doesn't contain", Collections.emptySet(), network.getConnectedNetNodes(new DummyNetNode(6)));
    }

    @Test
    public void testGetConnectedNetNodeCount() {

        assertEquals("Nodes connected to net node 1", 3, network.getConnectedNetNodeCount(netNode1));
        assertEquals("Nodes connected to net node 2", 1, network.getConnectedNetNodeCount(netNode2));
        assertEquals("Nodes connected to net node 3", 1, network.getConnectedNetNodeCount(netNode3));
        assertEquals("Nodes connected to net node 4", 2, network.getConnectedNetNodeCount(netNode4));
        assertEquals("Nodes connected to net node 5", 1, network.getConnectedNetNodeCount(netNode5));

        assertEquals("Nodes connected to a net node the network doesn't contain", 0, network.getConnectedNetNodeCount(new DummyNetNode(6)));
    }

    @Test (expected = ConnectionLimitExceededException.class)
    public void testAddConnectionLimitExceeded() {

        network.addConnection(netNode4, new DummyNetNode(6));
    }

    @Test
    public void testRemoveConnection() {

        // This removal disconnects net node 2 completely; it should no longer be part of the network
        network.removeConnection(netNode1, netNode2);

        assertEquals("Net nodes of network after removal", new HashSet<>(Arrays.asList(netNode1, netNode3, netNode4, netNode5)), network.getNetNodes());
        assertFalse("Network says it contains the removed net node 2", network.containsNetNode(netNode2));
        assertEquals("Nodes connected to net node 1 after removal", new HashSet<>(Arrays.asList(netNode3, netNode4)), network.getConnectedNetNodes(netNode1));

        assertEquals("Node id of net node 1 after removal", 0, netNode1.getNodeId());
        assertEquals("Node id of net node 2 after removal", -1, netNode2.getNodeId());
    }

    @Test
    public void testRemoveConnectionKeepDisconnectedPart() {

        // This removal disconnects net node 4 from the rest of the network
        // However, the connection between the nodes 4 and 5 should still exist (therefore, nodes 4 and 5 should still exist as well)
        network.removeConnection(netNode4, netNode1);

        assertEquals("Net nodes of network after removal", new HashSet<>(Arrays.asList(netNode1, netNode2, netNode3, netNode4, netNode5)), network.getNetNodes());
        assertEquals("Nodes connected to net node 1 after removal", new HashSet<>(Arrays.asList(netNode2, netNode3)), network.getConnectedNetNodes(netNode1));
        assertEquals("Nodes connected to net node 4 after removal", new HashSet<>(Arrays.asList(netNode5)), network.getConnectedNetNodes(netNode4));

        assertEquals("Node id of net node 1 after removal", 0, netNode1.getNodeId());
        // These node ids don't change because the two net nodes are still part of the network (even though they are disconnected from the rest)
        assertEquals("Node id of net node 4 after removal", 3, netNode4.getNodeId());
        assertEquals("Node id of net node 5 after removal", 4, netNode5.getNodeId());
    }

    @Test
    public void testRemoveNetNode() {

        // This removal also completely disconnects net node 5, which won't have any connections left
        network.removeNetNode(netNode4);

        assertEquals("Net nodes of network after removal", new HashSet<>(Arrays.asList(netNode1, netNode2, netNode3)), network.getNetNodes());
        assertFalse("Network says it contains the removed net node 4", network.containsNetNode(netNode4));
        assertFalse("Network says it contains the indirectly removed net node 5", network.containsNetNode(netNode5));
        assertEquals("Nodes connected to net node 1 after removal", new HashSet<>(Arrays.asList(netNode2, netNode3)), network.getConnectedNetNodes(netNode1));

        assertEquals("Node id of net node 1 after removal", 0, netNode1.getNodeId());
        assertEquals("Node id of net node 4 after removal", -1, netNode4.getNodeId());
        assertEquals("Node id of net node 5 after removal", -1, netNode5.getNodeId());
    }

}
