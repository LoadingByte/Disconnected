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

import static com.quartercode.classmod.factory.ClassmodFactory.factory;
import java.util.ArrayList;
import java.util.List;
import com.quartercode.classmod.extra.prop.CollectionPropertyDefinition;
import com.quartercode.classmod.extra.prop.PropertyDefinition;
import com.quartercode.classmod.extra.storage.StandardStorage;
import com.quartercode.classmod.extra.valuefactory.CloneValueFactory;
import com.quartercode.classmod.factory.CollectionPropertyDefinitionFactory;
import com.quartercode.classmod.factory.PropertyDefinitionFactory;
import com.quartercode.disconnected.server.world.comp.hardware.Hardware;
import com.quartercode.disconnected.server.world.comp.os.OS;
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
     * The active {@link OS operating system} instance which is currently running the computer.
     */
    public static final PropertyDefinition<OS>                                 OS;

    static {

        LOCATION = factory(PropertyDefinitionFactory.class).create("location", new StandardStorage<>());
        HARDWARE = factory(CollectionPropertyDefinitionFactory.class).create("hardware", new StandardStorage<>(), new CloneValueFactory<>(new ArrayList<>()));
        OS = factory(PropertyDefinitionFactory.class).create("os", new StandardStorage<>());

    }

}
