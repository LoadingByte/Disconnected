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

package com.quartercode.disconnected.resstore;

import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import com.quartercode.disconnected.sim.comp.ComputerPart;

/**
 * This class represents a loaded resource store computer part which is used by the resource store loader to load computer parts.
 * 
 * @see ResoureStoreLoader
 */
@XmlRootElement (namespace = "http://quartercode.com")
@XmlType (propOrder = { "type", "name", "attributes" })
public class StoredComputerPart {

    private Class<? extends ComputerPart> type;
    private String                        name;
    private List<Attribute>               attributes;

    /**
     * Creates a new empty stored computer part.
     */
    public StoredComputerPart() {

    }

    /**
     * Returns the type of the stored computer part.
     * The type defines which simulation computer part should be used.
     * 
     * @return The type of the stored computer part.
     */
    public Class<? extends ComputerPart> getType() {

        return type;
    }

    /**
     * Sets the type of the stored computer part.
     * The type defines which simulation computer part should be used.
     * 
     * @param type The new type of the stored computer part.
     */
    public void setType(Class<? extends ComputerPart> type) {

        this.type = type;
    }

    /**
     * Returns the name of the stored computer part.
     * 
     * @return The name of the stored computer part.
     */
    public String getName() {

        return name;
    }

    /**
     * Sets the name of the stored computer part.
     * 
     * @param name The new name of the stored computer part.
     */
    public void setName(String name) {

        this.name = name;
    }

    /**
     * Returns all attributes of the stored computer part.
     * Attributes are equally to extra variables of a simulation computer part.
     * 
     * @return All attributes of the stored computer part.
     */
    @XmlElementWrapper (name = "attributes")
    @XmlElement (name = "attribute")
    public List<Attribute> getAttributes() {

        return attributes;
    }

    /**
     * Sets the attribute list of the stored computer part to a new one.
     * Attributes are equally to extra variables of a simulation computer part.
     * 
     * @param attributes The new attribute list of the stored computer part.
     */
    public void setAttributes(List<Attribute> attributes) {

        this.attributes = attributes;
    }

}
