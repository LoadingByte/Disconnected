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

package com.quartercode.disconnected.server.world.comp.program;

/**
 * A process state listener is notified after the {@link ProcessState} of a {@link Process} has changed.
 * Such listeners can be activated by adding them to {@link Process#STATE_LISTENERS}.
 */
public abstract class ProcessStateListener {

    /**
     * Called after the {@link ProcessState} of the given {@link Process} has changed.
     * 
     * @param process The process whose state has changed.
     * @param oldState The old state of the process.
     * @param newState The new state of the process.
     */
    public abstract void changedState(Process<?> process, ProcessState oldState, ProcessState newState);

}
