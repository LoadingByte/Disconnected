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
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import com.quartercode.disconnected.util.ObjectAdapter;
import com.quartercode.mocl.base.Feature;
import com.quartercode.mocl.base.FeatureHolder;
import com.quartercode.mocl.base.def.AbstractFeature;
import com.quartercode.mocl.extra.Persistent;

/**
 * A property is a simple {@link Feature} which stores an object.
 * 
 * @param <T> The type of object which can be stored inside the property.
 */
@Persistent
public class Property<T> extends AbstractFeature implements Iterable<T> {

    @XmlElement
    @XmlJavaTypeAdapter (ObjectAdapter.class)
    private T object;

    /**
     * Creates a new empty property.
     * This is only recommended for direct field access (e.g. for serialization).
     */
    protected Property() {

    }

    /**
     * Creates a new property with the given name and {@link FeatureHolder}.
     * 
     * @param name The name of the property.
     * @param holder The feature holder which has and uses the new property.
     */
    public Property(String name, FeatureHolder holder) {

        super(name, holder);
    }

    /**
     * Returns the object which is stored inside the property.
     * 
     * @return The stored object.
     */
    public T get() {

        return object;
    }

    /**
     * Changes the object which is stored inside the property.
     * 
     * @param value The new stored object.
     */
    public void set(T value) {

        this.object = value;
    }

    @Override
    public Iterator<T> iterator() {

        List<T> list = new ArrayList<T>();
        list.add(object);
        return list.iterator();
    }

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = prime * result + (object == null ? 0 : object.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Property<?> other = (Property<?>) obj;
        if (object == null) {
            if (other.object != null) {
                return false;
            }
        } else if (!object.equals(other.object)) {
            return false;
        }
        return true;
    }

    @Override
    public String toInfoString() {

        return super.toInfoString() + ", object " + object;
    }

}
