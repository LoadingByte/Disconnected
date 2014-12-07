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
import org.apache.commons.lang3.reflect.TypeLiteral;
import com.quartercode.classmod.extra.conv.CFeatureHolder;
import com.quartercode.classmod.extra.func.FunctionDefinition;
import com.quartercode.disconnected.shared.util.XmlPersistent;

/**
 * A packet handle is a simple functional class which is called when a {@link Packet} arrives at a {@link Socket}.
 * However, the handler only receives the carried data object and no further metadata.<br>
 * <br>
 * Sadly, this class must be abstract since JAXB can't handle interfaces.
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
    public static final FunctionDefinition<Void> HANDLE = create(new TypeLiteral<FunctionDefinition<Void>>() {}, "name", "handle", "parameters", new Class[] { Socket.class, Object.class });

}