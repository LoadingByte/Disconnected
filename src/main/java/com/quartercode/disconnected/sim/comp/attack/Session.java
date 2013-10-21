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

package com.quartercode.disconnected.sim.comp.attack;

import com.quartercode.disconnected.sim.comp.Computer;
import com.quartercode.disconnected.sim.comp.os.OperatingSystem.RightLevel;

/**
 * This class represents an open session to another computer.
 * This also stores the right level for the session. A list of all right levels is provided by the attacked operating system.
 * 
 * @see Computer
 * @see RightLevel
 */
public class Session {

    private final Computer   computer;
    private final RightLevel rightLevel;

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

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = prime * result + (computer == null ? 0 : computer.hashCode());
        result = prime * result + (rightLevel == null ? 0 : rightLevel.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Session other = (Session) obj;
        if (computer == null) {
            if (other.computer != null) {
                return false;
            }
        } else if (!computer.equals(other.computer)) {
            return false;
        }
        if (rightLevel != other.rightLevel) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {

        return getClass().getName() + " [computer=" + computer + ", rightLevel=" + rightLevel + "]";
    }

}
