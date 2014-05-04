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

package com.quartercode.disconnected.graphics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Graphics state descriptors define the properties of {@link GraphicsState}s which can be created out of them with {@link #create()}.
 * That way there can be multiple fresh copies of the same {@link GraphicsState}.
 * 
 * @see GraphicsState
 */
public class GraphicsStateDescriptor {

    private static final Logger                                                   LOGGER  = LoggerFactory.getLogger(GraphicsStateDescriptor.class);

    private final String                                                          name;
    private final Map<Class<? extends GraphicsModule>, GraphicsModuleInformation> modules = new HashMap<>();

    /**
     * Creates a new graphics state descriptor that produces {@link GraphicsState} with the given name.
     * 
     * @param name The name of the {@link GraphicsState}s produces by the descriptor.
     */
    public GraphicsStateDescriptor(String name) {

        this.name = name;
    }

    /**
     * Returns the name new {@link GraphicsState}s will use.
     * Such names can be used for debugging purposes.
     * 
     * @return The name for new {@link GraphicsState}s.
     */
    public String getName() {

        return name;
    }

    /**
     * Returns a {@link Set} containing all {@link GraphicsModule}s that are added to the descriptor.
     * The {@link Set} only contains their {@link Class}es because {@link #create()} instantiates fresh copies every time it is called.
     * You can add new {@link GraphicsModule} using {@link #addModule(Class, String, int)} and remove them with {@link #removeModule(Class)}.
     * 
     * @return A {@link Set} containg the {@link Class}es of all registered {@link GraphicsModule}.
     */
    public Set<Class<? extends GraphicsModule>> getModules() {

        return Collections.unmodifiableSet(modules.keySet());
    }

    /**
     * Adds a new {@link GraphicsModule} to the descriptor.
     * The method only requires the module's {@link Class} because {@link #create()} instantiates a fresh copy every time it is called.
     * The name of the {@link GraphicsModule} and its priority (higher priorities are invoked first) are also set.
     * 
     * @param module The {@link Class} of the {@link GraphicsModule} to add to the descriptor.
     * @param name The name of the {@link GraphicsModule}. This name can be used later on for {@link GraphicsState#getModule(String)}.
     * @param priority The priority of the {@link GraphicsModule}. Modules with higher priorities are invoked first.
     */
    public void addModule(Class<? extends GraphicsModule> module, String name, int priority) {

        modules.put(module, new GraphicsModuleInformation(name, priority));
    }

    /**
     * Removess the given {@link GraphicsModule} from the descriptor.
     * The method only requires the module's {@link Class} because {@link #create()} instantiates a fresh copy every time it is called.
     * 
     * @param module The {@link Class} of the {@link GraphicsModule} to remove from the descriptor.
     */
    public void removeModule(Class<? extends GraphicsModule> module) {

        modules.remove(module);
    }

    /**
     * Creates a new {@link GraphicsState} using the data stored in the descriptor.
     * A new instance of every added {@link GraphicsModule} is created on every call of this method.
     * 
     * @return The created {@link GraphicsState}.
     */
    public GraphicsState create() {

        LOGGER.info("Creating instance of graphics state '{}'", name);

        List<GraphicsModule> moduleObjects = new ArrayList<>();
        // Create module objects
        for (Class<? extends GraphicsModule> moduleClass : modules.keySet()) {
            try {
                moduleObjects.add(moduleClass.newInstance());
            } catch (InstantiationException e) {
                LOGGER.error("No no-arg constructor available in '{}'; is it an abstract class or interface?", moduleClass.getName(), e);
            } catch (IllegalAccessException e) {
                LOGGER.error("No public no-arg constructor available in '{}'", moduleClass.getName(), e);
            }
        }

        // Sort module objects by priority (higher first)
        Collections.sort(moduleObjects, new Comparator<GraphicsModule>() {

            @Override
            public int compare(GraphicsModule o1, GraphicsModule o2) {

                return Integer.valueOf(modules.get(o2.getClass()).getPriority()).compareTo(modules.get(o1.getClass()).getPriority());

            }

        });

        // Associate names with modules and create map
        Map<String, GraphicsModule> moduleMap = new LinkedHashMap<>();
        for (GraphicsModule module : moduleObjects) {
            moduleMap.put(modules.get(module.getClass()).getName(), module);
        }

        return new GraphicsState(name, moduleMap);
    }

    /*
     * Utility class for storing information along with a graphics module class.
     */
    private static class GraphicsModuleInformation {

        private final String name;
        private final int    priority;

        public GraphicsModuleInformation(String name, int priority) {

            this.name = name;
            this.priority = priority;
        }

        public String getName() {

            return name;
        }

        public int getPriority() {

            return priority;
        }

    }

}
