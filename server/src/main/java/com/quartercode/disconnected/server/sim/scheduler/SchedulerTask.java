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

package com.quartercode.disconnected.server.sim.scheduler;

import static com.quartercode.classmod.factory.ClassmodFactory.factory;
import com.quartercode.classmod.extra.conv.CFeatureHolder;
import com.quartercode.classmod.extra.func.FunctionDefinition;
import com.quartercode.classmod.factory.FunctionDefinitionFactory;
import com.quartercode.disconnected.shared.util.XmlPersistent;

/**
 * A scheduler task is an action that can be scheduled to be executed using a {@link Scheduler}.
 * It contains the {@link #EXECUTE} function to call it when the right time has come.
 * Setting the {@link #isCancelled() cancellation} flag to {@code true} cancels the task and removes it from its scheduler.
 * That means that the task will no longer be executed.<br>
 * <br>
 * Please note that the state of a scheduler task must be serializable using JAXB persistence.
 * 
 * @see Scheduler
 * @see SchedulerTaskAdapter
 */
@XmlPersistent
public interface SchedulerTask extends CFeatureHolder {

    // ----- Functions -----

    /**
     * Executes the scheduler task.
     * This function should only be used by a {@link Scheduler} in order to execute a task.
     * 
     * <table>
     * <tr>
     * <th>Index</th>
     * <th>Type</th>
     * <th>Parameter</th>
     * <th>Description</th>
     * </tr>
     * <tr>
     * <td>0</td>
     * <td>{@link CFeatureHolder}</td>
     * <td>schedulerHolder</td>
     * <td>The feature holder that contains the scheduler which called the method.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void> EXECUTE = factory(FunctionDefinitionFactory.class).create("execute", new Class[] { CFeatureHolder.class });

    // ----- Regular Methods -----

    /**
     * Returns whether the task has been cancelled and should no longer be executed.
     * 
     * @return Whether the scheduler task has been cancelled.
     */
    public boolean isCancelled();

    /**
     * Cancels the scheduler task so it will no longer be executed.
     * This method should set the {@link #isCancelled()} flag to {@code true}.
     */
    public void cancel();

}
