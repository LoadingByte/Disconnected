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

import com.quartercode.disconnected.server.world.comp.net.NetworkModule;
import com.quartercode.disconnected.shared.world.comp.net.Address;

/**
 * A socket connection listener is notified when another computer would like to establish a {@link Socket} connection.
 * However, a connection listener is not a pure observer. For example, it is also able to prohibit new connections.
 *
 * @see NetworkModule#getConnectionListeners()
 */
public interface SocketConnectionListener {

    /**
     * Returns the opinion of the listener on whether the socket under the given {@link Address} is allowed to build a {@link Socket} connection.
     * See {@link ConnectionAllowance} for more information on the different opinions the listener can take.
     * If the connection request is allowed, {@link #onEstablish(Socket)} will be called with the created socket after some time.
     *
     * @param requester The address of the socket/service which wants to build a connection.
     * @param localPort The local port the request is sent to. If the connection request is allowed, it is used for the new socket.
     * @return Whether the socket under the given {@link Address} should be allowed to build a {@link Socket} connection.
     */
    public ConnectionAllowance onRequest(Address requester, int localPort);

    /**
     * This method is called when a new {@link Socket} connection was successfully established.
     * Note that {@link #onRequest(Address, int)} must have been called with the data of the new socket before this method.
     *
     * @param socket The socket which was built in order to represent the established connection.
     */
    public void onEstablish(Socket socket);

    /**
     * This enumeration defines the different results the {@link SocketConnectionListener#onEstablish(Socket)} method can return.
     * The combination of all allowance results of all connection listeners defines whether a {@link Socket} connection is allowed or not.<br>
     * <br>
     * Here're some examples for a connection request passing through some listeners:
     *
     * <pre>
     * Listener 1: {@link #SKIP_TO_NEXT}
     * Listener 2: {@link #SKIP_TO_NEXT}
     * Listener 3: {@link #SKIP_TO_NEXT}
     * &gt; Reject (no listener allowed the request)
     *
     * Listener 1: {@link #SKIP_TO_NEXT}
     * Listener 2: {@link #ALLOW_AFTER_ALL}
     * Listener 3: {@link #SKIP_TO_NEXT}
     * &gt; Allow (listener 2 allowed the request)
     *
     * Listener 1: {@link #ALLOW_AFTER_ALL}
     * Listener 2: {@link #REJECT_IMMEDIATELY}
     * &gt; Reject (listener 2 has explicitly rejected the request so listener 3 wasn't asked at all)
     * </pre>
     */
    public static enum ConnectionAllowance {

        /**
         * Don't change the allowance for the connection request and continue with the next listener.
         * That means that the listener doesn't care whether the request is allowed or not.
         */
        SKIP_TO_NEXT,
        /**
         * Allow the connection request if it passes through all listeners of which no one returns {@link #REJECT_IMMEDIATELY}.
         * That means that the listener explicitly wants the request to be allowed.
         */
        ALLOW_AFTER_ALL,
        /**
         * Immediately reject the connection request and don't ask anymore listeners.
         * That means that the listener explicitly wants the request to be rejected.
         */
        REJECT_IMMEDIATELY;

    }

}
