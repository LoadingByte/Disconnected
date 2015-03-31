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

package com.quartercode.disconnected.client.registry;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import com.quartercode.disconnected.shared.util.registry.extra.NamedValue;

/**
 * A data object that represents a client program by storing its name, category, and class.
 * 
 * @see ClientRegistries#CLIENT_PROGRAMS
 */
public class ClientProgram implements NamedValue {

    private final String   name;
    private final String   category;
    private final Class<?> type;

    /**
     * Creates a new client program data object.
     * 
     * @param name The internal name of the client program.
     * @param category The internal category the client program is associated to.
     * @param type The client program executor class which runs the client program.
     */
    public ClientProgram(String name, String category, Class<?> type) {

        Validate.notNull(name, "Client program name cannot be null");

        this.name = name;
        this.category = category;
        this.type = type;
    }

    /**
     * Returns the internal name of the client program.
     * It is used to reference the program without the {@link #getType() client program executor class}.
     * Note that it should not be directly displayed in the GUI because it's an internal key.
     * 
     * @return The client program's name.
     */
    @Override
    public String getName() {

        return name;
    }

    /**
     * Returns the internal category the client program is associated to.
     * Note that it should not be directly displayed in the GUI because it's an internal key.
     * 
     * @return The client program's category.
     */
    public String getCategory() {

        return category;
    }

    /**
     * Returns the class of the client program executor which runs the client program.
     * 
     * @return The client program executor class.
     */
    public Class<?> getType() {

        return type;
    }

    @Override
    public int hashCode() {

        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(Object obj) {

        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public String toString() {

        return ToStringBuilder.reflectionToString(this);
    }

}
