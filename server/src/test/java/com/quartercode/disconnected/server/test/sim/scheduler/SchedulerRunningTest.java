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
public class SchedulerRunningTest {

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

    public SchedulerRunningTest(int initialDelay, int periodicDelay) {

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
    public void testSchedule() {

        scheduler.schedule(null, "testGroup", initialDelay, periodicDelay, new TestSchedulerTask(0));

        int updates = !periodic ? initialDelay * 3 : initialDelay + periodicDelay * 3;
        for (int update = 0; update < updates; update++) {
            scheduler.update("testGroup");
        }

        int expectedExecutions = !periodic ? 1 : 4;
        int actualExecutions = schedulerTaskExecutions[0];
        assertEquals("Scheduler task invocations after " + updates + " updates (initialDelay=" + initialDelay + ", periodicDelay=" + periodicDelay + ")", expectedExecutions, actualExecutions);

        if (!periodic) {
            assertTrue("Non-periodic scheduler task hasn't been removed after execution", scheduler.getTasks().isEmpty());
        }
    }

    @Test
    public void testScheduleMultipleGroups() {

        scheduler.schedule(null, "testGroup1", initialDelay, periodicDelay, new TestSchedulerTask(0));
        scheduler.schedule(null, "testGroup2", initialDelay, periodicDelay, new TestSchedulerTask(1));

        int updates = initialDelay;
        for (int update = 0; update < updates; update++) {
            // Only update test group 1
            scheduler.update("testGroup1");
        }

        assertTrue("Scheduler group 1 wasn't invoked", schedulerTaskExecutions[0] == 1);
        assertTrue("Scheduler group 2 was invoked", schedulerTaskExecutions[1] == 0);
    }

    @Test
    public void testDeactivate() {

        scheduler.schedule(null, "testGroup", initialDelay, periodicDelay, new TestSchedulerTask(0));

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

        private TestSchedulerTask(int trackingIndex) {

            this.trackingIndex = trackingIndex;
        }

        @Override
        public void execute(CFeatureHolder holder) {

            schedulerTaskExecutions[trackingIndex]++;
        }

    }

}
