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

package com.quartercode.disconnected.server.sim.profile;

import com.quartercode.disconnected.server.util.RandomPool;
import com.quartercode.disconnected.server.world.World;

/**
 * A profile has a name and an associated {@link World}.
 * It also holds a {@link RandomPool}.
 * 
 * @see ProfileService
 */
public class Profile {

    /**
     * The default size that a {@link RandomPool}, which is used for a profile, should have.
     */
    public static final int DEFAULT_RANDOM_POOL_SIZE = 10;

    private String          name;

    private World           world;
    private RandomPool      random;

    /**
     * Creates a new profile with the given name.
     * Please note that the {@link World} and the {@link RandomPool} the profile stores must be set later on.
     * 
     * @param name The name for the new profile.
     */
    public Profile(String name) {

        this.name = name;
    }

    /**
     * Returns the name of the profile.
     * 
     * @return The name the profile has.
     */
    public String getName() {

        return name;
    }

    /**
     * Changes the name of the profile.
     * 
     * @param name The new name for the profile.
     */
    public void setName(String name) {

        this.name = name;
    }

    /**
     * Returns the {@link World} which is associated with the name of the profile.
     * 
     * @return The world the profile holds.
     */
    public World getWorld() {

        return world;
    }

    /**
     * Changes the {@link World} which is associated with the name of the profile.
     * This method should only be used by deserialization algorithms.<br>
     * This method also injects the set {@link RandomPool} into the new world.
     * 
     * @param world The new world for the profile.
     */
    public void setWorld(World world) {

        // Clear the random from the old world
        if (this.world != null) {
            this.world.injectRandom(null);
        }

        this.world = world;

        // Inject the random into the new world
        injectRandomIntoWorld();
    }

    /**
     * Returns the {@link RandomPool} that can be used by the stored {@link World}.
     * 
     * @return The random pool for the stored world.
     */
    public RandomPool getRandom() {

        return random;
    }

    /**
     * Changes the {@link RandomPool} that can be used by the stored {@link World}.
     * This method should only be used by deserialization algorithms.<br>
     * This method also injects the new random poll into the current world.
     * 
     * @param random The new random pool for the stored world.
     */
    public void setRandom(RandomPool random) {

        this.random = random;

        // Inject the new random into the current world
        injectRandomIntoWorld();
    }

    private void injectRandomIntoWorld() {

        if (world != null) {
            world.injectRandom(random);
        }
    }

}
