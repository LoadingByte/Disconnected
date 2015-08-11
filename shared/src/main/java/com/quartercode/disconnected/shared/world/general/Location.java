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

package com.quartercode.disconnected.shared.world.general;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import com.quartercode.disconnected.shared.util.XmlPersistent;
import com.quartercode.disconnected.shared.world.general.Location.LocationAdapter;

/**
 * This class stores a geographical location using relative x and y coordinates (from 0 to 1).
 */
@XmlPersistent
@XmlJavaTypeAdapter (LocationAdapter.class)
public class Location implements Serializable {

    private static final long serialVersionUID = 4734091462668039038L;

    @XmlAttribute
    private final float       x;
    @XmlAttribute
    private final float       y;

    /**
     * Creates a new empty location whose coordinates are both set to 0.
     */
    public Location() {

        x = 0;
        y = 0;
    }

    /**
     * Creates a new location with the given relative coordinates.
     * Both coordinates must be located between 0 and 1 (inclusive).
     *
     * @param x The relative x coordinate of the new location.
     * @param y The relative y coordinate of the new location.
     */
    public Location(float x, float y) {

        this.x = round(x);
        this.y = round(y);

        Validate.isTrue(x >= 0 && x <= 1, "X coordinate (%d) must be >= 0 and <= 1", x);
        Validate.isTrue(y >= 0 && y <= 1, "Y coordinate (%d) must be >= 0 and <= 1", y);
    }

    /**
     * Creates a new location using the two coordinates that are stored in the given location string.
     * The string must be using the format {@code x/y} (e.g. {@code 0.32/0.57}).
     * Both coordinates must be located between 0 and 1 (inclusive).
     *
     * @param string The location string to parse.
     */
    public Location(String string) {

        String[] stringParts = StringUtils.split(string, '/');
        Validate.isTrue(stringParts.length == 2, "The location string (%s) must be provided in the format x/y", string);

        x = round(Float.parseFloat(stringParts[0]));
        y = round(Float.parseFloat(stringParts[1]));

        Validate.isTrue(x >= 0 && x <= 1, "X coordinate (%d) must be >= 0 and <= 1", x);
        Validate.isTrue(y >= 0 && y <= 1, "Y coordinate (%d) must be >= 0 and <= 1", y);
    }

    private float round(float number) {

        return Math.round(number * 100) / 100F;
    }

    /**
     * Returns the relative x coordinate of the location.
     * Its value is located between 0 and 1 (inclusive).
     *
     * @return The x coordinate between 0 and 1 (inclusive).
     */
    public float getX() {

        return x;
    }

    /**
     * Returns a new location which has the given x coordinate, as well as the y coordinate of this location.
     * The value of the new coordinate must be located between 0 and 1 (inclusive).
     *
     * @param x The new relative x coordinate for the new location.
     * @return The new location with the given x coordinate.
     */
    public Location withX(float x) {

        Validate.isTrue(x >= 0 && x <= 1, "X coordinate (%d) must be >= 0 and <= 1", x);

        return new Location(x, y);
    }

    /**
     * Returns the relative y coordinate of the location.
     * Its value is located between 0 and 1 (inclusive).
     *
     * @return The y coordinate between 0 and 1 (inclusive).
     */
    public float getY() {

        Validate.isTrue(y >= 0 && y <= 1, "Y coordinate (%d) must be >= 0 and <= 1", y);

        return y;
    }

    /**
     * Returns a new location which has the given y coordinate, as well as the x coordinate of this location.
     * The value of the new coordinate must be located between 0 and 1 (inclusive).
     *
     * @param y The new relative y coordinate for the new location.
     * @return The new location with the given y coordinate.
     */
    public Location withY(float y) {

        return new Location(x, y);
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
        } else if (obj == null || ! (obj instanceof Location)) {
            return false;
        } else {
            Location other = (Location) obj;
            return x == other.x && y == other.y;
        }
    }

    /**
     * Returns the stored location as a string.
     * The returned string is using the format {@code x/y} (e.g. {@code 0.32/0.57}).
     *
     * @return A string representation of the location.
     */
    @Override
    public String toString() {

        return new StringBuilder().append(x).append("/").append(y).toString();
    }

    /**
     * An {@link XmlAdapter} that binds {@link Location} objects using their {@link Location#toString() string representation}.
     * If a JAXB property references a location object and doesn't specify a custom XML adapter, this adapter is used by default.
     *
     * @see Location
     */
    public static class LocationAdapter extends XmlAdapter<String, Location> {

        @Override
        public String marshal(Location v) {

            return v.toString();
        }

        @Override
        public Location unmarshal(String v) {

            return new Location(v);
        }

    }

}
