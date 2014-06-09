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

package com.quartercode.disconnected.sim;

import static com.quartercode.classmod.ClassmodFactory.create;
import org.apache.commons.lang3.reflect.TypeLiteral;
import com.quartercode.classmod.base.FeatureHolder;
import com.quartercode.classmod.extra.Function;
import com.quartercode.classmod.extra.FunctionDefinition;

/**
 * {@link FeatureHolder}s which implement this interface inherit the {@link #TICK_UPDATE} {@link Function} that is automatically invoked by the tick simulator.
 * The simulator goes over all {@link FeatureHolder}s of a world and invoked that {@link #TICK_UPDATE} {@link Function} on all tick updatables.
 */
public interface TickUpdatable extends FeatureHolder {

    /**
     * The tick update {@link Function} is automatically invoked by the tick simulator on every tick.
     * It should execute some activities related to the simulation of the world tree.
     */
    public static final FunctionDefinition<Void> TICK_UPDATE = create(new TypeLiteral<FunctionDefinition<Void>>() {}, "name", "tickUpdate", "parameters", new Class<?>[0]);

}
