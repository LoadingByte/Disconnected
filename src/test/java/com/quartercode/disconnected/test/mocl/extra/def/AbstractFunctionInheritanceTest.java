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

package com.quartercode.disconnected.test.mocl.extra.def;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import com.quartercode.disconnected.mocl.base.FeatureHolder;
import com.quartercode.disconnected.mocl.base.def.DefaultFeatureHolder;
import com.quartercode.disconnected.mocl.extra.FunctionExecutionException;
import com.quartercode.disconnected.mocl.extra.FunctionExecutor;
import com.quartercode.disconnected.mocl.extra.StopExecutionException;
import com.quartercode.disconnected.mocl.extra.def.AbstractFunction;

@RunWith (Parameterized.class)
public class AbstractFunctionInheritanceTest {

    @Parameters
    public static Collection<Object[]> data() {

        List<Object[]> data = new ArrayList<Object[]>();

        data.add(new Object[] { new Object[][] { { Parent.class, true }, { Child.class, false } }, Parent.class });
        data.add(new Object[] { new Object[][] { { Parent.class, true }, { Child.class, true } }, Child.class });

        return data;
    }

    private final Object[][]                     executors;
    private final Class<? extends FeatureHolder> variant;

    public AbstractFunctionInheritanceTest(Object[][] executors, Class<? extends FeatureHolder> variant) {

        this.executors = executors;
        this.variant = variant;
    }

    private FunctionExecutor<Void> createTestExecutor(final boolean[] invokationArray, final int index) {

        return new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FeatureHolder holder, Object... arguments) throws StopExecutionException {

                invokationArray[index] = true;
                return null;
            }

        };
    }

    @SuppressWarnings ("unchecked")
    @Test
    public void testInvoke() throws InstantiationException, IllegalAccessException, FunctionExecutionException {

        Map<Class<? extends FeatureHolder>, Set<FunctionExecutor<Void>>> executorMap = new HashMap<Class<? extends FeatureHolder>, Set<FunctionExecutor<Void>>>();
        boolean[] expectedInvokations = new boolean[executors.length];
        boolean[] actualInvokations = new boolean[executors.length];

        int index = 0;
        for (Object[] entry : executors) {
            expectedInvokations[index] = (Boolean) entry[1];

            Class<? extends FeatureHolder> variant = (Class<? extends FeatureHolder>) entry[0];
            if (!executorMap.containsKey(variant)) {
                executorMap.put(variant, new HashSet<FunctionExecutor<Void>>());
            }
            executorMap.get(variant).add(createTestExecutor(actualInvokations, index));

            index++;
        }

        AbstractFunction<Void> function = new AbstractFunction<Void>("testFunction", variant.newInstance(), executorMap);
        function.invoke();

        Assert.assertTrue("Invokation pattern doesn't equal", Arrays.equals(expectedInvokations, actualInvokations));
    }

    public static class Parent extends DefaultFeatureHolder {

    }

    public static class Child extends Parent {

    }

}
