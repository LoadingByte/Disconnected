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

package com.quartercode.disconnected.sim.scheduler;

import java.util.HashMap;
import java.util.Map;
import com.quartercode.classmod.base.FeatureDefinition;
import com.quartercode.classmod.base.FeatureHolder;
import com.quartercode.classmod.base.def.AbstractFeatureDefinition;

/**
 * The {@link FeatureDefinition} that defines a {@link Scheduler}.
 * Tasks can be scheduled for every created scheduler using the definition.
 * In order to make globally scheduled tasks removable, each global task must have a name.
 * Note that tasks can also be scheduled without a proper name, in which case a name with a sequential number will be used.
 * 
 * @see Scheduler
 */
public class SchedulerDefinition extends AbstractFeatureDefinition<Scheduler> {

    private final Map<String, SchedulerTask> globalTasks = new HashMap<>();

    /**
     * Creates a new scheduler definition for defining a {@link Scheduler} with the given name.
     * 
     * @param name The name of the defined scheduler.
     */
    public SchedulerDefinition(String name) {

        super(name);
    }

    /**
     * Returns the amount of global {@link SchedulerTask}s that are currently scheduled.
     * Such tasks must have been added using {@link #schedule(String, SchedulerTask)} before.
     * 
     * @return The amount of currently scheduled tasks.
     */
    public int countTasks() {

        return globalTasks.size();
    }

    /**
     * Globally schedules the given {@link SchedulerTask} under the given name.
     * That means that every created {@link Scheduler} will contain the task.
     * See the scheduler task class for more information about when the task will be executed.
     * 
     * @param name The name the task is assigned to.
     *        It can be used to remove the task from the definition using {@link #remove(String)}.
     * @param task The scheduler task that should be globally scheduled for execution.
     */
    public void schedule(String name, SchedulerTask task) {

        globalTasks.put(name, task);
    }

    /**
     * Removes the global {@link SchedulerTask} which is scheduled under the given name.
     * 
     * @param name The name the task was assigned to.
     */
    public void remove(String name) {

        globalTasks.remove(name);
    }

    @Override
    public Scheduler create(FeatureHolder holder) {

        Scheduler scheduler = new Scheduler(getName(), holder);

        for (SchedulerTask globalTask : globalTasks.values()) {
            scheduler.schedule(globalTask);
        }

        return scheduler;
    }

}
