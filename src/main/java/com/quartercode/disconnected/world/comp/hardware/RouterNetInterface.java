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

package com.quartercode.disconnected.world.comp.hardware;

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
import com.quartercode.classmod.extra.PropertyDefinition;
import com.quartercode.classmod.extra.storage.ReferenceCollectionStorage;
import com.quartercode.classmod.extra.storage.ReferenceStorage;
import com.quartercode.classmod.extra.storage.StandardStorage;
import com.quartercode.classmod.extra.valuefactory.CloneValueFactory;
import com.quartercode.disconnected.world.comp.hardware.Mainboard.NeedsMainboardSlot;
import com.quartercode.disconnected.world.comp.net.Address;
import com.quartercode.disconnected.world.comp.net.Backbone;
import com.quartercode.disconnected.world.comp.net.NetID;
import com.quartercode.disconnected.world.comp.net.Packet;
import com.quartercode.disconnected.world.comp.net.PacketProcessor;

/**
 * This class represents a router network interface that may be used by a router computer.
 * It is connected to other "neighbour" router interfaces and some "child" {@link NodeNetInterface}s.
 * Routers are able to hand over {@link Packet}s to neighbours or children depending on their destination.
 * 
 * @see Packet
 * @see NodeNetInterface
 * @see Hardware
 */
@NeedsMainboardSlot
public class RouterNetInterface extends Hardware implements PacketProcessor {

    // ----- Properties -----

    /**
     * The subnet this router network interface is responsible for.
     */
    public static final PropertyDefinition<Integer>                                                SUBNET;

    /**
     * A connection to the {@link Backbone} of the world that must not be available for all router network interfaces.
     * Please note that setting/unsetting the connection also adds/removes a reverse connection from the new backbone to this router interface.
     */
    public static final PropertyDefinition<Backbone>                                               BACKBONE_CONNECTION;

    /**
     * The other "neighbour" router interfaces this interface is physically connected to.
     * Please note that adding/removing a connection also adds/removes a reverse connection from the new neighbour to this router interface.
     */
    public static final CollectionPropertyDefinition<RouterNetInterface, List<RouterNetInterface>> NEIGHBOURS;

    /**
     * The "child" {@link NodeNetInterface}s this interface is physically connected to.
     * Please note that adding/removing a connection also sets/unsets a reverse connection from the new child to this router interface.
     */
    public static final CollectionPropertyDefinition<NodeNetInterface, List<NodeNetInterface>>     CHILDREN;

