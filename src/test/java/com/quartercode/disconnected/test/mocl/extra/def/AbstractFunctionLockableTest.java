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
import java.util.HashSet;
import java.util.List;
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
import com.quartercode.disconnected.mocl.extra.Lockable;
import com.quartercode.disconnected.mocl.extra.StopExecutionException;
import com.quartercode.disconnected.mocl.extra.def.AbstractFunction;

@RunWith (Parameterized.class)
public class AbstractFunctionLockableTest {

    @Parameters
    public static Collection<Object[]> data() {

        List<Object[]> data = new ArrayList<Object[]>();

        data.add(new Object[] { new boolean[] { true, true }, false });
        data.add(new Object[] { new boolean[] { true, false }, true });

        return data;
    }

    private final boolean[] expectedInvokations;
    private final boolean   locked;

    public AbstractFunctionLockableTest(boolean[] expectedInvokations, boolean locked) {

        this.expectedInvokations = expectedInvokations;
        this.locked = locked;
    }

    @Test
    public void testInvoke() throws InstantiationException, IllegalAccessException, FunctionExecutionException {

        final boolean[] actualInvokations = new boolean[2];
        Set<FunctionExecutor<Void>> executors = new HashSet<FunctionExecutor<Void>>();

        executors.add(new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FeatureHolder holder, Object... arguments) throws StopExecutionException {

                actualInvokations[0] = true;
                return null;
            }

        });

        executors.add(new FunctionExecutor<Void>() {

            @Override
            @Lockable
            public Void invoke(FeatureHolder holder, Object... arguments) throws StopExecutionException {

                actualInvokations[1] = true;
                return null;
            }

        });

        AbstractFunction<Void> function = new AbstractFunction<Void>("testFunction", new DefaultFeatureHolder(), new ArrayList<Class<?>>(), executors);
        function.setLocked(locked);
        function.invoke();

        Assert.assertTrue("Invokation pattern doesn't equal", Arrays.equals(expectedInvokations, actualInvokations));
    }

}
