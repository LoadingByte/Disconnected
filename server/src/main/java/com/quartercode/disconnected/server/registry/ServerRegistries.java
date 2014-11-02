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

import javax.xml.bind.JAXBContext;
import org.apache.commons.lang3.reflect.TypeLiteral;
import com.quartercode.classmod.base.FeatureDefinition;
import com.quartercode.classmod.base.FeatureHolder;
import com.quartercode.classmod.util.TreeInitializer;
import com.quartercode.disconnected.shared.registry.Registry;
import com.quartercode.disconnected.shared.registry.RegistryDefinition;
import com.quartercode.disconnected.shared.registry.extra.MapRegistry;
import com.quartercode.disconnected.shared.registry.extra.SetRegistry;
import com.quartercode.disconnected.shared.registrydef.SharedRegistries;

/**
 * A class that stores the default {@link RegistryDefinition}s which define the default {@link Registry}s used by the server.
 * 
 * @see RegistryDefinition
 * @see Registry
 */
public class ServerRegistries {

    /**
     * The jaxb context path entries for world serialization.
     * They define the packages that contain jaxb.index files and are used for creating new {@link JAXBContext}s.
     */
    public static final RegistryDefinition<SetRegistry<String>>                                               WORLD_CONTEXT_PATH;

    /**
     * The mappings put into the {@link TreeInitializer} which is used to initialize some features when deserializing worlds.
     * When a world is deserialized, all the defined features are requested from each feature holder that is an instance of the assigned type.
     */
    public static final RegistryDefinition<MapRegistry<Class<? extends FeatureHolder>, FeatureDefinition<?>>> WORLD_INITIALIZER_MAPPINGS;

    /**
     * The {@link SchedulerGroup}s which define at which point inside a tick a certain group should be executed.
     * Each scheduler task opts-in for such a group and will be executed when his group is called.
     * For example, all tasks with a group which has the priority 2 are executed before all tasks of a priority 1 group.<br>
     * <br>
     * Note that changes to this registry do not affect anything if the tick service ran once.
     */
    public static final RegistryDefinition<SetRegistry<SchedulerGroup>>                                       SCHEDULER_GROUPS;

    /**
     * A map that maps string representations to file types (class objects).
     * For example, the string {@code "directory"} is mapped to the directory class.
     */
    public static final RegistryDefinition<MapRegistry<String, Class<?>>>                                     FILE_TYPES;

    /**
     * The {@link WorldProgram}s that maps world program names (e.g. {@code fileManager}) to program executor classes and program sizes.
     * Note that the common locations of programs are stored in a shared registry {@link SharedRegistries#WORLD_PROGRAM_COMLOCS}.
     */
    public static final RegistryDefinition<SetRegistry<WorldProgram>>                                         WORLD_PROGRAMS;

    static {

        WORLD_CONTEXT_PATH = new RegistryDefinition<>("worldContextPath", new TypeLiteral<SetRegistry<String>>() {});
        WORLD_INITIALIZER_MAPPINGS = new RegistryDefinition<>("worldInitializerMappings", new TypeLiteral<MapRegistry<Class<? extends FeatureHolder>, FeatureDefinition<?>>>() {});
        SCHEDULER_GROUPS = new RegistryDefinition<>("schedulerGroups", new TypeLiteral<SetRegistry<SchedulerGroup>>() {});
        FILE_TYPES = new RegistryDefinition<>("fileTypes", new TypeLiteral<MapRegistry<String, Class<?>>>() {});
        WORLD_PROGRAMS = new RegistryDefinition<>("worldPrograms", new TypeLiteral<SetRegistry<WorldProgram>>() {});

    }

    private ServerRegistries() {

    }

}
