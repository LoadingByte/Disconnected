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
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.junit.Before;
import org.junit.Test;
import com.quartercode.classmod.base.FeatureHolder;
import com.quartercode.classmod.base.def.DefaultFeatureHolder;
import com.quartercode.disconnected.server.sim.scheduler.Scheduler;
import com.quartercode.disconnected.server.sim.scheduler.SchedulerDefinition;
import com.quartercode.disconnected.server.sim.scheduler.SchedulerTask;
import com.quartercode.disconnected.server.sim.scheduler.SchedulerTaskAdapter;

public class SchedulerDefinitionTest {

    private SchedulerDefinition schedulerDefinition;
    private SchedulerTask       testTask1;
    private SchedulerTask       testTask2;
    private SchedulerTask       testTask3;

    @Before
    public void setUp() {

        ToStringBuilder.setDefaultStyle(ToStringStyle.SHORT_PREFIX_STYLE);

        schedulerDefinition = new SchedulerDefinition("testSchedulerDefinition");

        testTask1 = new TestSchedulerTask(1);
        testTask2 = new TestSchedulerTask(2);
        testTask3 = new TestSchedulerTask(3);
    }

    @Test
    public void testScheduleAndCreate() {

        schedulerDefinition.schedule("testTask", FeatureHolder1.class, testTask1);

        Scheduler scheduler = new FeatureHolder1().get(schedulerDefinition);
        assertEquals("Scheduler object's tasks", new ArrayList<>(Arrays.asList(testTask1)), new ArrayList<>(scheduler.getTasks()));
    }

    @Test
    public void testScheduleAndRemoveAndCreate() {

        schedulerDefinition.schedule("testTask", FeatureHolder1.class, testTask1);
        schedulerDefinition.remove("testTask", FeatureHolder1.class);

        Scheduler scheduler = new FeatureHolder1().get(schedulerDefinition);
        assertTrue("Scheduler object's tasks are not empty", scheduler.getTasks().isEmpty());
    }

    @Test
    public void testScheduleAndCreateWithDifferentHolderClasses() {

        schedulerDefinition.schedule("testTask1", FeatureHolder1.class, testTask1);
        schedulerDefinition.schedule("testTask2", FeatureHolder2.class, testTask2);
        schedulerDefinition.schedule("testTask3", FeatureHolder3Extands2.class, testTask3);

        Scheduler scheduler1 = new FeatureHolder1().get(schedulerDefinition);
        assertEquals("Scheduler object's tasks with FeatureHolder1", new ArrayList<>(Arrays.asList(testTask1)), new ArrayList<>(scheduler1.getTasks()));

        Scheduler scheduler2 = new FeatureHolder2().get(schedulerDefinition);
        assertEquals("Scheduler object's tasks with FeatureHolder1", new ArrayList<>(Arrays.asList(testTask2)), new ArrayList<>(scheduler2.getTasks()));

        Scheduler scheduler3 = new FeatureHolder3Extands2().get(schedulerDefinition);
        assertEquals("Scheduler object's tasks with FeatureHolder3Extands2", new ArrayList<>(Arrays.asList(testTask2, testTask3)), new ArrayList<>(scheduler3.getTasks()));
    }

    @Test (expected = IllegalStateException.class)
    public void testScheduleTwice() {

        schedulerDefinition.schedule("testTask", FeatureHolder1.class, testTask1);
        schedulerDefinition.schedule("testTask", FeatureHolder1.class, testTask1);
    }

    @Test
    public void testScheduleTwiceDifferentName() {

        schedulerDefinition.schedule("testTask1", FeatureHolder1.class, testTask1);
        schedulerDefinition.schedule("testTask2", FeatureHolder1.class, testTask1);
    }

    @Test
    public void testScheduleTwiceDifferentHolderClass() {

        schedulerDefinition.schedule("testTask", FeatureHolder1.class, testTask1);
        schedulerDefinition.schedule("testTask", FeatureHolder2.class, testTask1);
    }

    @Test (expected = IllegalStateException.class)
    public void testScheduleTwiceDifferentTasks() {

        schedulerDefinition.schedule("testTask", FeatureHolder1.class, testTask1);
        schedulerDefinition.schedule("testTask", FeatureHolder1.class, testTask2);
    }

    private static class TestSchedulerTask extends SchedulerTaskAdapter {

        private TestSchedulerTask(int id) {

            super(id, id, "testGroup" + id);
        }

        @Override
        public void execute(FeatureHolder holder) {

            // Empty
        }

    }

    private static class FeatureHolder1 extends DefaultFeatureHolder {

    }

    private static class FeatureHolder2 extends DefaultFeatureHolder {

    }

    private static class FeatureHolder3Extands2 extends FeatureHolder2 {

    }

}
