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
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;

/**
 * A list property stores a list which can be modified through some modification interfaces.
 * 
 * @param <T> The type of elements which can be put inside the list property.
 */
public class ListProperty<T> extends Property {

    @XmlElement
    private List<T> list;

    /**
     * Creates a new empty list property.
     * This is only recommended for direct field access (e.g. for serialization).
     */
    protected ListProperty() {

    }

    /**
     * Creates a new list property with the given name and parent object.
     * 
     * @param name The name the new list property will have.
     * @param parent The parent object which has the new list property.
     */
    public ListProperty(String name, WorldObject parent) {

        super(name, parent);
    }

    /**
     * Returns an unmodifiable instance of the {@link List} the property stores.
     * You can't change the structure of the list, but you can modify the objects which are stored in it.
     * 
     * @return An unmodifiable instance of the {@link List} the property stores.
     */
    public List<T> get() {

        return Collections.unmodifiableList(list);
    }

    /**
     * Returns the elements of the list property which have the given type as a superclass.
     * 
     * @param type The type the returned elements have as a superclass.
     * @return Elements which have the given type as a superclass.
     */
    public <E> List<E> getElements(Class<E> type) {

        List<E> elements = new ArrayList<E>();
        for (T element : list) {
            if (type.isAssignableFrom(type.getClass())) {
                elements.add(type.cast(element));
            }
        }
        return elements;
    }

    /**
     * Adds the given element to the list.
     * The element has to be of the generic type T.
     * 
     * @param element The element to add to the list.
     */
    public void add(T element) {

        list.add(element);
    }

    /**
     * Removes the given element from the list (if the element is inside the list).
     * The element has to be of the generic type T.
     * 
     * @param element The element to remove from the list.
     */
    public void remove(T element) {

        list.remove(element);
    }

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (list == null ? 0 : list.hashCode());
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
        ListProperty<?> other = (ListProperty<?>) obj;
        if (list == null) {
            if (other.list != null) {
                return false;
            }
        } else if (!list.equals(other.list)) {
            return false;
        }
        return true;
    }

    @Override
    public String toInfoString() {

        return super.toInfoString() + " with " + list.size() + " entries";
    }

}
