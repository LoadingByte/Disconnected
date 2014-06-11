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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.reflect.TypeLiteral;
import com.quartercode.classmod.base.FeatureHolder;
import com.quartercode.classmod.extra.CollectionPropertyDefinition;
import com.quartercode.classmod.extra.FunctionDefinition;
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
import com.quartercode.disconnected.world.comp.net.RoutedPacket;

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

    /**
     * Processes the given {@link RoutedPacket} by implementing the rules that are described in {@link RoutedPacket#PATH}.
     * This method is not intended for public usage and takes care of sending routed packets between routers.
     * 
     * <table>
     * <tr>
     * <th>Index</th>
     * <th>Type</th>
     * <th>Parameter</th>
     * <th>Description</th>
     * </tr>
     * <tr>
     * <td>0</td>
     * <td>{@link RoutedPacket}</td>
     * <td>routedPacket</td>
     * <td>The routed packet that should be handled by the router net interface.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>                                                   PROCESS_ROUTED;

    static {

        PROCESS.addExecutor("default", RouterNetInterface.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                routePacket(invocation.getHolder(), (Packet) arguments[0]);

                return invocation.next(arguments);
            }

        });

        PROCESS_ROUTED = create(new TypeLiteral<FunctionDefinition<Void>>() {}, "name", "processRouted", "parameters", new Class<?>[] { RoutedPacket.class });
        PROCESS_ROUTED.addExecutor("default", RouterNetInterface.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                FeatureHolder holder = invocation.getHolder();
                RoutedPacket routedPacket = (RoutedPacket) arguments[0];
                Packet packet = routedPacket.get(RoutedPacket.PACKET).get();

                if (routedPacket.get(RoutedPacket.PATH).get().isEmpty()) {
                    if (!tryHandOverToChild(holder, packet)) {
                        routePacket(holder, packet);
                    }
                } else {
                    // Poll the next subnet from the path pseudo-queue
                    int nextSubnet = routedPacket.get(RoutedPacket.PATH).get().get(0);
                    routedPacket.get(RoutedPacket.PATH).remove(nextSubnet);

                    if (nextSubnet < 0) {
                        if (!tryHandOverToBackbone(holder, packet)) {
                            routePacket(holder, packet);
                        }
                    } else if (!tryHandOverToNeighbour(holder, routedPacket, packet, nextSubnet)) {
                        routePacket(holder, packet);
                    }
                }

                return invocation.next(arguments);
            }

            /*
             * Try to send the packet to its destination child node.
             */
            private boolean tryHandOverToChild(FeatureHolder router, Packet packet) {

                NetID destination = packet.get(Packet.DESTINATION).get().get(Address.NET_ID).get();
                int destinationSubnet = destination.get(NetID.SUBNET).get();
                int destinationId = destination.get(NetID.ID).get();

                if (destinationSubnet != router.get(SUBNET).get()) {
                    // Packet destination subnet does not equal the router's subnet
                    return false;
                }

                for (NodeNetInterface child : router.get(CHILDREN).get()) {
                    int childId = child.get(NodeNetInterface.NET_ID).get().get(NetID.ID).get();
                    if (childId == destinationId) {
                        child.get(NodeNetInterface.PROCESS).invoke(packet);
                        return true;
                    }
                }

                // No feasible child node found
                return false;
            }

            /*
             * Try to send the packet to the backbone if connected.
             */
            private boolean tryHandOverToBackbone(FeatureHolder router, Packet packet) {

                if (router.get(BACKBONE_CONNECTION).get() != null) {
                    router.get(BACKBONE_CONNECTION).get().get(Backbone.PROCESS).invoke(packet);
                    return true;
                }

                // Not connected to the backbone
                return false;
            }

            /*
             * Try to send the packet to the neighbour router with the given subnet.
             */
            private boolean tryHandOverToNeighbour(FeatureHolder router, RoutedPacket routedPacket, Packet packet, int nextSubnet) {

                for (RouterNetInterface neighbour : router.get(NEIGHBOURS).get()) {
                    int neighbourSubnet = neighbour.get(SUBNET).get();
                    if (neighbourSubnet == nextSubnet) {
                        neighbour.get(PROCESS_ROUTED).invoke(routedPacket);
                        return true;
                    }
                }

                // No feasible neighbour router found
                return false;
            }

        });

    }

    private static void routePacket(FeatureHolder router, Packet packet) {

        final int destinationSubnet = packet.get(Packet.DESTINATION).get().get(Address.NET_ID).get().get(NetID.SUBNET).get();
        List<Integer> path = calculatePathToDestination(new DestinationMatcher() {

            @Override
            public boolean matches(FeatureHolder router) {

                return router.get(SUBNET).get() == destinationSubnet;
            }

        }, new ArrayList<Integer>(), router, new HashSet<Integer>());

        // When no path is found, the destination router is not connected with the router that called this method.
        // That means that the only way to reach the destination is to send the packet over the backbone.
        if (path == null) {
            path = calculatePathToDestination(new DestinationMatcher() {

                @Override
                public boolean matches(FeatureHolder router) {

                    return router.get(BACKBONE_CONNECTION).get() != null;
                }

            }, new ArrayList<>(Arrays.asList(-1)), router, new HashSet<Integer>());
        }

        // When no path to the backbone is found, the routing must be aborted since there is no way to reach the destination.
        if (path == null) {
            return;
        }

        RoutedPacket routedPacket = new RoutedPacket();
        routedPacket.get(RoutedPacket.PACKET).set(packet);
        for (int pathEntry : path) {
            routedPacket.get(RoutedPacket.PATH).add(pathEntry);
        }

        router.get(PROCESS_ROUTED).invoke(routedPacket);
    }

    /*
     * This method just calculates "a" valid path from a starting router ("currentRouter") to a destination router.
     * The destination matcher is called once for every visited router and returns whether the provided router is the destination.
     * If that is the case, the given "pathEnd" list is used to start the path list from the back.
     */
    private static List<Integer> calculatePathToDestination(DestinationMatcher destinationMatcher, List<Integer> pathEnd, FeatureHolder currentRouter, Set<Integer> visitedSubnets) {

        if (destinationMatcher.matches(currentRouter)) {
            return pathEnd;
        }

        int startSubnet = currentRouter.get(SUBNET).get();
        visitedSubnets.add(startSubnet);

        List<Integer> shortestPath = null;
        for (RouterNetInterface neighbour : currentRouter.get(NEIGHBOURS).get()) {
            int neighbourSubnet = neighbour.get(SUBNET).get();

            if (!visitedSubnets.contains(neighbourSubnet)) {
                List<Integer> path = calculatePathToDestination(destinationMatcher, pathEnd, neighbour, visitedSubnets);
                if (path != null) {
                    path = new ArrayList<>(path);
                    path.add(0, neighbourSubnet);
                    if (shortestPath == null || path.size() < shortestPath.size()) {
                        shortestPath = path;
                    }
                }
            }
        }

        return shortestPath;
    }

    /**
     * Creates a new router network interface.
     */
    public RouterNetInterface() {

    }

    private static interface DestinationMatcher {

        public boolean matches(FeatureHolder router);

    }

}
