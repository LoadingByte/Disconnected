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

package com.quartercode.disconnected.sim.profile;

import com.quartercode.disconnected.sim.Simulation;

/**
 * A profile has a name and an associated simulation.
 * 
 * @see ProfileManager
 */
public class Profile {

    private final String name;
    private Simulation   simulation;

    /**
     * Creates a new profile with the given name and simulation.
     * 
     * @param name The name for the new profile.
     * @param simulation The simulation the new profile will hold.
     */
    public Profile(String name, Simulation simulation) {

        this.name = name;
        this.simulation = simulation;
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
     * Returns the simulation which is associated with the name of the profile.
     * If the simulation object is null, you can use {@link ProfileManager#setActive(Profile)} to deserialize the rest.
     * 
     * @return Ther simulation the profile holds.
     */
    public Simulation getSimulation() {

        return simulation;
    }

    /**
     * Changes the simulation object which is used in the profile.
     * This method should only be used by deserialization algorithms.
     * 
     * @param simulation The new simulation for the profile.
     */
    protected void setSimulation(Simulation simulation) {

        this.simulation = simulation;
    }

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = prime * result + (name == null ? 0 : name.hashCode());
        result = prime * result + (simulation == null ? 0 : simulation.hashCode());
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
        Profile other = (Profile) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (simulation == null) {
            if (other.simulation != null) {
                return false;
            }
        } else if (!simulation.equals(other.simulation)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {

        return name;
    }

}
