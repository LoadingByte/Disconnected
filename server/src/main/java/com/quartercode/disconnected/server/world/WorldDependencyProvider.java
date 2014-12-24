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

package com.quartercode.disconnected.server.world;

import java.util.Random;
import com.quartercode.disconnected.server.sim.scheduler.Scheduler;
import com.quartercode.disconnected.server.sim.scheduler.SchedulerRegistry;
import com.quartercode.disconnected.server.sim.scheduler.SchedulerRegistryProvider;
import com.quartercode.eventbridge.bridge.Bridge;

/**
 * An {@link World#setDependencyProvider(WorldDependencyProvider) "injected"} world dependency provider is used by a {@link World} object to retrieve the following dependencies:
 * 
 * <ul>
 * <li>{@link Random} object, {@link World#getRandom()}</li>
 * <li>{@link Bridge}, {@link World#getBridge()}</li>
 * <li>{@link SchedulerRegistry}, {@link World#getSchedulerRegistry()}</li>
 * </ul>
 * 
 * Those dependencies can then be used by all objects inside the world tree.
 * 
 * @see World
 */
public interface WorldDependencyProvider extends SchedulerRegistryProvider {

    /**
     * Returns the {@link Random} object that is provided to the world and all its objects.
     * 
     * @return The random object the world can use.
     * @see World#getRandom()
     */
    public Random getRandom();

    /**
     * Returns the {@link Bridge} that should be used for sending events by any object in the world tree.
     * 
     * @return The bridge the world can use.
     * @see World#getBridge()
     */
    public Bridge getBridge();

    /**
     * Returns the {@link SchedulerRegistry} that can be used by all {@link Scheduler} features in the world tree.
     * 
     * @return The scheduler registry the world can use.
     * @see World#getSchedulerRegistry()
     */
    @Override
    public SchedulerRegistry getSchedulerRegistry();

}
