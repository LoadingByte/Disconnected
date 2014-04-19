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

import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This thread calls the tick update on several tick actions.
 * It's an independent utility.
 * 
 * @see Ticker
 */
public class TickThread extends Thread {

    private static final Logger LOGGER = LoggerFactory.getLogger(TickThread.class);

    private final Ticker        ticker;

    /**
     * Creates a new tick thread and sets the ticker to use the informations from.
     * 
     * @param ticker The ticker to use the informations for the actions and the delay from.
     */
    public TickThread(Ticker ticker) {

        super("tick");

        this.ticker = ticker;
    }

    /**
     * Returns the ticker to use the informations for the actions and the delay from.
     * 
     * @return The ticker to use the informations for the actions and the delay from.
     */
    public Ticker getTicker() {

        return ticker;
    }

    @Override
    public void run() {

        long delay = ticker.getDelay();
        long next = System.currentTimeMillis();

        while (!isInterrupted()) {
            for (TickAction action : new ArrayList<TickAction>(ticker.getActions())) {
                try {
                    action.update();
                } catch (RuntimeException e) {
                    LOGGER.error("An unexpected exception occurred while the tick action '{}' was updated", action.getClass().getName(), e);
                }
            }

            next += delay;
            long sleep = next - System.currentTimeMillis();
            if (sleep > 0) {
                try {
                    Thread.sleep(sleep);
                } catch (InterruptedException e) {
                    // Interruption -> Exit thread
                    break;
                }
            }
        }
    }

}
