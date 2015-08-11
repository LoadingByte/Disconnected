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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import com.quartercode.disconnected.server.world.comp.net.NetNode;
import com.quartercode.disconnected.server.world.comp.net.Network;
import com.quartercode.disconnected.server.world.comp.net.Packet;
import com.quartercode.disconnected.server.world.comp.net.nodes.DeviceNode;
import com.quartercode.disconnected.server.world.comp.net.nodes.DownlinkRouterNode;
import com.quartercode.disconnected.server.world.comp.net.nodes.UplinkRouterNode;
import com.quartercode.disconnected.shared.world.comp.net.Address;
import com.quartercode.disconnected.shared.world.comp.net.NetId;

@RunWith (Parameterized.class)
public class MultiTierNetworkRoutingTest {

    // ----- Parameters -----

    @Parameters
    public static Collection<Object[]> data() {

        List<Object[]> data = new ArrayList<>();

        /*
         * Stay in the same network
         * The tests also check the packet handling routines of all net node types, so those don't have to be tested again later on.
         */

        // From device to device
        data.add(new Object[] { netId(0, 0), netId(0, 2), new NetId[] { netId(0, 1), netId(0, 2) }, true });
        // From device to uplink router
        data.add(new Object[] { netId(0, 0), netId(0, 1), new NetId[] { netId(0, 1) }, true });
        // From device to downlink router
        data.add(new Object[] { netId(0, 0), netId(0, 3), new NetId[] { netId(0, 1), netId(0, 3) }, true });

        // From uplink router to device
        data.add(new Object[] { netId(0, 1), netId(0, 0), new NetId[] { netId(0, 0) }, true });
        // From uplink router to downlink router
        data.add(new Object[] { netId(0, 1), netId(0, 3), new NetId[] { netId(0, 3) }, true });

        // From downlink router to device
        data.add(new Object[] { netId(0, 3), netId(0, 0), new NetId[] { netId(0, 1), netId(0, 0) }, true });
        // From downlink router to uplink router
        data.add(new Object[] { netId(0, 3), netId(0, 1), new NetId[] { netId(0, 1) }, true });

        /*
         * Only up
         */

        // One tier up
        // From device to device
        data.add(new Object[] { netId(0, 0), netId(2), new NetId[] { netId(0, 1), netId(0), netId(1), netId(2) }, true });
        // From device to downlink router
        data.add(new Object[] { netId(0, 0), netId(0), new NetId[] { netId(0, 1), netId(0) }, true });
        // From uplink router to device
        data.add(new Object[] { netId(0, 1), netId(2), new NetId[] { netId(0), netId(1), netId(2) }, true });

        // Two tiers up
        // From device to device
        data.add(new Object[] { netId(0, 3, 1), netId(2), new NetId[] { netId(0, 3, 0), netId(0, 3), netId(0, 1), netId(0), netId(1), netId(2) }, true });

        /*
         * Only down
         */

        // One tier down
        // From device to device
        data.add(new Object[] { netId(2), netId(0, 0), new NetId[] { netId(1), netId(0), netId(0, 1), netId(0, 0) }, true });
        // From device to uplink router
        data.add(new Object[] { netId(2), netId(0, 1), new NetId[] { netId(1), netId(0), netId(0, 1) }, true });
        // From downlink router to device
        data.add(new Object[] { netId(0), netId(0, 0), new NetId[] { netId(0, 1), netId(0, 0) }, true });

        // Two tiers down
        // From device to device
        data.add(new Object[] { netId(2), netId(0, 3, 1), new NetId[] { netId(1), netId(0), netId(0, 1), netId(0, 3), netId(0, 3, 0), netId(0, 3, 1) }, true });

        /*
         * Up, then down
         */

        // One tier up, then one tier down
        data.add(new Object[] { netId(0, 0), netId(1, 2), new NetId[] { netId(0, 1), netId(0), netId(1), netId(1, 0), netId(1, 1), netId(1, 2) }, true });

        // Two tiers up, then two tiers down
        data.add(new Object[] { netId(0, 3, 1), netId(1, 1, 1), new NetId[] { netId(0, 3, 0), netId(0, 3), netId(0, 1), netId(0), netId(1),
                netId(1, 0), netId(1, 1), netId(1, 1, 0), netId(1, 1, 1) }, true });

        /*
         * Dismissed packets because of invalid destinations
         */

        // The destination node "7" doesn't exist
        data.add(new Object[] { netId(0), netId(7), new NetId[0], false });

        // Net node "2" is used as a downlink router although it isn't one
        data.add(new Object[] { netId(0), netId(2, 0), new NetId[] { netId(1), netId(2) }, false });

        // The net node "3.0" is part of a network which doesn't have an uplink router; however, this call tries to send a packet downwards
        data.add(new Object[] { netId(3), netId(3, 0), new NetId[0], false });

        // ...; however, this call tries to send a packet upwards
        data.add(new Object[] { netId(3, 0), netId(3), new NetId[0], false });

        // The destination node "5" is not reachable from "0" because the two parts are cut-off from each other
        data.add(new Object[] { netId(0), netId(5), new NetId[0], false });

        return data;
    }

