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
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import javax.xml.bind.annotation.XmlElement;

/**
 * A queue property stores a queue which can be modified using the typical {@link Queue} methods.
 * 
 * @param <E> The type of elements which can be put inside the queue property.
 */
public class QueueProperty<E> extends Property implements Queue<E> {

    @XmlElement (name = "element")
    private Queue<E> queue;

    /**
     * Creates a new empty queue property.
     * This is only recommended for direct field access (e.g. for serialization).
     */
    protected QueueProperty() {

    }

    /**
     * Creates a new queue property with the given name and parent object.
     * 
     * @param name The name the new property will have.
     * @param parent The parent object which has the new property.
     */
    public QueueProperty(String name, WorldObject parent) {

        super(name, parent);

        queue = new LinkedList<E>();
    }

    @Override
    public E element() {

        return queue.element();
    }

    @Override
    public E peek() {

        return queue.peek();
    }

    /**
     * Returns the elements of the queue property which have the given type as a superclass or annotation.
     * 
     * @param type The type the returned elements have as a superclass or annotation.
     * @return Elements which have the given type as a superclass or annotation.
     */
    @SuppressWarnings ("unchecked")
    public <T> List<T> get(Class<T> type) {

        List<T> elements = new ArrayList<T>();
        for (E element : queue) {
            if (type.isAnnotation() && element.getClass().isAnnotationPresent((Class<? extends Annotation>) type) || type.isAssignableFrom(element.getClass())) {
                elements.add(type.cast(element));
            }
        }

        return elements;
    }

    @Override
    public boolean isEmpty() {

        return queue.isEmpty();
    }

    @Override
    public boolean contains(Object o) {

        return queue.contains(queue);
    }

    @Override
    public boolean containsAll(Collection<?> c) {

        return queue.containsAll(c);
    }

    @Override
    public int size() {

        return queue.size();
    }

    @Override
    public boolean add(E e) {

        return queue.add(e);
    }

    @Override
    public boolean offer(E e) {

        return queue.offer(e);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {

        return queue.addAll(c);
    }

    @Override
    public E remove() {

        return queue.remove();
    }

    @Override
    public E poll() {

        return queue.poll();
    }

    @Override
    public boolean remove(Object o) {

        return queue.remove(o);
    }

    @Override
    public boolean removeAll(Collection<?> c) {

        return queue.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {

        return queue.retainAll(c);
    }

    @Override
    public void clear() {

        queue.clear();
    }

    @Override
    public Object[] toArray() {

        return queue.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {

        return queue.toArray(a);
    }

    @Override
    public Iterator<E> iterator() {

        return queue.iterator();
    }

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (queue == null ? 0 : queue.hashCode());
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
        QueueProperty<?> other = (QueueProperty<?>) obj;
        if (queue == null) {
            if (other.queue != null) {
                return false;
            }
        } else if (!queue.equals(other.queue)) {
            return false;
        }
        return true;
    }

    @Override
    public String toInfoString() {

        return super.toInfoString() + " with " + queue.size() + " entries";
    }

    @Override
    public String toString() {

        return queue.toString();
    }

}
