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
import com.quartercode.disconnected.server.world.WorldFeatureHolder;
import com.quartercode.disconnected.server.world.comp.SizeUtil;
import com.quartercode.disconnected.server.world.comp.SizeUtil.DerivableSize;
import com.quartercode.disconnected.shared.comp.net.Address;

/**
 * This class represents a generic packet which can be sent to a network interface.
 * Every packet contains a destination {@link Address} and the data payload {@link Object} which should be sent.
 * Note that no source address is stored. That means that generic packets must not be sent from a network interface.
 * Such functionality can be added by subclasses, e.g. {@link TCPPacket}.
 * 
 * @see TCPPacket
 */
public class Packet extends WorldFeatureHolder implements DerivableSize {

    // ----- Properties -----

    /**
     * The network {@link Address} of the {@link Socket} the packet is sent to.
     */
    public static final PropertyDefinition<Address> DESTINATION;

    /**
     * The data payload {@link Object} which is sent.
     * The payload shouldn't be modified after construction.
     */
    public static final PropertyDefinition<Object>  DATA;

    static {

        DESTINATION = create(new TypeLiteral<PropertyDefinition<Address>>() {}, "name", "destination", "storage", new StandardStorage<>());
        DATA = create(new TypeLiteral<PropertyDefinition<Object>>() {}, "name", "data", "storage", new StandardStorage<>());

    }

    // ----- Functions -----

    static {

        GET_SIZE.addExecutor("data", Packet.class, SizeUtil.createGetSize(DATA));

    }

}
