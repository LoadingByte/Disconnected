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

import com.quartercode.classmod.extra.CFeatureHolder;
import com.quartercode.classmod.extra.def.DefaultCFeatureHolder;

/**
 * The world feature holder is a special {@link WorldChildFeatureHolder} which may have any {@link CFeatureHolder} as parent.
 * It allows {@link #getWorld()} to be accessed on feature holders which do not have a defined parent.
 * Each feature holder (apart from the root one) which is not a world child feature holder should extend this class.
 * 
 * @see World
 * @see DefaultCFeatureHolder
 * @see WorldChildFeatureHolder
 */
public class WorldFeatureHolder extends WorldChildFeatureHolder<CFeatureHolder> {

    /**
     * Creates a new world feature holder.
     */
    public WorldFeatureHolder() {

        setParentType(CFeatureHolder.class);
    }

}
