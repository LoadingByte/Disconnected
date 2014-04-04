
package com.quartercode.disconnected.sim.run;

import com.quartercode.classmod.base.FeatureHolder;
import com.quartercode.classmod.extra.ExecutorInvocationException;

/**
 * A schedule task is a unit that can be called by a {@link Scheduler}.
 * It only contains one {@link #execute(FeatureHolder)} method that is invoked whenever the {@link Scheduler} calls the task.
 * A task can be used multiple times in the same {@link Scheduler}, even at the same time.<br>
 * <br>
 * Please note that the state of a schedule task has to be restorable using JAXB persistence.
 * That means that every attribute must have a JAXB annotation etc.
 * 
 * @see Scheduler
 */
public interface ScheduleTask {

    /**
     * Executes some functionality the schedule task defines.
     * This method should only be used by the {@link Scheduler} in order to execute a task.
     * 
     * @param holder The {@link FeatureHolder} where the {@link Scheduler} that calls the method is used in.
     * @throws ExecutorInvocationException Something goes wrong while executing the method.
     *         Please not that these exceptions may cause errors because the scheduler just throws them up the stack.
     */
    public abstract void execute(FeatureHolder holder) throws ExecutorInvocationException;

}
