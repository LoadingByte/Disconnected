/*
 * This file is part of Disconnected.
 * Copyright (c) 2013 QuarterCode <http://quartercode.com/>
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

import static org.junit.Assert.assertEquals;
import java.io.StringReader;
import java.io.StringWriter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.junit.Test;
import com.quartercode.disconnected.server.sim.scheduler.DefaultScheduler;
import com.quartercode.disconnected.server.sim.scheduler.Scheduler;
import com.quartercode.disconnected.server.sim.scheduler.SchedulerTaskAdapter;
import com.quartercode.jtimber.api.node.Node;

public class SchedulerPersistenceTest {

    private static int schedulerTaskExecutions;

    @Test
    public void testScheduleWithPersistence() throws JAXBException {

        DefaultScheduler<?> scheduler = new DefaultScheduler<>();

        TestSchedulerTask task = new TestSchedulerTask();
        task.setProp(5);
        scheduler.schedule("testName", "testGroup", 5, 2, task);

        JAXBContext context = JAXBContext.newInstance(RootElement.class, TestSchedulerTask.class, DefaultScheduler.class);
        StringWriter serialized = new StringWriter();
        context.createMarshaller().marshal(new RootElement(scheduler), serialized);
        DefaultScheduler<?> copy = ((RootElement) context.createUnmarshaller().unmarshal(new StringReader(serialized.toString()))).getScheduler();

        for (int update = 0; update < 11; update++) {
            copy.update("testGroup");
        }

        assertEquals("Scheduler task executions after 11 updates", 4 * 5, schedulerTaskExecutions);
    }

    @Setter
    private static class TestSchedulerTask extends SchedulerTaskAdapter<Node<?>> {

        @XmlElement
        private int prop;

        @Override
        public void execute(Scheduler<? extends Node<?>> scheduler, Node<?> schedulerParent) {

            schedulerTaskExecutions += prop;
        }

    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @XmlRootElement
    private static class RootElement {

        @XmlElement
        private DefaultScheduler<?> scheduler;

    }

}
