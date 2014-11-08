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

package com.quartercode.disconnected.server.world.comp.hardware;

import static com.quartercode.classmod.ClassmodFactory.create;
import static com.quartercode.classmod.extra.Priorities.LEVEL_3;
import static com.quartercode.classmod.extra.Priorities.LEVEL_5;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.reflect.TypeLiteral;
import com.quartercode.classmod.extra.CFeatureHolder;
import com.quartercode.classmod.extra.CollectionPropertyDefinition;
import com.quartercode.classmod.extra.FunctionDefinition;
import com.quartercode.classmod.extra.FunctionExecutor;
import com.quartercode.classmod.extra.FunctionInvocation;
import com.quartercode.classmod.extra.PropertyDefinition;
import com.quartercode.classmod.extra.storage.ReferenceCollectionStorage;
import com.quartercode.classmod.extra.storage.ReferenceStorage;
import com.quartercode.classmod.extra.storage.StandardStorage;
import com.quartercode.classmod.extra.valuefactory.CloneValueFactory;
import com.quartercode.disconnected.server.world.comp.hardware.Mainboard.NeedsMainboardSlot;
import com.quartercode.disconnected.server.world.comp.net.Backbone;
import com.quartercode.disconnected.server.world.comp.net.Packet;
import com.quartercode.disconnected.server.world.comp.net.PacketProcessor;
import com.quartercode.disconnected.server.world.comp.net.RoutedPacket;
import com.quartercode.disconnected.shared.world.comp.net.NetID;

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

        BACKBONE_CONNECTION = create(new TypeLiteral<PropertyDefinition<Backbone>>() {}, "name", "backboneConnection", "storage", new ReferenceStorage<>(), "hidden", true);
        BACKBONE_CONNECTION.addSetterExecutor("addReverseConnection", RouterNetInterface.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                if (arguments[0] != null) {
                    RouterNetInterface holder = (RouterNetInterface) invocation.getCHolder();
                    Backbone connection = (Backbone) arguments[0];

                    if (!connection.getColl(Backbone.CHILDREN).contains(holder)) {
                        connection.addToColl(Backbone.CHILDREN, holder);
                    }
                }

                return invocation.next(arguments);
            }

        }, LEVEL_3);
        BACKBONE_CONNECTION.addSetterExecutor("removeReverseConnection", RouterNetInterface.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                if (arguments[0] == null) {
                    RouterNetInterface holder = (RouterNetInterface) invocation.getCHolder();
                    Backbone oldConnection = holder.getObj(BACKBONE_CONNECTION);

                    if (oldConnection != null && oldConnection.getColl(Backbone.CHILDREN).contains(holder)) {
                        oldConnection.removeFromColl(Backbone.CHILDREN, holder);
                    }
                }

                return invocation.next(arguments);
            }

        }, LEVEL_5);

        NEIGHBOURS = create(new TypeLiteral<CollectionPropertyDefinition<RouterNetInterface, List<RouterNetInterface>>>() {}, "name", "neighbours", "storage", new ReferenceCollectionStorage<>(), "collection", new CloneValueFactory<>(new ArrayList<>()), "hidden", true);
        NEIGHBOURS.addAdderExecutor("addReverseConnection", RouterNetInterface.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                RouterNetInterface holder = (RouterNetInterface) invocation.getCHolder();
                RouterNetInterface neighbour = (RouterNetInterface) arguments[0];

                if (!neighbour.getColl(NEIGHBOURS).contains(holder)) {
                    neighbour.addToColl(NEIGHBOURS, holder);
                }

                return invocation.next(arguments);
            }

        }, LEVEL_3);
        NEIGHBOURS.addRemoverExecutor("removeReverseConnection", RouterNetInterface.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                RouterNetInterface holder = (RouterNetInterface) invocation.getCHolder();
                RouterNetInterface neighbour = (RouterNetInterface) arguments[0];

                if (neighbour.getColl(NEIGHBOURS).contains(holder)) {
                    neighbour.removeFromColl(NEIGHBOURS, holder);
                }

                return invocation.next(arguments);
            }

        }, LEVEL_3);

        CHILDREN = create(new TypeLiteral<CollectionPropertyDefinition<NodeNetInterface, List<NodeNetInterface>>>() {}, "name", "children", "storage", new ReferenceCollectionStorage<>(), "collection", new CloneValueFactory<>(new ArrayList<>()));
        CHILDREN.addAdderExecutor("addReverseConnection", RouterNetInterface.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                RouterNetInterface holder = (RouterNetInterface) invocation.getCHolder();
                NodeNetInterface child = (NodeNetInterface) arguments[0];

                RouterNetInterface currentChildConnection = child.getObj(NodeNetInterface.CONNECTION);
                if (currentChildConnection == null || !currentChildConnection.equals(holder)) {
                    child.setObj(NodeNetInterface.CONNECTION, holder);
                }

                return invocation.next(arguments);
            }

        }, LEVEL_3);
        CHILDREN.addRemoverExecutor("removeReverseConnection", RouterNetInterface.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                RouterNetInterface holder = (RouterNetInterface) invocation.getCHolder();
                NodeNetInterface child = (NodeNetInterface) arguments[0];

                RouterNetInterface currentChildConnection = child.getObj(NodeNetInterface.CONNECTION);
                if (currentChildConnection != null && currentChildConnection.equals(holder)) {
                    child.setObj(NodeNetInterface.CONNECTION, null);
                }

                return invocation.next(arguments);
            }

        }, LEVEL_3);

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

                routePacket(invocation.getCHolder(), (Packet) arguments[0]);

                return invocation.next(arguments);
            }

        });

        PROCESS_ROUTED = create(new TypeLiteral<FunctionDefinition<Void>>() {}, "name", "processRouted", "parameters", new Class[] { RoutedPacket.class });
        PROCESS_ROUTED.addExecutor("default", RouterNetInterface.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                CFeatureHolder holder = invocation.getCHolder();
                RoutedPacket routedPacket = (RoutedPacket) arguments[0];
                Packet packet = routedPacket.getObj(RoutedPacket.PACKET);

                if (routedPacket.getColl(RoutedPacket.PATH).isEmpty()) {
                    if (!tryHandOverToChild(holder, packet)) {
                        routePacket(holder, packet);
                    }
                } else {
                    // Poll the next subnet from the path pseudo-queue
                    int nextSubnet = routedPacket.getColl(RoutedPacket.PATH).get(0);
                    routedPacket.removeFromColl(RoutedPacket.PATH, nextSubnet);

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
            private boolean tryHandOverToChild(CFeatureHolder router, Packet packet) {

                NetID destination = packet.getObj(Packet.DESTINATION).getNetId();
                int destinationSubnet = destination.getSubnet();
                int destinationId = destination.getId();

                if (destinationSubnet != router.getObj(SUBNET)) {
                    // Packet destination subnet does not equal the router's subnet
                    return false;
                }

                for (NodeNetInterface child : router.getColl(CHILDREN)) {
                    int childId = child.getObj(NodeNetInterface.NET_ID).getId();
                    if (childId == destinationId) {
                        child.invoke(NodeNetInterface.PROCESS, packet);
                        return true;
                    }
                }

                // No feasible child node found
                return false;
            }

            /*
             * Try to send the packet to the backbone if connected.
             */
            private boolean tryHandOverToBackbone(CFeatureHolder router, Packet packet) {

                if (router.getObj(BACKBONE_CONNECTION) != null) {
                    router.getObj(BACKBONE_CONNECTION).invoke(Backbone.PROCESS, packet);
                    return true;
                }

                // Not connected to the backbone
                return false;
            }

            /*
             * Try to send the packet to the neighbour router with the given subnet.
             */
            private boolean tryHandOverToNeighbour(CFeatureHolder router, RoutedPacket routedPacket, Packet packet, int nextSubnet) {

                for (RouterNetInterface neighbour : router.getColl(NEIGHBOURS)) {
                    int neighbourSubnet = neighbour.getObj(SUBNET);
                    if (neighbourSubnet == nextSubnet) {
                        neighbour.invoke(PROCESS_ROUTED, routedPacket);
                        return true;
                    }
                }

                // No feasible neighbour router found
                return false;
            }

        });

    }

    private static void routePacket(CFeatureHolder router, Packet packet) {

        final int destinationSubnet = packet.getObj(Packet.DESTINATION).getNetId().getSubnet();
        List<Integer> path = calculatePathToDestination(new DestinationMatcher() {

            @Override
            public boolean matches(CFeatureHolder router) {

                return router.getObj(SUBNET) == destinationSubnet;
            }

        }, new ArrayList<Integer>(), router, new HashSet<Integer>());

        // When no path is found, the destination router is not connected with the router that called this method.
        // That means that the only way to reach the destination is to send the packet over the backbone.
        if (path == null) {
            path = calculatePathToDestination(new DestinationMatcher() {

                @Override
                public boolean matches(CFeatureHolder router) {

                    return router.getObj(BACKBONE_CONNECTION) != null;
                }

            }, new ArrayList<>(Arrays.asList(-1)), router, new HashSet<Integer>());
        }

        // When no path to the backbone is found, the routing must be aborted since there is no way to reach the destination.
        if (path == null) {
            return;
        }

        RoutedPacket routedPacket = new RoutedPacket();
        routedPacket.setObj(RoutedPacket.PACKET, packet);
        for (int pathEntry : path) {
            routedPacket.addToColl(RoutedPacket.PATH, pathEntry);
        }

        router.invoke(PROCESS_ROUTED, routedPacket);
    }

    /*
     * This method just calculates "a" valid path from a starting router ("currentRouter") to a destination router.
     * The destination matcher is called once for every visited router and returns whether the provided router is the destination.
     * If that is the case, the given "pathEnd" list is used to start the path list from the back.
     */
    private static List<Integer> calculatePathToDestination(DestinationMatcher destinationMatcher, List<Integer> pathEnd, CFeatureHolder currentRouter, Set<Integer> visitedSubnets) {

        if (destinationMatcher.matches(currentRouter)) {
            return pathEnd;
        }

        int startSubnet = currentRouter.getObj(SUBNET);
        visitedSubnets.add(startSubnet);

        List<Integer> shortestPath = null;
        for (RouterNetInterface neighbour : currentRouter.getColl(NEIGHBOURS)) {
            int neighbourSubnet = neighbour.getObj(SUBNET);

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

    private static interface DestinationMatcher {

        public boolean matches(CFeatureHolder router);

    }

}
