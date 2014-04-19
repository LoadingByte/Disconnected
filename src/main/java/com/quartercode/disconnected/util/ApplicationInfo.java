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
 * Application info lets the user retrieve data that is packaged with the application, like the version of the application.
 */
public class ApplicationInfo {

    /**
     * The title of the application.
     */
    public static final String TITLE   = ApplicationInfo.class.getPackage().getImplementationTitle();

    /**
     * The used version of the application.
     */
    public static final String VERSION = ApplicationInfo.class.getPackage().getImplementationVersion();

    /**
     * The vendor who created the application.
     */
    public static final String VENDOR  = ApplicationInfo.class.getPackage().getImplementationVendor();

    private ApplicationInfo() {

    }

}
