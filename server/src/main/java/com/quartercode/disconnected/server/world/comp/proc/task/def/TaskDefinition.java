
package com.quartercode.disconnected.server.world.comp.proc.task.def;

/**
 * Instances of this class define a task, which includes the provision of its {@link TaskMetadata} and the creation of new {@link TaskExecutor} instances.
 * For the different applications of tasks (e.g. programs or scripts), different task definition objects must be used.
 * For example, program task definition objects just store the class of the program task executor and create new instances using reflection.
 *
 * @see TaskExecutor
 * @see TaskMetadata
 */
public interface TaskDefinition {

    /**
     * Returns a data object that defines the "frame" API of a defined {@link #createExecutorInstance() task executor}.
     * See the {@link TaskMetadata} object for more details on what is defined exactly.
     *
     * @return The metadata which configures the tasks.
     */
    public TaskMetadata getMetadata();

    /**
     * Creates a new instance of the {@link TaskExecutor} which runs the defined task.
     * The returned executor just needs to be {@link TaskExecutor#run(java.util.Map) ran}, all other internal preparations have already been done.<br>
     * <br>
     * Internally, the implementations of this method differ quite a bit.
     * For example, program task definition objects just store the class of the program task executor and create new instances using reflection.
     *
     * @return A new instance of the task executor which is able to run execute the defined task.
     */
    public TaskExecutor createExecutorInstance();

}
