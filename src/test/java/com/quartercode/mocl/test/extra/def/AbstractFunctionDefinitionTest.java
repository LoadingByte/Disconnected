
package com.quartercode.mocl.test.extra.def;

import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.quartercode.mocl.base.FeatureHolder;
import com.quartercode.mocl.extra.Function;
import com.quartercode.mocl.extra.FunctionExecutor;
import com.quartercode.mocl.extra.def.AbstractFunction;
import com.quartercode.mocl.extra.def.AbstractFunctionDefinition;

public class AbstractFunctionDefinitionTest {

    private AbstractFunctionDefinition<Void> functionDefinition;

    @Before
    public void setUp() {

        functionDefinition = new AbstractFunctionDefinition<Void>("testFunctionDefinition") {

            @Override
            protected Function<Void> create(FeatureHolder holder, List<FunctionExecutor<Void>> executors) {

                return new AbstractFunction<Void>(getName(), holder, executors);
            }

        };
    }

    @Test
    public void testCreateFeatureHolder() {

        FunctionExecutor<Void> executor = new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FeatureHolder holder, Object... arguments) {

                return null;
            }

        };
        functionDefinition.addExecutor("default", executor);
        Function<Void> function = functionDefinition.create(null);

        List<FunctionExecutor<Void>> expectedExecutors = new ArrayList<FunctionExecutor<Void>>();
        expectedExecutors.add(executor);
        Assert.assertEquals("Function object's executors", expectedExecutors, function.getExecutors());
    }

}
