/*
 * This file is part of Disconnected.
 * Copyright (c) 2013 QuarterCode <http://quartercode.com/>
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

package com.quartercode.disconnected.server.world.comp.proc.task.def;

import java.util.Map;
import com.quartercode.disconnected.server.world.comp.proc.Process;
import com.quartercode.disconnected.server.world.comp.proc.ProcessStateListener;
import com.quartercode.disconnected.server.world.comp.proc.task.exec.TaskProcess;
import com.quartercode.disconnected.server.world.comp.proc.task.exec.TaskRunner;
import com.quartercode.jtimber.api.node.Node;

/**
 * This interface implements a task which runs some kind of executable.
 * A typical task could be one specific aspect of a program or a task which has been programmed using a script.
 * For example, a file manager program might contain different tasks for listing, adding, and deleting files.
 * On the file system, all tasks which are related to the same "unit" (e.g. a program or a script) are stored in or referenced from one {@link TaskContainer} object,
 * which in turn is stored in a content file.<br>
 * <br>
 * The tasks can then be used in two ways:<br>
 * Firstly, a classic client-side file manager GUI could launch a server-side {@link InteractiveProcess} for a task container (e.g. a program or a script).
 * In human language, the program or script is launched in "interactive graphical mode".
 * Via that interactive process, the client-side GUI is able to launch different tasks of the task container in order to fulfill the player's commands.
 * For example, a "delete file" button for a program would launch a delete file task.<br>
 * Secondly, scripts are also able to use the individual tasks of a program or another script.
 * For example, a script could launch an add file task in order to create a new file programmatically.<br>
 * This way, the concept of tasks is abstracted and code duplication (for both player GUIs and script elements) is avoided.
 * Moreover, it is worth mentioning that a running task is <b>always</b> managed by a {@link TaskProcess}, even if it has been started by an interactive one.<br>
 * <br>
 * The whole task system provides an abstract API for tasks:<br>
 * Firstly, each task is started with a call to its {@link #run(Map)} method.
 * The map supplies the task which different {@link TaskMetadata#getInputParameters() input arguments}.
 * For example, a file delete task would be supplied with a file path so it knows which file to delete.<br>
 * At some defined points (in most cases on finish), the task can report to the {@link TaskRunner} by calling one or more of the defined {@link TaskMetadata#getCallbacks() callbacks}.
 * Such callbacks also carry output arguments which inform the task runner about the task's status or return some requested information (e.g. a file listing).<br>
 * As a special feature, tasks which implement the {@link ProcessStateListener} interface are automatically informed whenever their process state changes.
 * Since many tasks need to know when their state changes, this shortcut removes the need for external process state listeners.<br>
 * <br>
 * Note that the {@link TaskContainer} does not references to task executor classes directly, in order to make the system more flexible and suitable for scripts.
 * Instead, the container only stores {@link TaskDefinition}s, which in turn are able to {@link TaskDefinition#createExecutorInstance() create} instances of its defined task executor.
 * For example, the task definition used for program tasks stores the program task executor's class and uses reflection to create new executor instances.<br>
 * The whole "frame" API depicated above is configured via the {@link TaskMetadata} object which must be provided by each task definition.
 * The metadata object stores information about the input parameters and available callbacks of a task.
 * 
 * @see TaskDefinition
 * @see TaskRunner
 * @see TaskProcess
 */
public interface TaskExecutor extends Node<TaskRunner> {

    /**
     * This method is executed exactly one time when the task should start running
     * Sometimes, a task can execute all its activities and terminate afterwards in one single tick.
     * If thats not the case, this method probably needs to use a scheduler.
     * It is important to note that such a scheduler should be stored inside the task object itself or any of its "subobjects".
     * That way, the scheduler is automatically removed once the task stops.<br>
     * <br>
     * <b>Important note:</b>
     * If this method registers some kind of hooks apart from scheduler tasks (for example, if it registers socket connection listeners), they must be removed when the program stops.
     * In order to do that automatically, the methods for registering such hooks always provide a second method which are able to register hooks for a specific {@link Process}.
     * You should always use those method, as the hooks are automatically removed once the task process is stopped.
     * Note that you can retrieve the process which hosts a running tasks by getting the parent of the parent of a task:
     *
     * <pre>
     * class Task {
     *     void run(...) {
     *         TaskProcess process = getSingleParent().getSingleParent().
     *     }
     * }
     * </pre>
     *
     * @param inputArguments The {@link TaskMetadata#getInputParameters() input arguments} which supply the task with information about what it should do.
     *        For example, a file delete task would be supplied with a file path so it knows which file to delete.
     * @throws MissingArgumentException If the input argument map doesn't contain an input argument which is defined as non-optional.
     *         Note that this should be the only exception a task is throwing; all other exceptions should instead be handled as callbacks!
     */
    public void run(Map<String, Object> inputArguments);

}
