
package com.quartercode.disconnected.server.test.world.comp.proc.prog;

import static org.junit.Assert.assertEquals;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.jmock.Expectations;
import org.jmock.Sequence;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Rule;
import org.junit.Test;
import com.quartercode.disconnected.server.world.comp.proc.prog.StaticTask;
import com.quartercode.disconnected.server.world.comp.proc.prog.StaticTask.StaticTaskMetadataProvider;
import com.quartercode.disconnected.server.world.comp.proc.task.def.MissingArgumentException;
import com.quartercode.disconnected.server.world.comp.proc.task.def.TaskMetadata;
import com.quartercode.disconnected.server.world.comp.proc.task.def.TaskMetadata.OptionalParameter;
import com.quartercode.disconnected.server.world.comp.proc.task.def.TaskMetadata.Parameter;
import com.quartercode.disconnected.server.world.comp.proc.task.exec.TaskRunner;

public class StaticTaskTest {

    @Rule
    public JUnitRuleMockery context = new JUnitRuleMockery();

    @Test
    public void testMetadataProvider() {

        TaskMetadata actualMetadata = new StaticTaskMetadataProvider().getMetadata(TestStaticTask.class);

        TaskMetadata expectedMetadata = new TaskMetadata(
                Arrays.asList(new Parameter("value1", String.class), new OptionalParameter("value2", Integer.class, 5)),
                Arrays.asList(
                        new TaskMetadata.Callback("success", Collections.<Parameter> emptyList()),
                        new TaskMetadata.Callback("error", Arrays.asList(
                                new Parameter("code", Integer.class),
                                new Parameter("message", String.class)))));

        // Since the order of the input parameters and callbacks cannot be guaranteed, use sets for the equality check
        assertEquals("Computed task metadata input parameters", new HashSet<>(expectedMetadata.getInputParameters()), new HashSet<>(actualMetadata.getInputParameters()));
        assertEquals("Computed task metadata callbacks", new HashSet<>(expectedMetadata.getCallbacks()), new HashSet<>(actualMetadata.getCallbacks()));
    }

    @Test
    public void testRunLeaveDefault() {

        TestStaticTask task = new TestStaticTask();

        Map<String, Object> inputArguments = new HashMap<>();
        inputArguments.put("value1", "testvalue1");
        task.run(inputArguments);

        assertEquals("Object set for field 'value1'", "testvalue1", task.value1);
        assertEquals("Object set for field 'value2'", 5, task.value2);
        assertEquals("Number of times run() has been called", 1, task.runCount);
    }

    @Test
    public void testRunOverrideDefault() {

        TestStaticTask task = new TestStaticTask();

        Map<String, Object> inputArguments = new HashMap<>();
        inputArguments.put("value1", "testvalue1");
        inputArguments.put("value2", 14);
        task.run(inputArguments);

        assertEquals("Object set for field 'value1'", "testvalue1", task.value1);
        assertEquals("Object set for field 'value2'", 14, task.value2);
        assertEquals("Number of times run() has been called", 1, task.runCount);
    }

    @Test (expected = MissingArgumentException.class)
    public void testRunNoRequiredArgument() {

        TestStaticTask task = new TestStaticTask();

        Map<String, Object> inputArguments = new HashMap<>();
        // No "value1", which is required
        inputArguments.put("value2", 14);
        task.run(inputArguments);
    }

    @Test
    public void testCallback() {

        final TaskRunner taskRunner = context.mock(TaskRunner.class);
        TestStaticTask task = new TestStaticTask();
        task.addParent(taskRunner);

        final Map<String, Object> errorOutputArguments = new HashMap<>();
        errorOutputArguments.put("code", 7);
        errorOutputArguments.put("message", "testerror");

        // @formatter:off
        context.checking(new Expectations() {{

            final Sequence callbacks = context.sequence("callbacks");

            oneOf(taskRunner).callback("success", Collections.<String, Object> emptyMap()); inSequence(callbacks);
            oneOf(taskRunner).callback("error", errorOutputArguments); inSequence(callbacks);

        }});
        // @formatter:on

        task.success();
        task.error(7, "testerror");
    }

    public static class TestStaticTask extends StaticTask {

        // ----- Interface -----

        @InputParameter
        private String value1;
        @InputParameter (optional = true)
        private int    value2;

        // Defaults
        {
            value2 = 5;
        }

        @Callback
        private void success() {

            callback("success");
        }

        @Callback (params = { "code", "message" })
        private void error(int code, String message) {

            callback("error", code, message);
        }

        // ----- Logic -----

        private int runCount = 0;

        @Override
        public void run() {

            runCount++;
        }

    }

}
