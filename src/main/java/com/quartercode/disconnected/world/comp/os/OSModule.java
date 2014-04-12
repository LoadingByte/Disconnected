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

package com.quartercode.disconnected.world.comp.os;

import com.quartercode.classmod.extra.FunctionDefinition;
import com.quartercode.classmod.extra.def.DefaultChildFeatureHolder;
import com.quartercode.classmod.util.FunctionDefinitionFactory;

/**
 * The base class for all {@link OperatingSystem} modules.
 * Such {@link OperatingSystem} modules are an essential part of the {@link OperatingSystem}.
 * They define methods which are required by the {@link OperatingSystem} to interact with these modules.
 * 
 * @see OSModule#SET_RUNNING
 * @see OperatingSystem
 */
public abstract class OSModule extends DefaultChildFeatureHolder<OperatingSystem> {

    // ----- Functions -----

    /**
     * Called on the bootstrap (true) or shutdown (false) of the {@link OperatingSystem} which uses the module.
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
     * <td>{@link Boolean}</td>
     * <td>running</td>
     * <td>True if the {@link OperatingSystem} is booting up, false if it's shutting down.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void> SET_RUNNING = FunctionDefinitionFactory.create("setRunning", Boolean.class);

    /**
     * Creates a new generic {@link OperatingSystem} module.
     */
    public OSModule() {

    }

}
