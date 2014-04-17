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

    private final List<String> contextPath = new ArrayList<String>();
    private final List<URL>    themes      = new ArrayList<URL>();

    /**
     * Creates a new empty registry.
     */
    public Registry() {

    }

    /**
     * Returns all packages which are currently registered as context path entries.
     * JAXB can look up classes which are important for XML mapping in the <code>jaxb.index</code> files which is located in such packages.
     * 
     * @return All context path packages which are currently registered in the registry.
     */
    public List<String> getContextPath() {

        return Collections.unmodifiableList(contextPath);
    }

    /**
     * Registers a new context path package to the registry.
     * JAXB can look up classes which are important for XML mapping in the <code>jaxb.index</code> files which is located in such packages.
     * 
     * @param packageName The name of the new context path package.
     */
    public void registerContextPathEntry(String packageName) {

        if (!contextPath.contains(packageName)) {
            contextPath.add(packageName);
        }
    }

    /**
     * Unregisters a context path package from the registry.
     * JAXB can look up classes which are important for XML mapping in the <code>jaxb.index</code> files which is located in such packages.
     * 
     * @param packageName The name of the context path package to remove.
     */
    public void unregisterContextPathEntry(String packageName) {

        contextPath.remove(packageName);
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
