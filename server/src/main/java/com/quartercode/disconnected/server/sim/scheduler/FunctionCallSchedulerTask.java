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

import javax.xml.bind.annotation.XmlElement;
import com.quartercode.classmod.extra.conv.CFeatureHolder;
import com.quartercode.classmod.extra.func.Function;
import com.quartercode.classmod.extra.func.FunctionDefinition;
import com.quartercode.classmod.util.FeatureDefinitionReference;

/**
 * A function call scheduler task is a {@link SchedulerTask} that "just" calls a {@link Function} on execution.
 * It supports persistence and can restore scheduled function calls that were serialized before.
 * For achieving that, it uses the {@link FeatureDefinitionReference} utility.
 * 
 * @see SchedulerTask
 * @see FeatureDefinitionReference
 */
public class FunctionCallSchedulerTask extends SchedulerTaskAdapter {

    @XmlElement
    private FeatureDefinitionReference<FunctionDefinition<?>> functionDefinition;

    /**
     * Creates a new empty function call scheduler task.
     * This is only recommended for direct field access (e.g. for serialization).
     */
    protected FunctionCallSchedulerTask() {

    }

    /**
     * Creates a new function call scheduler task with the given {@link FeatureDefinitionReference}.
     * Note that the given reference must point to a {@link FunctionDefinition}.
     * 
     * @param functionDefinition A feature definition reference that references the function definition which defines
     *        the {@link Function} that should be called by the task.
     */
    public FunctionCallSchedulerTask(FeatureDefinitionReference<FunctionDefinition<?>> functionDefinition) {

        this.functionDefinition = functionDefinition;
    }

    @Override
    public void execute(CFeatureHolder holder) {

        holder.invoke(functionDefinition.getDefinition());
    }

}
