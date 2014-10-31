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

import java.util.HashMap;
import java.util.Map;

/**
 * This is the default implementation of the {@link RegistryService}.
 * 
 * @see RegistryService
 */
public class DefaultRegistryService implements RegistryService {

    private final Map<String, Registry<?>> registries = new HashMap<>();

    @Override
    public <R extends Registry<V>, V> R getRegistry(RegistryDefinition<R> definition) {

        String name = definition.getName();

        // Try to create the registry if it does not exist
        if (!registries.containsKey(name)) {
            R registry = definition.create();
            if (registry != null) {
                registries.put(name, registry);
            }
        }

        @SuppressWarnings ("unchecked")
        R registry = (R) registries.get(name);
        return registry;
    }

}
