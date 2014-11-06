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

import com.quartercode.disconnected.shared.world.comp.net.Address;

/**
 * A stream socket connection listener is called when another computer would like to set up a {@link Socket} connection.
 * It can prevent the creation of the connection as well as be notified when the connection was established successfully.<br>
 * <br>
 * Sadly, this class must be abstract since JAXB can't handle interfaces.
 * 
 * @see NetworkModule#CONNECTION_LISTENERS
 */
public abstract class SocketConnectionListener {

    /**
     * This enumeration defines the different results the {@link SocketConnectionListener#allow(Address, int)} method can return.
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

    /**
     * Returns the position of the listener on whether the socket under the given {@link Address} is allowed to build a {@link Socket} connection.
     * See {@link ConnectionAllowance} for more information on the different positions the listener can take.
     * If the connection request is allowed, the {@link #established(Socket)} will be called with the created socket after some time.
     * 
     * @param requestor The address of the socket/service which wants to build a connection.
     * @param localPort The local port the request is sent to.
     *        If the connection request is allowed, it is used for the new socket.
     * @return A connection allowance entry that controls the whether the building of the connection is allowed.
     *         See {@link ConnectionAllowance} for more information on the usage.
     */
    public abstract ConnectionAllowance allow(Address requestor, int localPort);

    /**
     * This method is called when a new {@link Socket} connection was successfully established.
     * Note that {@link #allow(Address, int)} has been called before this method.
     * 
     * @param socket The stream socket which was built in order to represent the established connection.
     */
    public abstract void established(Socket socket);

}
