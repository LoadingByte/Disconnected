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
import org.apache.commons.lang.Validate;

/**
 * This thread calls the tick update on a given simulator.
 * 
 * @see Simulator
 */
public class TickThread extends Thread {

    private static final Logger LOGGER = Logger.getLogger(TickThread.class.getName());

    private final Simulator     simulator;

    private int                 delay  = 1000;

    /**
     * Creates a new tick thread and sets the simulator.
     * 
     * @param simulator The simulator which will get called.
     */
    public TickThread(Simulator simulator) {

        super("simulation-tick");

        this.simulator = simulator;
    }

    /**
     * Returns the simulator which will get called.
     * 
     * @return The simulator which will get called.
     */
    public Simulator getSimulator() {

        return simulator;
    }

    /**
     * Returns the delay the thread should wait until the next tick.
     * 
     * @return The delay the thread should wait until the next tick.
     */
    public int getDelay() {

        return delay;
    }

    /**
     * Sets the delay the thread should wait until the next tick.
     * 
     * @param delay The delay the thread should wait until the next tick.
     */
    public void setDelay(int delay) {

        Validate.isTrue(delay > 0, "Delay must be > 0");

        this.delay = delay;
    }

    @Override
    public void run() {

        while (!isInterrupted()) {
            try {
                simulator.update();
            }
            catch (Throwable t) {
                LOGGER.log(Level.SEVERE, "An exception occurred while executing simulator update", t);
            }

            try {
                Thread.sleep(delay);
            }
            catch (InterruptedException e) {
                LOGGER.log(Level.SEVERE, "Tick thread has been interrupted", e);
            }
        }
    }

}
