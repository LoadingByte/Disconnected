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
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Assert;
import org.junit.Test;
import com.quartercode.disconnected.mocl.base.FeatureHolder;
import com.quartercode.disconnected.mocl.base.def.DefaultFeatureHolder;
import com.quartercode.disconnected.mocl.extra.FunctionExecutionException;
import com.quartercode.disconnected.mocl.extra.FunctionExecutor;
import com.quartercode.disconnected.mocl.extra.Prioritized;
import com.quartercode.disconnected.mocl.extra.StopExecutionException;
import com.quartercode.disconnected.mocl.extra.def.AbstractFunction;

public class AbstractFunctionPriorityTest {

    @Test
    public void testInvoke() throws FunctionExecutionException {

        Set<FunctionExecutor<Void>> executors = new HashSet<FunctionExecutor<Void>>();

        final AtomicBoolean invokedFunctionExecutor1 = new AtomicBoolean();
        executors.add(new FunctionExecutor<Void>() {

            @Override
            @Prioritized (3)
            public Void invoke(FeatureHolder holder, Object... arguments) throws StopExecutionException {

                invokedFunctionExecutor1.set(true);
                return null;
            }

        });

        final AtomicBoolean invokedFunctionExecutor2 = new AtomicBoolean();
        executors.add(new FunctionExecutor<Void>() {

            @Override
            @Prioritized (2)
            public Void invoke(FeatureHolder holder, Object... arguments) throws StopExecutionException {

                invokedFunctionExecutor2.set(true);
                throw new StopExecutionException("Test");
            }

        });

        final AtomicBoolean invokedFunctionExecutor3 = new AtomicBoolean();
        executors.add(new FunctionExecutor<Void>() {

            @Override
            @Prioritized (1)
            public Void invoke(FeatureHolder holder, Object... arguments) throws StopExecutionException {

                invokedFunctionExecutor3.set(true);
                return null;
            }

        });

        AbstractFunction<Void> function = new AbstractFunction<Void>("testFunction", new DefaultFeatureHolder(), new ArrayList<Class<?>>(), executors);
        function.invoke();

        Assert.assertTrue("Executor 1 wasn't invoked", invokedFunctionExecutor1.get());
        Assert.assertTrue("Executor 2 wasn't invoked", invokedFunctionExecutor2.get());
        Assert.assertFalse("Executor 3 was invoked", invokedFunctionExecutor3.get());
    }

}
