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

package com.quartercode.disconnected.world.comp.hardware;

import java.util.HashSet;
import java.util.Set;
import com.quartercode.classmod.extra.CollectionPropertyDefinition;
import com.quartercode.classmod.extra.PropertyDefinition;
import com.quartercode.classmod.extra.def.ObjectCollectionProperty;
import com.quartercode.classmod.extra.def.ObjectProperty;
import com.quartercode.disconnected.world.WorldChildFeatureHolder;
import com.quartercode.disconnected.world.comp.Computer;
import com.quartercode.disconnected.world.comp.Vulnerability;

/**
 * This class stores information about a part of hardware, like a mainboard, a cpu or a ram module.
 * This also contains a list of all vulnerabilities this hardware part has.
 * 
 * @see Computer
 */
public class Hardware extends WorldChildFeatureHolder<Computer> {

    // ----- Properties -----

    /**
     * The name of the hardware part.
     */
    public static final PropertyDefinition<String>                                      NAME;

    /**
     * The {@link Vulnerability}s the hardware part has.
     */
    public static final CollectionPropertyDefinition<Vulnerability, Set<Vulnerability>> VULNERABILITIES;

    static {

        NAME = ObjectProperty.createDefinition("name");
        VULNERABILITIES = ObjectCollectionProperty.createDefinition("vulnerabilities", new HashSet<Vulnerability>());

    }

    // ----- Properties End -----

    /**
     * Creates a new hardware part.
     */
    public Hardware() {

        setParentType(Computer.class);
    }

}
