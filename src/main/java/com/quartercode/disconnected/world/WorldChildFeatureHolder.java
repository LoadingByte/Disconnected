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

package com.quartercode.disconnected.world;

import com.quartercode.disconnected.mocl.base.FeatureHolder;
import com.quartercode.disconnected.mocl.extra.ChildFeatureHolder;
import com.quartercode.disconnected.mocl.extra.def.DefaultChildFeatureHolder;

/**
 * The world child feature holder is a special {@link ChildFeatureHolder} which allows to resolve the holding {@link World}.
 * 
 * @param <P> The type the parent {@link FeatureHolder} has to have.
 * @see World
 */
public class WorldChildFeatureHolder<P extends FeatureHolder> extends DefaultChildFeatureHolder<P> {

    /**
     * Creates a new world child feature holder.
     */
    public WorldChildFeatureHolder() {

    }

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
