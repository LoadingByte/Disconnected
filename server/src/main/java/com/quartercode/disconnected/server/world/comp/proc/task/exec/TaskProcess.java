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

package com.quartercode.disconnected.server.world.comp.proc.task.exec;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import org.apache.commons.lang3.Validate;
import com.quartercode.disconnected.server.world.comp.file.ContentFile;
import com.quartercode.disconnected.server.world.comp.proc.Process;
import com.quartercode.disconnected.server.world.comp.proc.ProcessStateListener;
import com.quartercode.disconnected.server.world.comp.proc.task.def.TaskContainer;
import com.quartercode.disconnected.server.world.comp.proc.task.def.TaskDefinition;
import com.quartercode.disconnected.server.world.comp.proc.task.def.TaskExecutor;
import com.quartercode.disconnected.shared.world.comp.file.FileRights;
import com.quartercode.disconnected.shared.world.comp.proc.WorldProcessState;
import com.quartercode.jtimber.api.node.Weak;

/**
 * This class represents a task {@link Process} which executes one single isolated {@link TaskExecutor}.
 * Task processes allow the creator of the task process to communicate with the task (and vice versa) through a custom {@link TaskRunner}.<br>
 * For example, a task process might host a running script executor task.
 * That task then launches child task processes with a custom task runner, each of which hosts another task which is used by the script.
 * Whenever one of the subprocesses finishes, it informs the custom task runner about its its results by calling a callback.
 * The task runner can then delegate that callback to the parent process.
 *
 * @see Process
 * @see TaskContainer
 * @see TaskExecutor
 */
public class TaskProcess extends Process {

    @Weak
    @XmlAttribute
    @XmlIDREF
    private ContentFile sourceFile;
    @XmlAttribute
    private String      taskName;

    @XmlElement
    private TaskRunner  taskRunner;

    // JAXB constructor
    protected TaskProcess() {

    }

    /**
     * Creates a new task process.
     *
     * @param sourceFile The executed {@link ContentFile} which contains a {@link TaskContainer}, which in turn contains the {@link TaskExecutor} that should be ran.
     *        Such a container might be a program or a script. Therefore, this file might be a program or a script file.
     * @param taskName The name of the task executor that should be ran.
     *        This name uniquely identifies the task executor inside the provided task container.
     *        For example, a file manager program might contain different task executors with the names "addFile", "moveFile", "deleteFile" etc.
     * @param taskRunner The custom {@link TaskRunner} which is responsible for the "framework" seen by the task.
     *        The runner both launches a task (and therefore also provides its input arguments) and processes calls to callbacks.
     * @throws IllegalArgumentException If the provided source file does not contain a task container object.
     */
    public TaskProcess(ContentFile sourceFile, String taskName, TaskRunner taskRunner) {

        Validate.notNull(sourceFile, "Task process source file cannot be null");
        Validate.isInstanceOf(TaskContainer.class, sourceFile.getContent(), "Task process source file must contain a task container");

        this.sourceFile = sourceFile;
        this.taskName = taskName;
        this.taskRunner = taskRunner;

        validateDynamicState();
    }

    private void validateDynamicState() {

        Validate.notNull(taskRunner, "Task process task runner cannot be null");
        Validate.isTrue(taskRunner.getTaskExecutor() == null, "Task process task runner '%s' is already in use", taskRunner);
    }

    /**
     * Returns the executed {@link ContentFile} which contains a {@link TaskContainer}, which in turn contains the {@link TaskExecutor} that should be ran.
     * Such a container might be a program or a script. Therefore, this file might be a program or a script file.
     *
     * @return The executable file the process executes.
     */
    public ContentFile getSourceFile() {

        return sourceFile;
    }

    /**
     * Returns the {@link TaskContainer} which contains the {@link TaskExecutor} that should be ran.
     * Such a container might be a program or a script.
     * It is stored inside the processe's {@link #getSource() executable source file}.
     *
     * @return The task container which contains the task executor for execution.
     */
    public TaskContainer getTaskContainer() {

        return (TaskContainer) sourceFile.getContent();
    }

    /**
     * Returns the name of the {@link TaskExecutor} that should be ran.
     * This name uniquely identifies the task executor inside the processe's {@link #getTaskContainer() task container}.
     * For example, a file manager program might contain different task executors with the names "addFile", "moveFile", "deleteFile" etc.
     *
     * @return The name of the task executor for execution, relative to the set {@link TaskContainer}.
     */
    public String getTaskName() {

        return taskName;
    }

    /**
     * Returns the actual {@link TaskExecutor} which contains the logic of the process.
     * This value is only available after process {@link #initialize() initialization} since that method derives it from the {@link #getSource() source file.}
     *
     * @return The task executor which runs the process.
     */
    public TaskExecutor getTaskExecutor() {

        return taskRunner.getTaskExecutor();
    }

    @Override
    protected void applyState(WorldProcessState state) {

        WorldProcessState oldState = getState();

        super.applyState(state);

        // If the task executor implements the "ProcessStateListener", call the listener method on that executor
        // Since many tasks need to know when their state changes, this feature removes the need for external process state listeners
        if (getTaskExecutor() instanceof ProcessStateListener) {
            ((ProcessStateListener) getTaskExecutor()).onStateChange(this, oldState, state);
        }
    }

    @Override
    protected void initialize() throws Exception {

        // Check that the content file and task runner objects are still valid
        validateDynamicState();

        super.initialize();

        // Check the read and execution rights on the source file
        sourceFile.checkRights("initialize task process using source file", getUser(), FileRights.READ, FileRights.EXECUTE);

        // Retrieve the task definition and let the task runner execute it
        TaskDefinition task = getTaskContainer().getTasks().get(taskName);
        Validate.isTrue(task != null, "The task container '%s' (file '%s') doesn't contain a task with the name '%s'", getTaskContainer(), sourceFile.getPath(), taskName);
        try {
            taskRunner.run(task);
        } catch (RuntimeException e) {
            throw new RuntimeException("Unexpected exception during initial execution of new task executor instance (class '" + getTaskExecutor().getClass().getName() + "')", e);
        }
    }

}
