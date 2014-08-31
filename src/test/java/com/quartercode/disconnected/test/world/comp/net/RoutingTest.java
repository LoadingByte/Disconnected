/*
 * This file is part of Disconnected.
 * Copyright (c) 2013 QuarterCode <http://www.quartercode.com/>
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

package com.quartercode.disconnected.test.world.comp.net;

import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import com.quartercode.classmod.extra.FunctionExecutor;
import com.quartercode.classmod.extra.FunctionInvocation;
import com.quartercode.classmod.extra.Prioritized;
import com.quartercode.disconnected.world.comp.hardware.NodeNetInterface;
import com.quartercode.disconnected.world.comp.hardware.RouterNetInterface;
import com.quartercode.disconnected.world.comp.net.Address;
import com.quartercode.disconnected.world.comp.net.Backbone;
import com.quartercode.disconnected.world.comp.net.NetID;
import com.quartercode.disconnected.world.comp.net.Packet;
import com.quartercode.disconnected.world.comp.net.PacketProcessor;

@RunWith (Parameterized.class)
public class RoutingTest {

    // ----- Parameters -----

    @Parameters
    public static Collection<Object[]> data() {

        List<Object[]> data = new ArrayList<>();

        data.add(new Object[] { new NetworkGenerator() {

            /*
             * Network topology:
             * 
             * Backbone --- Router --- Node
             * ______________ |
             * ______________ +------- Node
             */
            @Override
            public void generate(Backbone root) {

                RouterNetInterface router = generateRouter(0, root);
                addNodes(router, 2);
            }

        } });

        data.add(new Object[] { new NetworkGenerator() {

            /*
             * Network topology:
             * 
             * Backbone --- Router --- Node
             * _______________ |
             * ____________ Router --- Node
             */
            @Override
            public void generate(Backbone root) {

                RouterNetInterface line_1 = generateRouter(0, root);
                addNodes(line_1, 1);

                RouterNetInterface line_2 = generateRouter(1, null, line_1);
                addNodes(line_2, 1);
            }

        } });

        data.add(new Object[] { new NetworkGenerator() {

            /*
             * Network topology:
             * 
             * Backbone --- Router --- Node
             * _______________ |
             * ____________ Router
             * _______________ |
             * ____________ Router --- Node
             */
            @Override
            public void generate(Backbone root) {

                RouterNetInterface line_1 = generateRouter(0, root);
                addNodes(line_1, 1);

                RouterNetInterface line_2 = generateRouter(1, null, line_1);

                RouterNetInterface line_3 = generateRouter(2, null, line_2);
                addNodes(line_3, 1);
            }

        } });

        data.add(new Object[] { new NetworkGenerator() {

            /*
             * Network topology:
             * 
             * Backbone --- Router --- Node
             * _______________ |
             * ____________ Router
             * _______________ |
             * ____________ Router
             * _______________ |
             * ____________ Router --- Node
             */
            @Override
            public void generate(Backbone root) {

                RouterNetInterface line_1 = generateRouter(0, root);
                addNodes(line_1, 1);

                RouterNetInterface line_2 = generateRouter(1, null, line_1);
                RouterNetInterface line_3 = generateRouter(2, null, line_2);

                RouterNetInterface line_4 = generateRouter(3, null, line_3);
                addNodes(line_4, 1);
            }

        } });

        data.add(new Object[] { new NetworkGenerator() {

            /*
             * Network topology:
             * 
             * Backbone --- Router --- Node
             * _______________ |
             * ____________ Router --- Router --- Node
             * _______________ |
             * ____________ Router
             * _______________ |
             * ____________ Router --- Node
             */
            @Override
            public void generate(Backbone root) {

                // Line on the left
                RouterNetInterface line1_1 = generateRouter(0, root);
                addNodes(line1_1, 1);

                RouterNetInterface line1_2 = generateRouter(1, null, line1_1);
                RouterNetInterface line1_3 = generateRouter(2, null, line1_2);

                RouterNetInterface line1_4 = generateRouter(3, null, line1_3);
                addNodes(line1_4, 1);

                // Router on the right
                RouterNetInterface line2_1 = generateRouter(4, null, line1_2);
                addNodes(line2_1, 1);
            }

        } });

        data.add(new Object[] { new NetworkGenerator() {

            /*
             * Network topology:
             * 
             * _____________ Node _____ Node
             * _______________ | ________ |
             * Backbone --- Router --- Router
             * _______________ | ________ |
             * ____________ Router --- Router
             * _______________ | ________ |
             * _____________ Node _____ Node
             */
            @Override
            public void generate(Backbone root) {

                RouterNetInterface cycle_1 = generateRouter(0, root);
                addNodes(cycle_1, 1);

                RouterNetInterface cycle_2 = generateRouter(1, null, cycle_1);
                addNodes(cycle_2, 1);

                RouterNetInterface cycle_3 = generateRouter(2, null, cycle_2);
                addNodes(cycle_3, 1);

                RouterNetInterface cycle_4 = generateRouter(3, null, cycle_3, cycle_1);
                addNodes(cycle_4, 1);
            }

        } });

        data.add(new Object[] { new NetworkGenerator() {

            /*
             * Network topology:
             * 
             * _____________ Node _____ Node _____ Node
             * _______________ | ________ | ________ |
             * Backbone --- Router --- Router --- Router
             * _______________ | ___________________ |
             * ____________ Router --- Router --- Router
             * _______________ | ________ | ________ |
             * _____________ Node _____ Node _____ Node
             */
            @Override
            public void generate(Backbone root) {

                RouterNetInterface cycle_1 = generateRouter(0, root);
                addNodes(cycle_1, 1);

                RouterNetInterface cycle_2 = generateRouter(1, null, cycle_1);
                addNodes(cycle_2, 1);

                RouterNetInterface cycle_3 = generateRouter(2, null, cycle_2);
                addNodes(cycle_3, 1);

                RouterNetInterface cycle_4 = generateRouter(3, null, cycle_3);
                addNodes(cycle_4, 1);

                RouterNetInterface cycle_5 = generateRouter(4, null, cycle_4);
                addNodes(cycle_5, 1);

                RouterNetInterface cycle_6 = generateRouter(5, null, cycle_5, cycle_1);
                addNodes(cycle_6, 1);
            }

        } });

        data.add(new Object[] { new NetworkGenerator() {

            /*
             * Network topology:
             * 
             * _____________ Node _____ Node _____ Node
             * _______________ | ________ | ________ |
             * Backbone --- Router --- Router --- Router
             * _______________ | ___________________ |
             * ____________ Router --- Router --- Router
             * _______________ | ________ | ________ |
             * _____________ Node ____ Router ____ Node
             * __________________________ |
             * ________________________ Node
             */
            @Override
            public void generate(Backbone root) {

                // Cycle
                RouterNetInterface cycle_1 = generateRouter(0, root);
                addNodes(cycle_1, 1);

                RouterNetInterface cycle_2 = generateRouter(1, null, cycle_1);
                addNodes(cycle_2, 1);

                RouterNetInterface cycle_3 = generateRouter(2, null, cycle_2);
                addNodes(cycle_3, 1);

                RouterNetInterface cycle_4 = generateRouter(3, null, cycle_3);
                addNodes(cycle_4, 1);

                RouterNetInterface cycle_5 = generateRouter(4, null, cycle_4);
                addNodes(cycle_5, 1);

                RouterNetInterface cycle_6 = generateRouter(5, null, cycle_5, cycle_1);
                addNodes(cycle_6, 1);

                // Router on the bottom
                RouterNetInterface line_1 = generateRouter(6, null, cycle_5);
                addNodes(line_1, 1);
            }

        } });

        data.add(new Object[] { new NetworkGenerator() {

            /*
             * Network topology:
             * 
             * _____________ Node _____ Node _____ Node
             * _______________ | ________ | ________ |
             * Backbone --- Router --- Router --- Router
             * _______________ | ___________________ |
             * ____________ Router --- Router --- Router
             * _______________ | ________ | ________ |
             * _____________ Node ____ Router ____ Node
             * __________________________ |
             * _______________________ Router
             * __________________________ |
             * ________________________ Node
             */
            @Override
            public void generate(Backbone root) {

                // Cycle
                RouterNetInterface cycle_1 = generateRouter(0, root);
                addNodes(cycle_1, 1);

                RouterNetInterface cycle_2 = generateRouter(1, null, cycle_1);
                addNodes(cycle_2, 1);

                RouterNetInterface cycle_3 = generateRouter(2, null, cycle_2);
                addNodes(cycle_3, 1);

                RouterNetInterface cycle_4 = generateRouter(3, null, cycle_3);
                addNodes(cycle_4, 1);

                RouterNetInterface cycle_5 = generateRouter(4, null, cycle_4);
                addNodes(cycle_5, 1);

                RouterNetInterface cycle_6 = generateRouter(5, null, cycle_5, cycle_1);
                addNodes(cycle_6, 1);

                // Line on the bottom
                RouterNetInterface line_1 = generateRouter(6, null, cycle_5);

                RouterNetInterface line_2 = generateRouter(7, null, line_1);
                addNodes(line_2, 1);
            }

        } });

        data.add(new Object[] { new NetworkGenerator() {

            /*
             * Network topology:
             * 
             * _____________ Node _____ Node _______ Node _____ Node
             * _______________ | ________ | __________ | ________ |
             * Backbone --- Router --- Router ----- Router --- Router
             * _______________ | ________ | __________ | ________ |
             * ____________ Router --- Router _____ Router --- Router
             * _______________ | ________ | __________ | ________ |
             * _____________ Node _____ Node _______ Node _____ Node
             */
            @Override
            public void generate(Backbone root) {

                // Left cycle
                RouterNetInterface cycle1_1 = generateRouter(0, root);
                addNodes(cycle1_1, 1);

                RouterNetInterface cycle1_2 = generateRouter(1, null, cycle1_1);
                addNodes(cycle1_2, 1);

                RouterNetInterface cycle1_3 = generateRouter(2, null, cycle1_2);
                addNodes(cycle1_3, 1);

                RouterNetInterface cycle1_4 = generateRouter(3, null, cycle1_3, cycle1_1);
                addNodes(cycle1_4, 1);

                // Right cycle
                RouterNetInterface cycle2_1 = generateRouter(4, null, cycle1_2);
                addNodes(cycle2_1, 1);

                RouterNetInterface cycle2_2 = generateRouter(5, null, cycle2_1);
                addNodes(cycle2_2, 1);

                RouterNetInterface cycle2_3 = generateRouter(6, null, cycle2_2);
                addNodes(cycle2_3, 1);

                RouterNetInterface cycle2_4 = generateRouter(7, null, cycle2_3, cycle2_1);
                addNodes(cycle2_4, 1);
            }

        } });

        data.add(new Object[] { new NetworkGenerator() {

            /*
             * Network topology:
             * 
             * _____________ Node _____ Node _____ Node _______ Node _____ Node _____ Node
             * _______________ | ________ | ________ | __________ | ________ | ________ |
             * Backbone --- Router --- Router --- Router ----- Router --- Router --- Router
             * _______________ | ____________________|___________ | ___________________ |
             * ____________ Router --- Router --- Router _____ Router --- Router --- Router
             * _______________ | ________ | ________ | __________ | ________ | ________ |
             * _____________ Node _____ Node _____ Node _______ Node _____ Node _____ Node
             */
            @Override
            public void generate(Backbone root) {

                // Left cycle
                RouterNetInterface cycle1_1 = generateRouter(0, root);
                addNodes(cycle1_1, 1);

                RouterNetInterface cycle1_2 = generateRouter(1, null, cycle1_1);
                addNodes(cycle1_2, 1);

                RouterNetInterface cycle1_3 = generateRouter(2, null, cycle1_2);
                addNodes(cycle1_3, 1);

                RouterNetInterface cycle1_4 = generateRouter(3, null, cycle1_3);
                addNodes(cycle1_4, 1);

                RouterNetInterface cycle1_5 = generateRouter(4, null, cycle1_4);
                addNodes(cycle1_5, 1);

                RouterNetInterface cycle1_6 = generateRouter(5, null, cycle1_5, cycle1_1);
                addNodes(cycle1_6, 1);

                // Right cycle
                RouterNetInterface cycle2_1 = generateRouter(6, null, cycle1_3);
                addNodes(cycle2_1, 1);

                RouterNetInterface cycle2_2 = generateRouter(7, null, cycle2_1);
                addNodes(cycle2_2, 1);

                RouterNetInterface cycle2_3 = generateRouter(8, null, cycle2_2);
                addNodes(cycle2_3, 1);

                RouterNetInterface cycle2_4 = generateRouter(9, null, cycle2_3);
                addNodes(cycle2_4, 1);

                RouterNetInterface cycle2_5 = generateRouter(10, null, cycle2_4);
                addNodes(cycle2_5, 1);

                RouterNetInterface cycle2_6 = generateRouter(11, null, cycle2_5, cycle2_1);
                addNodes(cycle2_6, 1);
            }

        } });

        data.add(new Object[] { new NetworkGenerator() {

            /*
             * Network topology:
             * 
             * _____________ Node _____ Node _____ Node _______ Node _____ Node _____ Node
             * _______________ | ________ | ________ | __________ | ________ | ________ |
             * Backbone --- Router --- Router --- Router ----- Router --- Router --- Router
             * _______________ | ________ | _________|___________ | ________ | ________ |
             * ____________ Router --- Router --- Router _____ Router --- Router --- Router
             * _______________ | ________ | ________ | __________ | ________ | ________ |
             * _____________ Node _____ Node _____ Node _______ Node _____ Node _____ Node
             */
            @Override
            public void generate(Backbone root) {

                // Left cycle
                RouterNetInterface cycle1_1 = generateRouter(0, root);
                addNodes(cycle1_1, 1);

                RouterNetInterface cycle1_2 = generateRouter(1, null, cycle1_1);
                addNodes(cycle1_2, 1);

                RouterNetInterface cycle1_3 = generateRouter(2, null, cycle1_2);
                addNodes(cycle1_3, 1);

                RouterNetInterface cycle1_4 = generateRouter(3, null, cycle1_3);
                addNodes(cycle1_4, 1);

                RouterNetInterface cycle1_5 = generateRouter(4, null, cycle1_4, cycle1_2);
                addNodes(cycle1_5, 1);

                RouterNetInterface cycle1_6 = generateRouter(5, null, cycle1_5, cycle1_1);
                addNodes(cycle1_6, 1);

                // Right cycle
                RouterNetInterface cycle2_1 = generateRouter(6, null, cycle1_3);
                addNodes(cycle2_1, 1);

                RouterNetInterface cycle2_2 = generateRouter(7, null, cycle2_1);
                addNodes(cycle2_2, 1);

                RouterNetInterface cycle2_3 = generateRouter(8, null, cycle2_2);
                addNodes(cycle2_3, 1);

                RouterNetInterface cycle2_4 = generateRouter(9, null, cycle2_3);
                addNodes(cycle2_4, 1);

                RouterNetInterface cycle2_5 = generateRouter(10, null, cycle2_4, cycle2_2);
                addNodes(cycle2_5, 1);

                RouterNetInterface cycle2_6 = generateRouter(11, null, cycle2_5, cycle2_1);
                addNodes(cycle2_6, 1);
            }

        } });

        data.add(new Object[] { new NetworkGenerator() {

            /*
             * Network topology:
             * 
             * Backbone ---+--- Router --- Node
             * ___________ |
             * ________ Router --- Node
             */
            @Override
            public void generate(Backbone root) {

                RouterNetInterface router1 = generateRouter(0, root);
                addNodes(router1, 1);

                RouterNetInterface router2 = generateRouter(1, root);
                addNodes(router2, 1);
            }

        } });

        data.add(new Object[] { new NetworkGenerator() {

            /*
             * Network topology:
             * 
             * Backbone ---+--- Router --- Router --- Node
             * ___________ |
             * ________ Router --- Router --- Node
             */
            @Override
            public void generate(Backbone root) {

                // Top line
                RouterNetInterface line1_1 = generateRouter(0, root);

                RouterNetInterface line1_2 = generateRouter(1, null, line1_1);
                addNodes(line1_2, 1);

                // Bottom line
                RouterNetInterface line2_1 = generateRouter(2, root);

                RouterNetInterface line2_2 = generateRouter(3, null, line2_1);
                addNodes(line2_2, 1);
            }

        } });

        data.add(new Object[] { new NetworkGenerator() {

            /*
             * Network topology:
             * 
             * _________________ Node _____ Node
             * ___________________ | ________ |
             * Backbone ---+--- Router --- Router
             * ___________ | _____ | ________ |
             * ___________ | __ Router --- Router
             * ___________ | _____ | ________ |
             * ___________ | ___ Node _____ Node
             * ___________ |
             * ___________ |
             * ____________|____ Node _____ Node
             * ____________|______ | ________ |
             * ___________ +--- Router --- Router
             * ___________________ | ________ |
             * ________________ Router --- Router
             * ___________________ | ________ |
             * _________________ Node _____ Node
             */
            @Override
            public void generate(Backbone root) {

                // Top cycle
                RouterNetInterface cycle1_1 = generateRouter(0, root);
                addNodes(cycle1_1, 1);

                RouterNetInterface cycle1_2 = generateRouter(1, null, cycle1_1);
                addNodes(cycle1_2, 1);

                RouterNetInterface cycle1_3 = generateRouter(2, null, cycle1_2);
                addNodes(cycle1_3, 1);

                RouterNetInterface cycle1_4 = generateRouter(3, null, cycle1_3, cycle1_1);
                addNodes(cycle1_4, 1);

                // Bottom cycle
                RouterNetInterface cycle2_1 = generateRouter(4, root);
                addNodes(cycle2_1, 1);

                RouterNetInterface cycle2_2 = generateRouter(5, null, cycle2_1);
                addNodes(cycle2_2, 1);

                RouterNetInterface cycle2_3 = generateRouter(6, null, cycle2_2);
                addNodes(cycle2_3, 1);

                RouterNetInterface cycle2_4 = generateRouter(7, null, cycle2_3, cycle2_1);
                addNodes(cycle2_4, 1);
            }

        } });

        return data;
    }

    private static interface NetworkGenerator {

        public void generate(Backbone root);

    }

    // ----- Utility -----

    private static NetID generateNetID(int subnet, int id) {

        NetID netId = new NetID();
        netId.get(NetID.SUBNET).set(subnet);
        netId.get(NetID.ID).set(id);
        return netId;
    }

    private static Address generateAddress(NetID netId, int port) {

        Address address = new Address();
        address.get(Address.NET_ID).set(netId);
        address.get(Address.PORT).set(port);
        return address;
    }

    private static RouterNetInterface generateRouter(int subnet, Backbone backboneConnection, RouterNetInterface... neighbours) {

        RouterNetInterface router = new RouterNetInterface();
        router.get(RouterNetInterface.SUBNET).set(subnet);
        if (backboneConnection != null) {
            router.get(RouterNetInterface.BACKBONE_CONNECTION).set(backboneConnection);
        }

        for (RouterNetInterface neighbour : neighbours) {
            router.get(RouterNetInterface.NEIGHBOURS).add(neighbour);
        }

        return router;
    }

    private static void addNodes(RouterNetInterface router, int amount) {

        int subnet = router.get(RouterNetInterface.SUBNET).get();

        for (int id = 1; id <= amount; id++) {
            NodeNetInterface child = new NodeNetInterface();
            child.get(NodeNetInterface.CONNECTION).set(router);
            child.get(NodeNetInterface.NET_ID).set(generateNetID(subnet, id));
        }
    }

    // ----- PacketProcessor.PROCESS Callback -----

    /*
     * This field is changed by the test on every run.
     * The callback is called every time a packet is processed by a backbone, router or node.
     */
    private static ProcessPacketCallback processPacketCallback;

    private static interface ProcessPacketCallback {

        public void onProcessPacket(NetID processorID, Packet packet);

    }

    @BeforeClass
    public static void installProcessCallback() {

        Backbone.PROCESS.addExecutor("processPacketCallbackRunner", Backbone.class, new FunctionExecutor<Void>() {

            @Override
            @Prioritized (Prioritized.LEVEL_8)
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                processPacketCallback.onProcessPacket(null, (Packet) arguments[0]);

                return invocation.next(arguments);
            }

        });

        RouterNetInterface.PROCESS.addExecutor("processPacketCallbackRunner", RouterNetInterface.class, new FunctionExecutor<Void>() {

            @Override
            @Prioritized (Prioritized.LEVEL_8)
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                NetID processorID = generateNetID(invocation.getHolder().get(RouterNetInterface.SUBNET).get(), 0);
                processPacketCallback.onProcessPacket(processorID, (Packet) arguments[0]);

                return invocation.next(arguments);
            }

        });

        NodeNetInterface.PROCESS.addExecutor("processPacketCallbackRunner", NodeNetInterface.class, new FunctionExecutor<Void>() {

            @Override
            @Prioritized (Prioritized.LEVEL_8)
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                NetID processorID = invocation.getHolder().get(NodeNetInterface.NET_ID).get();
                processPacketCallback.onProcessPacket(processorID, (Packet) arguments[0]);

                return invocation.next(arguments);
            }

        });
    }

    @AfterClass
    public static void uninstallProcessCallback() {

        Backbone.PROCESS.removeExecutor("processPacketCallbackRunner", Backbone.class);
        RouterNetInterface.PROCESS.removeExecutor("processPacketCallbackRunner", RouterNetInterface.class);
        NodeNetInterface.PROCESS.removeExecutor("processPacketCallbackRunner", NodeNetInterface.class);
    }

    // ----- Actual Test -----

    private final NetworkGenerator networkGenerator;

    public RoutingTest(NetworkGenerator networkGenerator) {

        this.networkGenerator = networkGenerator;
    }

    // Use a timeout to catch endless cycle errors
    @Test (timeout = 5000)
    public void test() {

        Backbone root = new Backbone();
        networkGenerator.generate(root);

        List<NodeNetInterface> nodes = new ArrayList<>();
        {
            List<RouterNetInterface> visited = new ArrayList<>();
            for (RouterNetInterface child : root.get(Backbone.CHILDREN).get()) {
                if (!visited.contains(child)) {
                    nodes.addAll(getAllChildren(child, visited));
                }
            }
        }

        for (NodeNetInterface node1 : nodes) {
            for (NodeNetInterface node2 : nodes) {
                NetID sourceID = node1.get(NodeNetInterface.NET_ID).get();
                NetID destinationID = node2.get(NodeNetInterface.NET_ID).get();
                // Run a test between the two nodes (might be the same node)
                new TestExecutor(node1, sourceID, destinationID).run();
            }
        }
    }

    private List<NodeNetInterface> getAllChildren(RouterNetInterface router, List<RouterNetInterface> visited) {

        visited.add(router);

        List<NodeNetInterface> children = new ArrayList<>();
        for (RouterNetInterface neighbour : router.get(RouterNetInterface.NEIGHBOURS).get()) {
            if (!visited.contains(neighbour)) {
                children.addAll(getAllChildren(neighbour, visited));
            }
        }
        children.addAll(router.get(RouterNetInterface.CHILDREN).get());

        return children;
    }

    private static class TestExecutor {

        private final PacketProcessor source;
        private final NetID           sourceId;
        private final NetID           destinationId;

        private boolean               receivedPacket;

        public TestExecutor(PacketProcessor source, NetID sourceId, NetID destinationId) {

            this.source = source;
            this.sourceId = sourceId;
            this.destinationId = destinationId;

            processPacketCallback = new ProcessPacketCallback() {

                @Override
                public void onProcessPacket(NetID processorId, Packet packet) {

                    NetID destination = packet.get(Packet.DESTINATION).get().get(Address.NET_ID).get();

                    if (destination.equals(processorId) && destination.equals(TestExecutor.this.destinationId)) {
                        receivedPacket = true;
                    }
                }

            };
        }

        public void run() {

            Packet packet = new Packet();
            packet.get(Packet.SOURCE).set(generateAddress(sourceId, 0));
            packet.get(Packet.DESTINATION).set(generateAddress(destinationId, 0));
            packet.get(Packet.DATA).set("testdata");

            source.get(PacketProcessor.PROCESS).invoke(packet);

            String sourceString = sourceId.get(NetID.TO_STRING).invoke();
            String destinationString = destinationId.get(NetID.TO_STRING).invoke();
            assertTrue("Packet wasn't received by planned destination (" + sourceString + " -> " + destinationString + ")", receivedPacket);
        }

    }

}
