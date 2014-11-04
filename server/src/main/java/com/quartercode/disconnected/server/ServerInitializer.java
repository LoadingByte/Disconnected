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

package com.quartercode.disconnected.server;

import java.io.IOException;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.quartercode.disconnected.server.bridge.DefaultSBPAwareHandlerExtension.DefaultSBPAwareHandlerExtensionFactory;
import com.quartercode.disconnected.server.bridge.DefaultSBPIdentityExtension.DefaultSBPIdentityExtensionFactory;
import com.quartercode.disconnected.server.bridge.SBPAwareEventHandler;
import com.quartercode.disconnected.server.bridge.SBPAwareHandlerExtension;
import com.quartercode.disconnected.server.bridge.SBPIdentityExtension;
import com.quartercode.disconnected.server.identity.DefaultSBPIdentityService;
import com.quartercode.disconnected.server.identity.SBPIdentityService;
import com.quartercode.disconnected.server.registry.ServerRegistries;
import com.quartercode.disconnected.server.registry.config.AddWorldContextPathEntryCommand;
import com.quartercode.disconnected.server.registry.config.AddWorldInitializerMappingCommand;
import com.quartercode.disconnected.server.registry.config.ConfigureFileTypeCommand;
import com.quartercode.disconnected.server.registry.config.ConfigureSchedulerGroupCommand;
import com.quartercode.disconnected.server.registry.config.ConfigureWorldProgramCommand;
import com.quartercode.disconnected.server.registry.config.RemoveWorldContextPathEntryCommand;
import com.quartercode.disconnected.server.registry.config.RemoveWorldInitializerMappingCommand;
import com.quartercode.disconnected.server.sim.DefaultTickService;
import com.quartercode.disconnected.server.sim.TickBridgeProvider;
import com.quartercode.disconnected.server.sim.TickSchedulerUpdater;
import com.quartercode.disconnected.server.sim.TickService;
import com.quartercode.disconnected.server.sim.profile.DefaultProfileService;
import com.quartercode.disconnected.server.sim.profile.ProfileService;
import com.quartercode.disconnected.server.util.ResourceStore;
import com.quartercode.disconnected.server.world.comp.program.WorldProcessInterruptCommandHandler;
import com.quartercode.disconnected.server.world.comp.program.WorldProcessLaunchCommandHandler;
import com.quartercode.disconnected.shared.config.ConfigService;
import com.quartercode.disconnected.shared.config.util.RemoveNamedValueCommand;
import com.quartercode.disconnected.shared.event.program.control.WorldProcessInterruptCommand;
import com.quartercode.disconnected.shared.event.program.control.WorldProcessLaunchCommand;
import com.quartercode.disconnected.shared.registry.Registries;
import com.quartercode.disconnected.shared.util.ServiceRegistry;
import com.quartercode.eventbridge.EventBridgeFactory;
import com.quartercode.eventbridge.bridge.Bridge;
import com.quartercode.eventbridge.bridge.EventPredicate;
import com.quartercode.eventbridge.extra.predicate.TypePredicate;
import com.quartercode.eventbridge.factory.FactoryManager;

/**
 * This class contains the {@link #initialize()} method that configures everything that the server needs.
 * For example, the method could add some service implementations.
 */
public class ServerInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerInitializer.class);

    private static boolean      initialized;

    /**
     * Initializes everything that the server needs.
     * For example, this method could add some service implementations.
     */
    public static void initialize() {

        if (initialized) {
            return;
        }
        initialized = true;

        // Add server EventBridgeFactory mappings
        FactoryManager factoryManager = EventBridgeFactory.getFactoryManager();
        factoryManager.setFactory(SBPIdentityExtension.class, new DefaultSBPIdentityExtensionFactory());
        factoryManager.setFactory(SBPAwareHandlerExtension.class, new DefaultSBPAwareHandlerExtensionFactory());

        // Add server config command parsers
        addConfigCommandParsers(ServiceRegistry.lookup(ConfigService.class));

        // Fill resource store
        LOGGER.debug("Loading resource store data");
        try {
            ResourceStore.loadFromClasspath("/data");
        } catch (IOException e) {
            throw new RuntimeException("Cannot fill resource store", e);
        }

        // Initialize server services
        LOGGER.info("Initializing profile service and loading stored profiles");
        ServiceRegistry.register(ProfileService.class, new DefaultProfileService(Paths.get("profiles")));
        LOGGER.info("Initializing SBP identity service");
        ServiceRegistry.register(SBPIdentityService.class, new DefaultSBPIdentityService());
        LOGGER.info("Initializing tick service");
        initializeTickService();

        // Initialize event handlers
        LOGGER.debug("Adding event handlers");
        addEventHandlers(ServiceRegistry.lookup(TickService.class).getAction(TickBridgeProvider.class).getBridge());
    }

    private static void addConfigCommandParsers(ConfigService service) {

        // World context path
        service.addCommand("addWorldContextPathEntry", new AddWorldContextPathEntryCommand(Registries.get(ServerRegistries.WORLD_CONTEXT_PATH)));
        service.addCommand("removeWorldContextPathEntry", new RemoveWorldContextPathEntryCommand(Registries.get(ServerRegistries.WORLD_CONTEXT_PATH)));

        // World initializer mappings
        service.addCommand("addWorldInitializerMapping", new AddWorldInitializerMappingCommand(Registries.get(ServerRegistries.WORLD_INITIALIZER_MAPPINGS)));
        service.addCommand("removeWorldInitializerMapping", new RemoveWorldInitializerMappingCommand(Registries.get(ServerRegistries.WORLD_INITIALIZER_MAPPINGS)));

        // Scheduler groups
        service.addCommand("configureSchedulerGroup", new ConfigureSchedulerGroupCommand(Registries.get(ServerRegistries.SCHEDULER_GROUPS)));
        service.addCommand("removeSchedulerGroup", new RemoveNamedValueCommand<>("scheduler group", Registries.get(ServerRegistries.SCHEDULER_GROUPS)));

        // File types
        service.addCommand("configureFileType", new ConfigureFileTypeCommand(Registries.get(ServerRegistries.FILE_TYPES)));
        service.addCommand("removeFileType", new RemoveNamedValueCommand<>("file type", Registries.get(ServerRegistries.FILE_TYPES)));

        // World programs
        service.addCommand("configureWorldProgram", new ConfigureWorldProgramCommand(Registries.get(ServerRegistries.WORLD_PROGRAMS)));
        service.addCommand("removeWorldProgram", new RemoveNamedValueCommand<>("world program", Registries.get(ServerRegistries.WORLD_PROGRAMS)));
    }

    private static void initializeTickService() {

        TickService tickService = new DefaultTickService();
        ServiceRegistry.register(TickService.class, tickService);

        tickService.addAction(new TickBridgeProvider());
        tickService.addAction(new TickSchedulerUpdater());
    }

    private static void addEventHandlers(Bridge bridge) {

        addSBPAwareEventHandler(bridge, new WorldProcessLaunchCommandHandler(), new TypePredicate<>(WorldProcessLaunchCommand.class));
        addSBPAwareEventHandler(bridge, new WorldProcessInterruptCommandHandler(), new TypePredicate<>(WorldProcessInterruptCommand.class));
    }

    private static void addSBPAwareEventHandler(Bridge bridge, SBPAwareEventHandler<?> handler, EventPredicate<?> predicate) {

        bridge.getModule(SBPAwareHandlerExtension.class).addHandler(handler, predicate);
    }

    private ServerInitializer() {

    }

}
