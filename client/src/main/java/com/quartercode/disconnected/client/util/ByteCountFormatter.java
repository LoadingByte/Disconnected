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

package com.quartercode.disconnected.client.util;

import java.text.DecimalFormat;

/**
 * A simple formatter that converts byte counts (e.g. file sizes) into a human-readable format.
 * For example, {@code 1024} would be converted to {@code 1 kB}, while {@code 1024 * 1024 * 2 = 2097152} would be converted to {@code 2 MB}.
 * 
 * @see #format(long)
 */
public class ByteCountFormatter {

    private static final String[] UNITS = { "B", "kB", "MB", "GB", "TB", "PB" };

    /**
     * Converts the given byte count (e.g. a file size) into a human-readable format using a {@code B/kB/MB/GB/TB} unit suffix.
     * For example, {@code 1024} would be converted to {@code 1 kB}, while {@code 1024 * 1024 * 2 = 2097152} would be converted to {@code 2 MB}.
     * The returned string provides one decimal place if necessary (e.g. {@code 1024 * 5 + 200 = 5320} is converted to {@code 5.2 kB})<br>
     * <br>
     * Note that the maximum byte count is somewhere between {@code 1023 PB} and {@code 1024 PB}.
     * The exact maximum would be {@code 10 ^ (6 * log(1024))}; however, the real value is often lower because of inaccuracies.
     * If the maximum is exceeded, {@code ?B} is used as unit.
     * 
     * @param byteCount The byte count that should be converted into the human-readable format.
     * @return The human-readable string that represents the given byte count.
     */
    public static String format(long byteCount) {

        if (byteCount <= 0) {
            return "0 " + UNITS[0];
        } else {
            int digitGroups = (int) (Math.log10(byteCount) / Math.log10(1024));
            return new DecimalFormat("###0.#").format(byteCount / Math.pow(1024, digitGroups)) + " " + (digitGroups < UNITS.length ? UNITS[digitGroups] : "?B");
        }
    }

    private ByteCountFormatter() {

    }

}
