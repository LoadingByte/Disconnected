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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Assert;
import org.junit.Test;
import com.quartercode.disconnected.mocl.base.FeatureHolder;
import com.quartercode.disconnected.mocl.base.def.DefaultFeatureHolder;
import com.quartercode.disconnected.mocl.extra.ExecutorInvokationException;
import com.quartercode.disconnected.mocl.extra.FunctionExecutionException;
import com.quartercode.disconnected.mocl.extra.FunctionExecutor;
import com.quartercode.disconnected.mocl.extra.Prioritized;
import com.quartercode.disconnected.mocl.extra.ReturnNextException;
import com.quartercode.disconnected.mocl.extra.StopExecutionException;
import com.quartercode.disconnected.mocl.extra.def.AbstractFunction;

public class AbstractFunctionPriorityTest {

    @Test
    public void testInvoke() throws FunctionExecutionException {

        Map<String, FunctionExecutor<Integer>> executors = new HashMap<String, FunctionExecutor<Integer>>();

        final AtomicBoolean invokedFunctionExecutor1 = new AtomicBoolean();
        executors.put("4", new FunctionExecutor<Integer>() {

            @Override
            @Prioritized (4)
            public Integer invoke(FeatureHolder holder, Object... arguments) throws ExecutorInvokationException {

                invokedFunctionExecutor1.set(true);
                throw new ReturnNextException();
            }

        });

        final AtomicBoolean invokedFunctionExecutor2 = new AtomicBoolean();
        executors.put("1", new FunctionExecutor<Integer>() {

            @Override
            @Prioritized (3)
            public Integer invoke(FeatureHolder holder, Object... arguments) throws ExecutorInvokationException {

                invokedFunctionExecutor2.set(true);
                return 3;
            }

        });

        final AtomicBoolean invokedFunctionExecutor3 = new AtomicBoolean();
        executors.put("2", new FunctionExecutor<Integer>() {

            @Override
            @Prioritized (2)
            public Integer invoke(FeatureHolder holder, Object... arguments) throws ExecutorInvokationException {

                invokedFunctionExecutor3.set(true);
                throw new StopExecutionException("Test");
            }

        });

        final AtomicBoolean invokedFunctionExecutor4 = new AtomicBoolean();
        executors.put("3", new FunctionExecutor<Integer>() {

            @Override
            @Prioritized (1)
            public Integer invoke(FeatureHolder holder, Object... arguments) throws ExecutorInvokationException {

                invokedFunctionExecutor4.set(true);
                return 1;
            }

        });

        AbstractFunction<Integer> function = new AbstractFunction<Integer>("testFunction", new DefaultFeatureHolder(), new ArrayList<Class<?>>(), executors);
        int result = function.invoke();

        Assert.assertTrue("Executor 1 wasn't invoked", invokedFunctionExecutor1.get());
        Assert.assertTrue("Executor 2 wasn't invoked", invokedFunctionExecutor2.get());
        Assert.assertTrue("Executor 3 wasn't invoked", invokedFunctionExecutor3.get());
        Assert.assertFalse("Executor 4 was invoked", invokedFunctionExecutor4.get());
        Assert.assertEquals("Return value", 3, result);
    }

}
