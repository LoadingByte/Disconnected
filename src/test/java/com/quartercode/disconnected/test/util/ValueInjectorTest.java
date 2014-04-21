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

package com.quartercode.disconnected.test.util;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.quartercode.disconnected.util.InjectValue;
import com.quartercode.disconnected.util.ValueInjector;

public class ValueInjectorTest {

    private ValueInjector valueInjector;

    @Before
    public void setUp() {

        valueInjector = new ValueInjector();
        valueInjector.put("test1", 10);
        valueInjector.put("test2", "something");
    }

    @Test
    public void testRunOn() {

        TestClass1 object = new TestClass1();
        valueInjector.runOn(object);

        Assert.assertEquals("Injected value for 'test1'", 10, object.test1);
        Assert.assertEquals("Injected value for 'test2'", "something", object.test2);
    }

    @Test (expected = IllegalStateException.class)
    public void testRunOnMissingValue() {

        TestClass2 object = new TestClass2();
        valueInjector.runOn(object);
    }

    @Test
    public void testRunOnMissingValueAllowNull() {

        TestClass3 object = new TestClass3();
        valueInjector.runOn(object);

        Assert.assertEquals("Injected value for 'test1' (unavailable)", 0, object.test);
    }

    @Test (expected = IllegalStateException.class)
    public void testRunOnWrongType() {

        TestClass4 object = new TestClass4();
        valueInjector.runOn(object);
    }

    @Test
    public void testRunOnWrongAllowNull() {

        TestClass5 object = new TestClass5();
        valueInjector.runOn(object);

        Assert.assertEquals("Injected value for 'test1' (wrong type)", null, object.test);
    }

    private class TestClass1 {

        @InjectValue ("test1")
        private int    test1;

        @InjectValue ("test2")
        private String test2;

    }

    private class TestClass2 {

        // Not available
        @InjectValue ("test3")
        private int test;

    }

    private class TestClass3 {

        // Not available, but allow null
        @InjectValue (value = "test3", allowNull = true)
        private int test;

    }

    private class TestClass4 {

        // Wrong type
        @InjectValue ("test1")
        private String test;

    }

    private class TestClass5 {

        // Wrong type, but allow null
        @InjectValue (value = "test1", allowNull = true)
        private String test;

    }

}
