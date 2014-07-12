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

package com.quartercode.disconnected.test.util.storage;

import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.quartercode.disconnected.util.storage.GlobalStorage;

public class GlobalStorageTest {

    @Before
    public void setUp() {

        GlobalStorage.clear("test");
        GlobalStorage.put("test", 10);
        GlobalStorage.put("test", 7.43);
        GlobalStorage.put("test", "something");
    }

    @Test
    public void testGet() {

        List<Number> result1 = GlobalStorage.get("test", Number.class);
        Assert.assertTrue("Result does not contain put number", result1.contains(10));
        Assert.assertTrue("Result does not contain put number", result1.contains(7.43));

        List<StringBuilder> result2 = GlobalStorage.get("test", StringBuilder.class);
        Assert.assertEquals("Amount of string builder objects in global storage", 0, result2.size());
    }

    @Test
    public void testAddTwice() {

        // Put second instance of the same string into storage
        GlobalStorage.put("test", "something");

        List<String> result = GlobalStorage.get("test", String.class);
        Assert.assertEquals("Amount of string objects in global storage", 2, result.size());
    }

    @Test
    public void testRemove() {

        // Put second instance of the same string into storage
        GlobalStorage.put("test", "something");

        // Remove all "something" strings from storage
        GlobalStorage.remove("test", "something");

        List<String> result = GlobalStorage.get("test", String.class);
        Assert.assertEquals("Amount of string objects in global storage", 0, result.size());
    }

}
