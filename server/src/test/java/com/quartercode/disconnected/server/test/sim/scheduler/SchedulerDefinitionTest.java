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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import com.quartercode.classmod.def.extra.conv.DefaultCFeatureHolder;
import com.quartercode.classmod.extra.conv.CFeatureHolder;
import com.quartercode.disconnected.server.sim.scheduler.Scheduler;
import com.quartercode.disconnected.server.sim.scheduler.SchedulerDefinition;
import com.quartercode.disconnected.server.sim.scheduler.SchedulerTask;
import com.quartercode.disconnected.server.sim.scheduler.SchedulerTaskAdapter;

public class SchedulerDefinitionTest {

    private SchedulerDefinition schedulerDefinition;

    @Before
    public void setUp() {

        schedulerDefinition = new SchedulerDefinition("testSchedulerDefinition");
    }

    @Test
    public void testScheduleAndGet() {

        SchedulerTask testTask1 = new TestSchedulerTask("testTask1", 1);
        SchedulerTask testTask2 = new TestSchedulerTask("testTask2", 1);
        SchedulerTask testTask3 = new TestSchedulerTask("testTask3", 1);

        schedulerDefinition.schedule(testTask1, FeatureHolder1.class);
        schedulerDefinition.schedule(testTask2, FeatureHolder1.class);
        schedulerDefinition.schedule(testTask3, FeatureHolder1.class);

        assertEquals("Scheduled task with name 'testTask2'", testTask2, schedulerDefinition.getGlobalTask("testTask2", FeatureHolder1.class));
    }

    @Test
    public void testScheduleAndCreate() {

        SchedulerTask testTask = new TestSchedulerTask("testTask", 1);

        schedulerDefinition.schedule(testTask, FeatureHolder1.class);

        Scheduler scheduler = new FeatureHolder1().get(schedulerDefinition);
        assertEquals("Scheduler object's tasks", new ArrayList<>(Arrays.asList(testTask)), new ArrayList<>(scheduler.getTasks()));
    }

    @Test
    public void testScheduleAndRemoveAndCreate() {

        SchedulerTask testTask = new TestSchedulerTask("testTask", 1);

        schedulerDefinition.schedule(testTask, FeatureHolder1.class);
        schedulerDefinition.remove(testTask, FeatureHolder1.class);

        Scheduler scheduler = new FeatureHolder1().get(schedulerDefinition);
        assertTrue("Scheduler object's tasks are not empty", scheduler.getTasks().isEmpty());
    }

    @Test
    public void testScheduleAndRemoveWithNameAndCreate() {

        SchedulerTask testTask = new TestSchedulerTask("testTask", 1);

        schedulerDefinition.schedule(testTask, FeatureHolder1.class);
        schedulerDefinition.remove("testTask", FeatureHolder1.class);

        Scheduler scheduler = new FeatureHolder1().get(schedulerDefinition);
        assertTrue("Scheduler object's tasks are not empty", scheduler.getTasks().isEmpty());
    }

    @Test
    public void testScheduleAndCreateWithDifferentHolderClasses() {

        SchedulerTask testTask1 = new TestSchedulerTask("testTask1", 1);
        SchedulerTask testTask2 = new TestSchedulerTask("testTask2", 1);
        SchedulerTask testTask3 = new TestSchedulerTask("testTask3", 1);

        schedulerDefinition.schedule(testTask1, FeatureHolder1.class);
        schedulerDefinition.schedule(testTask2, FeatureHolder2.class);
        schedulerDefinition.schedule(testTask3, FeatureHolder3Extends2.class);

        Scheduler scheduler1 = new FeatureHolder1().get(schedulerDefinition);
        assertEquals("Scheduler object's tasks with FeatureHolder1", new ArrayList<>(Arrays.asList(testTask1)), new ArrayList<>(scheduler1.getTasks()));

        Scheduler scheduler2 = new FeatureHolder2().get(schedulerDefinition);
        assertEquals("Scheduler object's tasks with FeatureHolder1", new ArrayList<>(Arrays.asList(testTask2)), new ArrayList<>(scheduler2.getTasks()));

        Scheduler scheduler3 = new FeatureHolder3Extends2().get(schedulerDefinition);
        assertEquals("Scheduler object's tasks with FeatureHolder3Extands2", new ArrayList<>(Arrays.asList(testTask2, testTask3)), new ArrayList<>(scheduler3.getTasks()));
    }

    @Test
    public void testScheduleTwice() {

        SchedulerTask testTask = new TestSchedulerTask("testTask", 1);

        schedulerDefinition.schedule(testTask, FeatureHolder1.class);
        schedulerDefinition.schedule(testTask, FeatureHolder1.class);
    }

    @Test
    public void testScheduleTwiceDifferentHolderClass() {

        SchedulerTask testTask = new TestSchedulerTask("testTask", 1);

        schedulerDefinition.schedule(testTask, FeatureHolder1.class);
        schedulerDefinition.schedule(testTask, FeatureHolder2.class);
    }

    private static class TestSchedulerTask extends SchedulerTaskAdapter {

        private TestSchedulerTask(String name, int id) {

            super(name, "testGroup" + id, id, id);
        }

        @Override
        public void execute(CFeatureHolder holder) {

            // Empty
        }

    }

    private static class FeatureHolder1 extends DefaultCFeatureHolder {

    }

    private static class FeatureHolder2 extends DefaultCFeatureHolder {

    }

    private static class FeatureHolder3Extends2 extends FeatureHolder2 {

    }

}
