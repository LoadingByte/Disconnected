
package com.quartercode.disconnected.test.mocl.extra.def;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.junit.Assert;
import org.junit.Test;
import com.quartercode.disconnected.mocl.base.FeatureHolder;
import com.quartercode.disconnected.mocl.extra.FunctionExecutor;
import com.quartercode.disconnected.mocl.extra.StopExecutionException;
import com.quartercode.disconnected.mocl.extra.def.AbstractFunction.DefaultFunctionExecutorContainer;

public class DefaultFunctionExecutorContainerTest {

    @Test
    public void testGetValueAnnotated() {

        DefaultFunctionExecutorContainer<Void> container = new DefaultFunctionExecutorContainer<Void>("test", new FunctionExecutor<Void>() {

            @Override
            @TestAnnotation (value1 = 7, value2 = "test")
            public Void invoke(FeatureHolder holder, Object... arguments) throws StopExecutionException {

                return null;
            }
        });

        Assert.assertEquals("First read annotation value", 7, container.getValue(TestAnnotation.class, "value1"));
        Assert.assertEquals("Second read annotation value", "test", container.getValue(TestAnnotation.class, "value2"));
    }

    @Test
    public void testGetValueDefault() {

        DefaultFunctionExecutorContainer<Void> container = new DefaultFunctionExecutorContainer<Void>("test", new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FeatureHolder holder, Object... arguments) throws StopExecutionException {

                return null;
            }
        });

        Assert.assertEquals("First read annotation value", 2, container.getValue(TestAnnotation.class, "value1"));
        Assert.assertEquals("Second read annotation value", "defaultvalue", container.getValue(TestAnnotation.class, "value2"));
    }

    @Test
    public void testSetValue() {

        DefaultFunctionExecutorContainer<Void> container = new DefaultFunctionExecutorContainer<Void>("test", new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FeatureHolder holder, Object... arguments) throws StopExecutionException {

                return null;
            }
        });

        container.setValue(TestAnnotation.class, "value1", 17);
        container.setValue(TestAnnotation.class, "value2", "testvalue");

        Assert.assertEquals("First read annotation value", 17, container.getValue(TestAnnotation.class, "value1"));
        Assert.assertEquals("Second read annotation value", "testvalue", container.getValue(TestAnnotation.class, "value2"));
    }

    @Retention (RetentionPolicy.RUNTIME)
    public static @interface TestAnnotation {

        int value1 () default 2;

        String value2 () default "defaultvalue";

    }

}
