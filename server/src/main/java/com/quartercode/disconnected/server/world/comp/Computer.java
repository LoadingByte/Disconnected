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

package com.quartercode.disconnected.server.world.comp;

import static com.quartercode.classmod.ClassmodFactory.create;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.reflect.TypeLiteral;
import com.quartercode.classmod.extra.CollectionPropertyDefinition;
import com.quartercode.classmod.extra.PropertyDefinition;
import com.quartercode.classmod.extra.storage.StandardStorage;
import com.quartercode.classmod.extra.valuefactory.CloneValueFactory;
import com.quartercode.disconnected.server.world.comp.hardware.Hardware;
import com.quartercode.disconnected.server.world.comp.os.OperatingSystem;
import com.quartercode.disconnected.server.world.util.WorldFeatureHolder;
import com.quartercode.disconnected.shared.world.general.Location;

/**
 * This class stores information about a computer, like the {@link Location} or the {@link Hardware} parts.
 * 
 * @see Location
 * @see Hardware
 */
public class Computer extends WorldFeatureHolder {

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

        LOCATION = create(new TypeLiteral<PropertyDefinition<Location>>() {}, "name", "location", "storage", new StandardStorage<>());
        HARDWARE = create(new TypeLiteral<CollectionPropertyDefinition<Hardware, List<Hardware>>>() {}, "name", "hardware", "storage", new StandardStorage<>(), "collection", new CloneValueFactory<>(new ArrayList<>()));
        OS = create(new TypeLiteral<PropertyDefinition<OperatingSystem>>() {}, "name", "operatingSystem", "storage", new StandardStorage<>());

    }

}
