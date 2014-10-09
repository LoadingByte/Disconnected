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

package com.quartercode.disconnected.server.sim.scheduler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;
import com.quartercode.classmod.base.FeatureDefinition;
import com.quartercode.classmod.base.FeatureHolder;
import com.quartercode.classmod.base.def.AbstractFeatureDefinition;
import com.quartercode.classmod.extra.CFeatureHolder;

/**
 * The {@link FeatureDefinition} that defines a {@link Scheduler}.
 * Tasks can be scheduled for every created scheduler using the definition.
 * In order to make globally scheduled tasks removable, each global task must have a name.
 * Note that tasks can also be scheduled without a proper name, in which case a name with a sequential number will be used.
 * 
 * @see Scheduler
 */
public class SchedulerDefinition extends AbstractFeatureDefinition<Scheduler> {

    private final List<Pair<SchedulerTask, Class<? extends CFeatureHolder>>> globalTasks = new ArrayList<>();

    // Performance: Cache for different holder classes
    private final Map<Class<? extends CFeatureHolder>, List<SchedulerTask>>  classCache  = new HashMap<>();

    /**
     * Creates a new scheduler definition for defining a {@link Scheduler} with the given name.
     * 
     * @param name The name of the defined scheduler.
     */
    public SchedulerDefinition(String name) {

        super(name);
    }

    /**
     * Returns all global {@link SchedulerTask}s that are currently scheduled, along with their {@link CFeatureHolder} classes.
     * Such tasks must have been added using {@link #schedule(SchedulerTask, Class)} before.
     * 
     * @return All currently scheduled global tasks.
     */
    public List<Pair<SchedulerTask, Class<? extends CFeatureHolder>>> getGlobalTasks() {

        return Collections.unmodifiableList(globalTasks);
    }

    /**
     * Retrieves the {@link SchedulerTask} that is scheduled under the given holder class and has the given name.
     * If multiple tasks have the same name, the task that was scheduled first is returned.
     * 
     * @param name The name of the task that should be returned.
     * @param holderClass The {@link CFeatureHolder} class which was used when the task was scheduled.
     *        See {@link #schedule(SchedulerTask, Class)} for more information on holder classes.
     * 
     * @return The first task with the given name.
     */
    public SchedulerTask getGlobalTask(String name, Class<? extends CFeatureHolder> holderClass) {

        Validate.notNull(name, "Can't search for scheduler task with null name");

        for (Pair<SchedulerTask, Class<? extends CFeatureHolder>> element : globalTasks) {
            SchedulerTask task = element.getLeft();

            if (element.getRight() == holderClass && task.getName() != null && task.getName().equals(name)) {
                return task;
            }
        }

        return null;
    }

    /**
     * Globally schedules the given {@link SchedulerTask} under the given name with the given variant.
     * That means that every created {@link Scheduler} will contain the task.
     * See the scheduler task class for more information about when the task will be executed.
     * 
     * @param task The scheduler task that should be globally scheduled for execution.
     * @param holderClass The {@link CFeatureHolder} class to whose schedulers the task will be added to.
     *        It will also be used for every subclass of this class.
     *        For example, a task with holder class TestFH is only added to schedulers that are held by instances of TestFH or any subclass.
     */
    public void schedule(SchedulerTask task, Class<? extends CFeatureHolder> holderClass) {

        Pair<SchedulerTask, Class<? extends CFeatureHolder>> element = Pair.<SchedulerTask, Class<? extends CFeatureHolder>> of(task, holderClass);
        globalTasks.add(element);

        // Invalidate cache
        classCache.clear();
    }

    /**
     * Removes the given global {@link SchedulerTask} which is scheduled under the given variant.
     * 
     * @param task The task that should be removed.
     * @param holderClass The {@link CFeatureHolder} class which was used when the task was scheduled.
     *        See {@link #schedule(SchedulerTask, Class)} for more information on holder classes.
     */
    public void remove(SchedulerTask task, Class<? extends CFeatureHolder> holderClass) {

        Pair<SchedulerTask, Class<? extends CFeatureHolder>> element = Pair.<SchedulerTask, Class<? extends CFeatureHolder>> of(task, holderClass);
        globalTasks.remove(element);

        // Invalidate cache
        classCache.clear();
    }

    /**
     * Removes the global {@link SchedulerTask} which is scheduled under the given variant and has the given name.
     * That only works if the scheduled task has a non-null name.
     * If multiple tasks have the same name, the task that was scheduled first is removed.
     * 
     * @param name The name of the task for removal.
     * @param holderClass The {@link CFeatureHolder} class which was used when the task was scheduled.
     *        See {@link #schedule(SchedulerTask, Class)} for more information on holder classes.
     */
    public void remove(String name, Class<? extends CFeatureHolder> holderClass) {

        SchedulerTask removalTask = getGlobalTask(name, holderClass);

        if (removalTask != null) {
            remove(removalTask, holderClass);
        }
    }

    @Override
    public Scheduler create(FeatureHolder holder) {

        Validate.isInstanceOf(CFeatureHolder.class, holder, "Only the usage of ConvenientFeatureHolder is allowed");

        Scheduler scheduler = new Scheduler(getName(), (CFeatureHolder) holder);

        for (SchedulerTask globalTask : getGlobalTasksForClass( ((CFeatureHolder) holder).getClass())) {
            scheduler.schedule(globalTask);
        }

        return scheduler;
    }

    private List<SchedulerTask> getGlobalTasksForClass(Class<? extends CFeatureHolder> holderClass) {

        List<SchedulerTask> classGlobalTasks = classCache.get(holderClass);

        if (classGlobalTasks == null) {
            classGlobalTasks = new ArrayList<>();

            for (Pair<SchedulerTask, Class<? extends CFeatureHolder>> element : globalTasks) {
                if (element.getRight().isAssignableFrom(holderClass)) {
                    classGlobalTasks.add(element.getLeft());
                }
            }

            classCache.put(holderClass, classGlobalTasks);
        }

        return classGlobalTasks;
    }

}
