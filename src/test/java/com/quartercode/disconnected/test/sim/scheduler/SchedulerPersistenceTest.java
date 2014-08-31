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

import static org.junit.Assert.assertEquals;
import java.io.StringReader;
import java.io.StringWriter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import org.junit.Test;
import com.quartercode.classmod.base.FeatureHolder;
import com.quartercode.classmod.base.def.DefaultFeatureHolder;
import com.quartercode.disconnected.sim.scheduler.Scheduler;
import com.quartercode.disconnected.sim.scheduler.SchedulerTaskAdapter;

public class SchedulerPersistenceTest {

    private static int schedulerTaskExecutions;

    @Test
    public void testScheduleWithPersistence() throws JAXBException {

        Scheduler scheduler = new Scheduler("scheduler", new DefaultFeatureHolder());
        scheduler.schedule(new TestSchedulerTask(5, 2));

        JAXBContext context = JAXBContext.newInstance(Scheduler.class, TestSchedulerTask.class);
        StringWriter serialized = new StringWriter();
        context.createMarshaller().marshal(scheduler, serialized);
        Scheduler copy = (Scheduler) context.createUnmarshaller().unmarshal(new StringReader(serialized.toString()));

        for (int update = 0; update < 11; update++) {
            copy.update("testGroup");
        }

        assertEquals("Scheduler task executions after 11 updates", 4, schedulerTaskExecutions);
    }

    private static class TestSchedulerTask extends SchedulerTaskAdapter {

        // JAXB constructor
        @SuppressWarnings ("unused")
        protected TestSchedulerTask() {

        }

        private TestSchedulerTask(int initialDelay, int periodicDelay) {

            super(initialDelay, periodicDelay, "testGroup");
        }

        @Override
        public void execute(FeatureHolder holder) {

            schedulerTaskExecutions++;
        }

    }

}
