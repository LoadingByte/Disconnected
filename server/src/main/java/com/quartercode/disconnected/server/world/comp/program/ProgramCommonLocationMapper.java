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

package com.quartercode.disconnected.server.world.comp.program;

import java.util.HashMap;
import java.util.Map;
import com.quartercode.disconnected.shared.util.SeparatedPath;

/**
 * This utility class is used to map {@link ProgramExecutor} types (class objects) to their common locations.
 * Such common locations are file paths under which programs can be commonly found under.
 * Mappings can be added and removed dynamically.
 */
public class ProgramCommonLocationMapper {

    private static final Map<Class<? extends ProgramExecutor>, SeparatedPath> MAPPINGS = new HashMap<>();

    /**
     * Returns the {@link SeparatedPath} that represents the file path the given {@link ProgramExecutor} type can be commonly found under.
     * The class parameter is not generic in order to prevent unnecessary unchecked casts.
     * 
     * @param executorType The class object of the program executor type whose common location should be returned.
     * @return The common program file location of the given program executor class.
     */
    public static SeparatedPath getCommonLocation(Class<?> executorType) {

        return MAPPINGS.get(executorType);
    }

    /**
     * Adds the given mapping in order to make it available through the accessor methods.
     * 
     * @param executorType The {@link ProgramExecutor} class object that should be mapped to the given common location.
     * @param commonLocation The common location {@link SeparatedPath} that should be mapped to the given program executor class object.
     */
    public static void addMapping(Class<? extends ProgramExecutor> executorType, SeparatedPath commonLocation) {

        MAPPINGS.put(executorType, commonLocation);
    }

    /**
     * Removes the mapping that maps the given {@link ProgramExecutor} class object to some common location.
     * 
     * @param executorType The program executor class whose mapping should be removed.
     */
    public static void removeMapping(Class<? extends ProgramExecutor> executorType) {

        MAPPINGS.remove(executorType);
    }

    /**
     * Removes all added mappings.
     * Note that this method also removes the default mappings.
     */
    public static void clearMappings() {

        MAPPINGS.clear();
    }

    private ProgramCommonLocationMapper() {

    }

}
