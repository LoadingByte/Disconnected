/*
 * This file is part of Disconnected.
 * Copyright (c) 2013 QuarterCode <http://quartercode.com/>
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

package com.quartercode.disconnected.shared.util.registry;

/**
 * A registry service stores {@link Registry}s and makes them accessible through a {@link RegistryDefinition}.
 * When a registry does not yet exist, it is created using the {@link RegistryDefinition#create()} method.
 *
 * @see Registry
 * @see RegistryDefinition
 */
public interface RegistryService {

    /**
     * Returns the {@link Registry} which is defined by the given {@link RegistryDefinition}.
     * If the registry does not yet exist, it is created using the {@link RegistryDefinition#create()} method.
     *
     * @param definition The definition that defines the registry which should be returned.
     * @return The registry which is defined by the given definition.
     */
    public <R extends Registry<V>, V> R getRegistry(RegistryDefinition<R> definition);

}
