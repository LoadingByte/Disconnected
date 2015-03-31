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

package com.quartercode.disconnected.server.test.util;

import static org.junit.Assert.assertEquals;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import org.junit.Test;
import com.quartercode.disconnected.server.util.NullPreventer;

public class NullPreventerTest {

    @Test
    public void testPreventNull() {

        assertEquals("Null-prevented byte", Byte.valueOf((byte) 0), NullPreventer.prevent((Byte) null));
        assertEquals("Not null-prevented byte", Byte.valueOf((byte) 10), NullPreventer.prevent((byte) 10));

        assertEquals("Null-prevented short", Short.valueOf((short) 0), NullPreventer.prevent((Short) null));
        assertEquals("Not null-prevented short", Short.valueOf((short) 10), NullPreventer.prevent((short) 10));

        assertEquals("Null-prevented integer", Integer.valueOf(0), NullPreventer.prevent((Integer) null));
        assertEquals("Not null-prevented integer", Integer.valueOf(10), NullPreventer.prevent(10));

        assertEquals("Null-prevented long", Long.valueOf(0), NullPreventer.prevent((Long) null));
        assertEquals("Not null-prevented long", Long.valueOf(10), NullPreventer.prevent(10L));

        assertEquals("Null-prevented boolean", Boolean.FALSE, NullPreventer.prevent((Boolean) null));
        assertEquals("Not null-prevented boolean", Boolean.TRUE, NullPreventer.prevent(true));

        assertEquals("Null-prevented string", "", NullPreventer.prevent((String) null));
        assertEquals("Not null-prevented string", "test", NullPreventer.prevent("test"));

        assertEquals("Null-prevented list", new ArrayList<>(), NullPreventer.prevent((List<?>) null));
        List<Integer> testList = Arrays.asList(21, 64, 127, 256);
        assertEquals("Not null-prevented list", testList, NullPreventer.prevent(testList));

        assertEquals("Null-prevented set", new HashSet<>(), NullPreventer.prevent((Set<?>) null));
        Set<Integer> testSet = new HashSet<>(testList);
        assertEquals("Not null-prevented set", testSet, NullPreventer.prevent(testSet));

        assertEquals("Null-prevented queue", new LinkedList<>(), NullPreventer.prevent((Queue<?>) null));
        Queue<Integer> testQueue = new LinkedList<>(testList);
        assertEquals("Not null-prevented queue", testQueue, NullPreventer.prevent(testQueue));

        assertEquals("Null-prevented list", new HashMap<>(), NullPreventer.prevent((Map<?, ?>) null));
        Map<Integer, String> testMap = new HashMap<>();
        testMap.put(21, "test21");
        testMap.put(256, "test256");
        assertEquals("Not null-prevented list", testMap, NullPreventer.prevent(testMap));
    }

}
