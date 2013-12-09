
package com.quartercode.disconnected.test.mocl.extra.def;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.quartercode.disconnected.mocl.base.FeatureHolder;
import com.quartercode.disconnected.mocl.base.def.DefaultFeatureHolder;
import com.quartercode.disconnected.mocl.extra.Function;
import com.quartercode.disconnected.mocl.extra.FunctionExecutor;
import com.quartercode.disconnected.mocl.extra.StopExecutionException;
import com.quartercode.disconnected.mocl.extra.def.AbstractFunction;
import com.quartercode.disconnected.mocl.extra.def.AbstractFunctionDefinition;

public class AbstractFunctionDefinitionTest {

    private AbstractFunctionDefinition<Void> functionDefinition;

    @Before
    public void setUp() {

        functionDefinition = new AbstractFunctionDefinition<Void>("testFunctionDefinition") {

            @Override
            protected Function<Void> create(FeatureHolder holder, List<Class<?>> parameters, Map<Class<? extends FeatureHolder>, Set<FunctionExecutor<Void>>> executors) {

                return new AbstractFunction<Void>(getName(), holder, parameters, executors);
            }

        };
    }

    @SuppressWarnings ("unchecked")
    @Test
    public void testSetParameter() {

        functionDefinition.setParameter(0, String.class);
        functionDefinition.setParameter(5, Object.class);
        functionDefinition.setParameter(3, Integer.class);
        functionDefinition.setParameter(5, null);

        Assert.assertEquals("Parameter pattern", Arrays.asList(String.class, null, null, Integer.class), functionDefinition.getParameters());
    }

    @Test
    public void testCreateFeatureHolder() {

        FunctionExecutor<Void> executor = new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FeatureHolder holder, Object... arguments) throws StopExecutionException {

                return null;
            }

        };
        functionDefinition.addExecutor(FeatureHolder.class, "default", executor);
        functionDefinition.setParameter(0, String.class);
        Function<Void> function = functionDefinition.create(new DefaultFeatureHolder());

        List<Class<?>> expectedParameters = new ArrayList<Class<?>>();
        expectedParameters.add(String.class);
        Set<FunctionExecutor<Void>> expectedExecutors = new HashSet<FunctionExecutor<Void>>();
        expectedExecutors.add(executor);
        Assert.assertEquals("Function object's parameters", expectedParameters, function.getParameters());
        Assert.assertEquals("Function object's executors", expectedExecutors, function.getExecutors());
    }

}
