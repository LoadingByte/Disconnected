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
import java.util.ArrayList;
import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;
import com.quartercode.classmod.def.extra.conv.DefaultCFeatureHolder;
import com.quartercode.classmod.extra.conv.CFeatureHolder;
import com.quartercode.disconnected.server.sim.scheduler.Scheduler;
import com.quartercode.disconnected.server.sim.scheduler.SchedulerDefinition;
import com.quartercode.disconnected.server.sim.scheduler.SchedulerTaskAdapter;

public class SchedulerDefinitionTest {

    private SchedulerDefinition     schedulerDefinition;

    private final TestSchedulerTask task1 = new TestSchedulerTask(1);
    private final TestSchedulerTask task2 = new TestSchedulerTask(2);
    private final TestSchedulerTask task3 = new TestSchedulerTask(3);

    @Before
    public void setUp() {

        schedulerDefinition = new SchedulerDefinition("testSchedulerDefinition");
    }

    @Test
    public void testScheduleAndGetGlobalTasks() {

        schedulerDefinition.schedule(FeatureHolder1.class, "testTask1", "testGroup", 1, 1, task1);
        schedulerDefinition.schedule(FeatureHolder1.class, "testTask2", "testGroup", 1, 1, task2);
        schedulerDefinition.schedule(FeatureHolder1.class, "testTask3", "testGroup", 1, 1, task3);

        assertEquals("Scheduled tasks", new ArrayList<>(Arrays.asList(Pair.of(task1, FeatureHolder1.class), Pair.of(task2, FeatureHolder1.class), Pair.of(task3, FeatureHolder1.class))), new ArrayList<>(schedulerDefinition.getGlobalTasks()));
    }

    @Test
    public void testScheduleAndGetGlobalTask() {

        schedulerDefinition.schedule(FeatureHolder1.class, "testTask1", "testGroup", 1, 1, task1);
        schedulerDefinition.schedule(FeatureHolder1.class, "testTask2", "testGroup", 1, 1, task2);
        schedulerDefinition.schedule(FeatureHolder1.class, "testTask3", "testGroup", 1, 1, task3);

        assertEquals("Scheduled task with name 'testTask1'", task1, schedulerDefinition.getGlobalTask("testTask1", FeatureHolder1.class));
        assertEquals("Scheduled task with name 'testTask2'", task2, schedulerDefinition.getGlobalTask("testTask2", FeatureHolder1.class));
        assertEquals("Scheduled task with name 'testTask3'", task3, schedulerDefinition.getGlobalTask("testTask3", FeatureHolder1.class));
    }

    @Test
    public void testRemoveByTask() {

        schedulerDefinition.schedule(FeatureHolder1.class, "testTask1", "testGroup", 1, 1, task1);
        schedulerDefinition.schedule(FeatureHolder1.class, "testTask2", "testGroup", 1, 1, task2);
        schedulerDefinition.schedule(FeatureHolder1.class, "testTask3", "testGroup", 1, 1, task3);

        schedulerDefinition.remove(FeatureHolder1.class, task2);

        assertEquals("Scheduled tasks", new ArrayList<>(Arrays.asList(Pair.of(task1, FeatureHolder1.class), Pair.of(task3, FeatureHolder1.class))), new ArrayList<>(schedulerDefinition.getGlobalTasks()));
    }

    @Test
    public void testRemoveByName() {

        schedulerDefinition.schedule(FeatureHolder1.class, "testTask1", "testGroup", 1, 1, task1);
        schedulerDefinition.schedule(FeatureHolder1.class, "testTask2", "testGroup", 1, 1, task2);
        schedulerDefinition.schedule(FeatureHolder1.class, "testTask3", "testGroup", 1, 1, task3);

        schedulerDefinition.remove(FeatureHolder1.class, "testTask2");

        assertEquals("Scheduled tasks", new ArrayList<>(Arrays.asList(Pair.of(task1, FeatureHolder1.class), Pair.of(task3, FeatureHolder1.class))), new ArrayList<>(schedulerDefinition.getGlobalTasks()));
    }

