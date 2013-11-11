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

package com.quartercode.disconnected.sim.comp.file;

import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import com.quartercode.disconnected.sim.comp.SizeUtil;

/**
 * The string content class represents a simple string object which can be stored in a file.
 * This wrapper is useful for editable text files etc.
 * 
 * @see FileContent
 */
@XmlType (name = "string")
public class StringContent implements FileContent {

    @XmlValue
    private String string;

    /**
     * Creates a new empty string content wrapper.
     * This is only recommended for direct field access (e.g. for serialization).
     */
    protected StringContent() {

    }

    /**
     * Creates a new string content wrapper with the given inital string.
     * 
     * @param string The string the new wrapper holds after creation.
     */
    public StringContent(String string) {

        this.string = string;
    }

    @Override
    public long getSize() {

        return SizeUtil.getSize(string);
    }

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = prime * result + (string == null ? 0 : string.hashCode());
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
        StringContent other = (StringContent) obj;
        if (string == null) {
            if (other.string != null) {
                return false;
            }
        } else if (!string.equals(other.string)) {
            return false;
        }
        return true;
    }

    /**
     * Returns the string which is stored in the wrapper.
     * 
     * @return The stored string.
     */
    @Override
    public String toString() {

        return string;
    }

}
