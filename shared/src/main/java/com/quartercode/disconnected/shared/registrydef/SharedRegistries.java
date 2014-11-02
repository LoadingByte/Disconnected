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

package com.quartercode.disconnected.shared.registrydef;

import org.apache.commons.lang3.reflect.TypeLiteral;
import com.quartercode.disconnected.shared.comp.file.SeparatedPath;
import com.quartercode.disconnected.shared.registry.Registry;
import com.quartercode.disconnected.shared.registry.RegistryDefinition;
import com.quartercode.disconnected.shared.registry.extra.MapRegistry;

/**
 * A class that stores the default {@link RegistryDefinition}s which define the default {@link Registry}s used by both the server and the client.
 * 
 * @see RegistryDefinition
 * @see Registry
 */
public class SharedRegistries {

    /**
     * A map that maps world program keys (names) to common program locations.
     * Such common locations are file paths under which programs can be commonly found.
     */
    public static final RegistryDefinition<MapRegistry<String, SeparatedPath>> WORLD_PROGRAM_COMLOCS;

    static {

        WORLD_PROGRAM_COMLOCS = new RegistryDefinition<>("worldProgramComlocs", new TypeLiteral<MapRegistry<String, SeparatedPath>>() {});

    }

    private SharedRegistries() {

    }

}
