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

import javax.xml.bind.JAXBContext;
import org.apache.commons.lang3.reflect.TypeLiteral;
import com.quartercode.classmod.base.FeatureDefinition;
import com.quartercode.classmod.base.FeatureHolder;
import com.quartercode.classmod.util.TreeInitializer;
import com.quartercode.disconnected.shared.util.registry.Registry;
import com.quartercode.disconnected.shared.util.registry.RegistryDefinition;
import com.quartercode.disconnected.shared.util.registry.extra.MapRegistry;
import com.quartercode.disconnected.shared.util.registry.extra.SetRegistry;

/**
 * A class that stores the default {@link RegistryDefinition}s which define the default {@link Registry}s used by the server.
 * 
 * @see RegistryDefinition
 * @see Registry
 */
public class ServerRegistries {

    /**
     * The {@link PersistentClassScanDirective}s which define what should be scanned for persistent world classes.
     * The results of such scans are fed into new {@link JAXBContext}s for making the found classes visible to the contexts.
     */
    public static final RegistryDefinition<SetRegistry<PersistentClassScanDirective>>                         PERSISTENT_CLASS_SCAN_DIRECTIVES;

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
     * The {@link WorldProgram}s that maps world program names (e.g. {@code fileManager}) to program executor classes, sizes, and common locations.
     */
    public static final RegistryDefinition<SetRegistry<WorldProgram>>                                         WORLD_PROGRAMS;

    /**
     * The {@link VulnSource vulnerability source} definitions that are used to define the boundaries for generated vulnerabilities.
     * One source source should address one kind of "generic" vulnerability that can be concretized by a generated vulnerability.
     * Note that {@link VulnSourceRegistry#getValuesByUsage(String...)} can be used to retrieve the sources of a specific part (e.g. program).
     */
    public static final RegistryDefinition<VulnSourceRegistry>                                                VULN_SOURCES;

    static {

        PERSISTENT_CLASS_SCAN_DIRECTIVES = new RegistryDefinition<>("persistentClassScanDirectives", new TypeLiteral<SetRegistry<PersistentClassScanDirective>>() {});
        WORLD_INITIALIZER_MAPPINGS = new RegistryDefinition<>("worldInitializerMappings", new TypeLiteral<MapRegistry<Class<? extends FeatureHolder>, FeatureDefinition<?>>>() {});
        SCHEDULER_GROUPS = new RegistryDefinition<>("schedulerGroups", new TypeLiteral<SetRegistry<SchedulerGroup>>() {});
        FILE_TYPES = new RegistryDefinition<>("fileTypes", new TypeLiteral<MapRegistry<String, Class<?>>>() {});
        WORLD_PROGRAMS = new RegistryDefinition<>("worldPrograms", new TypeLiteral<SetRegistry<WorldProgram>>() {});
        VULN_SOURCES = new RegistryDefinition<>("vulnSources", new TypeLiteral<VulnSourceRegistry>() {});

    }

    private ServerRegistries() {

    }

}
