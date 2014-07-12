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

package com.quartercode.disconnected.sim;

import com.quartercode.disconnected.bridge.HandleInvocationProviderExtension;
import com.quartercode.eventbridge.EventBridgeFactory;
import com.quartercode.eventbridge.bridge.Bridge;
import com.quartercode.eventbridge.extra.extension.ReturnEventExtensionRequester;
import com.quartercode.eventbridge.extra.extension.ReturnEventExtensionReturner;
import com.quartercode.eventbridge.extra.extension.SendPredicateCheckExtension;

/**
 * The tick bridge provider extends the {@link TickRunnableInvoker} by providing a {@link Bridge} for the parent {@link TickService}.
 * If bridge functionality is needed, this class should be used over the plain runnable invoker.
 * 
 * @see Bridge
 */
public class TickBridgeProvider extends TickRunnableInvoker {

    private final Bridge bridge = EventBridgeFactory.create(Bridge.class);

    /**
     * Creates a new tick bridge provider.
     */
    public TickBridgeProvider() {

        bridge.addModule(EventBridgeFactory.create(ReturnEventExtensionRequester.class));
        bridge.addModule(EventBridgeFactory.create(ReturnEventExtensionReturner.class));
        bridge.addModule(EventBridgeFactory.create(SendPredicateCheckExtension.class));
        bridge.addModule(EventBridgeFactory.create(HandleInvocationProviderExtension.class));

        bridge.getModule(HandleInvocationProviderExtension.class).setInvocationProvider(this);
    }

    /**
     * Returns the {@link Bridge} that should be used for sending events by anything related to the simulation.
     * 
     * @return The server tick service bridge.
     */
    public Bridge getBridge() {

        return bridge;
    }

}