    private static NetId netId(Integer... nodeIds) {

        return new NetId(nodeIds);
    }

    // ----- Test Executor -----

    @Rule
    // @formatter:off
    public JUnitRuleMockery      context         = new JUnitRuleMockery() {{
        setThreadingPolicy(new Synchroniser());
    }};
    // @formatter:on

    private final NetId          source;
    private final NetId          destination;
    private final NetId[]        expectedRoute;
    private final NetId          expectedReceiver;

    private Network              rootNetwork;
    private final List<NetId>    trackedRoute    = new ArrayList<>();
    private final Mutable<NetId> trackedReceiver = new MutableObject<>();

    public MultiTierNetworkRoutingTest(NetId source, NetId destination, NetId[] expectedRoute, boolean expectSuccess) {

        this.source = source;
        this.destination = destination;
        this.expectedRoute = expectedRoute;
        expectedReceiver = expectSuccess ? expectedRoute[expectedRoute.length - 1] : null;
    }

    @Before
    public void setUp() {

        // Note that the node ids are only there for documenting which node id will be assigned to a node
        // They are ignored by the network parser

        // Legend for node types:
        // E: Device
        // D: Downlink router
        // U: Uplink router

        /*
         * Tier 0
         */

        // Note that the bottom part of the network is cut-off
        rootNetwork = parseNetwork(new String[] {
                "D0--D1--E2",
                "    |     ",
                "    D3--E4",
                "          ",
                "E5--E6    "
        });

        /*
         * Tier 1
         */

        Network subNetwork_0 = addSubNetwork(new String[] {
                "E0--U1--E2",
                "    |     ",
                "    D3    "
        }, rootNetwork, 0);

        Network subNetwork_1 = addSubNetwork(new String[] {
                "U0--D1--E2"
        }, rootNetwork, 1);

        // This network doesn't have an uplink router; therefore, all packets which should be sent upwards must be dismissed
        addSubNetwork(new String[] {
                "E0--E1"
        }, rootNetwork, 3);

        /*
         * Tier 2
         */

        addSubNetwork(new String[] {
                "U0--E1",
        }, subNetwork_0, 3);

        addSubNetwork(new String[] {
                "U0--E1"
        }, subNetwork_1, 1);

    }

    private Network parseNetwork(String[] definition) {

        // First step: Make the network definition more readable by parsing it into a char matrix
        char[][] charMatrix = createCharMatrix(definition);

        // Second step: The net node matrix contains a net node object at each position where the char matrix contains a net node definition
        NetNode[][] netNodeMatrix = createNetNodeMatrix(charMatrix);

        // Third step: Convert all that information into an actual network object
        return createNetwork(charMatrix, netNodeMatrix);
    }

    private char[][] createCharMatrix(String[] definition) {

        int width = definition[0].length() / 2;
        int height = definition.length;

        char[][] charMatrix = new char[height][width];

        for (int yPos = 0; yPos < height; yPos++) {
            for (int xPos = 0; xPos < width; xPos++) {
                charMatrix[yPos][xPos] = definition[yPos].charAt(xPos * 2);
            }
        }

        return charMatrix;
    }

    private NetNode[][] createNetNodeMatrix(char[][] matrix) {

        NetNode[][] netNodeMatrix = new NetNode[matrix.length][matrix[0].length];

        for (int yPos = 0; yPos < matrix.length; yPos += 2) {
            for (int xPos = 0; xPos < matrix[0].length; xPos += 2) {
                NetNode netNode = null;

                switch (matrix[yPos][xPos]) {
                    case 'E':
                        netNode = new ObservableDeviceNode(trackedRoute, trackedReceiver);
                        break;
                    case 'D':
                        netNode = new ObservableDownlinkRouterNode(trackedRoute, trackedReceiver);
                        break;
                    case 'U':
                        netNode = new ObservableUplinkRouterNode(trackedRoute, trackedReceiver);
                        break;
                }

                netNodeMatrix[yPos][xPos] = netNode;
            }
        }

        return netNodeMatrix;
    }

