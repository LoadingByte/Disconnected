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

package com.quartercode.disconnected.test.world.comp;

import static org.junit.Assert.assertEquals;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import com.quartercode.classmod.base.def.DefaultFeatureHolder;
import com.quartercode.classmod.extra.FunctionExecutor;
import com.quartercode.classmod.extra.FunctionInvocation;
import com.quartercode.disconnected.util.NullPreventer;
import com.quartercode.disconnected.world.comp.SizeUtil;
import com.quartercode.disconnected.world.comp.SizeUtil.DerivableSize;

@RunWith (Parameterized.class)
public class SizeUtilTest {

    @Parameters
    public static Collection<Object[]> data() {

        List<Object[]> data = new ArrayList<>();

        // Boolean
        data.add(new Object[] { true, 1 });

        // Integers (as well as bytes, shorts and longs)
        data.add(new Object[] { 0, 1 });
        data.add(new Object[] { 100, 1 });
        data.add(new Object[] { 127, 1 });
        data.add(new Object[] { 128, 2 });
        data.add(new Object[] { 1000, 2 });
        data.add(new Object[] { Integer.MAX_VALUE, 4 });

        // Doubles (as well as floats)
        data.add(new Object[] { 0D, 1 });
        data.add(new Object[] { 0.5D, 1 });
        data.add(new Object[] { 100D, 1 });
        data.add(new Object[] { 127D, 1 });
        data.add(new Object[] { 128D, 2 });
        data.add(new Object[] { 1000D, 2 });
        data.add(new Object[] { Double.MAX_VALUE, 8 });

        // Strings (as well as chars)
        data.add(new Object[] { "Test", 4 });
        data.add(new Object[] { "Stringystring", 13 });

        // Lists (as well as other iterables)
        data.add(new Object[] { Arrays.asList(new Object[] { "Test", true, 128 }), 4 + 1 + 2 });

        // Feature holders
        DerivableSize.GET_SIZE.addExecutor("test1", TestFeatureHolder.class, new FunctionExecutor<Long>() {

            @Override
            public Long invoke(FunctionInvocation<Long> invocation, Object... arguments) {

                return 100L + NullPreventer.prevent(invocation.next(arguments));
            }
        });
        DerivableSize.GET_SIZE.addExecutor("test2", TestFeatureHolder.class, new FunctionExecutor<Long>() {

            @Override
            public Long invoke(FunctionInvocation<Long> invocation, Object... arguments) {

                return 500L + NullPreventer.prevent(invocation.next(arguments));
            }
        });
        data.add(new Object[] { new TestFeatureHolder(), 100 + 500 });

        // Unknown objects or null (should return 0)
        data.add(new Object[] { null, 0 });
        data.add(new Object[] { new Exception(), 0 });

        return data;
    }

    private final Object object;
    private final long   expectedSize;

    public SizeUtilTest(Object object, long expectedSize) {

        this.object = object;
        this.expectedSize = expectedSize;
    }

    @Test
    public void testGetSize() {

        assertEquals("Calculated Size", expectedSize, SizeUtil.getSize(object));
    }

    private static class TestFeatureHolder extends DefaultFeatureHolder implements DerivableSize {

    }

}
