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

package com.quartercode.disconnected.client.registry;

import org.apache.commons.lang3.reflect.TypeLiteral;
import com.quartercode.disconnected.shared.registry.Registry;
import com.quartercode.disconnected.shared.registry.RegistryDefinition;
import com.quartercode.disconnected.shared.registry.extra.MapRegistry;
import com.quartercode.disconnected.shared.registry.extra.SetRegistry;

/**
 * A class that stores the default {@link RegistryDefinition}s which define the default {@link Registry}s used by the client.
 * 
 * @see RegistryDefinition
 * @see Registry
 */
public class ClientRegistries {

    /**
     * A map that maps client program names (e.g. {@code fileManager}) to client program descriptors.
     * All descriptors that should be available in the launch menu must be registered here.<br>
     * <br>
     * Note that the right side of the map does contain generic objects.
     * Those objects are guaranteed to be client program descriptors.
     * However, that class is not used here because that would create a dependency for all classes that use other
     * client registries and need to access this class.
     */
    public static final RegistryDefinition<MapRegistry<String, Object>> CLIENT_PROGRAMS;

    /**
     * The {@link Theme}s that should be loaded into the twl theme manager.
     * Note that only loaded themes can be used by graphical components.
     */
    public static final RegistryDefinition<SetRegistry<Theme>>          THEMES;

    static {

        CLIENT_PROGRAMS = new RegistryDefinition<>("clientPrograms", new TypeLiteral<MapRegistry<String, Object>>() {});
        THEMES = new RegistryDefinition<>("themes", new TypeLiteral<SetRegistry<Theme>>() {});

    }

    private ClientRegistries() {

    }

}