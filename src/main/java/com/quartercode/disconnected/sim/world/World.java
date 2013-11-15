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

package com.quartercode.disconnected.sim.world;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import com.quartercode.disconnected.util.InfoString;

/**
 * A world is a space which contains {@link WorldObject}s.
 * There is one {@link RootObject} which contains first level {@link WorldObject}s.
 * 
 * @see RootObject
 */
@XmlRootElement (namespace = "http://quartercode.com/")
public class World implements InfoString {

    @XmlElement
    private final RootObject root;

    /**
     * Creates a new empty world with a new {@link RootObject}.
     */
    public World() {

        root = new RootObject(this);
    }

    /**
     * Returns the {@link RootObject} which houses first level {@link WorldObject}s.
     * 
     * @return The {@link RootObject} of the world.
     */
    public RootObject getRoot() {

        return root;
    }

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = prime * result + (root == null ? 0 : root.hashCode());
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
        World other = (World) obj;
        if (root == null) {
            if (other.root != null) {
                return false;
            }
        } else if (!root.equals(other.root)) {
            return false;
        }
        return true;
    }

    @Override
    public String toInfoString() {

        return "root: " + root.toInfoString();
    }

    @Override
    public String toString() {

        return getClass().getName() + " [" + toInfoString() + "]";
    }

}
