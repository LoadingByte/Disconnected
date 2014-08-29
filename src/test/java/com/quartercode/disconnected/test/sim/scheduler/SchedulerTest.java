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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import com.quartercode.classmod.base.FeatureHolder;
import com.quartercode.classmod.base.def.DefaultFeatureHolder;
import com.quartercode.disconnected.sim.scheduler.Scheduler;
import com.quartercode.disconnected.sim.scheduler.SchedulerTaskAdapter;

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

        scheduler = new Scheduler("scheduler", new DefaultFeatureHolder());
    }

    @Test
    public void testSchedule() {

        scheduler.schedule(new TestScheduleTask(initialDelay, periodicDelay, "testGroup", 0));

        int updates = !periodic ? initialDelay * 3 : initialDelay + periodicDelay * 3;
        for (int update = 0; update < updates; update++) {
            scheduler.update("testGroup");
        }

        int expectedExecutions = !periodic ? 1 : 4;
        int actualExecutions = schedulerTaskExecutions[0];
        Assert.assertEquals("Scheduler task invocations after " + updates + " updates (initialDelay=" + initialDelay + ", periodicDelay=" + periodicDelay + ")", expectedExecutions, actualExecutions);
    }

    @Test
    public void testScheduleMultipleGroups() {

        scheduler.schedule(new TestScheduleTask(initialDelay, periodicDelay, "testGroup1", 0));
        scheduler.schedule(new TestScheduleTask(initialDelay, periodicDelay, "testGroup2", 1));

        int updates = initialDelay;
        for (int update = 0; update < updates; update++) {
            // Only update test group 1
            scheduler.update("testGroup1");
        }

        Assert.assertTrue("Scheduler group 1 wasn't invoked while group 2 was", schedulerTaskExecutions[0] == 1 && schedulerTaskExecutions[1] == 0);
    }

    private static class TestScheduleTask extends SchedulerTaskAdapter {

        private final int trackingIndex;

        private TestScheduleTask(int initialDelay, int periodicDelay, String group, int trackingIndex) {

            super(initialDelay, periodicDelay, group);

            this.trackingIndex = trackingIndex;
        }

        @Override
        public void execute(FeatureHolder holder) {

            schedulerTaskExecutions[trackingIndex]++;
        }

    }

}
