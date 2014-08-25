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

package com.quartercode.disconnected.sim.scheduler;

import javax.xml.bind.annotation.XmlElement;
import com.quartercode.classmod.base.FeatureHolder;
import com.quartercode.classmod.extra.Function;
import com.quartercode.classmod.extra.FunctionDefinition;
import com.quartercode.classmod.util.FeatureDefinitionReference;

/**
 * A function call schedule task is a {@link ScheduleTask} that "just" calls a {@link Function}.
 * It supports persistence and can restore scheduled function calls the were serialized before.
 * For achieving that, it uses the {@link FeatureDefinitionReference} utility.
 * 
 * @see ScheduleTask
 * @see FeatureDefinitionReference
 */
public class FunctionCallScheduleTask implements ScheduleTask {

    @XmlElement
    private FeatureDefinitionReference<FunctionDefinition<?>> functionDefinition;

    /**
     * Creates a new empty function call schedule task.
     * This is only recommended for direct field access (e.g. for serialization).
     */
    protected FunctionCallScheduleTask() {

    }

    /**
     * Creates a new function call schedule task with the given {@link FeatureDefinitionReference}.
     * Note that the given object must reference a {@link FunctionDefinition}.
     * 
     * @param functionDefinition A feature definition reference that references the function definition which defines
     *        the {@link Function} that should be called by the task.
     */
    public FunctionCallScheduleTask(FeatureDefinitionReference<FunctionDefinition<?>> functionDefinition) {

        this.functionDefinition = functionDefinition;
    }

    @Override
    public void execute(FeatureHolder holder) {

        holder.get(functionDefinition.getDefinition()).invoke();
    }

}
