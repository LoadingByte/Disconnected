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

package com.quartercode.disconnected.server.test;

import static org.junit.Assert.assertTrue;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.lang3.tuple.Pair;

public class ExtraAssert {

    public static void assertMapEquals(String message, Map<?, ?> map, Pair<?, ?>... entries) {

        assertTrue(message, map.size() == entries.length);

        Map<?, ?> mapClone = new HashMap<>(map);
        for (Pair<?, ?> expectedEntry : entries) {
            Object actualValue = mapClone.get(expectedEntry.getKey());
            assertTrue(message, Objects.equals(expectedEntry.getValue(), actualValue));

            mapClone.remove(expectedEntry.getKey());
        }
    }

    private ExtraAssert() {

    }

}
