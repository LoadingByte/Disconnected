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

package com.quartercode.disconnected.shared.registry;

import com.quartercode.disconnected.shared.util.ServiceRegistry;

/**
 * A utility class for working with {@link Registry}s and the {@link RegistryService}.
 * 
 * @see Registry
 * @see RegistryService
 */
public class Registries {

    /**
     * Looks up the {@link Registry} which is defined by the given {@link RegistryDefinition} using the default {@link RegistryService}.
     * That default service is retrieved using {@link ServiceRegistry#lookup(Class)}.
     * Effectively, this is just a shortcut.
     * 
     * @param definition The definition that defines the registry which should be returned.
     * @return The registry which is defined by the given definition.
     * @see RegistryService#getRegistry(RegistryDefinition)
     */
    public static <R extends Registry<V>, V> R get(RegistryDefinition<R> definition) {

        return ServiceRegistry.lookup(RegistryService.class).getRegistry(definition);
    }

    private Registries() {

    }

}
