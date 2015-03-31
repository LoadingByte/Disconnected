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

package com.quartercode.disconnected.server.test;

import static org.junit.Assert.assertTrue;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.lang3.tuple.Pair;

public class ExtraAssert {

    public static void assertCollectionEquals(String message, Collection<?> actualCollection, Object... expectedElements) {

        assertTrue(message, actualCollection.size() == expectedElements.length);
        assertTrue(message, actualCollection.containsAll(Arrays.asList(expectedElements)));
    }

    public static void assertListEquals(String message, List<?> actualList, Object... expectedElements) {

        assertTrue(message, actualList.size() == expectedElements.length);

        for (int index = 0; index < actualList.size(); index++) {
            assertTrue(message, Objects.equals(expectedElements[index], actualList.get(index)));
        }
    }

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
