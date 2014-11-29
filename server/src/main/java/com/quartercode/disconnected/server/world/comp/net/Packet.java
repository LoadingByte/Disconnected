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
import com.quartercode.disconnected.server.world.util.DerivableSize;
import com.quartercode.disconnected.server.world.util.SizeUtils;
import com.quartercode.disconnected.server.world.util.WorldFeatureHolder;
import com.quartercode.disconnected.shared.world.comp.net.Address;

/**
 * This class represents a packet which can be sent to a network interface.
 * Every packet contains the source and destination {@link Address}es, the used protocol and the data payload {@link Object} which should be sent.
 * 
 * @see Address
 */
public class Packet extends WorldFeatureHolder implements DerivableSize {

    // ----- Properties -----

    /**
     * The network {@link Address} of the {@link Socket} which sent the packet.
     */
    public static final PropertyDefinition<Address> SOURCE;

    /**
     * The network {@link Address} of the {@link Socket} the packet is sent to.
     */
    public static final PropertyDefinition<Address> DESTINATION;

    /**
     * A string that identifies the protocol the packet was sent with.
     * By default, {@code TCP} and {@code UDP} are allowed.
     */
    public static final PropertyDefinition<String>  PROTOCOL;

    /**
     * The data payload {@link Object} which is sent.
     * The payload shouldn't be modified after construction.
     */
    public static final PropertyDefinition<Object>  DATA;

    static {

        SOURCE = create(new TypeLiteral<PropertyDefinition<Address>>() {}, "name", "source", "storage", new StandardStorage<>());
        DESTINATION = create(new TypeLiteral<PropertyDefinition<Address>>() {}, "name", "destination", "storage", new StandardStorage<>());
        PROTOCOL = create(new TypeLiteral<PropertyDefinition<String>>() {}, "name", "protocol", "storage", new StandardStorage<>());
        DATA = create(new TypeLiteral<PropertyDefinition<Object>>() {}, "name", "data", "storage", new StandardStorage<>());

    }

    // ----- Functions -----

    static {

        GET_SIZE.addExecutor("data", Packet.class, SizeUtils.createGetSize(DATA));

    }

}
