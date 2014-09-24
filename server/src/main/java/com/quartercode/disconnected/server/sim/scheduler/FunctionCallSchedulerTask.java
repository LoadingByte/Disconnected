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
import com.quartercode.classmod.base.FeatureHolder;
import com.quartercode.classmod.extra.Function;
import com.quartercode.classmod.extra.FunctionDefinition;
import com.quartercode.classmod.util.FeatureDefinitionReference;

/**
 * A function call scheduler task is a {@link SchedulerTask} that "just" calls a {@link Function}.
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
     * Creates a new function call scheduler task with the given {@link FeatureDefinitionReference} which is executed <b>once</b> after the given initial delay.
     * Note that the given reference must point to a {@link FunctionDefinition}.
     * See the provided methods of the {@link SchedulerTask} class for more information on the other parameters.
     * 
     * @param name The name that can be used to identify the task inside a {@link Scheduler}.
     *        This field may be {@code null}, in which case the task is anonymous.
     * @param group The group which defines at which point inside a tick the task should be executed.
     * @param initialDelay The amount of ticks that must elapse before the task is executed.
     * @param functionDefinition A feature definition reference that references the function definition which defines
     *        the {@link Function} that should be called by the task.
     * 
     * @see SchedulerTask#getInitialDelay()
     * @see SchedulerTask#getGroup()
     */
    public FunctionCallSchedulerTask(String name, String group, int initialDelay, FeatureDefinitionReference<FunctionDefinition<?>> functionDefinition) {

        super(name, group, initialDelay);

        this.functionDefinition = functionDefinition;
    }

    /**
     * Creates a new function call scheduler task with the given {@link FeatureDefinitionReference} whose execution starts after the given initial delay
     * and then continues with gaps of the given periodic delay.
     * See the provided methods of the {@link SchedulerTask} class for more information on the other parameters.
     * 
     * @param name The name that can be used to identify the task inside a {@link Scheduler}.
     *        This field may be {@code null}, in which case the task is anonymous.
     * @param group The group which defines at which point inside a tick the task should be executed.
     * @param initialDelay The amount of ticks that must elapse before the task is executed for the first time.
     * @param periodicDelay The amount of ticks that must elapse before the task is executed for any subsequent time.
     * @param functionDefinition A feature definition reference that references the function definition which defines
     *        the {@link Function} that should be called by the task.
     * 
     * @see SchedulerTask#getInitialDelay()
     * @see SchedulerTask#getPeriodicDelay()
     * @see SchedulerTask#getGroup()
     */
    public FunctionCallSchedulerTask(String name, String group, int initialDelay, int periodicDelay, FeatureDefinitionReference<FunctionDefinition<?>> functionDefinition) {

        super(name, group, initialDelay, periodicDelay);

        this.functionDefinition = functionDefinition;
    }

    @Override
    public void execute(FeatureHolder holder) {

        holder.get(functionDefinition.getDefinition()).invoke();
    }

}
