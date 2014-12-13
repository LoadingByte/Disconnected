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

import static com.quartercode.classmod.factory.ClassmodFactory.factory;
import com.quartercode.classmod.extra.conv.CFeatureHolder;
import com.quartercode.classmod.extra.func.FunctionDefinition;
import com.quartercode.classmod.factory.FunctionDefinitionFactory;
import com.quartercode.disconnected.shared.util.XmlPersistent;
import com.quartercode.disconnected.shared.world.comp.net.Address;

/**
 * A stream socket connection listener is called when another computer would like to set up a {@link Socket} connection.
 * It can prevent the creation of the connection as well as be notified when the connection was successfully established.
 * 
 * @see NetModule#CONNECTION_LISTENERS
 */
@XmlPersistent
public interface SocketConnectionListener extends CFeatureHolder {

    /**
     * This enumeration defines the different results the {@link SocketConnectionListener#ON_ESTABLISH} method can return.
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
     * Returns the opinion of the listener on whether the socket under the given {@link Address} is allowed to build a {@link Socket} connection.
     * See {@link ConnectionAllowance} for more information on the different opinions the listener can take.
     * If the connection request is allowed, {@link #ON_ESTABLISH} will be called with the created socket after some time.
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
     * <td>{@link Address}</td>
     * <td>requester</td>
     * <td>The address of the socket/service which wants to build a connection.</td>
     * </tr>
     * <tr>
     * <td>1</td>
     * <td>{@link Integer}</td>
     * <td>localPort</td>
     * <td>The local port the request is sent to. If the connection request is allowed, it is used for the new socket.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<ConnectionAllowance> ON_REQUEST   = factory(FunctionDefinitionFactory.class).create("onRequest", new Class[] { Address.class, Integer.class });

    /**
     * This method is called when a new {@link Socket} connection was successfully established.
     * Note that {@link #ON_REQUEST} must have been called before this method.
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
     * <td>The stream socket which was built in order to represent the established connection.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>                ON_ESTABLISH = factory(FunctionDefinitionFactory.class).create("onEstablish", new Class[] { Socket.class });

}
