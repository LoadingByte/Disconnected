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
 * This is a simple class which enumerates some important {@link ResourceBundle}s as constants.
 */
public class ResourceBundles {

    /**
     * The ui bundle contains all objects related to the basic game ui (menus etc.).
     * This does not contain anything related to desktop programs or the desktop itself.
     */
    public static final ResourceBundle UI      = getBundle("ui");

    /**
     * The kernel bundle contains all objects related to the operating system kernel.
     * This does not contain anything like sessions etc.
     */
    public static final ResourceBundle KERNEL  = getBundle("kernel");

    /**
     * The shell bundle contains all objects related to the shell interface.
     * This does not contain anything related to shell programs.
     */
    public static final ResourceBundle SHELL   = getBundle("shell");

    /**
     * The desktop bundle contains all objects related to the desktop ui (launch menu etc.).
     * This does not contain anything related to desktop programs.
     */
    public static final ResourceBundle DESKTOP = getBundle("desktop");

    /**
     * This returns the bundle of the program with the given name.
     * 
     * @param name The name of the program the returned bundle belongs to.
     * @return The bundle of the program with the given name.
     */
    public static ResourceBundle forProgram(String name) {

        return getBundle("program-" + name);
    }

    private static ResourceBundle getBundle(String name) {

        return ResourceBundle.getBundle("i18n." + name + "." + name);
    }

    private ResourceBundles() {

    }

}
