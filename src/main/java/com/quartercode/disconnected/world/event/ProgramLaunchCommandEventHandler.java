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

package com.quartercode.disconnected.world.event;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.quartercode.classmod.extra.CollectionProperty;
import com.quartercode.classmod.extra.CollectionPropertyDefinition;
import com.quartercode.classmod.extra.Property;
import com.quartercode.classmod.extra.PropertyDefinition;
import com.quartercode.disconnected.sim.ProfileManager;
import com.quartercode.disconnected.util.ServiceRegistry;
import com.quartercode.disconnected.world.World;
import com.quartercode.disconnected.world.comp.Computer;
import com.quartercode.disconnected.world.comp.file.ContentFile;
import com.quartercode.disconnected.world.comp.file.FileSystemModule;
import com.quartercode.disconnected.world.comp.os.OperatingSystem;
import com.quartercode.disconnected.world.comp.program.Process;
import com.quartercode.disconnected.world.comp.program.ProcessModule;
import com.quartercode.disconnected.world.comp.program.ProgramExecutor;
import com.quartercode.eventbridge.bridge.module.EventHandler;

/**
 * The program launch command event handler executes {@link ProgramLaunchCommandEvent}s.
 * It should run on every tick server.
 * 
 * @see ProgramLaunchCommandEvent
 */
public class ProgramLaunchCommandEventHandler implements EventHandler<ProgramLaunchCommandEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProgramLaunchCommandEventHandler.class);

    @Override
    public void handle(ProgramLaunchCommandEvent event) {

        Computer playerComputer = getPlayerComputer();

        // Get the source file
        ContentFile source = getSourceFile(playerComputer, event.getFilePath());
        // Cancel if the program cannot be found
        if (source == null) {
            return;
        }

        // Create a new process
        Process<?> sessionProcess = getSessionProcess(playerComputer);
        Process<?> process = sessionProcess.get(Process.CREATE_CHILD).invoke();

        // Set the source file
        process.get(Process.SOURCE).set(source);

        // Initialize the process
        try {
            process.get(Process.INITIALIZE).invoke(event.getPid());
        } catch (Exception e) {
            LOGGER.warn("Error while initializing process with pid {}; client failure?", event.getPid(), e);
            abort(sessionProcess, process);
            return;
        }

        // Set the provided properties
        ProgramExecutor executor = process.get(Process.EXECUTOR).get();
        Class<?> executorClass = executor.getClass();
        Map<String, Object> executorProperties = event.getExecutorProperties();
        for (Entry<String, Object> property : executorProperties.entrySet()) {
            try {
                Field field = executorClass.getField(property.getKey());
                if (field != null) {
                    Object fieldValue = field.get(null);
                    // Warning: This construction might fill properties with entries of the wrong type
                    if (fieldValue instanceof PropertyDefinition) {
                        castProperty(executor.get((PropertyDefinition<?>) fieldValue)).set(property.getValue());
                    } else if (fieldValue instanceof CollectionPropertyDefinition && property.getValue() instanceof Collection) {
                        CollectionProperty<?, ?> feature = executor.get((CollectionPropertyDefinition<?, ?>) fieldValue);
                        for (Object entry : (Collection<?>) property.getValue()) {
                            castCollectionProperty(feature).add(entry);
                        }
                    }
                }
            } catch (Exception e) {
                LOGGER.warn("Error while setting property '{}' of program executor '{}' to '{}'; client failure?", property.getKey(), executor, property.getValue(), e);
                abort(sessionProcess, process);
                return;
            }
        }

        // Run the program
        try {
            executor.get(ProgramExecutor.RUN).invoke();
        } catch (Exception e) {
            LOGGER.warn("Program executor '{}' threw unexpected exception on start; invalid executor property/client failure? (map '{}')", executor, executorProperties, e);
            abort(sessionProcess, process);
        }
    }

    @SuppressWarnings ("unchecked")
    private <T> Property<T> castProperty(Property<?> property) {

        return (Property<T>) property;
    }

    @SuppressWarnings ("unchecked")
    private <E, C extends Collection<E>> CollectionProperty<E, C> castCollectionProperty(CollectionProperty<?, ?> property) {

        return (CollectionProperty<E, C>) property;
    }

    private void abort(Process<?> parent, Process<?> process) {

        parent.get(Process.CHILDREN).remove(process);
    }

    protected Computer getPlayerComputer() {

        World world = ServiceRegistry.lookup(ProfileManager.class).getActive().getWorld();
        // Just use first available computer as the player's one
        return world.get(World.COMPUTERS).get().get(0);
    }

    protected Process<?> getSessionProcess(Computer computer) {

        OperatingSystem os = computer.get(Computer.OS).get();
        // Just use the root process as the player's session
        return os.get(OperatingSystem.PROC_MODULE).get().get(ProcessModule.ROOT_PROCESS).get();
    }

    protected ContentFile getSourceFile(Computer computer, String path) {

        FileSystemModule fsModule = computer.get(Computer.OS).get().get(OperatingSystem.FS_MODULE).get();
        return (ContentFile) fsModule.get(FileSystemModule.GET_FILE).invoke(path);
    }

}
