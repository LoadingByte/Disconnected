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

package com.quartercode.disconnected.server.sim;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the default implementation of the {@link TickService}.
 * 
 * @see TickService
 */
public class DefaultTickService implements TickService {

    private static final Logger    LOGGER              = LoggerFactory.getLogger(DefaultTickService.class);

    private TickThread             thread;
    private final List<TickAction> actions             = new ArrayList<>();
    private int                    delay               = DEFAULT_DELAY;

    // Performance: Object cache
    private List<TickAction>       unmodifiableActions = Collections.unmodifiableList(actions);

    @Override
    public List<TickAction> getActions() {

        return unmodifiableActions;
    }

    @Override
    public <T> T getAction(Class<T> type) {

        for (TickAction action : actions) {
            if (type.isInstance(action)) {
                return type.cast(action);
            }
        }

        return null;
    }

    @Override
    public void addAction(TickAction action) {

        for (TickAction testAction : actions) {
            if (testAction.getClass().equals(action.getClass())) {
                throw new IllegalStateException("There is already a tick action using the class " + testAction.getClass().getName());
            }
        }

        actions.add(action);
        unmodifiableActions = Collections.unmodifiableList(actions);
    }

    @Override
    public void removeAction(TickAction action) {

        actions.remove(action);
        unmodifiableActions = Collections.unmodifiableList(actions);
    }

    @Override
    public int getDelay() {

        return delay;
    }

    @Override
    public void setDelay(int delay) {

        Validate.isTrue(delay > 0, "Delay (%d) must be > 0", delay);
        this.delay = delay;
    }

    @Override
    public boolean isRunning() {

        return thread != null && thread.isAlive();
    }

    @Override
    public void setRunning(boolean running) {

        if (running && !isRunning()) {
            LOGGER.debug("Starting up tick thread");
            thread = new TickThread(this);
            thread.start();
        } else if (!running && isRunning()) {
            LOGGER.debug("Shutting down tick thread");
            thread.interrupt();
            thread = null;
        }
    }

}
