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

package com.quartercode.disconnected.server.sim.profile;

import java.util.Random;
import org.apache.commons.lang3.Validate;
import com.quartercode.disconnected.server.world.World;

/**
 * A data class which stores the persistent {@link World} and {@link Random} objects of a profile.
 * 
 * @see ProfileService
 */
public class ProfileData {

    private final World  world;
    private final Random random;

    /**
     * Creates a new profile data object.
     * 
     * @param world The {@link World} of the profile.
     * @param random The {@link Random} object that is used by the given world.
     */
    public ProfileData(World world, Random random) {

        Validate.notNull(world, "Cannot create profile data with null world");
        Validate.notNull(random, "Cannot create profile data with null random object");

        this.world = world;
        this.random = random;
    }

    /**
     * Returns the {@link World} of the profile.
     * 
     * @return The profile world.
     */
    public World getWorld() {

        return world;
    }

    /**
     * Returns the {@link Random} object that is used by the stored {@link World} (see {@link #getWorld()}).
     * 
     * @return The profile random object.
     */
    public Random getRandom() {

        return random;
    }

    // No hashCode(), equals(), toString() because the calculation cost would be massive

}
