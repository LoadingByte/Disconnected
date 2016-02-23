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

package com.quartercode.disconnected.server.registry;

import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.Validate;
import com.quartercode.disconnected.shared.util.registry.extra.NamedValue;

/**
 * A data object that represents a world program by storing its size and {@link WorldProgramTask tasks}.
 *
 * @see ServerRegistries#WORLD_PROGRAMS
 */
public class WorldProgram extends RegistryObject implements NamedValue {

    private final String                 name;
    private final long                   size;
    private final List<WorldProgramTask> tasks;

    /**
     * Creates a new world program data object.
     *
     * @param name The {@link #getName() name} (or "key") of the world program.
     * @param size The {@link #getSize() size} of the world program (in bytes).
     * @param tasks The {@link #getTasks() tasks} of the world program, each of which implements a small aspect of the program's functionality.
     */
    public WorldProgram(String name, long size, List<WorldProgramTask> tasks) {

        Validate.notNull(name, "World program name cannot be null");
        Validate.isTrue(size >= 0, "World program size must be >= 0: %d", size);
        Validate.notNull(tasks, "World program task list cannot be null");

        this.name = name;
        this.size = size;
        this.tasks = Collections.unmodifiableList(tasks);
    }

    /**
     * Returns the name (or "key") of the world program.
     * It is used to reference the program, e.g. when it is stored inside a file.
     *
     * @return The program name.
     */
    @Override
    public String getName() {

        return name;
    }

    /**
     * Returns the size of the world program (in bytes).
     *
     * @return The program size.
     */
    public long getSize() {

        return size;
    }

    /**
     * Returns the defined {@link WorldProgramTask tasks} of the world program, each of which implements a small aspect of the program's functionality.
     * See the task documentation for more information on tasks.
     *
     * @return The program tasks.
     */
    public List<WorldProgramTask> getTasks() {

        return tasks;
    }

    /**
     * A data object that defines a task which runs one specific aspect of a {@link WorldProgram}.
     * For example, a file manager program might contain different tasks for listing, adding, and deleting files.
     * See the world classes related to tasks for more internal information about the implementation and usage of tasks.
     *
     * @see WorldProgram
     */
    public static class WorldProgramTask extends RegistryObject {

        private final String   name;
        private final Class<?> executorClass;

        /**
         * Creates a new world program task data object.
         *
         * @param name The {@link #getName() name} (or "key") of the world program task.
         * @param executorClass The {@link #getType() task executor class} which implements the logic of the new task.
         */
        public WorldProgramTask(String name, Class<?> executorClass) {

            Validate.notNull(name, "World program task name cannot be null");
            Validate.notNull(executorClass, "World program task executor class cannot be null");

            this.name = name;
            this.executorClass = executorClass;
        }

        /**
         * Returns the name (or "key") of the world program task.
         * In combination with the {@link WorldProgram#getName() program name} of the program the task is part of, it uniquely identifies the task.
         * Note that multiple tasks of <b>different</b> can have the same name, as long as there are not two tasks with the same name inside a single program.
         *
         * @return The task name.
         */
        public String getName() {

            return name;
        }

        /**
         * Returns the task executor class which implements the logic of the defined task.
         * This class must be defined for all world program tasks.
         *
         * @return The task executor class.
         */
        public Class<?> getExecutorClass() {

            return executorClass;
        }

    }

}
