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

import com.quartercode.disconnected.world.ListProperty;
import com.quartercode.disconnected.world.ObjectProperty;
import com.quartercode.disconnected.world.Property;
import com.quartercode.disconnected.world.PropertyDefinition;
import com.quartercode.disconnected.world.WorldObject;
import com.quartercode.disconnected.world.comp.hardware.Hardware;
import com.quartercode.disconnected.world.comp.os.OperatingSystem;
import com.quartercode.disconnected.world.general.Location;

/**
 * This class stores information about a computer, like the {@link Location} or the {@link Hardware} parts.
 * 
 * @see Location
 * @see Hardware
 */
public class Computer extends WorldObject {

    // ----- Property Definitions -----

    /**
     * The {@link Location} where the computer actually is.
     */
    public static final PropertyDefinition<ObjectProperty<Location>>        LOCATION;

    /**
     * The {@link Hardware} parts the computer contains.
     */
    public static final PropertyDefinition<ListProperty<Hardware>>          HARDWARE;

    /**
     * The {@link OperatingSystem} which is running on the computer.
     */
    public static final PropertyDefinition<ObjectProperty<OperatingSystem>> OS;

    static {

        LOCATION = new PropertyDefinition<ObjectProperty<Location>>("location") {

            @Override
            public ObjectProperty<Location> createProperty(WorldObject parent) {

                return new ObjectProperty<Location>(getName(), parent);
            }

        };

        HARDWARE = new PropertyDefinition<ListProperty<Hardware>>("hardware") {

            @Override
            public ListProperty<Hardware> createProperty(WorldObject parent) {

                return new ListProperty<Hardware>(getName(), parent);
            }

        };

        OS = new PropertyDefinition<ObjectProperty<OperatingSystem>>("operatingSystem") {

            @Override
            public ObjectProperty<OperatingSystem> createProperty(WorldObject parent) {

                return new ObjectProperty<OperatingSystem>(getName(), parent);
            }

        };

    }

    // ----- Property Definitions End -----

    /**
     * Creates a new empty computer.
     * This is only recommended for direct field access (e.g. for serialization).
     */
    protected Computer() {

    }

    /**
     * Creates a new computer which has the given parent object.
     * 
     * @param parent The parent {@link WorldObject} which has a {@link Property} which houses the new object.
     */
    public Computer(WorldObject parent) {

        super(parent);
    }

    @Override
    public String toInfoString() {

        String hardwareInfo = "";
        for (Hardware hardwarePart : get(HARDWARE)) {
            hardwareInfo += hardwarePart.toInfoString() + ", ";
        }
        return "loc " + get(LOCATION).toInfoString() + ", " + hardwareInfo + get(OS).toInfoString();
    }

    @Override
    public String toString() {

        return getClass().getName() + " [" + toInfoString() + "]";
    }

}
