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
 * An object property is a simple property which stores a simple object.
 * 
 * @param <T> The type of object which can be stored inside the object property.
 */
public class ObjectProperty<T> extends Property {

    @XmlElement
    private T value;

    /**
     * Creates a new empty object property.
     * This is only recommended for direct field access (e.g. for serialization).
     */
    protected ObjectProperty() {

    }

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
    public T get() {

        return value;
    }

    /**
     * Changes the value which is stored inside the object property.
     * 
     * @param value The new stored value.
     */
    public void set(T value) {

        this.value = value;
    }

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (value == null ? 0 : value.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ObjectProperty<?> other = (ObjectProperty<?>) obj;
        if (value == null) {
            if (other.value != null) {
                return false;
            }
        } else if (!value.equals(other.value)) {
            return false;
        }
        return true;
    }

    @Override
    public String toInfoString() {

        return super.toInfoString() + ", value " + value;
    }

}
