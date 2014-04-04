
package com.quartercode.disconnected.test.sim;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import com.quartercode.classmod.base.FeatureDefinition;
import com.quartercode.classmod.base.def.DefaultFeatureHolder;
import com.quartercode.classmod.extra.ExecutorInvocationException;
import com.quartercode.classmod.extra.FunctionDefinition;
import com.quartercode.classmod.extra.FunctionExecutor;
import com.quartercode.classmod.extra.FunctionInvocation;
import com.quartercode.classmod.util.FunctionDefinitionFactory;
import com.quartercode.disconnected.sim.run.FunctionCallScheduleTask;
import com.quartercode.disconnected.sim.run.Scheduler;

@RunWith (Parameterized.class)
public class FunctionCallScheduleTaskTest {

    private static boolean executedTestFunction;

    @Parameters
    public static Collection<Object[]> data() {

        List<Object[]> data = new ArrayList<Object[]>();

        data.add(new Object[] { 1 });
        data.add(new Object[] { 5 });
        data.add(new Object[] { 100 });

        return data;
    }

    private final int         delay;

    private TestFeatureHolder schedulerHolder;
    private Scheduler         scheduler;

    public FunctionCallScheduleTaskTest(int delay) {

        this.delay = delay;
    }

    @Before
    public void setUp() {

        executedTestFunction = false;

        schedulerHolder = new TestFeatureHolder();
        scheduler = schedulerHolder.get(TestFeatureHolder.SCHEDULER);
    }

    @Test
    public void testSchedule() throws ExecutorInvocationException {

        scheduler.schedule(new FunctionCallScheduleTask(TestFeatureHolder.TEST_FUNCTION_WITH_COMPLEX_NAME, TestFeatureHolder.class), delay);

        for (int update = 0; update < delay; update++) {
            scheduler.update();
        }

        Assert.assertTrue("Function call schedule task with delay of " + delay + " called function after " + (delay + 1) + " updates", executedTestFunction);
    }

    @Test
    public void testScheduleWithPersistence() throws JAXBException, ExecutorInvocationException {

        scheduler.schedule(new FunctionCallScheduleTask(TestFeatureHolder.TEST_FUNCTION_WITH_COMPLEX_NAME, TestFeatureHolder.class), delay);

        JAXBContext context = JAXBContext.newInstance(Scheduler.class, FunctionCallScheduleTask.class, TestFeatureHolder.class, RootElement.class);
        StringWriter serialized = new StringWriter();
        RootElement root = new RootElement();
        root.testFeatureHolder = schedulerHolder;
        context.createMarshaller().marshal(root, serialized);
        RootElement rootCopy = (RootElement) context.createUnmarshaller().unmarshal(new StringReader(serialized.toString()));
        Scheduler copy = rootCopy.testFeatureHolder.get(TestFeatureHolder.SCHEDULER);

        for (int update = 0; update < delay; update++) {
            copy.update();
        }

        Assert.assertTrue("Function call schedule task with delay of " + delay + " called function after " + (delay + 1) + " updates", executedTestFunction);
    }

    private static class TestFeatureHolder extends DefaultFeatureHolder {

        public static final FeatureDefinition<Scheduler> SCHEDULER = Scheduler.createDefinition("scheduler");

        public static final FunctionDefinition<String>   TEST_FUNCTION_WITH_COMPLEX_NAME;

        static {

            TEST_FUNCTION_WITH_COMPLEX_NAME = FunctionDefinitionFactory.create("testFunctionWithComplexName", TestFeatureHolder.class, new FunctionExecutor<String>() {

                @Override
                public String invoke(FunctionInvocation<String> invocation, Object... arguments) throws ExecutorInvocationException {

                    executedTestFunction = true;
                    return invocation.next(arguments);
                }

            });

        }

    }

    @XmlRootElement
    private static class RootElement {

        @XmlElement
        public TestFeatureHolder testFeatureHolder;

    }

}