    @Test
    public void testScheduleAndCreate() {

        schedulerDefinition.schedule(FeatureHolder1.class, "testTask1", "testGroup", 1, 1, task1);
        schedulerDefinition.schedule(FeatureHolder1.class, "testTask2", "testGroup", 1, 1, task2);
        schedulerDefinition.schedule(FeatureHolder1.class, "testTask3", "testGroup", 1, 1, task3);

        Scheduler scheduler = new FeatureHolder1().get(schedulerDefinition);
        assertEquals("Scheduler object's tasks", new ArrayList<>(Arrays.asList(task1, task2, task3)), new ArrayList<>(scheduler.getTasks()));
    }

    @Test
    public void testScheduleAndRemoveByTaskAndCreate() {

        schedulerDefinition.schedule(FeatureHolder1.class, "testTask1", "testGroup", 1, 1, task1);
        schedulerDefinition.schedule(FeatureHolder1.class, "testTask2", "testGroup", 1, 1, task2);
        schedulerDefinition.schedule(FeatureHolder1.class, "testTask3", "testGroup", 1, 1, task3);

        schedulerDefinition.remove(FeatureHolder1.class, task2);

        Scheduler scheduler = new FeatureHolder1().get(schedulerDefinition);
        assertEquals("Scheduler object's tasks", new ArrayList<>(Arrays.asList(task1, task3)), new ArrayList<>(scheduler.getTasks()));
    }

    @Test
    public void testScheduleAndRemoveByNameAndCreate() {

        schedulerDefinition.schedule(FeatureHolder1.class, "testTask1", "testGroup", 1, 1, task1);
        schedulerDefinition.schedule(FeatureHolder1.class, "testTask2", "testGroup", 1, 1, task2);
        schedulerDefinition.schedule(FeatureHolder1.class, "testTask3", "testGroup", 1, 1, task3);

        schedulerDefinition.remove(FeatureHolder1.class, "testTask2");

        Scheduler scheduler = new FeatureHolder1().get(schedulerDefinition);
        assertEquals("Scheduler object's tasks", new ArrayList<>(Arrays.asList(task1, task3)), new ArrayList<>(scheduler.getTasks()));
    }

    @Test
    public void testScheduleAndCreateWithDifferentHolderClasses() {

        schedulerDefinition.schedule(FeatureHolder1.class, "testTask1", "testGroup", 1, 1, task1);
        schedulerDefinition.schedule(FeatureHolder2.class, "testTask2", "testGroup", 1, 1, task2);
        schedulerDefinition.schedule(FeatureHolder3Extends2.class, "testTask3", "testGroup", 1, 1, task3);

        Scheduler scheduler1 = new FeatureHolder1().get(schedulerDefinition);
        assertEquals("Scheduler object's tasks with FeatureHolder1", new ArrayList<>(Arrays.asList(task1)), new ArrayList<>(scheduler1.getTasks()));

        Scheduler scheduler2 = new FeatureHolder2().get(schedulerDefinition);
        assertEquals("Scheduler object's tasks with FeatureHolder2", new ArrayList<>(Arrays.asList(task2)), new ArrayList<>(scheduler2.getTasks()));

        Scheduler scheduler3 = new FeatureHolder3Extends2().get(schedulerDefinition);
        assertEquals("Scheduler object's tasks with FeatureHolder3Extands2", new ArrayList<>(Arrays.asList(task2, task3)), new ArrayList<>(scheduler3.getTasks()));
    }

    @Test
    public void testScheduleTwice() {

        // No error
        schedulerDefinition.schedule(FeatureHolder1.class, "testTask1", "testGroup", 1, 1, task1);
        schedulerDefinition.schedule(FeatureHolder1.class, "testTask1", "testGroup", 1, 1, task1);
    }

    @Test
    public void testScheduleTwiceDifferentHolderClass() {

        // No error
        schedulerDefinition.schedule(FeatureHolder1.class, "testTask1", "testGroup", 1, 1, task1);
        schedulerDefinition.schedule(FeatureHolder2.class, "testTask1", "testGroup", 1, 1, task1);
    }

    @RequiredArgsConstructor
    @Getter
    private static class TestSchedulerTask extends SchedulerTaskAdapter {

        private final int id;

        @Override
        public void execute(CFeatureHolder holder) {

        }

        @Override
        public String toString() {

            return Integer.toHexString(System.identityHashCode(this));
        }

    }

    private static class FeatureHolder1 extends DefaultCFeatureHolder {

    }

    private static class FeatureHolder2 extends DefaultCFeatureHolder {

    }

    private static class FeatureHolder3Extends2 extends FeatureHolder2 {

    }

}
