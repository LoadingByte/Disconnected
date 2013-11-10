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

package com.quartercode.disconnected.sim.comp.program.shell;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * The shell list is a normal array list which contains elements.
 * The difference is the {@link #toString()} method which outputs the list in a shell-like format.
 * You can also assign colors to the different elements.
 * 
 * @see ArrayList
 * @param <E> The type of objects which will be stored in the shell list.
 */
public class ShellList<E> extends ArrayList<E> {

    private static final long   serialVersionUID = -144200453214812311L;

    private final Map<E, Color> colors           = new HashMap<E, Color>();

    /**
     * Creates a new empty shell list.
     * 
     * @see ArrayList#ArrayList()
     */
    public ShellList() {

        super();
    }

    /**
     * Creates a new shell list and adds the given collection.
     * 
     * @param c A collection of elements to add on creation.
     * @see ArrayList#ArrayList(Collection)
     */
    public ShellList(Collection<? extends E> c) {

        super(c);
    }

    /**
     * Creates a new shell list with the given inital capacity.
     * 
     * @param initialCapacity The capacity the list has on creation.
     * @see ArrayList#ArrayList(int)
     */
    public ShellList(int initialCapacity) {

        super(initialCapacity);
    }

    /**
     * Returns the color which is assigned to the given element.
     * 
     * @param element The element whose color should be returned.
     * @return The color which is assigned to the given element.
     */
    public Color getColor(E element) {

        return colors.get(element);
    }

    /**
     * Assigns a new color to the given element.
     * 
     * @param element The element whose color is reassigned.
     * @param color The new color for the given element.
     */
    public void setColor(E element, Color color) {

        colors.put(element, color);
    }

    /**
     * Adds a new element to the shell list and sets the given color.
     * 
     * @param element The new element to add to the list.
     * @param color The color which is assigned to the new element,
     * @return True (as specified by {@link Collection#add(Object)}).
     */
    public boolean add(E element, Color color) {

        boolean returnValue = super.add(element);
        colors.put(element, color);
        return returnValue;
    }

    /**
     * Adds a new element to the shell list at the given index and sets the given color.
     * 
     * @param index The index the new element will appear at.
     * @param element The new element to add to the list.
     * @param color The color which is assigned to the new element.
     */
    public void add(int index, E element, Color color) {

        super.add(index, element);
        colors.put(element, color);
    }

    /**
     * Replaces the element at the given index with the given element and sets the given color.
     * 
     * @param index The index of the element to replace.
     * @param element The new element to replace the old one.
     * @param color The color which is assigned to the new element.
     * @return The element which was at the given index before.
     */
    public E set(int index, E element, Color color) {

        E returnValue = super.set(index, element);
        colors.remove(returnValue);
        colors.put(element, color);
        return returnValue;
    }

    @Override
    public E remove(int index) {

        colors.remove(get(index));
        return super.remove(index);
    }

    @Override
    public boolean remove(Object o) {

        colors.remove(o);
        return super.remove(o);
    }

    @Override
    public boolean removeAll(Collection<?> c) {

        for (Object o : c) {
            colors.remove(o);
        }
        return super.removeAll(c);
    }

    @Override
    public String toString() {

        String printedList = "";
        for (E entry : this) {
            printedList += "   <span";
            if (getColor(entry) != null) {
                printedList += " style=\"color: #" + Integer.toHexString(getColor(entry).getRGB()).substring(2) + "\"";
            }
            printedList += ">" + entry.toString() + "</span>";
        }
        return printedList.length() > 3 ? printedList.substring(3) : "";
    }

}
