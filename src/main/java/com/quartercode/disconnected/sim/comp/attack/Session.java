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

import com.quartercode.disconnected.sim.comp.os.OperatingSystem;
import com.quartercode.disconnected.sim.comp.os.User;
import com.quartercode.disconnected.util.InfoString;

/**
 * This class represents an open session to another operating system.
 * This also stores the user the session is running on.
 * 
 * @see OperatingSystem
 * @see User
 */
public class Session implements InfoString {

    private final OperatingSystem target;
    private final User            user;

    /**
     * Creates a new open sesson and sets the attacked operating system and the user the session uses.
     * 
     * @param target The operating system this session is connected to.
     * @param user The user the session uses for executing commands.
     */
    public Session(OperatingSystem target, User user) {

        this.target = target;
        this.user = user;
    }

    /**
     * Returns the operating system this session is connected to.
     * 
     * @return The operating system this session is connected to.
     */
    public OperatingSystem getTarget() {

        return target;
    }

    /**
     * Returns the user the session uses for executing commands.
     * 
     * @return The user the session uses for executing commands.
     */
    public User getUser() {

        return user;
    }

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = prime * result + (target == null ? 0 : target.hashCode());
        result = prime * result + (user == null ? 0 : user.hashCode());
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
        if (target == null) {
            if (other.target != null) {
                return false;
            }
        } else if (!target.equals(other.target)) {
            return false;
        }
        if (user == null) {
            if (other.user != null) {
                return false;
            }
        } else if (!user.equals(other.user)) {
            return false;
        }
        return true;
    }

    @Override
    public String toInfoString() {

        return "computer " + target.getHost().getId() + " as " + user.getName();
    }

    @Override
    public String toString() {

        return getClass().getName() + " [" + toInfoString() + "]";
    }

}
