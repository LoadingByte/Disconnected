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

/**
 * An object property is a simple property which stores a simple object.
 * 
 * @param <T> The type of object which can be stored inside the object property.
 */
public class ObjectProperty<T> extends Property<T> {

    private T value;

    /**
     * Creates a new object property with the given name and parent object.
     * 
     * @param name The name the new object property will have.
     * @param parent The parent object which has the new object property.
     */
    public ObjectProperty(String name, WorldObject parent) {

        super(name, parent);
    }

    /**
     * Returns the value which is stored inside the object property.
     * 
     * @return The stored value.
     */
    @Override
    public T getValue() {

        return value;
    }

    /**
     * Changes the value which is stored inside the object property.
     * 
     * @param value The new stored value.
     */
    @Override
    public void setValue(T value) {

        this.value = value;
    }

}
