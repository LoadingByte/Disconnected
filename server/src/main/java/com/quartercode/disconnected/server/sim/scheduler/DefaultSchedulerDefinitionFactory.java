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

import org.apache.commons.lang3.Validate;
import com.quartercode.classmod.base.FeatureDefinition;
import com.quartercode.classmod.base.FeatureHolder;
import com.quartercode.classmod.def.base.AbstractFeatureDefinition;
import com.quartercode.classmod.extra.conv.CFeatureHolder;

/**
 * The default factory implementation provider for the {@link SchedulerDefinitionFactory}.
 * 
 * @see SchedulerDefinitionFactory
 */
public class DefaultSchedulerDefinitionFactory implements SchedulerDefinitionFactory {

    @Override
    public FeatureDefinition<Scheduler> create(String name) {

        Validate.notNull(name, "Name of new scheduler definition cannot be null");

        return new AbstractFeatureDefinition<Scheduler>(name) {

            @Override
            public Scheduler create(FeatureHolder holder) {

                Validate.isInstanceOf(CFeatureHolder.class, holder, "Schedulers can only be held by CFeatureHolders");
                Validate.isInstanceOf(SchedulerRegistryProvider.class, holder, "Schedulers can only be held by SchedulerRegistryHolders");

                return new DefaultScheduler(getName(), (CFeatureHolder) holder);
            }

        };
    }

}
