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

package com.quartercode.disconnected.sim.run;

import java.util.logging.Level;
import java.util.logging.Logger;
import com.quartercode.classmod.base.FeatureHolder;
import com.quartercode.classmod.extra.CollectionProperty;
import com.quartercode.classmod.extra.ExecutorInvocationException;
import com.quartercode.classmod.extra.Property;
import com.quartercode.disconnected.sim.Simulation;

/**
 * This class implements the root tick update mechanisms for the entire simulation.
 */
public class TickSimulator implements TickAction {

    private static final Logger LOGGER = Logger.getLogger(TickSimulator.class.getName());

    private Simulation          simulation;

    /**
     * Creates a new empty tick simulator.
     */
    public TickSimulator() {

    }

    /**
     * Creates a new tick simulator and sets the simulation to simulate.
     * 
     * @param simulation The simulation to simulate.
     */
    public TickSimulator(Simulation simulation) {

        this.simulation = simulation;
    }

    /**
     * Returns the simulation to simulate.
     * 
     * @return The simulation to simulate.
     */
    public Simulation getSimulation() {

        return simulation;
    }

    /**
     * Sets the simulation to simulate to a new one.
     * The action will take place in the next tick.
     * 
     * @param simulation The new simulation to simulate.
     */
    public void setSimulation(Simulation simulation) {

        this.simulation = simulation;
    }

    /**
     * Executes the basic (root) tick update which is called in the same intervals.
     * This calls some subroutines which actually simulate a tick.
     */
    @Override
    public void update() {

        if (simulation != null) {
            try {
                // Execute world object ticks
                updateObject(simulation.getWorld());
            } catch (ExecutorInvocationException e) {
                LOGGER.log(Level.SEVERE, "Unexcpected function execution exception during world tick update", e);
            }
        }
    }

    private void updateObject(Object object) throws ExecutorInvocationException {

        if (object instanceof TickUpdatable) {
            ((TickUpdatable) object).get(TickUpdatable.TICK_UPDATE).invoke();
        }

        if (object instanceof Iterable) {
            for (Object child : (Iterable<?>) object) {
                updateObject(child);
            }
        } else if (object instanceof Property) {
            updateObject( ((Property<?>) object).get());
        } else if (object instanceof CollectionProperty) {
            updateObject( ((CollectionProperty<?, ?>) object).get());
        }
    }

}
