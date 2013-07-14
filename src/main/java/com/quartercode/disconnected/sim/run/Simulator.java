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

import com.quartercode.disconnected.sim.Simulation;

/**
 * This class implements the root simulation update method for executing the simulation.
 * The tick update will actually get called by a running tick thread.
 * 
 * @see Simulation
 * @see TickThread
 */
public class Simulator {

    private Simulation simulation;
    private TickThread tickThread;

    /**
     * Creates a new simulator and sets the simulation which should be simulated.
     * 
     * @param simulation The simulation which should be simulated.
     */
    public Simulator(Simulation simulation) {

        this.simulation = simulation;
    }

    /**
     * Returns the simulation which is simulated by this simulator.
     * 
     * @return The simulation which is simulated by this simulator.
     */
    public Simulation getSimulation() {

        return simulation;
    }

    /**
     * Returns if the tick thread is currently running.
     * 
     * @return If the tick thread is currently running.
     */
    public boolean isRunning() {

        return tickThread != null && tickThread.isAlive();
    }

    /**
     * Changes the status of the tick thread.
     * This can start and stop the tick update.
     * 
     * @param running If the tick thread should run.
     */
    public void setRunning(boolean running) {

        if (running && !isRunning()) {
            tickThread = new TickThread(this);
            tickThread.start();
        } else if (!running && isRunning()) {
            tickThread.interrupt();
            tickThread = null;
        }
    }

    /**
     * Returns the current tick thread which executes the actual tick update.
     * If the update is not running, this returns null.
     * 
     * @return The current tick thread which executes the actual tick update.
     */
    public TickThread getTickThread() {

        return tickThread;
    }

    /**
     * This method executes the basic tick update which is called in the same intervals.
     * This calls some subroutines which actually simulate a tick.
     */
    public void update() {

    }

}
