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

package com.quartercode.disconnected.graphics;

/**
 * This constant class holds the {@link GraphicsStateDescriptor}s for the {@link GraphicsState}s used by Disconnected.
 * 
 * @see GraphicsStateDescriptor
 */
public class DefaultStates {

    /**
     * The desktop which displays a ui users can use in order to control a computer.
     */
    public static final GraphicsStateDescriptor DESKTOP = new GraphicsStateDescriptor("desktop");

    private DefaultStates() {

    }

}