    static {

        SUBNET = create(new TypeLiteral<PropertyDefinition<Integer>>() {}, "name", "subnet", "storage", new StandardStorage<>());

        BACKBONE_CONNECTION = create(new TypeLiteral<PropertyDefinition<Backbone>>() {}, "name", "backboneConnection", "storage", new ReferenceStorage<>(), "ignoreEquals", true);
        BACKBONE_CONNECTION.addSetterExecutor("addReverseConnection", RouterNetInterface.class, new FunctionExecutor<Void>() {

            @Override
            @Prioritized (Prioritized.LEVEL_3)
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                if (arguments[0] != null) {
                    RouterNetInterface holder = (RouterNetInterface) invocation.getHolder();
                    Backbone connection = (Backbone) arguments[0];

                    if (!connection.get(Backbone.CHILDREN).get().contains(holder)) {
                        connection.get(Backbone.CHILDREN).add(holder);
                    }
                }

                return invocation.next(arguments);
            }

        });
        BACKBONE_CONNECTION.addSetterExecutor("removeReverseConnection", RouterNetInterface.class, new FunctionExecutor<Void>() {

            @Override
            @Prioritized (Prioritized.LEVEL_5)
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                if (arguments[0] == null) {
                    RouterNetInterface holder = (RouterNetInterface) invocation.getHolder();
                    Backbone oldConnection = holder.get(BACKBONE_CONNECTION).get();

                    if (oldConnection != null && oldConnection.get(Backbone.CHILDREN).get().contains(holder)) {
                        oldConnection.get(Backbone.CHILDREN).remove(holder);
                    }
                }

                return invocation.next(arguments);
            }

        });

        NEIGHBOURS = create(new TypeLiteral<CollectionPropertyDefinition<RouterNetInterface, List<RouterNetInterface>>>() {}, "name", "neighbours", "storage", new ReferenceCollectionStorage<>(), "collection", new CloneValueFactory<>(new ArrayList<>()), "ignoreEquals", true);
        NEIGHBOURS.addAdderExecutor("addReverseConnection", RouterNetInterface.class, new FunctionExecutor<Void>() {

            @Override
            @Prioritized (Prioritized.LEVEL_3)
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                RouterNetInterface holder = (RouterNetInterface) invocation.getHolder();
                RouterNetInterface neighbour = (RouterNetInterface) arguments[0];

                if (!neighbour.get(NEIGHBOURS).get().contains(holder)) {
                    neighbour.get(NEIGHBOURS).add(holder);
                }

                return invocation.next(arguments);
            }

        });
        NEIGHBOURS.addRemoverExecutor("removeReverseConnection", RouterNetInterface.class, new FunctionExecutor<Void>() {

            @Override
            @Prioritized (Prioritized.LEVEL_3)
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                RouterNetInterface holder = (RouterNetInterface) invocation.getHolder();
                RouterNetInterface neighbour = (RouterNetInterface) arguments[0];

                if (neighbour.get(NEIGHBOURS).get().contains(holder)) {
                    neighbour.get(NEIGHBOURS).remove(holder);
                }

                return invocation.next(arguments);
            }

        });

        CHILDREN = create(new TypeLiteral<CollectionPropertyDefinition<NodeNetInterface, List<NodeNetInterface>>>() {}, "name", "children", "storage", new ReferenceCollectionStorage<>(), "collection", new CloneValueFactory<>(new ArrayList<>()));
        CHILDREN.addAdderExecutor("addReverseConnection", RouterNetInterface.class, new FunctionExecutor<Void>() {

            @Override
            @Prioritized (Prioritized.LEVEL_3)
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                RouterNetInterface holder = (RouterNetInterface) invocation.getHolder();
                NodeNetInterface child = (NodeNetInterface) arguments[0];

                RouterNetInterface currentChildConnection = child.get(NodeNetInterface.CONNECTION).get();
                if (currentChildConnection == null || !currentChildConnection.equals(holder)) {
                    child.get(NodeNetInterface.CONNECTION).set(holder);
                }

                return invocation.next(arguments);
            }

        });
        CHILDREN.addRemoverExecutor("removeReverseConnection", RouterNetInterface.class, new FunctionExecutor<Void>() {

            @Override
            @Prioritized (Prioritized.LEVEL_3)
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                RouterNetInterface holder = (RouterNetInterface) invocation.getHolder();
                NodeNetInterface child = (NodeNetInterface) arguments[0];

                RouterNetInterface currentChildConnection = child.get(NodeNetInterface.CONNECTION).get();
                if (currentChildConnection != null && currentChildConnection.equals(holder)) {
                    child.get(NodeNetInterface.CONNECTION).set(null);
                }

                return invocation.next(arguments);
            }

        });

    }

    // ----- Functions -----

    static {

        PROCESS.addExecutor("default", RouterNetInterface.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                FeatureHolder holder = invocation.getHolder();
                Packet packet = (Packet) arguments[0];

                // Routing cascade
                if (!tryRouteToChild(holder, packet)) {
                    if (!tryRouteToNeighbour(holder, packet)) {
                        tryRouteToBackbone(holder, packet);
                    }
                }

                return invocation.next(arguments);
            }

            /*
             * Try to send the packet to a feasible child node.
             */
            private boolean tryRouteToChild(FeatureHolder router, Packet packet) {

                NetID destination = packet.get(Packet.RECEIVER).get().get(Address.NET_ID).get();
                int destinationID = destination.get(NetID.ID).get();

                if (destination.get(NetID.SUBNET).get() != router.get(SUBNET).get()) {
                    // Packet destination subnet does not equal the router's subnet: Abort routing
                    return false;
                }

                for (NodeNetInterface child : router.get(CHILDREN).get()) {
                    NetID childID = child.get(NodeNetInterface.NET_ID).get();
                    if (childID.get(NetID.ID).get() == destinationID) {
                        child.get(NodeNetInterface.PROCESS).invoke(packet);
                        return true;
                    }
                }

                // No feasible child found: Abort routing
                return false;
            }

            /*
             * Try to send the packet to the next router on the shortest path to the packet's destination.
             */
            private boolean tryRouteToNeighbour(FeatureHolder router, Packet packet) {

                int destinationSubnet = packet.get(Packet.RECEIVER).get().get(Address.NET_ID).get().get(NetID.SUBNET).get();
                RouterNetInterface nextRouter = getNextShortestPathRouter(router, destinationSubnet);

                if (nextRouter == null) {
                    // No next router found: Abort routing
                    return false;
                }

                nextRouter.get(PROCESS).invoke(packet);
                return true;
            }

            private RouterNetInterface getNextShortestPathRouter(FeatureHolder router, int destinationSubnet) {

                List<RouterNetInterface> neighbours = router.get(NEIGHBOURS).get();

                if (neighbours.isEmpty()) {
                    // No connected routers
                    return null;
                }

                // Calculate the next router of the shortest path to the given destination
                // Note that the algorithm which is used might not caclulate the shortest path in some cases
                int shortestDistance = Integer.MAX_VALUE;
                RouterNetInterface shortestDistanceNeighbour = null;
                for (RouterNetInterface neighbour : neighbours) {
                    int distance = getShortestPathDistance(neighbour, destinationSubnet, new HashSet<Integer>());
                    if (distance >= 0 && distance < shortestDistance) {
                        shortestDistance = distance;
                        shortestDistanceNeighbour = neighbour;
                    }
                }

                if (shortestDistance == Integer.MAX_VALUE) {
                    // No route found
                    return null;
                }

                return shortestDistanceNeighbour;
            }

            private int getShortestPathDistance(RouterNetInterface start, int destinationSubnet, Set<Integer> visitedSubnets) {

                int startSubnet = start.get(SUBNET).get();

                if (startSubnet == destinationSubnet) {
                    // Distance between two references of the same router is 0
                    return 0;
                } else {
                    // Add the start router to the "visited" list in order to prevent endless cycle errors
                    visitedSubnets.add(startSubnet);

                    // Retrieve the distances of all unvisited neighbours to the destination and record the shortest one
                    int shortestDistance = Integer.MAX_VALUE;
                    for (RouterNetInterface neighbour : start.get(NEIGHBOURS).get()) {
                        int neighbourSubnet = neighbour.get(SUBNET).get();
                        if (!visitedSubnets.contains(neighbourSubnet)) {
                            int distance = getShortestPathDistance(neighbour, destinationSubnet, visitedSubnets);
                            if (distance >= 0 && distance < shortestDistance) {
                                shortestDistance = distance;
                            }
                        }
                    }
                    if (shortestDistance == Integer.MAX_VALUE) {
                        shortestDistance = -1;
                    }

                    // If a path was found, increase the distance by one in order to add the node -> neighbour jump
                    if (shortestDistance >= 0) {
                        shortestDistance++;
                    }

                    return shortestDistance;
                }
            }

            /*
             * Try to send the packet to a connected backbone.
             */
            private boolean tryRouteToBackbone(FeatureHolder router, Packet packet) {

                if (router.get(BACKBONE_CONNECTION).get() != null) {
                    router.get(BACKBONE_CONNECTION).get().get(Backbone.PROCESS).invoke(packet);
                    return true;
                } else {
                    RouterNetInterface uplink = null;
                    {
                        List<RouterNetInterface> allNeighbours = new ArrayList<>();
                        recordAllNeighbours((RouterNetInterface) router, allNeighbours);
                        for (RouterNetInterface neighbour : allNeighbours) {
                            Backbone backboneConnection = neighbour.get(BACKBONE_CONNECTION).get();
                            if (backboneConnection != null) {
                                uplink = neighbour;
                                break;
                            }
                        }
                    }

                    RouterNetInterface nextRouter = getNextShortestPathRouter(router, uplink.get(SUBNET).get());
                    if (nextRouter == null) {
                        // No next router found: Abort routing
                        return false;
                    }

                    nextRouter.get(PROCESS).invoke(packet);
                    return true;
                }
            }

            private void recordAllNeighbours(RouterNetInterface router, List<RouterNetInterface> list) {

                list.add(router);

                for (RouterNetInterface neighbour : router.get(RouterNetInterface.NEIGHBOURS).get()) {
                    if (!list.contains(neighbour)) {
                        recordAllNeighbours(neighbour, list);
                    }
                }
            }

        });

    }

    /**
     * Creates a new router network interface.
     */
    public RouterNetInterface() {

    }

}
