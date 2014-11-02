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

package com.quartercode.disconnected.server.registry;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import com.quartercode.disconnected.shared.registry.extra.NamedValue;
import com.quartercode.disconnected.shared.registrydef.SharedRegistries;

/**
 * A data object that represents a world program.
 * Note that it does not contain the common location of a program since it is stored in {@link SharedRegistries#WORLD_PROGRAM_COMLOCS}.
 * See {@link ServerRegistries#WORLD_PROGRAMS} for more details.
 */
public class WorldProgram implements NamedValue {

    private final String   name;
    private final Class<?> type;
    private final long     size;

    /**
     * Creates a new world program data object.
     * 
     * @param name The key (name) of the program.
     * @param type The program executor class which runs the program.
     * @param size The size of the program (in bytes).
     */
    public WorldProgram(String name, Class<?> type, long size) {

        this.name = name;
        this.type = type;
        this.size = size;
    }

    /**
     * Returns the key (name) of the world program.
     * It is used to reference the program without the program executor class.
     * 
     * @return The program key.
     */
    @Override
    public String getName() {

        return name;
    }

    /**
     * Returns the class of the program executor which runs the world program.
     * 
     * @return The program executor class.
     */
    public Class<?> getType() {

        return type;
    }

    /**
     * Returns the size of the world program (in bytes).
     * 
     * @return The program size.
     */
    public long getSize() {

        return size;
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
