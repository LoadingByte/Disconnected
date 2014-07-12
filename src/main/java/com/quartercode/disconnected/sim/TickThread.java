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
 * This thread calls the tick update on several {@link TickAction}s.
 * It's an independent utility.
 * 
 * @see TickService
 */
public class TickThread extends Thread {

    private static final Logger LOGGER = LoggerFactory.getLogger(TickThread.class);

    private final TickService   service;

    /**
     * Creates a new tick thread that will use the given {@link TickService}.
     * 
     * @param service The tick service to retrieve the tick data from.
     */
    public TickThread(TickService service) {

        super("tick");

        this.service = service;
    }

    /**
     * Returns the {@link TickService} that is used to retrieve tick data from.
     * 
     * @return The assigned tick service.
     */
    public TickService getService() {

        return service;
    }

    @Override
    public void run() {

        long delay = service.getDelay();
        long next = System.currentTimeMillis();

        while (!isInterrupted()) {
            for (TickAction action : new ArrayList<>(service.getActions())) {
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
