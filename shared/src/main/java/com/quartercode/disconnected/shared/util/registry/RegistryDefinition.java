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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.apache.commons.lang3.reflect.Typed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An object that defines a {@link Registry} by mapping a registry name to an exact type literal.
 * Such a definition object can be used to retrieve a registry instance with {@link RegistryService#getRegistry(RegistryDefinition)}.
 *
 * @param <R> The type of the defined registry.
 *        This defined registry type should also contain all possible generic parameters; no wildcards (?) should be used.
 *        For example, {@code ListRegistry<String>} should be used instead of {@code ListRegistry<?>}.
 * @see Registry
 * @see RegistryService
 */
public class RegistryDefinition<R extends Registry<?>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RegistryDefinition.class);

    private final String        name;
    private final Typed<R>      type;

    /**
     * Creates a new registry definition for a {@link Registry} of the given type.
     *
     * @param name The name of the defined registry.
     * @param type The exact type of the defined registry.
     *        This type should also contain all possible generic parameters; no wildcards (?) should be used.
     *        For example, {@code ListRegistry<String>} should be used instead of {@code ListRegistry<?>}.
     */
    public RegistryDefinition(String name, Typed<R> type) {

        this.name = name;
        this.type = type;
    }

    /**
     * Returns the name of the defined {@link Registry}.
     * Note that the name is not stored in the final registry object.
     *
     * @return The registry name.
     */
    public String getName() {

        return name;
    }

    /**
     * Returns the exact type of the defined {@link Registry}.
     * This type should also contain all possible generic parameters; no wildcards (?) should be used.
     * For example, {@code ListRegistry<String>} should be used instead of {@code ListRegistry<?>}.
     *
     * @return The registry type.
     */
    public Typed<R> getType() {

        return type;
    }

    /**
     * Creates a new instance of the defined registry.
     * Note that the registry name should not be passed into the new registry object.<br>
     * <br>
     * By default, this method creates a new instance of the registry type defined in {@link #getType()}.
     * It can be overridden to change the default behavior.
     *
     * @return A new registry instance.
     */
    public R create() {

        try {
            @SuppressWarnings ("unchecked")
            R instance = (R) TypeUtils.getRawType(type.getType(), null).newInstance();
            return instance;
        } catch (InstantiationException | IllegalAccessException e) {
            LOGGER.error("Cannot instantiate registry '{}' for type '{}' using newInstance()", name, type, e);
            return null;
        }
    }

    @Override
    public int hashCode() {

        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(Object obj) {

        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public String toString() {

        return ToStringBuilder.reflectionToString(this);
    }

}
