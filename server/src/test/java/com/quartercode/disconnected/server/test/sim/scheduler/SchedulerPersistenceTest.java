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
import static org.junit.Assert.assertEquals;
import java.io.StringReader;
import java.io.StringWriter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import org.junit.Test;
import com.quartercode.classmod.def.extra.conv.DefaultCFeatureHolder;
import com.quartercode.classmod.def.extra.prop.DefaultProperty;
import com.quartercode.classmod.extra.func.FunctionExecutor;
import com.quartercode.classmod.extra.func.FunctionInvocation;
import com.quartercode.classmod.extra.prop.PropertyDefinition;
import com.quartercode.classmod.extra.storage.StandardStorage;
import com.quartercode.classmod.factory.PropertyDefinitionFactory;
import com.quartercode.disconnected.server.sim.scheduler.DefaultScheduler;
import com.quartercode.disconnected.server.sim.scheduler.SchedulerTaskAdapter;

public class SchedulerPersistenceTest {

    private static int schedulerTaskExecutions;

    @Test
    public void testScheduleWithPersistence() throws JAXBException {

        DefaultScheduler scheduler = new DefaultScheduler("scheduler", new DefaultCFeatureHolder());

        TestSchedulerTask task = new TestSchedulerTask();
        task.setObj(TestSchedulerTask.PROP, 5);
        scheduler.schedule("testName", "testGroup", 5, 2, task);

        JAXBContext context = JAXBContext.newInstance(TestSchedulerTask.class, DefaultScheduler.class, DefaultProperty.class, StandardStorage.class);
        StringWriter serialized = new StringWriter();
        context.createMarshaller().marshal(scheduler, serialized);
        DefaultScheduler copy = (DefaultScheduler) context.createUnmarshaller().unmarshal(new StringReader(serialized.toString()));

        for (int update = 0; update < 11; update++) {
            copy.update("testGroup");
        }

        assertEquals("Scheduler task executions after 11 updates", 4 * 5, schedulerTaskExecutions);
    }

    private static class TestSchedulerTask extends SchedulerTaskAdapter {

        // ----- Properties -----

        public static final PropertyDefinition<Integer> PROP;

        static {

            PROP = factory(PropertyDefinitionFactory.class).create("prop", new StandardStorage<>());

        }

        // ----- Functions -----

        static {

            EXECUTE.addExecutor("default", TestSchedulerTask.class, new FunctionExecutor<Void>() {

                @Override
                public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                    schedulerTaskExecutions += invocation.getCHolder().getObj(PROP);

                    return invocation.next(arguments);
                }

            });

        }

    }

}
