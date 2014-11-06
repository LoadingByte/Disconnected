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
import com.quartercode.classmod.extra.PropertyDefinition;
import com.quartercode.classmod.extra.storage.StandardStorage;
import com.quartercode.disconnected.shared.world.comp.net.Address;

/**
 * This class represents a {@link Packet} which is sent from one network interface to another using the tcp protocol.
 * That means that the packet also contains the source {@link Address} of the network interface it was sent from.
 * 
 * @see Packet
 */
public class TCPPacket extends Packet {

    // ----- Properties -----

    /**
     * The network {@link Address} of the {@link Socket} which sent the packet.
     */
    public static final PropertyDefinition<Address> SOURCE;

    static {

        SOURCE = create(new TypeLiteral<PropertyDefinition<Address>>() {}, "name", "source", "storage", new StandardStorage<>());

    }

}
