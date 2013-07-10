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

package com.quartercode.disconnected;

import java.io.File;

/**
 * A static storage class which stores important game values;
 */
public class Disconnected {

    private static final File RESOURCES = new File("resources");

    /**
     * Returns the current implemented version.
     * 
     * @return The current implemented version.
     */
    public static String getVersion() {

        return Disconnected.class.getPackage().getImplementationVersion();
    }

    /**
     * Returns the resources directory.
     * 
     * @return The resources directory.
     */
    public static File getResources() {

        return RESOURCES;
    }

    private Disconnected() {

    }

}
