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
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.reflect.TypeLiteral;
import com.quartercode.classmod.extra.CFeatureHolder;
import com.quartercode.classmod.extra.FunctionExecutor;
import com.quartercode.classmod.extra.FunctionInvocation;
import com.quartercode.classmod.extra.Prioritized;
import com.quartercode.classmod.extra.PropertyDefinition;
import com.quartercode.classmod.extra.storage.ReferenceStorage;
import com.quartercode.classmod.extra.storage.StandardStorage;
import com.quartercode.disconnected.server.world.comp.Computer;
import com.quartercode.disconnected.server.world.comp.hardware.Mainboard.NeedsMainboardSlot;
import com.quartercode.disconnected.server.world.comp.net.NetworkModule;
import com.quartercode.disconnected.server.world.comp.net.Packet;
import com.quartercode.disconnected.server.world.comp.net.PacketProcessor;
import com.quartercode.disconnected.server.world.comp.os.OperatingSystem;
import com.quartercode.disconnected.shared.world.comp.net.NetID;

/**
 * This class represents a node network interface that may be used by a normal computer.
 * It can only send {@link Packet}s to a {@link RouterNetInterface} and receive ones from it.
 * 
 * @see Packet
 * @see NetID
 * @see RouterNetInterface
 * @see Hardware
 */
@NeedsMainboardSlot
public class NodeNetInterface extends Hardware implements PacketProcessor {

    // ----- Properties -----

    /**
     * The {@link NetID} this interface can be found under.<br>
     * <br>
     * Exceptions that can occur when setting:
     * 
     * <table>
     * <tr>
     * <th>Exception</th>
     * <th>When?</th>
     * </tr>
     * <tr>
     * <td>{@link IllegalArgumentException}</td>
     * <td>The subnet of the new net id is not equal to the one used by the set {@link #CONNECTION}.</td>
     * </tr>
     * </table>
     */
    public static final PropertyDefinition<NetID>              NET_ID;

    /**
     * The {@link RouterNetInterface} the node interface is connected to.
     * It can only exchange {@link Packet}s with that interface.<br>
     * Please note that changing this property clears any set {@link #NET_ID}.
     * Setting/unsetting the parent router also adds/removes a connection from the router to this node interface.
     */
    public static final PropertyDefinition<RouterNetInterface> CONNECTION;

    static {

        NET_ID = create(new TypeLiteral<PropertyDefinition<NetID>>() {}, "name", "netId", "storage", new StandardStorage<>());
        NET_ID.addSetterExecutor("checkSubnetAgainstConnection", NodeNetInterface.class, new FunctionExecutor<Void>() {

            @Override
            @Prioritized (Prioritized.LEVEL_6)
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                NetID netId = (NetID) arguments[0];
                RouterNetInterface connection = invocation.getCHolder().getObj(CONNECTION);

                if (netId != null && connection != null) {
                    int newSubnet = netId.getSubnet();
                    int conSubnet = connection.getObj(RouterNetInterface.SUBNET);

                    Validate.isTrue(newSubnet == conSubnet, "The subnet of the new net id (%d) must be equal to the one connected to (%d)", newSubnet, conSubnet);
                }

                return invocation.next(arguments);
            }

        });

        CONNECTION = create(new TypeLiteral<PropertyDefinition<RouterNetInterface>>() {}, "name", "connection", "storage", new ReferenceStorage<>(), "hidden", true);
        CONNECTION.addSetterExecutor("invalidateNetID", NodeNetInterface.class, new FunctionExecutor<Void>() {

            @Override
            @Prioritized (Prioritized.LEVEL_5 + Prioritized.LEVEL_7)
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                CFeatureHolder holder = invocation.getCHolder();

                RouterNetInterface oldConnection = holder.getObj(CONNECTION);
                if (oldConnection == null || !oldConnection.equals(arguments[0])) {
                    holder.setObj(NET_ID, null);
                }

                return invocation.next(arguments);
            }

        });
        CONNECTION.addSetterExecutor("addReverseConnection", NodeNetInterface.class, new FunctionExecutor<Void>() {

            @Override
            @Prioritized (Prioritized.LEVEL_3)
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                if (arguments[0] != null) {
                    NodeNetInterface holder = (NodeNetInterface) invocation.getCHolder();
                    RouterNetInterface connection = (RouterNetInterface) arguments[0];

                    if (!connection.getColl(RouterNetInterface.CHILDREN).contains(holder)) {
                        connection.addToColl(RouterNetInterface.CHILDREN, holder);
                    }
                }

                return invocation.next(arguments);
            }

        });
        CONNECTION.addSetterExecutor("removeReverseConnection", NodeNetInterface.class, new FunctionExecutor<Void>() {

            @Override
            @Prioritized (Prioritized.LEVEL_5)
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                if (arguments[0] == null) {
                    NodeNetInterface holder = (NodeNetInterface) invocation.getCHolder();
                    RouterNetInterface oldConnection = holder.getObj(CONNECTION);

                    if (oldConnection != null && oldConnection.getColl(RouterNetInterface.CHILDREN).contains(holder)) {
                        oldConnection.removeFromColl(RouterNetInterface.CHILDREN, holder);
                    }
                }

                return invocation.next(arguments);
            }

        });

    }

    // ----- Functions -----

    static {

        PROCESS.addExecutor("default", NodeNetInterface.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                NodeNetInterface holder = (NodeNetInterface) invocation.getCHolder();
                Packet packet = (Packet) arguments[0];

                // Routing cascade
                if (!tryRouteToOs(holder, packet)) {
                    tryRouteToRouter(holder, packet);
                }

                return invocation.next(arguments);
            }

            /*
             * Try to deliver the packet to the operating system.
             */
            private boolean tryRouteToOs(NodeNetInterface netInterface, Packet packet) {

                NetID destination = packet.getObj(Packet.DESTINATION).getNetId();

                if (!destination.equals(netInterface.getObj(NET_ID))) {
                    // Packet destination is not this network interface
                    return false;
                }

                // Only deliver the packet if the net interface is connected to a computer
                if (netInterface.getParent() != null) {
                    NetworkModule netModule = netInterface.getParent().getObj(Computer.OS).getObj(OperatingSystem.NET_MODULE);
                    netModule.invoke(NetworkModule.HANDLE, packet);
                }

                return true;
            }

            /*
             * Try to route the packet to a connected router.
             */
            private boolean tryRouteToRouter(CFeatureHolder netInterface, Packet packet) {

                if (netInterface.getObj(CONNECTION) == null) {
                    // Network interface is not connected to a router
                    return false;
                }

                netInterface.getObj(CONNECTION).invoke(RouterNetInterface.PROCESS, packet);
                return true;
            }

        });

    }

}
