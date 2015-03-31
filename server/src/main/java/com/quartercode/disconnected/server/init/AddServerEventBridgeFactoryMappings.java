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

package com.quartercode.disconnected.server.init;

import com.quartercode.disconnected.server.bridge.DefaultSBPAwareHandlerExtension.DefaultSBPAwareHandlerExtensionFactory;
import com.quartercode.disconnected.server.bridge.DefaultSBPIdentityExtension.DefaultSBPIdentityExtensionFactory;
import com.quartercode.disconnected.server.bridge.SBPAwareHandlerExtension;
import com.quartercode.disconnected.server.bridge.SBPIdentityExtension;
import com.quartercode.disconnected.shared.util.init.Initializer;
import com.quartercode.disconnected.shared.util.init.InitializerSettings;
import com.quartercode.eventbridge.EventBridgeFactory;
import com.quartercode.eventbridge.factory.FactoryManager;

@InitializerSettings (groups = "addEventBridgeFactoryMappings")
public class AddServerEventBridgeFactoryMappings implements Initializer {

    @Override
    public void initialize() {

        FactoryManager factoryManager = EventBridgeFactory.getFactoryManager();

        factoryManager.setFactory(SBPIdentityExtension.class, new DefaultSBPIdentityExtensionFactory());
        factoryManager.setFactory(SBPAwareHandlerExtension.class, new DefaultSBPAwareHandlerExtensionFactory());
    }

}
