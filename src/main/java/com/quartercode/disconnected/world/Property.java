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

package com.quartercode.disconnected.world;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import com.quartercode.disconnected.util.InfoString;

/**
 * A property is used to store an object variable of a class and handle modifications.
 * For accessing properties, you can used {@link PropertyDefinition} constants.
 * 
 * @see WorldObject
 */
public abstract class Property implements InfoString {

    @XmlAttribute
    private String      name;
    private WorldObject parent;

    /**
     * Creates a new empty property.
     * This is only recommended for direct field access (e.g. for serialization).
     */
    protected Property() {

    }

    /**
     * Creates a new property with the given name and parent object.
     * 
     * @param name The name the new property will have.
     * @param parent The parent object which has the new property.
     */
    protected Property(String name, WorldObject parent) {

        this.name = name;
        this.parent = parent;
    }

    /**
     * Returns the name the property has.
     * The name is used for storing and accessing the property in a {@link WorldObject}.
     * 
     * @return The name the property has.
     */
    public String getName() {

        return name;
    }

    /**
     * Returns the parent object which actually has this property.
     * 
     * @return The object which has this property.
     */
    public WorldObject getParent() {

        return parent;
    }

    /**
     * Resolves the world object which has this property during umarshalling.
     * 
     * @param unmarshaller The unmarshaller which unmarshals this property.
     * @param parent The object which was unmarshalled as the parent one from the xml structure.
     */
    protected void beforeUnmarshal(Unmarshaller unmarshaller, Object parent) {

        if (parent instanceof WorldObject) {
            this.parent = (WorldObject) parent;
        }
    }

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = prime * result + (name == null ? 0 : name.hashCode());
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
        Property other = (Property) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }

    @Override
    public String toInfoString() {

        return getClass().getSimpleName() + " " + name;
    }

    @Override
    public String toString() {

        return getClass().getName() + " [" + toInfoString() + "]";
    }

}
