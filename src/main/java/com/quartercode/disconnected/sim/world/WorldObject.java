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

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import com.quartercode.disconnected.util.InfoString;

/**
 * A world object is an object inside a world.
 * It can have different {@link Property} objects which are used for setting the state of the object.
 * Those {@link Property} objects are be defined by {@link PropertyDefinition} constants which should be located in the actual subclass.
 */
public class WorldObject implements InfoString {

    private WorldObject    parent;
    @XmlElement (name = "property")
    private List<Property> properties;

    /**
     * Creates a new empty world object.
     * This is only recommended for direct field access (e.g. for serialization).
     */
    protected WorldObject() {

    }

    /**
     * Creates a new world object which has the given parent one.
     * 
     * @param parent The parent world object which has a {@link Property} which houses the new object.
     */
    public WorldObject(WorldObject parent) {

        this.parent = parent;
        properties = new ArrayList<Property>();
    }

    /**
     * Returns The parent world object which has a {@link Property} which houses this object.
     * 
     * @return The parent object which houses this object.
     */
    public WorldObject getParent() {

        return parent;
    }

    /**
     * Resolves the {@link World} this world object is in.
     * 
     * @return The {@link World} this object is in.
     */
    public World getWorld() {

        return parent == null ? null : parent.getWorld();
    }

    /**
     * Returns the {@link Property} which is defined by the given {@link PropertyDefinition}.
     * If there's no such {@link Property} yet, the {@link PropertyDefinition} creates a new one.
     * 
     * @param definition The {@link PropertyDefinition} which defines how the {@link Property} looks.
     * @return The found or created {@link Property}.
     */
    @SuppressWarnings ("unchecked")
    public <P extends Property> P get(PropertyDefinition<P> definition) {

        for (Property property : properties) {
            if (property.getName().equals(definition.getName())) {
                return (P) property;
            }
        }

        P property = definition.createProperty(this);
        properties.add(property);
        return property;
    }

    /**
     * Resolves the parent object which has a property which holds this objects during umarshalling.
     * 
     * @param unmarshaller The unmarshaller which unmarshals this objects.
     * @param parent The object which was unmarshalled as the parent one from the xml structure.
     */
    protected void beforeUnmarshal(Unmarshaller unmarshaller, Object parent) {

        if (parent instanceof Property) {
            this.parent = ((Property) parent).getParent();
        }
    }

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = prime * result + (properties == null ? 0 : properties.hashCode());
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
        WorldObject other = (WorldObject) obj;
        if (properties == null) {
            if (other.properties != null) {
                return false;
            }
        } else if (!properties.equals(other.properties)) {
            return false;
        }
        return true;
    }

    @Override
    public String toInfoString() {

        String propertyString = "";
        for (Property property : properties) {
            propertyString += ", " + property.toInfoString();
        }
        propertyString = propertyString.substring(2);

        return "properties: " + propertyString;
    }

    @Override
    public String toString() {

        return getClass().getName() + " [" + toInfoString() + "]";
    }

}
