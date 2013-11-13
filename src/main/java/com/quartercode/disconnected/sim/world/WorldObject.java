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

package com.quartercode.disconnected.sim.world;

import java.util.ArrayList;
import java.util.List;

/**
 * A world object is an object inside a world.
 * It can have different {@link Property} objects which are used for setting the state of the object.
 * Those {@link Property} objects are be defined by {@link PropertyDefinition} constants which should be located in the actual subclass.
 */
public class WorldObject {

    private final List<Property<?>> properties;

    /**
     * Creates a new world object.
     */
    public WorldObject() {

        properties = new ArrayList<Property<?>>();
    }

    /**
     * Returns the {@link Property} which is defined by the given {@link PropertyDefinition}.
     * If there's no such {@link Property} yet, the {@link PropertyDefinition} creates a new one.
     * 
     * @param definition The {@link PropertyDefinition} which defines how the {@link Property} looks.
     * @return The found or created {@link Property}.
     */
    @SuppressWarnings ("unchecked")
    public <P extends Property<?>> P get(PropertyDefinition<P> definition) {

        for (Property<?> property : properties) {
            if (property.getName().equals(definition.getName())) {
                return (P) property;
            }
        }

        P property = definition.createProperty(this);
        properties.add(property);
        return property;
    }

}
