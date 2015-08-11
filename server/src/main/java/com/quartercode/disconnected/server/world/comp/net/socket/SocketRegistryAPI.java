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

package com.quartercode.disconnected.server.world.comp.net.socket;

import java.util.List;
import com.quartercode.disconnected.shared.world.comp.net.Address;

public interface SocketRegistryAPI {

    /**
     * Returns all the {@link Socket}s which were {@link #createSocket(Address, int) created} using the network module.
     * Note that this list might contain some sockets whose {@link Socket#connect()} function has not yet been called.
     * Those sockets are {@link SocketState#INACTIVE inactive} at the moment.
     * However, it never contains sockets which have already been {@link Socket#disconnect() disconnected}.
     * Those sockets are automatically removed from the list.
     *
     * @return The sockets which were created using the network module.
     */
    public List<Socket> getSockets();

    /**
     * Returns all registered {@link SocketConnectionListener}s; they are notified when another computer would like to establish a {@link Socket} connection.
     * However, connection listeners are not pure observers. For example, they are also able to prohibit new connections.
     *
     * @return All registered socket connection listeners.
     */
    public List<SocketConnectionListener> getConnectionListeners();

    /**
     * Registers a new {@link SocketConnectionListener} which will be notified when another computer tries to establish a {@link Socket} connection.
     * See {@link #getConnectionListeners()} for more information on connection listeners.
     *
     * @param connectionListener The socket connection listener which should be registered.
     */
    public void addConnectionListener(SocketConnectionListener connectionListener);

    /**
     * Unregisters the given {@link SocketConnectionListener}, so that it will no longer be notified when another computer tries to establish a {@link Socket} connection.
     * See {@link #getConnectionListeners()} for more information on connection listeners.
     *
     * @param connectionListener The socket connection listener which should be unregistered.
     */
    public void removeConnectionListener(SocketConnectionListener connectionListener);

    /**
     * Creates a new {@link Socket} which can establish a connection between the parent computer and the given {@link Address}.
     * The {@link Socket#getLocalPort() local port} of the socket is randomly chosen.
     * After creation, the new socket must be configured, e.g. by adding {@link Socket#getPacketHandlers() packet handlers} to it.
     * Finally, it should be started up with {@link Socket#connect()}.
     *
     * @param destination The address of the socket on the destination computer the new socket should send packets to and receive packets from.
     * @return The newly created and nearly fully functional socket.
     *         Don't forget to configure and start up the socket after creation.
     */
    public Socket createSocket(Address destination);

    /**
     * Creates a new {@link Socket} which can establish a connection between the given local port on parent computer and the given {@link Address}.
     * In contrast to {@link #createSocket(Address)}, the {@link Socket#getLocalPort() local port} of the socket can be manually chosen.
     * After creation, the new socket must be configured, e.g. by adding {@link Socket#getPacketHandlers() packet handlers} to it.
     * Finally, it should be started up with {@link Socket#connect()}.
     *
     * @param destination The address of the socket on the destination computer the new socket should send packets to and receive packets from.
     * @param localPort The {@link #getLocalPort() local port} the socket should be bound to.
     *        If you want the OS to choose a random free port for the new socket, use {@link #createSocket(Address)} instead of this method.
     * @return The newly created and nearly fully functional socket.
     *         Don't forget to configure and start up the socket after creation.
     * @throws IllegalArgumentException If the provided local port is neither {@code -1} (random free port) nor in range {@code 1 <= port <= 65535}.
     * @throws IllegalStateException If the provided combination of "localPort" and "destination" is already used by another socket.
     */
    public Socket createSocket(Address destination, int localPort);

}
