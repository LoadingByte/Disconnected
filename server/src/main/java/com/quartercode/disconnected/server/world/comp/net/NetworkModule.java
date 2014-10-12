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

import static com.quartercode.classmod.ClassmodFactory.create;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.reflect.TypeLiteral;
import com.quartercode.classmod.extra.CFeatureHolder;
import com.quartercode.classmod.extra.CollectionPropertyDefinition;
import com.quartercode.classmod.extra.FunctionDefinition;
import com.quartercode.classmod.extra.FunctionExecutor;
import com.quartercode.classmod.extra.FunctionInvocation;
import com.quartercode.classmod.extra.Prioritized;
import com.quartercode.classmod.extra.storage.StandardStorage;
import com.quartercode.classmod.extra.valuefactory.CloneValueFactory;
import com.quartercode.disconnected.server.util.RandomPool;
import com.quartercode.disconnected.server.world.comp.Computer;
import com.quartercode.disconnected.server.world.comp.hardware.Hardware;
import com.quartercode.disconnected.server.world.comp.hardware.NodeNetInterface;
import com.quartercode.disconnected.server.world.comp.net.Socket.SocketState;
import com.quartercode.disconnected.server.world.comp.net.SocketConnectionListener.ConnectionAllowance;
import com.quartercode.disconnected.server.world.comp.os.OSModule;
import com.quartercode.disconnected.server.world.comp.os.OperatingSystem;

/**
 * This class represents an {@link OperatingSystem} module which is used to send and receive network {@link Packet}s.
 * The module also abstracts the concept of {@link Packet}s and introduces {@link Socket}s for easier data transfer.
 * It is an essential part of the {@link OperatingSystem} and is directly used by it.
 * 
 * @see Packet
 * @see Socket
 * @see OSModule
 * @see OperatingSystem
 */
public class NetworkModule extends OSModule {

    // ----- Properties -----

    /**
     * The {@link Socket}s that were created by the network module through the {@link #CREATE_SOCKET} function.
     * Note that this list might contain some sockets whose {@link Socket#CONNECT} function has not yet been called.
     * However, it never contains sockets whose {@link Socket#DISCONNECT} function has been called.
     */
    public static final CollectionPropertyDefinition<Socket, List<Socket>>                                     SOCKETS;

    /**
     * A list of {@link SocketConnectionListener} that are called when another computer would like to set up a {@link Socket} connection.
     * They can prevent the creation of the connection as well as be notified when the connection was established successfully.
     */
    public static final CollectionPropertyDefinition<SocketConnectionListener, List<SocketConnectionListener>> CONNECTION_LISTENERS;

    static {

        SOCKETS = create(new TypeLiteral<CollectionPropertyDefinition<Socket, List<Socket>>>() {}, "name", "sockets", "storage", new StandardStorage<>(), "collection", new CloneValueFactory<>(new ArrayList<>()));
        CONNECTION_LISTENERS = create(new TypeLiteral<CollectionPropertyDefinition<SocketConnectionListener, List<SocketConnectionListener>>>() {}, "name", "connectionListeners", "storage", new StandardStorage<>(), "collection", new CloneValueFactory<>(new ArrayList<>()));

    }

    // ----- Functions -----

    /**
     * Creates a new {@link Socket} that can send packets from the parent computer.
     * After creation, the new socket must be configured and then started up with {@link Socket#CONNECT}.
     */
    public static final FunctionDefinition<Socket>                                                             CREATE_SOCKET;

