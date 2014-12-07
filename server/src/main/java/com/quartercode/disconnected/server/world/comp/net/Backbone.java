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

package com.quartercode.disconnected.server.world.comp.net;

import static com.quartercode.classmod.extra.func.Priorities.LEVEL_3;
import static com.quartercode.classmod.factory.ClassmodFactory.factory;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.quartercode.classmod.extra.conv.CFeatureHolder;
import com.quartercode.classmod.extra.func.FunctionExecutor;
import com.quartercode.classmod.extra.func.FunctionInvocation;
import com.quartercode.classmod.extra.prop.CollectionPropertyDefinition;
import com.quartercode.classmod.extra.storage.ReferenceCollectionStorage;
import com.quartercode.classmod.extra.valuefactory.CloneValueFactory;
import com.quartercode.classmod.factory.CollectionPropertyDefinitionFactory;
import com.quartercode.disconnected.server.world.comp.hardware.NodeNetInterface;
import com.quartercode.disconnected.server.world.comp.hardware.RouterNetInterface;
import com.quartercode.disconnected.server.world.util.WorldFeatureHolder;
import com.quartercode.disconnected.shared.world.comp.net.NetID;

/**
 * Backbones are <i>"magical connectors"</i> that connect different {@link RouterNetInterface}s together.
 * One backbone basically represents <i>"the entire internet infrastructure"</i> in an abstract form.
 * Backbones send the {@link Packet}s, which they should process, to the router that provides the shortest route to the target subnet.
 */
public class Backbone extends WorldFeatureHolder implements PacketProcessor {

    // ----- Properties -----

    /**
     * The root {@link RouterNetInterface}s the backbone is physically connected to.
     * Please note that adding/removing a connection also adds/removes a reverse connection from the new child to this backbone.
     */
    public static final CollectionPropertyDefinition<RouterNetInterface, List<RouterNetInterface>> CHILDREN;

    static {

        CHILDREN = factory(CollectionPropertyDefinitionFactory.class).create("children", new ReferenceCollectionStorage<>(), new CloneValueFactory<>(new ArrayList<>()));
        CHILDREN.addAdderExecutor("addReverseConnection", Backbone.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                Backbone holder = (Backbone) invocation.getCHolder();
                RouterNetInterface child = (RouterNetInterface) arguments[0];

                Backbone currentChildConnection = child.getObj(RouterNetInterface.BACKBONE_CONNECTION);
                if (currentChildConnection == null || !currentChildConnection.equals(holder)) {
                    child.setObj(RouterNetInterface.BACKBONE_CONNECTION, holder);
                }

                return invocation.next(arguments);
            }

        }, LEVEL_3);
        CHILDREN.addRemoverExecutor("removeReverseConnection", Backbone.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                Backbone holder = (Backbone) invocation.getCHolder();
                RouterNetInterface child = (RouterNetInterface) arguments[0];

                Backbone currentChildConnection = child.getObj(RouterNetInterface.BACKBONE_CONNECTION);
                if (currentChildConnection != null && currentChildConnection.equals(holder)) {
                    child.setObj(RouterNetInterface.BACKBONE_CONNECTION, null);
                }

                return invocation.next(arguments);
            }

        }, LEVEL_3);

    }

    // ----- Functions -----

    static {

        PROCESS.addExecutor("default", Backbone.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                routeToChild(invocation.getCHolder(), (Packet) arguments[0]);

                return invocation.next(arguments);
            }

            /*
             * Try to send the packet to a feasible child router.
             */
            private boolean routeToChild(CFeatureHolder backbone, Packet packet) {

                NetID packetDest = packet.getObj(Packet.DESTINATION).getNetId();
                int packetDestSubnet = packetDest.getSubnet();

                Set<Integer> visitedSubnets = new HashSet<>();
                for (RouterNetInterface child : backbone.getColl(Backbone.CHILDREN)) {
                    List<NodeNetInterface> nodes = new ArrayList<>();
                    recordAllNodes(child, nodes, visitedSubnets);
                    for (NodeNetInterface node : nodes) {
                        if (node.getObj(NodeNetInterface.NET_ID).getSubnet() == packetDestSubnet) {
                            child.invoke(NodeNetInterface.PROCESS, packet);
                            return true;
                        }
                    }
                }

                // No feasible child node found
                return false;
            }

            private void recordAllNodes(RouterNetInterface router, List<NodeNetInterface> list, Set<Integer> visitedSubnets) {

                int routerSubnet = router.getObj(RouterNetInterface.SUBNET);

                if (!visitedSubnets.contains(routerSubnet)) {
                    visitedSubnets.add(routerSubnet);
                    visitedSubnets.add(router.getObj(RouterNetInterface.SUBNET));
                    list.addAll(router.getColl(RouterNetInterface.CHILDREN));

                    for (RouterNetInterface neighbour : router.getColl(RouterNetInterface.NEIGHBOURS)) {
                        recordAllNodes(neighbour, list, visitedSubnets);
                    }
                }
            }

        });

    }

}
