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

package com.quartercode.disconnected.sim.run.attack;

import com.quartercode.disconnected.sim.comp.Computer;
import com.quartercode.disconnected.sim.comp.OperatingSystem.RightLevel;

/**
 * This class represents an open session to another computer.
 * This also stores the right level for the session. A list of all right levels is provided by the attacked operating system.
 * 
 * @see Computer
 * @see RightLevel
 */
public class Session {

    private Computer   computer;
    private RightLevel rightLevel;

    /**
     * Creates a new open sesson and sets the attacked computer and the inital right level.
     * 
     * @param computer The computer this session is connected to.
     * @param rightLevel The inital right level the session user has.
     */
    public Session(Computer computer, RightLevel rightLevel) {

        this.computer = computer;
        this.rightLevel = rightLevel;
    }

    /**
     * Returns the computer this session is connected to.
     * 
     * @return The computer this session is connected to.
     */
    public Computer getComputer() {

        return computer;
    }

    /**
     * Returns the inital right level the session user has.
     * 
     * @return The inital right level the session user has.
     */
    public RightLevel getRightLevel() {

        return rightLevel;
    }

}
