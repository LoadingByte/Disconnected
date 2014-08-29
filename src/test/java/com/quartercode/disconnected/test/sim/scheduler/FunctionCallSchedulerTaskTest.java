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

package com.quartercode.disconnected.test.sim.scheduler;

import static com.quartercode.classmod.ClassmodFactory.create;
import java.io.StringReader;
import java.io.StringWriter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlRootElement;
import org.apache.commons.lang3.reflect.TypeLiteral;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.quartercode.classmod.base.FeatureDefinition;
import com.quartercode.classmod.base.def.DefaultFeatureHolder;
import com.quartercode.classmod.extra.FunctionDefinition;
import com.quartercode.classmod.extra.FunctionExecutor;
import com.quartercode.classmod.extra.FunctionInvocation;
import com.quartercode.classmod.util.FeatureDefinitionReference;
import com.quartercode.disconnected.sim.scheduler.FunctionCallSchedulerTask;
import com.quartercode.disconnected.sim.scheduler.Scheduler;
import com.quartercode.disconnected.sim.scheduler.SchedulerDefinition;

public class FunctionCallSchedulerTaskTest {

    private static boolean    executedTestFunction;

    private TestFeatureHolder schedulerHolder;
    private Scheduler         scheduler;

    @Before
    public void setUp() {

        executedTestFunction = false;

        schedulerHolder = new TestFeatureHolder();
        scheduler = schedulerHolder.get(TestFeatureHolder.SCHEDULER);

        scheduler.schedule(new FunctionCallSchedulerTask(1, "testGroup", new FeatureDefinitionReference<FunctionDefinition<?>>(TestFeatureHolder.class, "TEST_FUNCTION")));
    }

    @Test
    public void testSchedule() {

        scheduler.update("testGroup");
        Assert.assertTrue("Function call scheduler task didn't call test function", executedTestFunction);
    }

    @Test
    public void testScheduleWithPersistence() throws JAXBException {

        JAXBContext context = JAXBContext.newInstance(Scheduler.class, FunctionCallSchedulerTask.class, TestFeatureHolder.class);
        StringWriter serialized = new StringWriter();
        context.createMarshaller().marshal(schedulerHolder, serialized);
        TestFeatureHolder schedulerHolderCopy = (TestFeatureHolder) context.createUnmarshaller().unmarshal(new StringReader(serialized.toString()));
        Scheduler copy = schedulerHolderCopy.get(TestFeatureHolder.SCHEDULER);

        copy.update("testGroup");
        Assert.assertTrue("Function call scheduler task didn't call test function", executedTestFunction);
    }

    @XmlRootElement
    private static class TestFeatureHolder extends DefaultFeatureHolder {

        public static final FeatureDefinition<Scheduler> SCHEDULER = new SchedulerDefinition("scheduler");

        public static final FunctionDefinition<String>   TEST_FUNCTION;

        static {

            TEST_FUNCTION = create(new TypeLiteral<FunctionDefinition<String>>() {}, "name", "testFunction", "parameters", new Class[0]);
            TEST_FUNCTION.addExecutor("default", TestFeatureHolder.class, new FunctionExecutor<String>() {

                @Override
                public String invoke(FunctionInvocation<String> invocation, Object... arguments) {

                    executedTestFunction = true;
                    return invocation.next(arguments);
                }

            });

        }

    }

}