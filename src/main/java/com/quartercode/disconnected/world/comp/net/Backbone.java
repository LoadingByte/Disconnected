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

package com.quartercode.disconnected.world.comp.net;

import static com.quartercode.classmod.ClassmodFactory.create;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.reflect.TypeLiteral;
import com.quartercode.classmod.base.FeatureHolder;
import com.quartercode.classmod.extra.CollectionPropertyDefinition;
import com.quartercode.classmod.extra.FunctionExecutor;
import com.quartercode.classmod.extra.FunctionInvocation;
import com.quartercode.classmod.extra.Prioritized;
import com.quartercode.classmod.extra.storage.ReferenceCollectionStorage;
import com.quartercode.classmod.extra.valuefactory.CloneValueFactory;
import com.quartercode.disconnected.world.World;
import com.quartercode.disconnected.world.WorldChildFeatureHolder;
import com.quartercode.disconnected.world.comp.hardware.NodeNetInterface;
import com.quartercode.disconnected.world.comp.hardware.RouterNetInterface;

/**
 * Backbones are <i>"magical connectors"</i> that connect different {@link RouterNetInterface}s together.
 * One backbone basically represents <i>"the entire internet infrastructure"</i> in an abstract form.
 * Backbones send the {@link Packet}s, which they should process, to the router that provides the shortest route to the target subnet.
 */
public class Backbone extends WorldChildFeatureHolder<World> implements PacketProcessor {

    // ----- Properties -----

    /**
     * The root {@link RouterNetInterface}s the backbone is physically connected to.
     * Please note that adding/removing a connection also adds/removes a reverse connection from the new child to this backbone.
     */
    public static final CollectionPropertyDefinition<RouterNetInterface, List<RouterNetInterface>> CHILDREN;

    static {

        CHILDREN = create(new TypeLiteral<CollectionPropertyDefinition<RouterNetInterface, List<RouterNetInterface>>>() {}, "name", "children", "storage", new ReferenceCollectionStorage<>(), "collection", new CloneValueFactory<>(new ArrayList<>()));
        CHILDREN.addAdderExecutor("addReverseConnection", Backbone.class, new FunctionExecutor<Void>() {

            @Override
            @Prioritized (Prioritized.LEVEL_3)
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                Backbone holder = (Backbone) invocation.getHolder();
                RouterNetInterface child = (RouterNetInterface) arguments[0];

                Backbone currentChildConnection = child.get(RouterNetInterface.BACKBONE_CONNECTION).get();
                if (currentChildConnection == null || !currentChildConnection.equals(holder)) {
                    child.get(RouterNetInterface.BACKBONE_CONNECTION).set(holder);
                }

                return invocation.next(arguments);
            }

        });
        CHILDREN.addRemoverExecutor("removeReverseConnection", Backbone.class, new FunctionExecutor<Void>() {

            @Override
            @Prioritized (Prioritized.LEVEL_3)
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                Backbone holder = (Backbone) invocation.getHolder();
                RouterNetInterface child = (RouterNetInterface) arguments[0];

                Backbone currentChildConnection = child.get(RouterNetInterface.BACKBONE_CONNECTION).get();
                if (currentChildConnection != null && currentChildConnection.equals(holder)) {
                    child.get(RouterNetInterface.BACKBONE_CONNECTION).set(null);
                }

                return invocation.next(arguments);
            }

        });

    }

    // ----- Functions -----

    static {

        PROCESS.addExecutor("default", Backbone.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                routeToChild(invocation.getHolder(), (Packet) arguments[0]);

                return invocation.next(arguments);
            }

            /*
             * Try to send the packet to a feasible child router.
             */
            private boolean routeToChild(FeatureHolder backbone, Packet packet) {

                NetID packetDest = packet.get(Packet.RECEIVER).get().get(Address.NET_ID).get();
                int packetDestSubnet = packetDest.get(NetID.SUBNET).get();

                Set<Integer> visitedSubnets = new HashSet<>();
                for (RouterNetInterface child : backbone.get(Backbone.CHILDREN).get()) {
                    List<NodeNetInterface> nodes = new ArrayList<>();
                    recordAllNodes(child, nodes, visitedSubnets);
                    for (NodeNetInterface node : nodes) {
                        if (node.get(NodeNetInterface.NET_ID).get().get(NetID.SUBNET).get() == packetDestSubnet) {
                            child.get(NodeNetInterface.PROCESS).invoke(packet);
                            return true;
                        }
                    }
                }

                // No feasible child node found
                return false;
            }

            private void recordAllNodes(RouterNetInterface router, List<NodeNetInterface> list, Set<Integer> visitedSubnets) {

                int routerSubnet = router.get(RouterNetInterface.SUBNET).get();

                if (!visitedSubnets.contains(routerSubnet)) {
                    visitedSubnets.add(routerSubnet);
                    visitedSubnets.add(router.get(RouterNetInterface.SUBNET).get());
                    list.addAll(router.get(RouterNetInterface.CHILDREN).get());

                    for (RouterNetInterface neighbour : router.get(RouterNetInterface.NEIGHBOURS).get()) {
                        recordAllNodes(neighbour, list, visitedSubnets);
                    }
                }
            }

        });

    }

    /**
     * Creates a new backbone.
     */
    public Backbone() {

    }

}
