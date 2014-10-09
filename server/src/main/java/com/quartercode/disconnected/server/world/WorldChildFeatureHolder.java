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
import com.quartercode.classmod.extra.def.DefaultCChildFeatureHolder;

/**
 * The world child feature holder is a special {@link CFeatureHolder} which allows to resolve the holding {@link World}.
 * 
 * @param <P> The type the parent {@link CFeatureHolder} has to have.
 * @see World
 * @see DefaultCChildFeatureHolder
 */
public class WorldChildFeatureHolder<P extends CFeatureHolder> extends DefaultCChildFeatureHolder<P> {

    /**
     * Resolves the {@link World} this world child feature holder is in.
     * 
     * @return The {@link World} which uses this feature holder.
     */
    public World getWorld() {

        if (getParent() instanceof World) {
            return (World) getParent();
        } else if (getParent() instanceof WorldChildFeatureHolder) {
            return ((WorldChildFeatureHolder<?>) getParent()).getWorld();
        } else {
            return null;
        }
    }

}
