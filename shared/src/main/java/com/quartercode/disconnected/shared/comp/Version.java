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

package com.quartercode.disconnected.shared.comp;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlElement;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * This class represents a simple version.
 * A version contains a major version, minor version and a revision.
 * An example version string could be {@code 2.5.1} (using the format {@code major.minor.revision}).
 */
public class Version implements Serializable {

    @XmlElement
    private final int major;
    @XmlElement
    private final int minor;
    @XmlElement
    private final int revision;

    /**
     * Creates a new empty version object with the major, minor and revision components being set to 0.
     */
    public Version() {

        major = 0;
        minor = 0;
        revision = 0;
    }

    /**
     * Creates a new version object with the given three version components.
     * Note that all components must be {@code >= 0}.
     * 
     * @param major The major version component which is changed after very large changes.
     * @param minor The minor version component which is changed after the addition of a new big features.
     * @param revision The revision version component which is changed after fixes.
     */
    public Version(int major, int minor, int revision) {

        Validate.isTrue(major >= 0, "Major version number (%d) must be >= 0", major);
        Validate.isTrue(minor >= 0, "Minor version number (%d) must be >= 0", minor);
        Validate.isTrue(revision >= 0, "Revision version number (%d) must be >= 0", revision);

        this.major = major;
        this.minor = minor;
        this.revision = revision;
    }

    /**
     * Creates a new version object using the version that is stored in the given version string.
     * The string must be using the format {@code major.minor.revision} (e.g. {@code 2.5.1}).
     * Note that all parts must be {@code >= 0}.
     * 
     * @param string The version string to parse.
     */
    public Version(String string) {

        String[] stringParts = StringUtils.split(string, '.');
        Validate.isTrue(stringParts.length == 3, "The version string (%s) must be splitted in 3 parts by dots (e.g. 2.5.1)", string);

        major = Integer.parseInt(stringParts[0]);
        minor = Integer.parseInt(stringParts[1]);
        revision = Integer.parseInt(stringParts[2]);

        Validate.isTrue(major >= 0, "Major version number (%d) must be >= 0", major);
        Validate.isTrue(minor >= 0, "Minor version number (%d) must be >= 0", minor);
        Validate.isTrue(revision >= 0, "Revision version number (%d) must be >= 0", revision);
    }

    /**
     * Returns the major version component which is changed after very large changes.
     * It is the first version string component ({@code X.x.x}).
     * Note that the major version must always be {@code >= 0}.
     * 
     * @return The major version number.
     */
    public int getMajor() {

        return major;
    }

    /**
     * Creates a new version that is based of this version and has the given major version component.
     * That component is the one which is changed after very large changes.
     * Note that the new major version must be {@code >= 0}.
     * 
     * @param major The new major version number.
     * @return The new version object with the given major version number.
     */
    public Version withMajor(int major) {

        Validate.isTrue(major >= 0, "Major version number (%d) must be >= 0", major);

        return new Version(major, minor, revision);
    }

    /**
     * Returns the minor version component which is changed after the addition of a new big features.
     * It is the second version string component ({@code x.X.x}).
     * Note that the minor version must always be {@code >= 0}.
     * 
     * @return The minor version number.
     */
    public int getMinor() {

        return minor;
    }

    /**
     * Creates a new version that is based of this version and has the given minor version component.
     * That component is the one which is changed after the addition of a new big features.
     * Note that the new minor version must be {@code >= 0}.
     * 
     * @param minor The new minor version number.
     * @return The new version object with the given minor version number.
     */
    public Version withMinor(int minor) {

        Validate.isTrue(minor >= 0, "Minor version number (%d) must be >= 0", minor);

        return new Version(major, minor, revision);
    }

    /**
     * Returns the revision version component which is changed after fixes.
     * It is the third version string component ({@code x.x.X}).
     * Note that the revision version must always be {@code >= 0}.
     * 
     * @return The revision version number.
     */
    public int getRevision() {

        return revision;
    }

    /**
     * Creates a new version that is based of this version and has the given revision version component.
     * That component is the one which is changed after fixes.
     * Note that the new revision version must be {@code >= 0}.
     * 
     * @param revision The new revision version number.
     * @return The new version object with the given revision version number.
     */
    public Version withRevision(int revision) {

        Validate.isTrue(revision >= 0, "Revision version number (%d) must be >= 0", revision);

        return new Version(major, minor, revision);
    }

    @Override
    public int hashCode() {

        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(Object obj) {

        return EqualsBuilder.reflectionEquals(this, obj);
    }

    /**
     * Returns the stored version as a string.
     * The string is using the format {@code major.minor.revision} (e.g. {@code 2.5.1}).
     * 
     * @return A string representation of the version.
     */
    @Override
    public String toString() {

        return new StringBuilder(major).append(".").append(minor).append(".").append(revision).toString();
    }

}
