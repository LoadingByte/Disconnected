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

import com.quartercode.classmod.base.FeatureHolder;

/**
 * A scheduler registry provider is able to provide a {@link SchedulerRegistry} when the {@link #getSchedulerRegistry()} method is called.
 * Note that {@link Scheduler}s expect their {@link FeatureHolder holders} to be such a provider in order to retrieve their scheduler registry.
 * See the {@link Scheduler} class for more information on that.
 * 
 * @see SchedulerRegistry
 */
public interface SchedulerRegistryProvider {

    /**
     * Returns the {@link SchedulerRegistry} that is provided by the scheduler registry provider.
     * Note that it may be {@code null}.
     * 
     * @return The provided scheduler registry.
     */
    public SchedulerRegistry getSchedulerRegistry();

}
