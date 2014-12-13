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
import org.junit.Before;
import org.junit.Test;
import com.quartercode.classmod.def.extra.conv.DefaultCFeatureHolder;
import com.quartercode.classmod.extra.conv.CFeatureHolder;
import com.quartercode.disconnected.server.sim.scheduler.Scheduler;
import com.quartercode.disconnected.server.sim.scheduler.SchedulerTaskAdapter;

public class SchedulerTest {

    private Scheduler               scheduler;

    private final TestSchedulerTask task1 = new TestSchedulerTask(1);
    private final TestSchedulerTask task2 = new TestSchedulerTask(2);
    private final TestSchedulerTask task3 = new TestSchedulerTask(3);

    @Before
    public void setUp() {

        scheduler = new Scheduler("scheduler", new DefaultCFeatureHolder());

        scheduler.schedule("testTask1", "testGroup1", 1, 1, task1);
        scheduler.schedule("testTask2", "testGroup2", 1, 1, task2);
        scheduler.schedule("testTask3", "testGroup3", 1, 1, task3);
    }

    @Test
    public void testScheduleAndGetTasks() {

        assertEquals("Scheduled tasks", new ArrayList<>(Arrays.asList(task1, task2, task3)), new ArrayList<>(scheduler.getTasks()));
    }

    @Test
    public void testScheduleAndGetTask() {

        assertEquals("Scheduled task with name 'testTask1'", task1, scheduler.getTask("testTask1"));
        assertEquals("Scheduled task with name 'testTask2'", task2, scheduler.getTask("testTask2"));
        assertEquals("Scheduled task with name 'testTask3'", task3, scheduler.getTask("testTask3"));
    }

    @RequiredArgsConstructor
    @Getter
    private static class TestSchedulerTask extends SchedulerTaskAdapter {

        private final int id;

        @Override
        public void execute(CFeatureHolder holder) {

        }

    }

}
