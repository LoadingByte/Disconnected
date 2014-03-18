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

import java.util.ResourceBundle;

/**
 * A resource bundle group is a simple class that stores the bundle name and creates new {@link ResourceBundle}s on every request.
 * That way {@link ResourceBundle}s don't need to be cached.
 * 
 * @see ResourceBundle
 */
public class ResourceBundleGroup {

    private final String name;

    /**
     * Creates a new resource bundle group that accesses the {@link ResourceBundle} with the given name.
     * 
     * @param name The {@link ResourceBundle} the new group retrieves.
     */
    public ResourceBundleGroup(String name) {

        this.name = name;
    }

    /**
     * Retrieves the {@link ResourceBundle} that has the set name. No caching is going on here.
     * 
     * @return The {@link ResourceBundle} with the set name.
     */
    public ResourceBundle get() {

        return ResourceBundle.getBundle("i18n." + name + "." + name);
    }

}
