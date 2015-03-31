/*
 * This file is part of Disconnected.
 * Copyright (c) 2013 QuarterCode <http://quartercode.com/>
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

package com.quartercode.disconnected.client.graphics.desktop.prog;

/**
 * A client program state context object holds the state of a {@link ClientProgramExecutor} and allows to add listeners.
 * One such state context object is responsible for holding the state of one executor instance.
 * It is typically injected into the executor in order to allow it to view and manage its state.
 * 
 * @see ClientProgramExecutor
 */
public interface ClientProgramStateContext {

    /**
     * Sets the client program state to "stopped" and calls all {@link #addStoppingListener(Runnable) stopping listeners}.
     * This method might also do some other stuff depending on the implementation.
     */
    public void stop();

    /**
     * Adds the given {@link Runnable listener} which is guaranteed to be called when the program state changes to {@link #stop() stopped}.
     * 
     * @param listener The listener which should be added.
     */
    public void addStoppingListener(Runnable listener);

}
