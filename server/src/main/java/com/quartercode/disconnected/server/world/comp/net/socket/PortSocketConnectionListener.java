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

import javax.xml.bind.annotation.XmlAttribute;
import com.quartercode.disconnected.server.world.comp.net.NetworkModule;
import com.quartercode.disconnected.server.world.util.WorldNode;
import com.quartercode.disconnected.shared.world.comp.net.Address;

/**
 * A special {@link SocketConnectionListener} that only listens for calls in which the request/socket is assigned to a certain local port.
 * It is useful for service programs that only listen to one specific port.
 * For example, a webserver would only listen to the port {@code 80}.
 */
public abstract class PortSocketConnectionListener extends WorldNode<NetworkModule> implements SocketConnectionListener {

    @XmlAttribute
    private int localPort;

    // JAXB constructor
    protected PortSocketConnectionListener() {

    }

    /**
     * Creates a new port socket connection listener.
     *
     * @param localPort The {@link Socket#getLocalPort() local port} all socket requests/connections must be "assigned" to for being forwarded to the listener methods of this class.
     *        Effectively, the listener only listens for calls on this local port.
     */
    public PortSocketConnectionListener(int localPort) {

        this.localPort = localPort;
    }

    /**
     * Returns the {@link Socket#getLocalPort() local port} all socket requests/connections must be "assigned" to for being forwarded to the listener methods of this class.
     * Effectively, the listener only listens for calls on this local port.
     *
     * @return The local port all handled {@link Socket} requests/connections must be "assigned" to.
     */
    public int getLocalPort() {

        return localPort;
    }

    @Override
    public final ConnectionAllowance onRequest(Address requester, int localPort) {

        if (localPort == this.localPort) {
            return doOnRequest(requester);
        } else {
            return ConnectionAllowance.SKIP_TO_NEXT;
        }
    }

    @Override
    public final void onEstablish(Socket socket) {

        if (socket.getLocalPort() == localPort) {
            doOnEstablish(socket);
        }
    }

    /**
     * Returns the opinion of the listener on whether the socket under the given {@link Address} is allowed to build a {@link Socket} connection.
     * See {@link ConnectionAllowance} for more information on the different opinions the listener can take.
     * If the connection request is allowed, {@link #onEstablish(Socket)} will be called with the created socket after some time.
     * Note that this method is only fed with socket request to the set {@link #getLocalPort() local port} of the listener.
     *
     * @param requester The address of the socket/service which wants to build a connection.
     * @return Whether the socket under the given {@link Address} should be allowed to build a {@link Socket} connection.
     */
    public abstract ConnectionAllowance doOnRequest(Address requester);

    /**
     * This method is called when a new {@link Socket} connection was successfully established.
     * Note that {@link #onRequest(Address, int)} must have been called with the data of the new socket before this method.
     * Therefore, this method is only fed with socket connections to the set {@link #getLocalPort() local port} of the listener.
     *
     * @param socket The socket which was built in order to represent the established connection.
     */
    public abstract void doOnEstablish(Socket socket);

}
