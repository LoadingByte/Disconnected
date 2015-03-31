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

package com.quartercode.disconnected.server.world.comp.net;

import static com.quartercode.classmod.factory.ClassmodFactory.factory;
import com.quartercode.classmod.extra.conv.CFeatureHolder;
import com.quartercode.classmod.extra.func.FunctionDefinition;
import com.quartercode.classmod.factory.FunctionDefinitionFactory;
import com.quartercode.disconnected.shared.util.XmlPersistent;

/**
 * A packet handle is a simple functional class which is called when a {@link Packet} arrives at a {@link Socket}.
 * However, the handler only receives the carried data object and no further metadata.<br>
 * <br>
 * Actually, the {@link Socket#PACKET_HANDLERS packet handlers of a socket} are called when scheduler invokes the group {@code computerProgramUpdate}.
 * When that happens, all queued packets are polled from the {@link Socket#INCOMING_PACKET_QUEUE} and handed over to the handlers.
 * 
 * @see Socket
 * @see Packet
 */
@XmlPersistent
public interface PacketHandler extends CFeatureHolder {

    /**
     * This method is called when a {@link Packet} with the given data object arrives at the given {@link Socket}.
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
     * <td>The socket which received the given packet. Most of the times, this is the socket the packet handler was added to.</td>
     * </tr>
     * <tr>
     * <td>1</td>
     * <td>{@link Object}</td>
     * <td>data</td>
     * <td>The data object that was carried by the received packet.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void> HANDLE = factory(FunctionDefinitionFactory.class).create("handle", new Class[] { Socket.class, Object.class });

}
