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

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import javax.xml.bind.annotation.XmlElement;

/**
 * A list property stores a list which can be modified using the typical {@link List} methods.
 * 
 * @param <E> The type of elements which can be put inside the list property.
 */
public class ListProperty<E> extends Property implements List<E> {

    @XmlElement (name = "element")
    private List<E> list;

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

        list = new ArrayList<E>();
    }

    @Override
    public E get(int index) {

        return list.get(index);
    }

    /**
     * Returns the elements of the list property which have the given type as a superclass or annotation.
     * 
     * @param type The type the returned elements have as a superclass or annotation.
     * @return Elements which have the given type as a superclass or annotation.
     */
    @SuppressWarnings ("unchecked")
    public <T> List<T> get(Class<T> type) {

        List<T> elements = new ArrayList<T>();
        for (E element : list) {
            if (type.isAnnotation() && element.getClass().isAnnotationPresent((Class<? extends Annotation>) type) || type.isAssignableFrom(element.getClass())) {
                elements.add(type.cast(element));
            }
        }

        return elements;
    }

    @Override
    public boolean isEmpty() {

        return list.isEmpty();
    }

    @Override
    public boolean contains(Object o) {

        return list.contains(list);
    }

    @Override
    public boolean containsAll(Collection<?> c) {

        return list.containsAll(c);
    }

    @Override
    public int indexOf(Object o) {

        return list.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {

        return list.lastIndexOf(o);
    }

    @Override
    public int size() {

        return list.size();
    }

    @Override
    public boolean add(E e) {

        return list.add(e);
    }

    @Override
    public void add(int index, E element) {

        list.add(index, element);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {

        return list.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {

        return list.addAll(index, c);
    }

    @Override
    public E set(int index, E element) {

        return list.set(index, element);
    }

    @Override
    public boolean remove(Object o) {

        return list.remove(o);
    }

    @Override
    public E remove(int index) {

        return list.remove(index);
    }

    @Override
    public boolean removeAll(Collection<?> c) {

        return list.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {

        return list.retainAll(c);
    }

    @Override
    public void clear() {

        list.clear();
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {

        return list.subList(fromIndex, toIndex);
    }

    @Override
    public Object[] toArray() {

        return list.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {

        return list.toArray(a);
    }

    @Override
    public Iterator<E> iterator() {

        return list.iterator();
    }

    @Override
    public ListIterator<E> listIterator() {

        return list.listIterator();
    }

    @Override
    public ListIterator<E> listIterator(int index) {

        return list.listIterator(index);
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

    @Override
    public String toString() {

        return list.toString();
    }

}
