
package com.quartercode.disconnected.server.world.comp.proc.task.def;

import com.quartercode.disconnected.server.world.comp.proc.task.exec.TaskProcess;
import com.quartercode.disconnected.server.world.comp.proc.task.exec.TaskRunner;
import com.quartercode.disconnected.server.world.util.WorldNode;

/**
 * Extending this abstract class adapter works exactly the same as implementing the {@link TaskExecutor} interface; however, this adapter adds some useful methods.
 * See {@link TaskExecutor} for more information.
 *
 * @see #getRunner()
 * @see #getProcess()
 */
public abstract class TaskExecutorAdapter extends WorldNode<TaskRunner> implements TaskExecutor {

    /**
     * Returns the {@link TaskRunner} which is responsible for running this task executor, as well as providing its "frame API".
     * For example, you are able to {@link TaskRunner#callback(String, java.util.Map) call callbacks} through the task runner object.<br>
     * Internally, this method just returns the {@link #getSingleParent() parent} of this task executor object.
     *
     * @return The task runner which has been assigned to this task.
     */
    protected TaskRunner getRunner() {

        return getSingleParent();
    }

    /**
     * Returns the {@link TaskProcess} which represents this task executor in the process tree.
     * See the task process class for more details.
     *
     * @return The process which runs the task.
     */
    protected TaskProcess getProcess() {

        return getRunner().getSingleParent();
    }

}
