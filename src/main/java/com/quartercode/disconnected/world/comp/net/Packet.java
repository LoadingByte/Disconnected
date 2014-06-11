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

package com.quartercode.disconnected.world.comp.net;

import static com.quartercode.classmod.ClassmodFactory.create;
import org.apache.commons.lang3.reflect.TypeLiteral;
import com.quartercode.classmod.base.def.DefaultFeatureHolder;
import com.quartercode.classmod.extra.PropertyDefinition;
import com.quartercode.classmod.extra.storage.StandardStorage;
import com.quartercode.disconnected.world.comp.SizeUtil;
import com.quartercode.disconnected.world.comp.SizeUtil.DerivableSize;

/**
 * This class represents a packet which can be sent between network interfaces.
 * A packet contains a source, a destination (both represented by addresses) and a data payload {@link Object} which should be sent.
 */
public class Packet extends DefaultFeatureHolder implements DerivableSize {

    // ----- Properties -----

    /**
     * The network {@link Address} which sends the packet.
     */
    public static final PropertyDefinition<Address> SOURCE;

    /**
     * The network {@link Address} the packet should be sent to.
     */
    public static final PropertyDefinition<Address> DESTINATION;

    /**
     * The data payload {@link Object} which should be sent.
     * The payload can't be modified after construction.
     */
    public static final PropertyDefinition<Object>  DATA;

    static {

        SOURCE = create(new TypeLiteral<PropertyDefinition<Address>>() {}, "name", "source", "storage", new StandardStorage<>());
        DESTINATION = create(new TypeLiteral<PropertyDefinition<Address>>() {}, "name", "destination", "storage", new StandardStorage<>());
        DATA = create(new TypeLiteral<PropertyDefinition<Object>>() {}, "name", "data", "storage", new StandardStorage<>());

    }

    // ----- Functions -----

    static {

        GET_SIZE.addExecutor("data", Packet.class, SizeUtil.createGetSize(DATA));

    }

    /**
     * Creates a new packet.
     */
    public Packet() {

    }

}
