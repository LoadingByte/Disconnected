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

package com.quartercode.disconnected.world.comp;

import javax.xml.bind.annotation.XmlAttribute;
import org.apache.commons.lang.Validate;

/**
 * This class represents a simple version.
 * A version contains a major version, minor version and a patch level.
 */
public class Version {

    @XmlAttribute
    private int major;
    @XmlAttribute
    private int minor;
    @XmlAttribute
    private int patchLevel;

    /**
     * Creates a new version object using default values (0, 0 and 0).
     */
    public Version() {

    }

    /**
     * Creates a new version object and sets the values to the given ones.
     * 
     * @param major The major version which is changed after very large changes.
     * @param minor The minor version which is changed if there are new features.
     * @param patchLevel The patch level which is changed after fixes.
     */
    public Version(int major, int minor, int patchLevel) {

        this.major = major;
        this.minor = minor;
        this.patchLevel = patchLevel;
    }

    /**
     * Creates a new version object and sets the values by parsing a version string.
     * This is using the format MAJOR.MINOR.PATCHLEVEL (e.g. 1.2.5).
     * 
     * @param version The version string to parse the values from.
     */
    public Version(String version) {

        String[] versionParts = version.split("\\.");
        Validate.isTrue(versionParts.length == 3, "The version string must be splitted in 3 parts by dots (e.g. 1.2.5): ", version);

        major = Integer.parseInt(versionParts[0]);
        minor = Integer.parseInt(versionParts[1]);
        patchLevel = Integer.parseInt(versionParts[2]);
    }

    /**
     * Returns the major version which is changed after very large changes.
     * 
     * @return The major version.
     */
    public int getMajor() {

        return major;
    }

    /**
     * Returns the minor version which is changed if there are new features.
     * 
     * @return The minor version.
     */
    public int getMinor() {

        return minor;
    }

    /**
     * Returns the patch level which is changed after fixes.
     * 
     * @return The patch level.
     */
    public int getPatchLevel() {

        return patchLevel;
    }

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = prime * result + major;
        result = prime * result + minor;
        result = prime * result + patchLevel;
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
        Version other = (Version) obj;
        if (major != other.major) {
            return false;
        }
        if (minor != other.minor) {
            return false;
        }
        if (patchLevel != other.patchLevel) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {

        return major + "." + minor + "." + patchLevel;
    }

}
