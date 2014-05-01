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

package com.quartercode.disconnected.world.comp;

import java.util.ArrayList;
import java.util.List;
import com.quartercode.classmod.extra.CollectionPropertyDefinition;
import com.quartercode.classmod.extra.PropertyDefinition;
import com.quartercode.classmod.extra.def.ObjectCollectionProperty;
import com.quartercode.classmod.extra.def.ObjectProperty;
import com.quartercode.disconnected.world.World;
import com.quartercode.disconnected.world.WorldChildFeatureHolder;
import com.quartercode.disconnected.world.comp.hardware.Hardware;
import com.quartercode.disconnected.world.comp.os.OperatingSystem;
import com.quartercode.disconnected.world.general.Location;

/**
 * This class stores information about a computer, like the {@link Location} or the {@link Hardware} parts.
 * 
 * @see Location
 * @see Hardware
 */
public class Computer extends WorldChildFeatureHolder<World> {

    // ----- Properties -----

    /**
     * The {@link Location} of the computer.
     */
    public static final PropertyDefinition<Location>                           LOCATION;

    /**
     * The {@link Hardware} parts the computer contains.
     */
    public static final CollectionPropertyDefinition<Hardware, List<Hardware>> HARDWARE;

    /**
     * The active {@link OperatingSystem} instance which is currently running the computer.
     */
    public static final PropertyDefinition<OperatingSystem>                    OS;

    static {

        LOCATION = ObjectProperty.createDefinition("location");
        HARDWARE = ObjectCollectionProperty.createDefinition("hardware", new ArrayList<Hardware>());
        OS = ObjectProperty.createDefinition("operatingSystem");

    }

    /**
     * Creates a new computer.
     */
    public Computer() {

        setParentType(World.class);
    }

}
