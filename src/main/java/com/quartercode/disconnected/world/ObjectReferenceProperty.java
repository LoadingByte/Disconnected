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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlValue;

/**
 * An object reference property is a property which references a simple object without defining it.
 * During serialization, there is only an id reference serialized. That means that the referenced object has to have an {@link XmlID} annotation.
 * 
 * @param <T> The type of object which can be referenced inside the object property. The class has to have an {@link XmlID} annotation.
 */
public class ObjectReferenceProperty<T> extends Property implements Iterable<T> {

    @XmlValue
    @XmlIDREF
    private T reference;

    /**
     * Creates a new empty object reference property.
     * This is only recommended for direct field access (e.g. for serialization).
     */
    protected ObjectReferenceProperty() {

    }

    /**
     * Creates a new object reference property with the given name and parent object.
     * 
     * @param name The name the new object property will have.
     * @param parent The parent object which has the new object property.
     */
    public ObjectReferenceProperty(String name, WorldObject parent) {

        super(name, parent);
    }

    /**
     * Returns the object which is referenced inside the object property.
     * 
     * @return The referenced object.
     */
    public T get() {

        return reference;
    }

    /**
     * Changes the object which is referenced inside the object property.
     * 
     * @param value The new referenced object.
     */
    public void set(T value) {

        this.reference = value;
    }

    @Override
    public Iterator<T> iterator() {

        List<T> children = new ArrayList<T>();
        children.add(reference);
        return children.iterator();
    }

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (reference == null ? 0 : reference.hashCode());
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
        ObjectReferenceProperty<?> other = (ObjectReferenceProperty<?>) obj;
        if (reference == null) {
            if (other.reference != null) {
                return false;
            }
        } else if (!reference.equals(other.reference)) {
            return false;
        }
        return true;
    }

    @Override
    public String toInfoString() {

        return super.toInfoString() + ", object " + reference;
    }

}
