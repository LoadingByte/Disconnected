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

package com.quartercode.disconnected.shared.util.registry;

import java.util.Collection;
import com.quartercode.disconnected.shared.util.registry.extra.MultipleValueRegistry;
import com.quartercode.disconnected.shared.util.registry.extra.SingleValueRegistry;

/**
 * A registry is an object that stores a certain number of other objects (like a list).
 * All objects must be an instance of a certain generic type.
 * Different {@link MultipleValueRegistry} registry implementations are used for implementing different types of storages.
 * However, the {@link SingleValueRegistry} is used to only store a single value.
 * 
 * @param <V> The type of value that can be stored inside the registry.
 * @see RegistryDefinition
 * @see RegistryService
 */
public interface Registry<V> extends Iterable<V> {

    /**
     * Returns a (possibly unmodifiable) view of all objects that are stored in the registry.
     * In case of a {@link SingleValueRegistry}, a collection with one object is returned.
     * 
     * @return All stored objects.
     */
    public Collection<V> getValues();

}
