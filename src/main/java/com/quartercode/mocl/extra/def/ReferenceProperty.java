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

package com.quartercode.mocl.extra.def;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import com.quartercode.disconnected.util.ObjectAdapter;
import com.quartercode.mocl.base.FeatureHolder;
import com.quartercode.mocl.base.def.AbstractFeature;
import com.quartercode.mocl.extra.Persistent;
import com.quartercode.mocl.extra.Property;

/**
 * A reference property is a simple {@link Property} which stores an object.
 * During serialization, there is only an id reference serialized. That means that the referenced object has to have an {@link XmlID} annotation.
 * 
 * @param <T> The type of object which can be stored inside the reference property.
 * @see Property
 */
@Persistent
public class ReferenceProperty<T> extends AbstractFeature implements Property<T> {

    @XmlElement
    @XmlJavaTypeAdapter (ObjectAdapter.class)
    private T reference;

    /**
     * Creates a new empty reference property.
     * This is only recommended for direct field access (e.g. for serialization).
     */
    protected ReferenceProperty() {

    }

    /**
     * Creates a new reference property with the given name and {@link FeatureHolder}.
     * 
     * @param name The name of the reference property.
     * @param holder The feature holder which has and uses the new reference property.
     */
    public ReferenceProperty(String name, FeatureHolder holder) {

        super(name, holder);
    }

    @Override
    public T get() {

        return reference;
    }

    @Override
    public void set(T value) {

        this.reference = value;
    }

    @Override
    public Iterator<T> iterator() {

        List<T> list = new ArrayList<T>();
        list.add(reference);
        return list.iterator();
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
        ReferenceProperty<?> other = (ReferenceProperty<?>) obj;
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

        return super.toInfoString() + ", referencing " + reference;
    }

}
