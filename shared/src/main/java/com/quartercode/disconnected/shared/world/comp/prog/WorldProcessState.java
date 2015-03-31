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

package com.quartercode.disconnected.shared.world.comp.prog;

/**
 * The process state defines the management state of a running process.
 * It stores whether the process is running, interrupted etc.
 */
public enum WorldProcessState {

    /**
     * The process is running and the update executes every tick.
     * This is the default state of a process.
     */
    RUNNING (true),
    /**
     * The execution is suspended, tick updates will be ignored.
     */
    SUSPENDED (false),
    /**
     * The execution is interrupted friendly and should be stopped soon.
     * If a process notes this state, it should try to execute last activities and then stop the execution.
     */
    INTERRUPTED (true),
    /**
     * The execution is permanently stopped.
     * If a process is stopped, it won't be able to start again.
     */
    STOPPED (true);

    private final boolean tickState;

    private WorldProcessState(boolean tickState) {

        this.tickState = tickState;
    }

    /**
     * Returns true if the process state should allow execution of the process it is applied on.
     * 
     * @return Whether the state should allow the active process execution.
     */
    public boolean isTickState() {

        return tickState;
    }

}
