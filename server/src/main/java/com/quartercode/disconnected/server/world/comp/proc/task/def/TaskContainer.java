
package com.quartercode.disconnected.server.world.comp.proc.task.def;

import java.util.Map;

/**
 * A task container just stores different {@link TaskDefinition}s which belong to one logical "unit" (e.g. a program or a script).
 * The {@link #getTasks()} method provides all those stored task definitions.
 * Moreover, each task is mapped to a string key which can be seen as the task name.
 * For example, a class {@code Program} would implement this class and return all available program task definitions for its program.
 *
 * @see TaskDefinition
 */
public interface TaskContainer {

    /**
     * Returns the <b>all</b> {@link TaskDefinition}s which are provided by this task container.
     * Each task is mapped to a string key which can be seen as the task name.
     * Therefore, you can retrieve a specific task if you know its name and it exists inside this container.
     * Note that nested names should use dots ({@code "."}) for separating the different path elements (e.g. {@code "fileTools.deleteFile"}).
     *
     * @return All task definitions which are stored in this container.
     */
    public Map<String, TaskDefinition> getTasks();

}
