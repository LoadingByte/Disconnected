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

import java.lang.ref.WeakReference;
import com.quartercode.classmod.base.FeatureHolder;
import com.quartercode.classmod.extra.ValueSupplier;
import com.quartercode.disconnected.world.World;

/**
 * This class implements the tick update mechanism for simulating a {@link World}.
 */
public class TickSimulator implements TickAction {

    private volatile WeakReference<World> world;

    /**
     * Returns the {@link World} that is currently simulated by the tick simulator.
     * 
     * @return The currently simulated world.
     */
    public World getWorld() {

        return world == null ? null : world.get();
    }

    /**
     * Changes the {@link World} that is currently simulated by the tick simulator.
     * The change will take place in the next tick.
     * 
     * @param world The new world to simulate.
     */
    public void setWorld(World world) {

        this.world = world == null ? null : new WeakReference<>(world);
    }

    /**
     * Executes the tick update on all {@link TickUpdatable}s of the set world.
     */
    @Override
    public void update() {

        if (getWorld() != null) {
            // Execute world object ticks
            updateObject(getWorld());
        }
    }

    private void updateObject(Object object) {

        if (object instanceof TickUpdatable) {
            ((TickUpdatable) object).get(TickUpdatable.TICK_UPDATE).invoke();
        }

        if (object instanceof FeatureHolder) {
            for (Object child : (FeatureHolder) object) {
                updateObject(child);
            }
        } else if (object instanceof ValueSupplier) {
            updateObject( ((ValueSupplier<?>) object).get());
        }
    }

}
