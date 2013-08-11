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

package com.quartercode.disconnected;

import com.quartercode.disconnected.profile.ProfileManager;
import com.quartercode.disconnected.resstore.ResoureStore;
import com.quartercode.disconnected.sim.run.Simulator;

/**
 * A static storage class which stores important game values.
 */
public class Disconnected {

    private static ResoureStore   resoureStore;
    private static ProfileManager profileManager;

    private static Simulator      simulator;

    /**
     * Returns the title of the product.
     * 
     * @return The title of the product.
     */
    public static String getTitle() {

        return Disconnected.class.getPackage().getImplementationTitle();
    }

    /**
     * Returns the current implemented version of the product.
     * 
     * @return The current implemented version of the product.
     */
    public static String getVersion() {

        return Disconnected.class.getPackage().getImplementationVersion();
    }

    /**
     * Returns the vendor who created the product.
     * 
     * @return The vendor who created the product.
     */
    public static String getVendor() {

        return Disconnected.class.getPackage().getImplementationVendor();
    }

    /**
     * Returns the active resource store for storing loaded resource store resources.
     * 
     * @return The active resource store.
     */
    public static ResoureStore getResoureStore() {

        return resoureStore;
    }

    /**
     * Sets the active resource store for storing loaded resource store resources to a new one.
     * 
     * @param resoureStore The new active resource store.
     */
    public static void setResoureStore(ResoureStore resoureStore) {

        Disconnected.resoureStore = resoureStore;
    }

    /**
     * Returns the active profile manager for storing loaded profiles (which are actually simulations).
     * 
     * @return The active profile manager.
     */
    public static ProfileManager getProfileManager() {

        return profileManager;
    }

    /**
     * Sets the active profile manager to a new profile manager.
     * 
     * @param profileManager The new active profile manager.
     */
    public static void setProfileManager(ProfileManager profileManager) {

        Disconnected.profileManager = profileManager;
    }

    /**
     * Returns the active simulator.
     * 
     * @return The active simulator.
     */
    public static Simulator getSimulator() {

        return simulator;
    }

    /**
     * Sets the active simulator to a new simulator.
     * 
     * @param simulator The new active simulator.
     */
    public static void setSimulator(Simulator simulator) {

        Disconnected.simulator = simulator;
    }

    private Disconnected() {

    }

}
