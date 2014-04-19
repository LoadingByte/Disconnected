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

import com.quartercode.disconnected.util.RandomPool;
import com.quartercode.disconnected.world.World;

/**
 * This class represents a simulation which stores information about the simulated {@link World} and a generic {@link RandomPool}.
 * To actually run the simulation, you need to run a {@link TickSimulator} inside of a {@link Ticker}.
 * 
 * @see World
 * @see RandomPool
 */
public class Simulation {

    /**
     * The default size a random pool which is used for a simulation should have.
     */
    public static final int  RANDOM_POOL_SIZE = 10;

    private World            world;
    private final RandomPool random;

    /**
     * Creates a new empty simulation using the given {@link RandomPool}.
     * 
     * @param random The random pool to use for generating anything inside the simulation.
     */
    public Simulation(RandomPool random) {

        this.random = random;
    }

    /**
     * Returns the {@link World} which is used for the simulation.
     * 
     * @return The simulated {@link World}.
     */
    public World getWorld() {

        return world;
    }

    /**
     * Changes the {@link World} which is used for the simulation.
     * 
     * @param world The new simulated world.
     */
    public void setWorld(World world) {

        this.world = world;
    }

    /**
     * Returns the {@link RandomPool} to use for generating anything inside the simulation.
     * 
     * @return The {@link RandomPool} for the simulation.
     */
    public RandomPool getRandom() {

        return random;
    }

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = prime * result + (random == null ? 0 : random.hashCode());
        result = prime * result + (world == null ? 0 : world.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Simulation other = (Simulation) obj;
        if (random == null) {
            if (other.random != null) {
                return false;
            }
        } else if (!random.equals(other.random)) {
            return false;
        }
        if (world == null) {
            if (other.world != null) {
                return false;
            }
        } else if (!world.equals(other.world)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {

        return getClass().getName() + " [random=" + random + "]";
    }

}
