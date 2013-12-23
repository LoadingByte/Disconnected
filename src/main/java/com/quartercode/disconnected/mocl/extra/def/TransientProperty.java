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

package com.quartercode.disconnected.mocl.extra.def;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import com.quartercode.disconnected.mocl.base.FeatureHolder;
import com.quartercode.disconnected.mocl.base.def.AbstractFeature;
import com.quartercode.disconnected.mocl.extra.Property;

/**
 * A transient property is a simple {@link Property} which stores an object and can't be serialized by JAXB.
 * 
 * @param <T> The type of object which can be stored inside the transient property.
 * @see Property
 */
public class TransientProperty<T> extends AbstractFeature implements Property<T> {

    private T object;

    /**
     * Creates a new transient property with the given name and {@link FeatureHolder}.
     * 
     * @param name The name of the transient property.
     * @param holder The feature holder which has and uses the new transient property.
     */
    public TransientProperty(String name, FeatureHolder holder) {

        super(name, holder);
    }

    /**
     * Creates a new transient property with the given name and {@link FeatureHolder}, and sets the initial value.
     * 
     * @param name The name of the transient property.
     * @param holder The feature holder which has and uses the new transient property.
     * @param initialValue The value the new transient property has directly after creation.
     */
    public TransientProperty(String name, FeatureHolder holder, T initialValue) {

        super(name, holder);

        set(initialValue);
    }

    @Override
    public T get() {

        return object;
    }

    @Override
    public void set(T value) {

        object = value;
    }

    @Override
    public Iterator<T> iterator() {

        Set<T> set = new HashSet<T>();
        set.add(object);
        return set.iterator();
    }

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (object == null ? 0 : object.hashCode());
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
        TransientProperty<?> other = (TransientProperty<?>) obj;
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
    public String toString() {

        return getClass().getName() + " [name=" + getName() + ", object=" + object + "]";
    }

}
