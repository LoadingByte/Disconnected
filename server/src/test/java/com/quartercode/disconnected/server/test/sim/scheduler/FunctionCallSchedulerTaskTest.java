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

package com.quartercode.disconnected.server.test.sim.scheduler;

import static com.quartercode.classmod.factory.ClassmodFactory.factory;
import static org.junit.Assert.assertTrue;
import java.io.StringReader;
import java.io.StringWriter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.Before;
import org.junit.Test;
import com.quartercode.classmod.def.extra.conv.DefaultCFeatureHolder;
import com.quartercode.classmod.def.extra.prop.DefaultProperty;
import com.quartercode.classmod.extra.func.FunctionDefinition;
import com.quartercode.classmod.extra.func.FunctionExecutor;
import com.quartercode.classmod.extra.func.FunctionInvocation;
import com.quartercode.classmod.extra.storage.StandardStorage;
import com.quartercode.classmod.factory.FunctionDefinitionFactory;
import com.quartercode.classmod.util.FeatureDefinitionReference;
import com.quartercode.disconnected.server.sim.scheduler.FunctionCallSchedulerTask;
import com.quartercode.disconnected.server.sim.scheduler.SchedulerTask;

public class FunctionCallSchedulerTaskTest {

    private static boolean            executedTestFunction;

    private FunctionCallSchedulerTask task;

    @Before
    public void setUp() {

        executedTestFunction = false;

        task = new FunctionCallSchedulerTask();
        task.setObj(FunctionCallSchedulerTask.FUNCTION_DEFINITION, new FeatureDefinitionReference<FunctionDefinition<?>>(TestFeatureHolder.class, "TEST_FUNCTION"));
    }

    @Test
    public void testSchedule() {

        task.invoke(SchedulerTask.EXECUTE, new TestFeatureHolder());
        assertTrue("Function call scheduler task didn't call test function", executedTestFunction);
    }

    @Test
    public void testScheduleWithPersistence() throws JAXBException {

        Class<?>[] classes = { FunctionCallSchedulerTaskContainer.class, FunctionCallSchedulerTask.class, DefaultProperty.class, StandardStorage.class, FeatureDefinitionReference.class };
        JAXBContext context = JAXBContext.newInstance(classes);
        StringWriter serialized = new StringWriter();
        context.createMarshaller().marshal(new FunctionCallSchedulerTaskContainer(task), serialized);
        FunctionCallSchedulerTask taskCopy = ((FunctionCallSchedulerTaskContainer) context.createUnmarshaller().unmarshal(new StringReader(serialized.toString()))).getTask();

        taskCopy.invoke(SchedulerTask.EXECUTE, new TestFeatureHolder());
        assertTrue("Function call scheduler task didn't call test function after persistence roundtrip", executedTestFunction);
    }

    @XmlRootElement
    private static class TestFeatureHolder extends DefaultCFeatureHolder {

        public static final FunctionDefinition<String> TEST_FUNCTION;

        static {

            TEST_FUNCTION = factory(FunctionDefinitionFactory.class).create("testFunction", new Class[0]);
            TEST_FUNCTION.addExecutor("default", TestFeatureHolder.class, new FunctionExecutor<String>() {

                @Override
                public String invoke(FunctionInvocation<String> invocation, Object... arguments) {

                    executedTestFunction = true;
                    return invocation.next(arguments);
                }

            });

        }

    }

    @AllArgsConstructor
    @Getter
    @XmlRootElement
    protected static class FunctionCallSchedulerTaskContainer {

        @XmlElement
        private FunctionCallSchedulerTask task;

        // JAXB constructor
        protected FunctionCallSchedulerTaskContainer() {

        }

    }

}
