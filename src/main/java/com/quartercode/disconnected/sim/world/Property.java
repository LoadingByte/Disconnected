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

import javax.xml.bind.annotation.XmlElement;

/**
 * A property is used to store an object variable of a class and handle modifications.
 * For accessing properties, you can used {@link PropertyDefinition} constants.
 * 
 * @param <I> The type of object the property stores internally.
 * @see WorldObject
 */
public abstract class Property<I> {

    private final String      name;
    private final WorldObject parent;

    /**
     * Creates a new property with the given name and parent object.
     * 
     * @param name The name the new property will have.
     * @param parent The parent object which has the new property.
     */
    protected Property(String name, WorldObject parent) {

        this.name = name;
        this.parent = parent;
    }

    /**
     * Returns the name the property has.
     * The name is used for storing and accessing the property in a {@link WorldObject}.
     * 
     * @return The name the property has.
     */
    public String getName() {

        return name;
    }

    /**
     * Returns the parent object which actually has this property.
     * 
     * @return The object which has this property.
     */
    public WorldObject getParent() {

        return parent;
    }

    /**
     * Returns the object the property stores internally.
     * This should not be used for accessing the property externally.
     * 
     * @return The object the property stores internally.
     */
    @XmlElement
    protected abstract I getValue();

    /**
     * Changes the object the property stores internally.
     * This should not be used for accessing the property externally.
     * 
     * @param value The new object the property will store.
     */
    protected abstract void setValue(I value);

}
