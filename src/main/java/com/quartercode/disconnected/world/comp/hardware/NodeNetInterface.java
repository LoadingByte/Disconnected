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
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.reflect.TypeLiteral;
import com.quartercode.classmod.base.FeatureHolder;
import com.quartercode.classmod.extra.FunctionExecutor;
import com.quartercode.classmod.extra.FunctionInvocation;
import com.quartercode.classmod.extra.Prioritized;
import com.quartercode.classmod.extra.PropertyDefinition;
import com.quartercode.classmod.extra.storage.ReferenceStorage;
import com.quartercode.classmod.extra.storage.StandardStorage;
import com.quartercode.disconnected.world.comp.Computer;
import com.quartercode.disconnected.world.comp.hardware.Mainboard.NeedsMainboardSlot;
import com.quartercode.disconnected.world.comp.net.Address;
import com.quartercode.disconnected.world.comp.net.NetID;
import com.quartercode.disconnected.world.comp.net.NetworkModule;
import com.quartercode.disconnected.world.comp.net.Packet;
import com.quartercode.disconnected.world.comp.net.PacketProcessor;
import com.quartercode.disconnected.world.comp.os.OperatingSystem;

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
                RouterNetInterface connection = invocation.getHolder().get(CONNECTION).get();

                if (netId != null && connection != null) {
                    int newSubnet = netId.get(NetID.SUBNET).get();
                    int conSubnet = connection.get(RouterNetInterface.SUBNET).get();

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

                FeatureHolder holder = invocation.getHolder();

                RouterNetInterface oldConnection = holder.get(CONNECTION).get();
                if (oldConnection == null || !oldConnection.equals(arguments[0])) {
                    holder.get(NET_ID).set(null);
                }

                return invocation.next(arguments);
            }

        });
        CONNECTION.addSetterExecutor("addReverseConnection", NodeNetInterface.class, new FunctionExecutor<Void>() {

            @Override
            @Prioritized (Prioritized.LEVEL_3)
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                if (arguments[0] != null) {
                    NodeNetInterface holder = (NodeNetInterface) invocation.getHolder();
                    RouterNetInterface connection = (RouterNetInterface) arguments[0];

                    if (!connection.get(RouterNetInterface.CHILDREN).get().contains(holder)) {
                        connection.get(RouterNetInterface.CHILDREN).add(holder);
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
                    NodeNetInterface holder = (NodeNetInterface) invocation.getHolder();
                    RouterNetInterface oldConnection = holder.get(CONNECTION).get();

                    if (oldConnection != null && oldConnection.get(RouterNetInterface.CHILDREN).get().contains(holder)) {
                        oldConnection.get(RouterNetInterface.CHILDREN).remove(holder);
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

                NodeNetInterface holder = (NodeNetInterface) invocation.getHolder();
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

                NetID destination = packet.get(Packet.DESTINATION).get().get(Address.NET_ID).get();

                if (!destination.equals(netInterface.get(NET_ID).get())) {
                    // Packet destination is not this network interface
                    return false;
                }

                // Only deliver the packet if the net interface is connected to a computer
                if (netInterface.getParent() != null) {
                    NetworkModule netModule = netInterface.getParent().get(Computer.OS).get().get(OperatingSystem.NET_MODULE).get();
                    netModule.get(NetworkModule.HANDLE).invoke(packet);
                }

                return true;
            }

            /*
             * Try to route the packet to a connected router.
             */
            private boolean tryRouteToRouter(FeatureHolder netInterface, Packet packet) {

                if (netInterface.get(CONNECTION).get() == null) {
                    // Network interface is not connected to a router
                    return false;
                }

                netInterface.get(CONNECTION).get().get(RouterNetInterface.PROCESS).invoke(packet);
                return true;
            }

        });

    }

}
