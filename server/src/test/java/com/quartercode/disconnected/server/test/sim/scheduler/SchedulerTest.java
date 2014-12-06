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
import java.util.Collection;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import com.quartercode.classmod.def.extra.conv.DefaultCFeatureHolder;
import com.quartercode.classmod.extra.conv.CFeatureHolder;
import com.quartercode.disconnected.server.sim.scheduler.Scheduler;
import com.quartercode.disconnected.server.sim.scheduler.SchedulerTaskAdapter;

@RunWith (Parameterized.class)
public class SchedulerTest {

    private static int[] schedulerTaskExecutions;

    @Parameters
    public static Collection<Object[]> data() {

        List<Object[]> data = new ArrayList<>();

        data.add(new Object[] { 1, -1 });
        data.add(new Object[] { 5, -1 });
        data.add(new Object[] { 100, -1 });

        data.add(new Object[] { 5, 1 });
        data.add(new Object[] { 5, 5 });
        data.add(new Object[] { 5, 100 });

        return data;
    }

    private final int     initialDelay;
    private final int     periodicDelay;

    private final boolean periodic;

    private Scheduler     scheduler;

    public SchedulerTest(int initialDelay, int periodicDelay) {

        this.initialDelay = initialDelay;
        this.periodicDelay = periodicDelay;

        periodic = periodicDelay > 0;
    }

    @Before
    public void setUp() {

        schedulerTaskExecutions = new int[2];

        scheduler = new Scheduler("scheduler", new DefaultCFeatureHolder());
    }

    @Test
    public void testGetTasks() {

        TestSchedulerTask task1 = new TestSchedulerTask(null, "testGroup", initialDelay, periodicDelay, 0);
        TestSchedulerTask task2 = new TestSchedulerTask(null, "testGroup", initialDelay, periodicDelay, 0);

        scheduler.schedule(task1);
        scheduler.schedule(task2);

        assertEquals("Scheduled tasks", new ArrayList<>(Arrays.asList(task1, task2)), new ArrayList<>(scheduler.getTasks()));
    }

    @Test
    public void testGetTask() {

        scheduler.schedule(new TestSchedulerTask("testName1", "testGroup", initialDelay, periodicDelay, 0));
        TestSchedulerTask testTask = new TestSchedulerTask("testName2", "testGroup", initialDelay, periodicDelay, 0);
        scheduler.schedule(testTask);
        scheduler.schedule(new TestSchedulerTask("testName1", "testGroup3", initialDelay, periodicDelay, 0));

        assertEquals("Scheduled task with name 'testName2'", testTask, scheduler.getTask("testName2"));
    }

    @Test
    public void testSchedule() {

        scheduler.schedule(new TestSchedulerTask(null, "testGroup", initialDelay, periodicDelay, 0));

        int updates = !periodic ? initialDelay * 3 : initialDelay + periodicDelay * 3;
        for (int update = 0; update < updates; update++) {
            scheduler.update("testGroup");
        }

        int expectedExecutions = !periodic ? 1 : 4;
        int actualExecutions = schedulerTaskExecutions[0];
        assertEquals("Scheduler task invocations after " + updates + " updates (initialDelay=" + initialDelay + ", periodicDelay=" + periodicDelay + ")", expectedExecutions, actualExecutions);
    }

    @Test
    public void testScheduleMultipleGroups() {

        scheduler.schedule(new TestSchedulerTask(null, "testGroup1", initialDelay, periodicDelay, 0));
        scheduler.schedule(new TestSchedulerTask(null, "testGroup2", initialDelay, periodicDelay, 1));

        int updates = initialDelay;
        for (int update = 0; update < updates; update++) {
            // Only update test group 1
            scheduler.update("testGroup1");
        }

        assertTrue("Scheduler group 1 wasn't invoked while group 2 was", schedulerTaskExecutions[0] == 1 && schedulerTaskExecutions[1] == 0);
    }

    @Test
    public void testDeactivate() {

        scheduler.schedule(new TestSchedulerTask(null, "testGroup", initialDelay, periodicDelay, 0));

        scheduler.setActive(false);

        for (int update = 0; update < initialDelay; update++) {
            scheduler.update("testGroup");
        }

        scheduler.setActive(true);

        // Update the scheduler again to test whether the previous updates changed something they shouldn't have changed
        for (int update = 0; update < initialDelay; update++) {
            scheduler.update("testGroup");
        }

        int actualExecutions = schedulerTaskExecutions[0];
        assertEquals("Scheduler task was executed although scheduler was deactivated", 1, actualExecutions);
    }

    private static class TestSchedulerTask extends SchedulerTaskAdapter {

        private final int trackingIndex;

        private TestSchedulerTask(String name, String group, int initialDelay, int periodicDelay, int trackingIndex) {

            super(name, group, initialDelay, periodicDelay);

            this.trackingIndex = trackingIndex;
        }

        @Override
        public void execute(CFeatureHolder holder) {

            schedulerTaskExecutions[trackingIndex]++;
        }

    }

}