    private Network createNetwork(char[][] charMatrix, NetNode[][] netNodeMatrix) {

        Network network = new Network();

        for (int yPos = 0; yPos < charMatrix.length; yPos++) {
            for (int xPos = 0; xPos < charMatrix[0].length; xPos++) {
                char controlChar = charMatrix[yPos][xPos];

                if (controlChar == '-') {
                    network.addConnection(netNodeMatrix[yPos][xPos - 1], netNodeMatrix[yPos][xPos + 1]);
                } else if (controlChar == '|') {
                    network.addConnection(netNodeMatrix[yPos - 1][xPos], netNodeMatrix[yPos + 1][xPos]);
                }
            }
        }

        return network;
    }

    private Network addSubNetwork(String[] definition, Network parentNetwork, int parentDownlinkNodeId) {

        Network subNetwork = parseNetwork(definition);
        ((DownlinkRouterNode) parentNetwork.getNetNodeByNodeId(parentDownlinkNodeId)).setSubNetwork(subNetwork);
        return subNetwork;
    }

    // Use a timeout to catch endless cycle errors
    @Test (timeout = 10000)
    public void test() {

        final Packet packet = context.mock(Packet.class);

        // @formatter:off
        context.checking(new Expectations() {{

            allowing(packet).getDestination();
                will(returnValue(new Address(destination, 1)));

        }});
        // @formatter:on

        NetNode sourceNode = getNetNodeByNetId(source);
        sourceNode.process(packet);

        assertArrayEquals("The route a packet took from '" + source + "' to '" + destination + "'", expectedRoute, trackedRoute.toArray());
        assertEquals("The receiver of the packet", expectedReceiver, trackedReceiver.getValue());
    }

    private NetNode getNetNodeByNetId(NetId netId) {

        Queue<Integer> nodeIds = new LinkedList<>(netId.getNodeIdsAtTiers());
        Network currentNetwork = rootNetwork;

        while (nodeIds.size() >= 2) {
            currentNetwork = ((DownlinkRouterNode) currentNetwork.getNetNodeByNodeId(nodeIds.poll())).getSubNetwork();
        }

        return currentNetwork.getNetNodeByNodeId(nodeIds.poll());
    }

    // ----- Observable Net Nodes -----

    @RequiredArgsConstructor
    private static class ObservableDeviceNode extends DeviceNode {

        private final List<NetId>    trackedRoute;
        private final Mutable<NetId> trackedReceiver;

        @Override
        protected void deliverPacketToNetNode(Packet packet, NetNode netNode) {

            trackedRoute.add(netNode.getNetId());
            super.deliverPacketToNetNode(packet, netNode);
        }

        @Override
        protected void deliverPacketToNetInterface(Packet packet) {

            trackedReceiver.setValue(getNetId());
        }

    }

    @RequiredArgsConstructor
    private static class ObservableDownlinkRouterNode extends DownlinkRouterNode {

        private final List<NetId>    trackedRoute;
        private final Mutable<NetId> trackedReceiver;

        @Override
        protected void deliverPacketToNetNode(Packet packet, NetNode netNode) {

            trackedRoute.add(netNode.getNetId());
            super.deliverPacketToNetNode(packet, netNode);
        }

        @Override
        public UplinkRouterNode getDownlinkTarget() {

            UplinkRouterNode downlinkTarget = super.getDownlinkTarget();

            if (downlinkTarget != null) {
                // A call to this method indicates that the packet is sent to the downlink target
                trackedRoute.add(downlinkTarget.getNetId());
            }

            return downlinkTarget;
        }

        @Override
        protected void deliverPacketToNetInterface(Packet packet) {

            trackedReceiver.setValue(getNetId());
        }

    }

    @RequiredArgsConstructor
    private static class ObservableUplinkRouterNode extends UplinkRouterNode {

        private final List<NetId>    trackedRoute;
        private final Mutable<NetId> trackedReceiver;

        @Override
        protected void deliverPacketToNetNode(Packet packet, NetNode netNode) {

            trackedRoute.add(netNode.getNetId());
            super.deliverPacketToNetNode(packet, netNode);
        }

        @Override
        protected void deliverPacketToNetInterface(Packet packet) {

            trackedReceiver.setValue(getNetId());
        }

        @Override
        public DownlinkRouterNode getUplinkTarget() {

            DownlinkRouterNode uplinkTarget = super.getUplinkTarget();

            // A call to this method indicates that the packet is sent to the uplink target
            trackedRoute.add(uplinkTarget.getNetId());

            return uplinkTarget;
        }

    }

}
