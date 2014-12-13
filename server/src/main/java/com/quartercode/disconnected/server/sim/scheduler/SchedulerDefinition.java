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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;
import com.quartercode.classmod.base.FeatureDefinition;
import com.quartercode.classmod.base.FeatureHolder;
import com.quartercode.classmod.def.base.AbstractFeatureDefinition;
import com.quartercode.classmod.extra.conv.CFeatureHolder;

/**
 * The {@link FeatureDefinition} that defines a {@link Scheduler}.
 * Tasks can be scheduled for every created scheduler using the definition.
 * In order to make globally scheduled tasks removable, each global task must have a name.
 * Note that tasks can also be scheduled without a proper name, in which case a name with a sequential number will be used.
 * 
 * @see Scheduler
 */
public class SchedulerDefinition extends AbstractFeatureDefinition<Scheduler> {

    private final List<Pair<ScheduledTask, Class<? extends CFeatureHolder>>> globalTasks = new ArrayList<>();

    // Performance: Cache for different holder classes
    private final Map<Class<? extends CFeatureHolder>, List<ScheduledTask>>  classCache  = new HashMap<>();

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
     * Such tasks must have been added using {@link #schedule(Class, String, String, int, SchedulerTask)} or {@link #schedule(Class, String, String, int, int, SchedulerTask)} before.
     * 
     * @return All currently scheduled global tasks.
     */
    public List<Pair<SchedulerTask, Class<? extends CFeatureHolder>>> getGlobalTasks() {

        List<Pair<SchedulerTask, Class<? extends CFeatureHolder>>> tasks = new ArrayList<>();

        for (Pair<ScheduledTask, Class<? extends CFeatureHolder>> globalTask : globalTasks) {
            Pair<SchedulerTask, Class<? extends CFeatureHolder>> element = Pair.<SchedulerTask, Class<? extends CFeatureHolder>> of(globalTask.getLeft().getTask(), globalTask.getRight());
            tasks.add(element);
        }

        return tasks;
    }

    /**
     * Retrieves the {@link SchedulerTask} that is scheduled under the given holder class and has the given name.
     * If multiple tasks have the same name, the task that was scheduled first is returned.
     * 
     * @param name The name of the task that should be returned.
     * @param holderClass The {@link CFeatureHolder} class which was used when the task was scheduled.
     *        See {@link #schedule(Class, String, String, int, int, SchedulerTask)} for more information on holder classes.
     * 
     * @return The first task with the given name.
     */
    public SchedulerTask getGlobalTask(String name, Class<? extends CFeatureHolder> holderClass) {

        Validate.notNull(name, "Can't search for scheduler task with null name");

        for (Pair<ScheduledTask, Class<? extends CFeatureHolder>> element : globalTasks) {
            ScheduledTask task = element.getLeft();

            if (element.getRight() == holderClass && task.getName() != null && task.getName().equals(name)) {
                return task.getTask();
            }
        }

        return null;
    }

    /**
     * Schedules a global {@link SchedulerTask#cloneStateless() stateless copy} of the given {@link SchedulerTask} for the given holder class.
     * That means that every {@link Scheduler}, which is a feature of an instance of the holder class, will contain the task.
     * Note that the task can be can be removed using {@link #remove(Class, SchedulerTask)} or {@link #remove(Class, String)}.
     * Also note that calling this method is equivalent to calling {@link #schedule(Class, String, String, int, int, SchedulerTask)} with a {@code periodicDelay} of {@code -1}.<br>
     * <br>
     * The {@code name} can be used to retrieve the task from the scheduler definition using {@link #getGlobalTask(String, Class)}.
     * Moreover, it can be used to remove the global task using {@link #remove(Class, String)}.
     * Note that this field may be {@code null}, in which case the scheduler task is anonymous.<br>
     * <br>
     * The {@code initialDelay} defines the delay (amount of ticks) after which the task should be executed once.
     * In other words, that delay must elapse before the task is executed.
     * After the execution, the task is removed and won't be called again.
     * The delay is defined "inclusively". For example, a task with an initial delay of 5 is executed on the fifth tick after it was scheduled.
     * An immediate execution on the next group tick can be achieved by setting the initial delay to {@code 1}.<br>
     * <br>
     * The {@code group} field defines at which point during a tick the task should be executed.
     * A map of groups to priorities is used by the caller of the {@link Scheduler#update(String)} method.
     * For example, all tasks with a group which has the priority 2 are executed before all tasks of a priority 1 group.
     * 
     * @param holderClass The {@link CFeatureHolder} class to whose schedulers the task will be added to.
     *        It will also be used for every subclass of this class.
     *        For example, a task with holder class {@code TestFH} is only added to schedulers that are held by instances of {@code TestFH} or any subclass.
     * @param name The name that can be used to identify the task from inside the scheduler.
     *        This field may be {@code null}, in which case the task is anonymous.
     * @param group The group which defines at which point during a tick the task should be executed.
     * @param initialDelay The amount of ticks that must elapse before the task is executed once.
     * @param task The scheduler task that should be globally scheduled for execution.
     *        The same task object is internally used multiple times because a stateless copy is created each time it is locally scheduled.
     *        However, that is only possible if the task itself has no other state or overrides the {@link SchedulerTask#cloneStateless()} method.
     */
    public void schedule(Class<? extends CFeatureHolder> holderClass, String name, String group, int initialDelay, SchedulerTask task) {

        schedule(holderClass, name, group, initialDelay, -1, task);
    }

    /**
     * Schedules a global {@link SchedulerTask#cloneStateless() stateless copy} of the given {@link SchedulerTask} for the given holder class.
     * That means that every {@link Scheduler}, which is a feature of an instance of the holder class, will contain the task.
     * Note that the task can be can be removed using {@link #remove(Class, SchedulerTask)} or {@link #remove(Class, String)}.<br>
     * <br>
     * The {@code name} can be used to retrieve the task from the scheduler definition using {@link #getGlobalTask(String, Class)}.
     * Moreover, it can be used to remove the global task using {@link #remove(Class, String)}.
     * Note that this field may be {@code null}, in which case the scheduler task is anonymous.<br>
     * <br>
     * The two fields {@code initialDelay} and {@code periodicDelay} define the delays (amount of ticks) after which the task should be executed.
     * The initial delay must elapse before the task is executed for the first time.
     * The periodic delay must elapse before the task is executed for any subsequent time (the task is called repetitively).
     * Note that the periodic delay can also be {@code -1}. In that case, the task is cancelled after it was executed for the first time.
     * Any delay is defined "inclusively". For example, a task with an initial delay of 5 is executed on the fifth tick after it was scheduled.
     * An immediate execution on the next group tick can be achieved by setting the initial delay to {@code 1}.
     * An execution of the task on each subsequent tick is caused by setting the periodic delay to {@code 1}.<br>
     * <br>
     * The {@code group} field defines at which point during a tick the task should be executed.
     * A map of groups to priorities is used by the caller of the {@link Scheduler#update(String)} method.
     * For example, all tasks with a group which has the priority 2 are executed before all tasks of a priority 1 group.
     * 
     * @param holderClass The {@link CFeatureHolder} class to whose schedulers the task will be added to.
     *        It will also be used for every subclass of this class.
     *        For example, a task with holder class {@code TestFH} is only added to schedulers that are held by instances of {@code TestFH} or any subclass.
     * @param name The name that can be used to identify the task from inside the scheduler.
     *        This field may be {@code null}, in which case the task is anonymous.
     * @param group The group which defines at which point during a tick the task should be executed.
     * @param initialDelay The amount of ticks that must elapse before the task is executed for the first time.
     * @param periodicDelay The amount of ticks that must elapse before the task is executed for any subsequent time.
     * @param task The scheduler task that should be globally scheduled for execution.
     *        The same task object is internally used multiple times because a stateless copy is created each time it is locally scheduled.
     *        However, that is only possible if the task itself has no other state or overrides the {@link SchedulerTask#cloneStateless()} method.
     */
    public void schedule(Class<? extends CFeatureHolder> holderClass, String name, String group, int initialDelay, int periodicDelay, SchedulerTask task) {

        ScheduledTask scheduledTask = new ScheduledTask(name, group, initialDelay, periodicDelay, task);
        Pair<ScheduledTask, Class<? extends CFeatureHolder>> element = Pair.<ScheduledTask, Class<? extends CFeatureHolder>> of(scheduledTask, holderClass);
        globalTasks.add(element);

        // Invalidate cache
        classCache.clear();
    }

    /**
     * Removes the given global {@link SchedulerTask} which is scheduled under the given holder class.
     * 
     * @param holderClass The {@link CFeatureHolder} class which was used when the task was scheduled.
     *        See {@link #schedule(Class, String, String, int, int, SchedulerTask)} for more information on holder classes.
     * @param task The task that should be removed.
     */
    public void remove(Class<? extends CFeatureHolder> holderClass, SchedulerTask task) {

        ScheduledTask removalTask = null;

        for (Pair<ScheduledTask, Class<? extends CFeatureHolder>> element : globalTasks) {
            ScheduledTask scheduledTask = element.getLeft();

            if (element.getRight() == holderClass && scheduledTask.getTask().equals(task)) {
                removalTask = scheduledTask;
                break;
            }
        }

        if (removalTask != null) {
            remove(holderClass, removalTask);
        }
    }

    /**
     * Removes the global {@link SchedulerTask} which is scheduled under the given holder class and has the given name.
     * That only works if the scheduled task has a non-null name.
     * If multiple tasks have the same name, the task that was scheduled first is removed.
     * 
     * @param holderClass The {@link CFeatureHolder} class which was used when the task was scheduled.
     *        See {@link #schedule(Class, String, String, int, int, SchedulerTask)} for more information on holder classes.
     * @param name The name of the task for removal.
     */
    public void remove(Class<? extends CFeatureHolder> holderClass, String name) {

        ScheduledTask removalTask = null;

        for (Pair<ScheduledTask, Class<? extends CFeatureHolder>> element : globalTasks) {
            ScheduledTask scheduledTask = element.getLeft();

            if (element.getRight() == holderClass && scheduledTask.getName().equals(name)) {
                removalTask = scheduledTask;
                break;
            }
        }

        if (removalTask != null) {
            remove(holderClass, removalTask);
        }
    }

    private void remove(Class<? extends CFeatureHolder> holderClass, ScheduledTask task) {

        Pair<ScheduledTask, Class<? extends CFeatureHolder>> element = Pair.<ScheduledTask, Class<? extends CFeatureHolder>> of(task, holderClass);
        globalTasks.remove(element);

        // Invalidate cache
        classCache.clear();
    }

    @Override
    public Scheduler create(FeatureHolder holder) {

        Validate.isInstanceOf(CFeatureHolder.class, holder, "Only the usage of CFeatureHolder is allowed");

        Scheduler scheduler = new Scheduler(getName(), (CFeatureHolder) holder);

        for (ScheduledTask globalTask : getGlobalTasksForClass( ((CFeatureHolder) holder).getClass())) {
            scheduler.schedule(globalTask.getName(), globalTask.getGroup(), globalTask.getInitialDelay(), globalTask.getPeriodicDelay(), globalTask.getTask());
        }

        return scheduler;
    }

    private List<ScheduledTask> getGlobalTasksForClass(Class<? extends CFeatureHolder> holderClass) {

        List<ScheduledTask> classGlobalTasks = classCache.get(holderClass);

        if (classGlobalTasks == null) {
            classGlobalTasks = new ArrayList<>();

            for (Pair<ScheduledTask, Class<? extends CFeatureHolder>> element : globalTasks) {
                if (element.getRight().isAssignableFrom(holderClass)) {
                    classGlobalTasks.add(element.getLeft());
                }
            }

            classCache.put(holderClass, classGlobalTasks);
        }

        return classGlobalTasks;
    }

}
