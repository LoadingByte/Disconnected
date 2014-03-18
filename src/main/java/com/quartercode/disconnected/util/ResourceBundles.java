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

/**
 * This is a simple class which enumerates some important {@link ResourceBundleGroup}s as constants.
 * It also enables quick access to typical bundle groups like desktop program resources.
 * 
 * @see ResourceBundleGroup
 */
public class ResourceBundles {

    /**
     * The desktop bundle contains all objects related to the desktop ui (launch menu etc.).
     * This does not contain anything related to desktop programs.
     */
    public static final ResourceBundleGroup DESKTOP = new ResourceBundleGroup("desktop");

    /**
     * Returns the {@link ResourceBundleGroup} of the desktop program with the given name.
     * 
     * @param name The name of the desktop program the returned bundle group belongs to.
     * @return The {@link ResourceBundleGroup} of the desktop program which has the given name.
     */
    public static ResourceBundleGroup forProgram(String name) {

        return new ResourceBundleGroup("program-" + name);
    }

    private ResourceBundles() {

    }

}
