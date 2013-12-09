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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;
import com.quartercode.disconnected.mocl.base.FeatureHolder;
import com.quartercode.disconnected.mocl.base.def.DefaultFeatureHolder;
import com.quartercode.disconnected.mocl.extra.FunctionExecutionException;
import com.quartercode.disconnected.mocl.extra.FunctionExecutor;
import com.quartercode.disconnected.mocl.extra.StopExecutionException;
import com.quartercode.disconnected.mocl.extra.def.AbstractFunction;

public class AbstractFunctionTest {

    @Test
    public void testInvoke() throws FunctionExecutionException {

        final List<Object> actualArguments = new ArrayList<Object>();
        final Object returnValue = "ReturnValue";
        FunctionExecutor<Object> executor = new FunctionExecutor<Object>() {

            @Override
            public Object invoke(FeatureHolder holder, Object... arguments) throws StopExecutionException {

                actualArguments.addAll(Arrays.asList(arguments));
                return returnValue;
            }

        };

        Map<Class<? extends FeatureHolder>, Set<FunctionExecutor<Object>>> executorMap = new HashMap<Class<? extends FeatureHolder>, Set<FunctionExecutor<Object>>>();
        Set<FunctionExecutor<Object>> executors = new HashSet<FunctionExecutor<Object>>();
        executors.add(executor);
        executorMap.put(FeatureHolder.class, executors);
        AbstractFunction<Object> function = new AbstractFunction<Object>("testFunction", new DefaultFeatureHolder(), new ArrayList<Class<?>>(), executorMap);

        List<Object> arguments = new ArrayList<Object>();
        arguments.add("Test");
        arguments.add(String.class);
        arguments.add(new Object[] { "Test", 12345, true });
        Object actualReturnValue = function.invoke(arguments.toArray(new Object[arguments.size()]));

        Assert.assertEquals("Received arguments", arguments, actualArguments);
        Assert.assertEquals("Received return value", returnValue, actualReturnValue);
    }

}
