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

package com.quartercode.disconnected.server.world.comp.prog;

import com.quartercode.disconnected.shared.world.comp.prog.WorldProcessState;

/**
 * A process state listener is notified <b>after</b> the {@link WorldProcessState} of a {@link Process} has changed.
 * Such listeners can be activated by adding the to a process using {@link Process#addStateListener(ProcessStateListener)}.
 */
public interface ProcessStateListener {

    /**
     * Called <b>after</b> the {@link WorldProcessState} of the given {@link Process} has been changed.
     * That means that a call to {@link Process#getState()} would already retrieve the new state.
     * Note that if the state change operation is recursive, the state of all child processes has already been changed as well.
     *
     * @param process The process whose state has changed.
     * @param oldState The state of the process before the state has been changed.
     * @param newState The state of the process after the state has been changed.
     */
    public void onStateChange(Process<?> process, WorldProcessState oldState, WorldProcessState newState);

}
