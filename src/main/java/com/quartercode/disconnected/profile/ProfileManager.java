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

package com.quartercode.disconnected.profile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.quartercode.disconnected.sim.Simulation;

/**
 * This class manages different stored simulations called "profiles".
 * For loading or saving profiles, you need to use the profile serializer.
 * 
 * @see ProfileSerializer
 */
public class ProfileManager {

    private final List<Simulation> profiles = new ArrayList<Simulation>();

    /**
     * Creates a new profile manager.
     */
    public ProfileManager() {

    }

    /**
     * Returns all loaded profiles (which are actually simulations).
     * 
     * @return All loaded profiles.
     */
    public List<Simulation> getProfiles() {

        return Collections.unmodifiableList(profiles);
    }

    /**
     * Adds a new profile (which is actually a simulation).
     * 
     * @param simulation The new profile to add.
     */
    public void addProfile(Simulation simulation) {

        profiles.add(simulation);
    }

    /**
     * Removes a loaded profile (which is actually a simulation).
     * 
     * @param simulation The loaded profile to remove.
     */
    public void removeProfile(Simulation simulation) {

        profiles.remove(simulation);
    }

}
