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

package com.quartercode.disconnected.test.sim;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import com.quartercode.classmod.base.FeatureHolder;
import com.quartercode.classmod.base.def.DefaultFeatureHolder;
import com.quartercode.disconnected.sim.run.ScheduleTask;
import com.quartercode.disconnected.sim.run.Scheduler;

@RunWith (Parameterized.class)
public class SchedulerTest {

    private static boolean executedScheduleTask;

    @Parameters
    public static Collection<Object[]> data() {

        List<Object[]> data = new ArrayList<Object[]>();

        data.add(new Object[] { 1 });
        data.add(new Object[] { 5 });
        data.add(new Object[] { 100 });

        return data;
    }

    private final int delay;

    private Scheduler scheduler;

    public SchedulerTest(int delay) {

        this.delay = delay;
    }

    @Before
    public void setUp() {

        executedScheduleTask = false;

        scheduler = new Scheduler("scheduler", new DefaultFeatureHolder());
    }

    @Test
    public void testSchedule() {

        scheduler.schedule(new ScheduleTask() {

            @Override
            public void execute(FeatureHolder holder) {

                executedScheduleTask = true;
            }
        }, delay);

        for (int update = 0; update < delay; update++) {
            scheduler.update();
        }

        Assert.assertTrue("Schedule task with delay of " + delay + " got executed after " + (delay + 1) + " updates", executedScheduleTask);
    }

    @Test
    public void testScheduleWithPersistence() throws JAXBException {

        scheduler.schedule(new TestScheduleTask(), delay);

        JAXBContext context = JAXBContext.newInstance(Scheduler.class, TestScheduleTask.class);
        StringWriter serialized = new StringWriter();
        context.createMarshaller().marshal(scheduler, serialized);
        Scheduler copy = (Scheduler) context.createUnmarshaller().unmarshal(new StringReader(serialized.toString()));

        for (int update = 0; update < delay; update++) {
            copy.update();
        }

        Assert.assertTrue("Schedule task with delay of " + delay + " got executed after " + (delay + 1) + " updates", executedScheduleTask);
    }

    private static class TestScheduleTask implements ScheduleTask {

        @Override
        public void execute(FeatureHolder holder) {

            executedScheduleTask = true;
        }

    }

}
