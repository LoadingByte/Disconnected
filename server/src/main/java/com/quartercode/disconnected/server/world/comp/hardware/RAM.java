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

package com.quartercode.disconnected.server.world.comp.hardware;

import static com.quartercode.classmod.factory.ClassmodFactory.factory;
import com.quartercode.classmod.extra.prop.PropertyDefinition;
import com.quartercode.classmod.extra.storage.StandardStorage;
import com.quartercode.classmod.factory.PropertyDefinitionFactory;
import com.quartercode.disconnected.server.world.comp.hardware.Mainboard.NeedsMainboardSlot;

/**
 * This class represents a ram module of a computer.
 * A ram module has a size (given in bytes) and an access frequency (given in hertz).
 * 
 * @see Hardware
 */
@NeedsMainboardSlot
public class RAM extends Hardware {

    // ----- Properties -----

    /**
     * The size of the ram module, given in bytes.
     */
    public static final PropertyDefinition<Long> SIZE;

    /**
     * The access frequency of the ram module, given in hertz.
     */
    public static final PropertyDefinition<Long> FREQUENCY;

    static {

        SIZE = factory(PropertyDefinitionFactory.class).create("size", new StandardStorage<>());
        FREQUENCY = factory(PropertyDefinitionFactory.class).create("frequency", new StandardStorage<>());

    }

}
