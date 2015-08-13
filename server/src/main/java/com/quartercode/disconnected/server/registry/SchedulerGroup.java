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

import org.apache.commons.lang3.Validate;
import com.quartercode.disconnected.shared.util.registry.extra.NamedValue;

/**
 * A data object that represents a scheduler group.
 *
 * @see ServerRegistries#SCHEDULER_GROUPS
 */
public class SchedulerGroup extends RegistryObject implements NamedValue {

    private final String name;
    private final int    priority;

    /**
     * Creates a new scheduler group data object.
     *
     * @param name The name of the group.
     * @param priority The priority of the group.
     */
    public SchedulerGroup(String name, int priority) {

        Validate.notNull(name, "Scheduler group name cannot be null");

        this.name = name;
        this.priority = priority;
    }

    /**
     * Returns the name of the group.
     * See {@link ServerRegistries#SCHEDULER_GROUPS} for more details.
     *
     * @return The group name.
     */
    @Override
    public String getName() {

        return name;
    }

    /**
     * Returns the priority of the group.
     * See {@link ServerRegistries#SCHEDULER_GROUPS} for more details.
     *
     * @return The group priority.
     */
    public int getPriority() {

        return priority;
    }

}
