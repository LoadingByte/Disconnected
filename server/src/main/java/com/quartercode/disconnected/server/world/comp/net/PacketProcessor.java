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
import com.quartercode.classmod.base.FeatureHolder;
import com.quartercode.classmod.extra.FunctionDefinition;

/**
 * This class represents a "thing" that can take {@link Packet}s and do something with them.
 * For example, network interfaces and backbones are packet processors.
 * 
 * @see Packet
 */
public interface PacketProcessor extends FeatureHolder {

    /**
     * Processes the given {@link Packet} in terms of the packet processor context.
     * This method could resend the packet, or it could redirect the packet to a parent computer.
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
     * <td>The packet that should be handled by the packet processor.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void> PROCESS = create(new TypeLiteral<FunctionDefinition<Void>>() {}, "name", "process", "parameters", new Class[] { Packet.class });

}
