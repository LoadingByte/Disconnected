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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.lang3.tuple.Pair;
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

    private final Map<Pair<String, Class<? extends FeatureHolder>>, SchedulerTask> globalTasks = new LinkedHashMap<>();

    // Performance: Cache for different variants
    private final Map<Class<? extends FeatureHolder>, List<SchedulerTask>>         classCache  = new HashMap<>();

    /**
     * Creates a new scheduler definition for defining a {@link Scheduler} with the given name.
     * 
     * @param name The name of the defined scheduler.
     */
    public SchedulerDefinition(String name) {

        super(name);
    }

    /**
     * Returns all global {@link SchedulerTask}s that are currently scheduled.
     * Such tasks must have been added using {@link #schedule(String, Class, SchedulerTask)} before.
     * 
     * @return All currently scheduled global tasks.
     */
    public Map<Pair<String, Class<? extends FeatureHolder>>, SchedulerTask> getGlobalTasks() {

        return Collections.unmodifiableMap(globalTasks);
    }

    /**
     * Globally schedules the given {@link SchedulerTask} under the given name with the given variant.
     * That means that every created {@link Scheduler} will contain the task.
     * See the scheduler task class for more information about when the task will be executed.
     * 
     * @param name The name the task is assigned to.
     *        It can be used to remove the task from the definition using {@link #remove(String)}.
     * @param holderClass The {@link FeatureHolder} class to whose schedulers the task will be added to.
     *        It will also be used for every subclass of this class.
     *        For example, a task with holder class TestFH is only added to schedulers that are held by instances of TestFH or any subclass.
     * @param task The scheduler task that should be globally scheduled for execution.
     */
    public void schedule(String name, Class<? extends FeatureHolder> holderClass, SchedulerTask task) {

        Pair<String, Class<? extends FeatureHolder>> key = Pair.<String, Class<? extends FeatureHolder>> of(name, holderClass);

        if (globalTasks.containsKey(key)) {
            throw new IllegalStateException("Global task with name '" + name + "' and holder class '" + holderClass.getName() + "' is already scheduled");
        }

        globalTasks.put(key, task);

        // Invalidate variant cache
        classCache.clear();
    }

    /**
     * Removes the global {@link SchedulerTask} which is scheduled under the given name and variant.
     * 
     * @param name The name the task was assigned to.
     * @param holderClass The {@link FeatureHolder} class which was used when the task was scheduled.
     *        See {@link #schedule(String, Class, SchedulerTask)} for more information on holder classes.
     */
    public void remove(String name, Class<? extends FeatureHolder> holderClass) {

        globalTasks.remove(Pair.<String, Class<? extends FeatureHolder>> of(name, holderClass));

        // Invalidate variant cache
        classCache.clear();
    }

    @Override
    public Scheduler create(FeatureHolder holder) {

        Scheduler scheduler = new Scheduler(getName(), holder);

        for (SchedulerTask globalTask : getGlobalTasksForClass(holder.getClass())) {
            scheduler.schedule(globalTask);
        }

        return scheduler;
    }

    private List<SchedulerTask> getGlobalTasksForClass(Class<? extends FeatureHolder> holderClass) {

        List<SchedulerTask> classGlobalTasks = classCache.get(holderClass);

        if (classGlobalTasks == null) {
            classGlobalTasks = new ArrayList<>();

            for (Entry<Pair<String, Class<? extends FeatureHolder>>, SchedulerTask> globalTask : globalTasks.entrySet()) {
                if (globalTask.getKey().getRight().isAssignableFrom(holderClass)) {
                    classGlobalTasks.add(globalTask.getValue());
                }
            }

            classCache.put(holderClass, classGlobalTasks);
        }

        return classGlobalTasks;
    }

}
