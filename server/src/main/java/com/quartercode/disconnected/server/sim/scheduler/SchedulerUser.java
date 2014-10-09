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

import com.quartercode.classmod.extra.CFeatureHolder;

/**
 * {@link CFeatureHolder}s which implement this interface inherit the {@link #SCHEDULER} feature which is a {@link Scheduler} that
 * is automatically updated by the tick simulator.
 * The simulator visits all {@link CFeatureHolder}s of a world and updates all automatic schedulers with all groups in the correct order.
 * 
 * @see Scheduler
 */
public interface SchedulerUser extends CFeatureHolder {

    /**
     * The {@link Scheduler} which is automatically updated by the tick simulator on every tick.
     */
    public static final SchedulerDefinition SCHEDULER = new SchedulerDefinition("scheduler");

}
