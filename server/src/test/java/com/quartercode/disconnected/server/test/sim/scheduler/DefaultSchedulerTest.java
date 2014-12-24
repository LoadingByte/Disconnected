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

import static com.quartercode.disconnected.server.test.ExtraAssert.assertCollectionEquals;
import static com.quartercode.disconnected.server.test.ExtraAssert.assertListEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.junit.Before;
import org.junit.Test;
import com.quartercode.classmod.def.extra.conv.DefaultCFeatureHolder;
import com.quartercode.disconnected.server.sim.scheduler.DefaultScheduler;
import com.quartercode.disconnected.server.sim.scheduler.SchedulerRegistry;
import com.quartercode.disconnected.server.sim.scheduler.SchedulerRegistryProvider;
import com.quartercode.disconnected.server.sim.scheduler.SchedulerTaskAdapter;

public class DefaultSchedulerTest {

    private DefaultScheduler        scheduler;
    private final SchedulerRegistry schedulerRegistry = new SchedulerRegistry();

    private final TestSchedulerTask task1             = new TestSchedulerTask(1);
    private final TestSchedulerTask task2             = new TestSchedulerTask(2);
    private final TestSchedulerTask task3             = new TestSchedulerTask(3);

    @Before
    public void setUp() {

        scheduler = new DefaultScheduler("scheduler", new TestSchedulerHolder(schedulerRegistry));

        scheduler.schedule("testTask1", "testGroup1", 1, 1, task1);
        scheduler.schedule("testTask2", "testGroup2", 1, 1, task2);
        scheduler.schedule("testTask3", "testGroup2", 1, 1, task3);
    }

    @Test
    public void testScheduleAndGetTasks() {

        assertEquals("Scheduled tasks", new ArrayList<>(Arrays.asList(task1, task2, task3)), new ArrayList<>(scheduler.getTasks()));
    }

    @Test
    public void testScheduleAndGetTaskByName() {

        assertEquals("Scheduled task with name 'testTask1'", task1, scheduler.getTaskByName("testTask1"));
        assertEquals("Scheduled task with name 'testTask2'", task2, scheduler.getTaskByName("testTask2"));
        assertEquals("Scheduled task with name 'testTask3'", task3, scheduler.getTaskByName("testTask3"));
    }

    @Test
    public void testScheduleAndGetTasksByGroup() {

        assertListEquals("Scheduled tasks with name 'testGroup1'", scheduler.getTasksByGroup("testGroup1"), task1);
        assertListEquals("Scheduled tasks with name 'testGroup2'", scheduler.getTasksByGroup("testGroup2"), task2, task3);
    }

    @Test
    public void testScheduleAddToRegistry() {

        assertCollectionEquals("Schedulers in scheduler registry", schedulerRegistry.getSchedulers(), scheduler);
    }

    @Test
    public void testCancelRemoveFromRegistry() {

        task1.cancel();
        task2.cancel();
        task3.cancel();

        // Update the scheduler in order to remove the tasks
        scheduler.update("testGroup1");
        scheduler.update("testGroup2");

        assertTrue("Scheduler registry is not empty", schedulerRegistry.getSchedulers().isEmpty());
    }

    @RequiredArgsConstructor
    @Getter
    private static class TestSchedulerHolder extends DefaultCFeatureHolder implements SchedulerRegistryProvider {

        private final SchedulerRegistry schedulerRegistry;

    }

    @RequiredArgsConstructor
    @Getter
    private static class TestSchedulerTask extends SchedulerTaskAdapter {

        private final int id;

    }

}