    /**
     * This is an internal function that sends a new {@link Packet} with the given data object over the network.
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
     * <td>{@link Socket}</td>
     * <td>socket</td>
     * <td>The socket which called the function and likes to send the packet.</td>
     * </tr>
     * <tr>
     * <td>1</td>
     * <td>{@link Object}</td>
     * <td>data</td>
     * <td>The data object which should be put into {@link Packet#DATA}.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>                                                               SEND;

    /**
     * This is an internal function that delivers the given {@link Packet} to the {@link Socket} which has opened the connection.
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
     * <td>{@link Packet}</td>
     * <td>packet</td>
     * <td>The packet which should be delivered to its destination socket.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>                                                               HANDLE;

    static {

        CREATE_SOCKET = create(new TypeLiteral<FunctionDefinition<Socket>>() {}, "name", "createSocket", "parameters", new Class[0]);
        CREATE_SOCKET.addExecutor("default", NetworkModule.class, new FunctionExecutor<Socket>() {

            @Override
            public Socket invoke(FunctionInvocation<Socket> invocation, Object... arguments) {

                NetworkModule holder = (NetworkModule) invocation.getCHolder();

                Socket socket = new Socket();
                // Set the local port to 0 (random) so applications don't have to do that
                socket.setObj(Socket.LOCAL_PORT, 0);
                holder.addCol(SOCKETS, socket);

                invocation.next(arguments);
                return socket;
            }

        });

        // Add a listener to the Socket.CONNECT function that generates a free local port if that is requested by the socket
        Socket.CONNECT.addExecutor("generateLocalPort", Socket.class, new FunctionExecutor<Void>() {

            @Override
            @Prioritized (Prioritized.LEVEL_7 + Prioritized.SUBLEVEL_5)
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                Socket holder = (Socket) invocation.getCHolder();

                if (holder.getObj(Socket.LOCAL_PORT) == 0) {
                    holder.setObj(Socket.LOCAL_PORT, getFreePort(holder));
                }

                return invocation.next(arguments);
            }

            /*
             * Returns a port which is not yet used by another socket.
             * Note that this method doesn't consider the socket destinations for allocating ports multiple times because that would be too expensive.
             */
            private int getFreePort(Socket holder) {

                RandomPool random = holder.getRandom();
                int minPort = 49152;
                int maxPort = 65535;

                int port;
                do {
                    port = random.nextInt(maxPort + 1 - minPort) + minPort;
                } while (!isPortFree(holder, port));

                return port;
            }

