
package com.quartercode.mocl.test.extra.def;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import com.quartercode.mocl.base.FeatureHolder;
import com.quartercode.mocl.extra.Execution;
import com.quartercode.mocl.extra.Execution.ExecutionPolicy;
import com.quartercode.mocl.extra.FunctionExecutor;
import com.quartercode.mocl.extra.Prioritized;
import com.quartercode.mocl.extra.def.AbstractFunction;

public class AbstractFunctionTest {

    private AbstractFunction<Object> function;

    @Test
    @Ignore
    public void testInvoke() {

        final List<Object> actualArguments = new ArrayList<Object>();
        final Object returnValue = "ReturnValue";
        FunctionExecutor<Object> executor = new FunctionExecutor<Object>() {

            @Override
            public Object invoke(FeatureHolder holder, Object... arguments) {

                actualArguments.addAll(Arrays.asList(arguments));
                return returnValue;
            }

        };

        List<FunctionExecutor<Object>> executors = new ArrayList<FunctionExecutor<Object>>();
        executors.add(executor);
        function = new AbstractFunction<Object>("testFunction", null, executors);

        List<Object> arguments = new ArrayList<Object>();
        arguments.add("Test");
        arguments.add(String.class);
        arguments.add(new Object[] { "Test", 12345, true });
        Object actualReturnValue = function.invoke(arguments.toArray(new Object[arguments.size()]));

        Assert.assertEquals("Received arguments", arguments, actualArguments);
        Assert.assertEquals("Received return value", returnValue, actualReturnValue);
    }

    @Test
    public void testInvokePriority() {

        List<FunctionExecutor<Object>> executors = new ArrayList<FunctionExecutor<Object>>();
        executors.add(new FunctionExecutor1<Object>());
        executors.add(new FunctionExecutor2<Object>());
        executors.add(new FunctionExecutor3<Object>());
        function = new AbstractFunction<Object>("testFunction", null, executors);
        function.invoke();

        Assert.assertTrue("Executor 1 wasn't invoked", invokedFunctionExecutor1);
        Assert.assertTrue("Executor 2 wasn't invoked", invokedFunctionExecutor2);
        Assert.assertFalse("Executor 3 was invoked", invokedFunctionExecutor3);
    }

    private boolean invokedFunctionExecutor1;

    @Prioritized (3)
    private class FunctionExecutor1<R> implements FunctionExecutor<R> {

        @Override
        public R invoke(FeatureHolder holder, Object... arguments) {

            invokedFunctionExecutor1 = true;
            return null;
        }

    }

    private boolean invokedFunctionExecutor2;

    @Prioritized (2)
    @Execution (ExecutionPolicy.THIS)
    private class FunctionExecutor2<R> implements FunctionExecutor<R> {

        @Override
        public R invoke(FeatureHolder holder, Object... arguments) {

            invokedFunctionExecutor2 = true;
            return null;
        }

    }

    private boolean invokedFunctionExecutor3;

    @Prioritized (1)
    private class FunctionExecutor3<R> implements FunctionExecutor<R> {

        @Override
        public R invoke(FeatureHolder holder, Object... arguments) {

            invokedFunctionExecutor3 = true;
            return null;
        }

    }

}
