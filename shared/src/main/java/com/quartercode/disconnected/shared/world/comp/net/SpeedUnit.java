/*
 * This file is part of Disconnected.
 * Copyright (c) 2013 QuarterCode <http://quartercode.com/>
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

package com.quartercode.disconnected.shared.world.comp.net;

import com.quartercode.disconnected.shared.world.comp.ByteUnit;

/**
 * A speed unit represents a unit for the speed of a connection in bytes per second. There are several stages like "kilo-", "mega-", "giga-" etc.
 * The speed unit uses powers of the base 1000.
 * The speed unit class is manly used to convert between the units.
 *
 * @see ByteUnit
 */
public enum SpeedUnit {

    /**
     * A byte equals 1000^0 bytes (1 byte).
     */
    BYTE,
    /**
     * A kilobyte equals 1000^1 bytes (1000 bytes).
     */
    KILOBYTE,
    /**
     * A megabyte equals 1000^2 bytes (1000000 bytes).
     */
    MEGABYTE,
    /**
     * A gigabyte equals 1000^3 bytes (1000000000 bytes).
     */
    GIGABYTE,
    /**
     * A terabyte equals 1000^4 bytes (1000000000000 bytes).
     */
    TERABYTE,
    /**
     * A petabyte equals 1000^5 bytes (1000000000000000 bytes).
     */
    PETABYTE;

    /**
     * Converts the given source amount which is set in the given source unit into this unit.
     *
     * @param source The source amount of "speed" (set in the source unit).
     * @param sourceUnit The source unit which the source amount uses.
     * @return The converted amount of "speed".
     */
    public long convert(long source, SpeedUnit sourceUnit) {

        // Convert to bytes
        long speed = (long) (source * Math.pow(1000, sourceUnit.ordinal()));
        // Convert to target unit
        return (long) (speed / Math.pow(1000, ordinal()));
    }

}
