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

package com.quartercode.disconnected.world;

/**
 * A property definition is used to get a {@link Property}.
 * It contains the name of the property and the type it will have as a generic parameter.
 * 
 * @param <P> The type the defined property will have.
 * @see Property
 */
public abstract class PropertyDefinition<P extends Property> {

    private final String name;

    /**
     * Creates a new property definition for defining a {@link Property} with the given name.
     * 
     * @param name The name the defined {@link Property} has.
     */
    public PropertyDefinition(String name) {

        this.name = name;
    }

    /**
     * Returns the name the defined {@link Property} has.
     * 
     * @return The name the defined {@link Property} has.
     */
    public String getName() {

        return name;
    }

    /**
     * Creates a new {@link Property} of the generic parameter P using the given parent object.
     * 
     * @param parent The parent object which has the new property.
     * @return The new created property.
     */
    public abstract P createProperty(WorldObject parent);

}
