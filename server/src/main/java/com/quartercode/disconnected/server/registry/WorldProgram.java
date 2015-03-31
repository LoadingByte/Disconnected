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

package com.quartercode.disconnected.server.registry;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import com.quartercode.disconnected.shared.util.registry.extra.NamedValue;
import com.quartercode.disconnected.shared.world.comp.file.SeparatedPath;

/**
 * A data object that represents a world program by storing its program executor class, size, and common file location.
 * 
 * @see ServerRegistries#WORLD_PROGRAMS
 */
public class WorldProgram implements NamedValue {

    private final String        name;
    private final Class<?>      type;
    private final long          size;
    private final SeparatedPath commonLocation;

    /**
     * Creates a new world program data object.
     * 
     * @param name The key (name) of the program.
     * @param type The program executor class which runs the program.
     * @param size The size of the program (in bytes).
     * @param commonLocation The file path the program can be commonly found under.
     *        When a new computer is generated, the default program paths are retrieved using this common location attribute.
     *        Moreover, the file name of the common location is used to retrieve a program file through the {@code PATH} variable.
     */
    public WorldProgram(String name, Class<?> type, long size, SeparatedPath commonLocation) {

        Validate.notNull(name, "World program name cannot be null");

        this.name = name;
        this.type = type;
        this.size = size;
        this.commonLocation = commonLocation;
    }

    /**
     * Returns the key (name) of the world program.
     * It is used to reference the program without the {@link #getType() program executor class}.
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

    /**
     * Returns the file path the program can be commonly found under.
     * When a new computer is generated, the default program paths are retrieved using this common location attribute.
     * Moreover, the file name of the common location is used to retrieve a program file through the {@code PATH} variable.
     * 
     * @return The common program file path.
     */
    public SeparatedPath getCommonLocation() {

        return commonLocation;
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
