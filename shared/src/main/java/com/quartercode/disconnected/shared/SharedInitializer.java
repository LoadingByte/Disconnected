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

package com.quartercode.disconnected.shared;

import com.quartercode.disconnected.shared.bridge.DefaultHandleInvocationProviderExtension.DefaultHandleInvocationProviderExtensionFactory;
import com.quartercode.disconnected.shared.bridge.HandleInvocationProviderExtension;
import com.quartercode.disconnected.shared.util.ServiceRegistry;
import com.quartercode.disconnected.shared.util.config.ConfigService;
import com.quartercode.disconnected.shared.util.config.DefaultConfigService;
import com.quartercode.disconnected.shared.util.config.extra.ClasspathConfigLoader;
import com.quartercode.disconnected.shared.util.registry.DefaultRegistryService;
import com.quartercode.disconnected.shared.util.registry.RegistryService;
import com.quartercode.eventbridge.EventBridgeFactory;
import com.quartercode.eventbridge.factory.FactoryManager;

/**
 * This class contains the {@link #initialize()} and {@link #initializeFinal()} methods that configures everything that both the server and the client need.
 * For example, the methods could add some service implementations.
 */
public class SharedInitializer {

    private static boolean initialized;
    private static boolean initializedFinal;

    /**
     * Initializes everything that both the server and the client need.
     * For example, this method could add some service implementations.
     */
    public static void initialize() {

        if (initialized) {
            return;
        }
        initialized = true;

        // Add shared EventBridgeFactory mappings
        FactoryManager factoryManager = EventBridgeFactory.getFactoryManager();
        factoryManager.setFactory(HandleInvocationProviderExtension.class, new DefaultHandleInvocationProviderExtensionFactory());

        // Initialize shared services
        ServiceRegistry.register(RegistryService.class, new DefaultRegistryService());
        ServiceRegistry.register(ConfigService.class, new DefaultConfigService());
    }

    /**
     * Initializes everything that both the server and the client need afterwards those two components called their initializers.
     * For example, this method could execute some actions that depend on the server or the client.
     */
    public static void initializeFinal() {

        if (initializedFinal) {
            return;
        }
        initializedFinal = true;

        // Load configs
        ClasspathConfigLoader.load(ServiceRegistry.lookup(ConfigService.class), "/config");
    }

    private SharedInitializer() {

    }

}
