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

package com.quartercode.disconnected.server.world.comp.os;

import static com.quartercode.classmod.factory.ClassmodFactory.factory;
import com.quartercode.classmod.extra.func.FunctionDefinition;
import com.quartercode.classmod.factory.FunctionDefinitionFactory;
import com.quartercode.disconnected.server.world.util.WorldChildFeatureHolder;

/**
 * The base class for all {@link OS operating system} modules.
 * Such OS modules are an essential part of the operating system.
 * They define methods which are required by the operating system to interact with these modules.
 * 
 * @see OSModule#SET_RUNNING
 * @see OS
 */
public abstract class OSModule extends WorldChildFeatureHolder<OS> {

    // ----- Functions -----

    /**
     * Called on the bootstrap (true) or shutdown (false) of the {@link OS operating system} which uses the module.
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
     * <td>{@code True} if the operating system is booting up, {@code false} if it's shutting down.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void> SET_RUNNING = factory(FunctionDefinitionFactory.class).create("setRunning", new Class[] { Boolean.class });

    /**
     * Creates a new generic {@link OS operating system} module.
     */
    public OSModule() {

        setParentType(OS.class);
    }

}
