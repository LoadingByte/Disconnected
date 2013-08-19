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

import javax.xml.bind.annotation.XmlAttribute;
import org.apache.commons.lang.Validate;

/**
 * This class stores a location using x and y relative coordinates.
 */
public class Location {

    private float x;
    private float y;

    /**
     * Creates a new empty relative location.
     */
    public Location() {

    }

    /**
     * Creates a new relative location and sets the coordinates.
     * 
     * @param x The relative x coordinate.
     * @param y The relative y coordinate.
     */
    public Location(float x, float y) {

        setX(x);
        setY(y);
    }

    /**
     * Returns the relative x coordinate.
     * 
     * @return The relative x coordinate.
     */
    @XmlAttribute
    public float getX() {

        return x;
    }

    /**
     * Sets the relative x coordinate.
     * 
     * @param x The new relative x coordinate.
     */
    public void setX(float x) {

        Validate.isTrue(x >= 0 && x <= 1, "X must be in range 0 <= x <= 1");
        this.x = Math.round(x * 100) / 100F;
    }

    /**
     * Returns the relative y coordinate.
     * 
     * @return The relative y coordinate.
     */
    @XmlAttribute
    public float getY() {

        return y;
    }

    /**
     * Sets the relative y coordinate.
     * 
     * @param y The new relative y coordinate.
     */
    public void setY(float y) {

        Validate.isTrue(y >= 0 && y <= 1, "Y must be in range 0 <= x <= 1");
        this.y = Math.round(y * 100) / 100F;
    }

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = prime * result + Float.floatToIntBits(x);
        result = prime * result + Float.floatToIntBits(y);
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
        Location other = (Location) obj;
        if (Float.floatToIntBits(x) != Float.floatToIntBits(other.x)) {
            return false;
        }
        if (Float.floatToIntBits(y) != Float.floatToIntBits(other.y)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {

        return getClass().getName() + " [x=" + x + ", y=" + y + "]";
    }

}
