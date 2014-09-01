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

package com.quartercode.disconnected.server.world.comp;

/**
 * A byte unit represents a unit for bytes. There are several stages like "kilo-", "mega-", "giga-" etc.
 * The byte unit uses powers of the base 1024.
 * The byte unit class is manly used to convert between the units.
 */
public enum ByteUnit {

    /**
     * A byte equals 1024^0 bytes (1 byte).
     */
    BYTE,
    /**
     * A kilobyte equals 1024^1 bytes (1024 bytes).
     */
    KILOBYTE,
    /**
     * A megabyte equals 1024^2 bytes (1048576 bytes).
     */
    MEGABYTE,
    /**
     * A gigabyte equals 1024^3 bytes (1073741824 bytes).
     */
    GIGABYTE,
    /**
     * A terabyte equals 1024^4 bytes (1099511627776 bytes).
     */
    TERABYTE,
    /**
     * A petabyte equals 1024^5 bytes (1125899906842624 bytes).
     */
    PETABYTE;

    /**
     * Converts the given source amount which is set in the given source unit into this unit.
     * 
     * @param source The source amount of bytes (set in the source unit).
     * @param sourceUnit The source unit which the source amount uses.
     * @return The converted amount of bytes.
     */
    public long convert(long source, ByteUnit sourceUnit) {

        // Convert to bytes
        long bytes = (long) (source * Math.pow(1024, sourceUnit.ordinal()));
        // Convert to target unit
        return (long) (bytes / Math.pow(1024, ordinal()));
    }

}
