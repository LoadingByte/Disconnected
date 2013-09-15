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

import com.quartercode.disconnected.graphics.GraphicsManager;
import com.quartercode.disconnected.profile.ProfileManager;
import com.quartercode.disconnected.sim.Simulation;
import com.quartercode.disconnected.sim.run.TickSimulator;
import com.quartercode.disconnected.sim.run.Ticker;

/**
 * A static storage class which stores important game values.
 */
public class Disconnected {

    private static Registry        registry;
    private static ProfileManager  profileManager;
    private static Ticker          ticker;
    private static GraphicsManager graphicsManager;

    private static Simulation      simulation;

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
     * Returns the active registry for holding all kinds of classes for a later use.
     * 
     * @return The active registry for holding all kinds of classes for a later use.
     */
    public static Registry getRegistry() {

        return registry;
    }

    /**
     * Sets the active registry for holding all kinds of classes to a new one.
     * 
     * @param registry The new active registry for holding all kinds of classes.
     */
    public static void setRegistry(Registry registry) {

        Disconnected.registry = registry;
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
     * Returns the current active ticker which controls the tick thread.
     * 
     * @return The current active ticker which controls the tick thread.
     */
    public static Ticker getTicker() {

        return ticker;
    }

    /**
     * Sets the current active ticker which controls the tick thread to a new one.
     * 
     * @param ticker The new ticker which will control the tick thread.
     */
    public static void setTicker(Ticker ticker) {

        Disconnected.ticker = ticker;
    }

    /**
     * Returns the active graphics manager.
     * 
     * @return The active graphics manager.
     */
    public static GraphicsManager getGraphicsManager() {

        return graphicsManager;
    }

    /**
     * Sets the active graphics manager to a new graphics manager.
     * 
     * @param graphicsManager The new active graphics manager.
     */
    public static void setGraphicsManager(GraphicsManager graphicsManager) {

        Disconnected.graphicsManager = graphicsManager;
    }

    /**
     * Returns the current active simulation.
     * 
     * @return The current active simulation.
     */
    public static Simulation getSimulation() {

        return simulation;
    }

    /**
     * Sets the current active simulation to a new one.
     * The action will take place in the next tick.
     * 
     * @param simulation The new simulation.
     */
    public static void setSimulation(Simulation simulation) {

        Disconnected.simulation = simulation;

        if (ticker != null) {
            ticker.getAction(TickSimulator.class).setSimulation(simulation);
        }
    }

    private Disconnected() {

    }

}
