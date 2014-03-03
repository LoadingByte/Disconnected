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

package com.quartercode.disconnected.util;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A registry holds all kinds of classes and TWL themes for a later use, e.g. for serialization using JAXB.
 * 
 * @see Class
 * @see URL
 */
public class Registry {

    private final List<Class<?>> classes = new ArrayList<Class<?>>();
    private final List<URL>      themes  = new ArrayList<URL>();

    /**
     * Creates a new empty registry.
     */
    public Registry() {

    }

    /**
     * Returns all classes which are currently registered in the registry.
     * Those classes are known object tree ones which are used by JAXB.
     * 
     * @return All classes which are currently registered in the registry.
     */
    public List<Class<?>> getClasses() {

        return Collections.unmodifiableList(classes);
    }

    /**
     * Returns the registered classes which have the given type as a superclass.
     * Those classes are known object tree ones which are used by JAXB.
     * 
     * @param type The type to use for the selection.
     * @return The registered classes which have the given type as a superclass.
     */
    @SuppressWarnings ("unchecked")
    public <T> List<Class<? extends T>> getClasses(Class<T> type) {

        List<Class<? extends T>> classes = new ArrayList<Class<? extends T>>();
        for (Class<?> c : this.classes) {
            if (type.isAssignableFrom(c)) {
                classes.add((Class<? extends T>) c);
            }
        }
        return classes;
    }

    /**
     * Registers a new class to the registry.
     * Those classes are known object tree ones which are used by JAXB.
     * 
     * @param c The new class to register in the registry.
     */
    public void registerClass(Class<?> c) {

        if (!classes.contains(c)) {
            classes.add(c);
        }
    }

    /**
     * Unregisters a class from the registry.
     * Those classes are known object tree ones which are used by JAXB.
     * 
     * @param c The class to unregister from the registry.
     */
    public void unregisterClass(Class<?> c) {

        if (classes.contains(c)) {
            classes.remove(c);
        }
    }

    /**
     * Returns all theme urls which are currently registered.
     * Theme urls are used for generating a global theme file including all registered themes.
     * 
     * @return All theme urls which are currently registered.
     */
    public List<URL> getThemes() {

        return Collections.unmodifiableList(themes);
    }

    /**
     * Registers a theme url to the registry.
     * Theme urls are used for generating a global theme file including all registered themes.
     * 
     * @param theme The theme url to register in the registry.
     */
    public void registerTheme(URL theme) {

        if (!themes.contains(theme)) {
            themes.add(theme);
        }
    }

    /**
     * Unregisters a theme url from the registry.
     * Theme urls are used for generating a global theme file including all registered themes.
     * 
     * @param theme The theme url to unregister from the registry.
     */
    public void unregisterTheme(URL theme) {

        if (themes.contains(theme)) {
            themes.remove(theme);
        }
    }

}
