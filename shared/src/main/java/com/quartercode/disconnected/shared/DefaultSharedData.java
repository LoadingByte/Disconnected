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
import com.quartercode.eventbridge.EventBridgeFactory;
import com.quartercode.eventbridge.factory.FactoryManager;

/**
 * This class contains methods that configure everything that both the server and the client need.
 * For example, a method could load some data into a storage class or add some values to a service configuration.
 */
public class DefaultSharedData {

    /**
     * Adds the default custom mappings to the {@link EventBridgeFactory}.
     */
    public static void addCustomEventBridgeFactoryMappings() {

        FactoryManager factoryManager = EventBridgeFactory.getFactoryManager();

        factoryManager.setFactory(HandleInvocationProviderExtension.class, new DefaultHandleInvocationProviderExtensionFactory());
    }

    private DefaultSharedData() {

    }

}