            // Returns whether the given port is already used by another socket
            private boolean isPortFree(CFeatureHolder holder, int port) {

                for (Socket socket : holder.getCol(SOCKETS)) {
                    if (socket.getObj(Socket.LOCAL_PORT) == port) {
                        return false;
                    }
                }

                return true;
            }

        });

        // Add a listener to the Socket.CONNECT function that checks whether the connection the socket wants to establish is free
        Socket.CONNECT.addExecutor("checkFree", Socket.class, new FunctionExecutor<Void>() {

            @Override
            @Prioritized (Prioritized.LEVEL_7)
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                Socket holder = (Socket) invocation.getCHolder();
                int localPort = holder.getObj(Socket.LOCAL_PORT);
                Address destination = holder.getObj(Socket.DESTINATION);

                for (Socket socket : holder.getParent().getCol(SOCKETS)) {
                    if (socket.getObj(Socket.LOCAL_PORT) == localPort && socket.getObj(Socket.DESTINATION).equals(destination)) {
                        throw new IllegalStateException("Socket with local port '" + localPort + "' and destination '" + destination.invoke(Address.TO_STRING) + "' is already bound");
                    }
                }

                return invocation.next(arguments);
            }

        });

        // Add a listener to the Socket.DISCONNECT function that removes the socket from the list
        Socket.DISCONNECT.addExecutor("removeFromNetModule", Socket.class, new FunctionExecutor<Void>() {

            @Override
            @Prioritized (Prioritized.LEVEL_2)
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                Socket holder = (Socket) invocation.getCHolder();

                if (holder.getParent() != null) {
                    holder.getParent().removeCol(SOCKETS, holder);
                }

                return invocation.next(arguments);
            }

        });

        // Add a setter listener to the Socket.STATE property that notifies all connection listeners if the state of a socket changes to CONNECTED
        Socket.STATE.addSetterExecutor("notifySocketConnectionListeners", Socket.class, new FunctionExecutor<Void>() {

            @Override
            @Prioritized (Prioritized.LEVEL_8)
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                Socket holder = (Socket) invocation.getCHolder();
                boolean stateChangesToConnected = arguments[0] == SocketState.CONNECTED && holder.getObj(Socket.STATE) != SocketState.CONNECTED;

                invocation.next(arguments);

                if (stateChangesToConnected) {
                    for (SocketConnectionListener connectionListener : holder.getCol(CONNECTION_LISTENERS)) {
                        connectionListener.established(holder);
                    }
                }

                return null;
            }

        });

        SEND = create(new TypeLiteral<FunctionDefinition<Void>>() {}, "name", "send", "parameters", new Class[] { Socket.class, Object.class });
        SEND.addExecutor("default", NetworkModule.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                NetworkModule holder = (NetworkModule) invocation.getCHolder();
                Socket socket = (Socket) arguments[0];
                Object data = arguments[1];

                // Retrieve the network interface the packet will be sent by
                NodeNetInterface netInterface = getNetInterface(holder);

                // Construct the address of the sending socket
                Address sourceAddress = new Address();
                sourceAddress.setObj(Address.NET_ID, netInterface.getObj(NodeNetInterface.NET_ID));
                sourceAddress.setObj(Address.PORT, socket.getObj(Socket.LOCAL_PORT));

                // Construct a new packet
                Packet packet = new Packet();
                packet.setObj(Packet.SOURCE, sourceAddress);
                packet.setObj(Packet.DESTINATION, socket.getObj(Socket.DESTINATION));
                packet.setObj(Packet.DATA, data);

                // Send the packet
                netInterface.invoke(NodeNetInterface.PROCESS, packet);

                return invocation.next(arguments);
            }

            private NodeNetInterface getNetInterface(NetworkModule holder) {

                Computer computer = holder.getParent().getParent();
                for (Hardware hardware : computer.getCol(Computer.HARDWARE)) {
                    if (hardware instanceof NodeNetInterface) {
                        return (NodeNetInterface) hardware;
                    }
                }

                return null;
            }

        });

        HANDLE = create(new TypeLiteral<FunctionDefinition<Void>>() {}, "name", "handle", "parameters", new Class[] { Packet.class });
        HANDLE.addExecutor("default", NetworkModule.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                CFeatureHolder holder = invocation.getCHolder();
                Packet packet = (Packet) arguments[0];
                Address packetSource = packet.getObj(Packet.SOURCE);
                Address packetDestination = packet.getObj(Packet.DESTINATION);
                int packetDestinationPort = packetDestination.getObj(Address.PORT);

                // Find the socket the packet was sent to
                Socket responsibleSocket = null;
                for (Socket socket : holder.getCol(SOCKETS)) {
                    if (socket.getObj(Socket.LOCAL_PORT) == packetDestinationPort && socket.getObj(Socket.DESTINATION).equals(packetSource)) {
                        responsibleSocket = socket;
                        break;
                    }
                }

                // Create a new socket if the destination of the packet is not yet bound
                // Note that this creation must be allowed by the connection listeners
                if (responsibleSocket == null) {
                    responsibleSocket = tryCreateSocket(holder, packetSource, packetDestinationPort);
                }

                // Hand the packet over to the socket so it can handle it
                if (responsibleSocket != null) {
                    responsibleSocket.invoke(Socket.HANDLE, packet);
                }

                return invocation.next(arguments);
            }

            private Socket tryCreateSocket(CFeatureHolder holder, Address requestor, int localPort) {

                // Iterate over the opinions of all connection listeners and
                boolean allowAfterAll = false;
                for (SocketConnectionListener connectionListener : holder.getCol(CONNECTION_LISTENERS)) {
                    ConnectionAllowance allowance = connectionListener.allow(requestor, localPort);
                    if (allowance == ConnectionAllowance.ALLOW_AFTER_ALL) {
                        allowAfterAll = true;
                    } else if (allowance == ConnectionAllowance.REJECT_IMMEDIATELY) {
                        return null;
                    }
                }
                if (!allowAfterAll) {
                    return null;
                }

                // If the socket creation is allowed, create the socket
                Socket socket = new Socket();
                socket.setObj(Socket.LOCAL_PORT, localPort);
                socket.setObj(Socket.DESTINATION, requestor);
                holder.addCol(SOCKETS, socket);
                return socket;
            }

        });

        SET_RUNNING.addExecutor("disconnectSockets", NetworkModule.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                // Only invoke on shutdown
                if (!(Boolean) arguments[0]) {
                    for (Socket socket : invocation.getCHolder().getCol(SOCKETS)) {
                        socket.invoke(Socket.DISCONNECT);
                    }
                }

                return invocation.next(arguments);
            }

        });

    }

}
