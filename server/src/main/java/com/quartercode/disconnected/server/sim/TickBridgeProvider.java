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

package com.quartercode.disconnected.server.sim;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.quartercode.disconnected.server.bridge.SBPAwareEventHandler;
import com.quartercode.disconnected.server.bridge.SBPAwareEventHandlerExceptionCatcher;
import com.quartercode.disconnected.server.bridge.SBPAwareHandlerExtension;
import com.quartercode.disconnected.server.bridge.SBPIdentityExtension;
import com.quartercode.disconnected.server.identity.SBPIdentityService;
import com.quartercode.disconnected.shared.bridge.HandleInvocationProviderExtension;
import com.quartercode.disconnected.shared.identity.SBPIdentity;
import com.quartercode.disconnected.shared.util.ServiceRegistry;
import com.quartercode.eventbridge.EventBridgeFactory;
import com.quartercode.eventbridge.bridge.Bridge;
import com.quartercode.eventbridge.bridge.BridgeConnector;
import com.quartercode.eventbridge.bridge.Event;
import com.quartercode.eventbridge.bridge.module.EventHandler;
import com.quartercode.eventbridge.bridge.module.EventHandlerExceptionCatcher;
import com.quartercode.eventbridge.bridge.module.StandardHandlerModule;
import com.quartercode.eventbridge.extra.extension.ReturnEventExtensionRequester;
import com.quartercode.eventbridge.extra.extension.ReturnEventExtensionReturner;

/**
 * The tick bridge provider extends the {@link TickRunnableInvoker} by providing a {@link Bridge} for the parent {@link TickService}.
 * If bridge functionality is needed, this class should be used over the plain runnable invoker.
 *
 * @see Bridge
 */
public class TickBridgeProvider extends TickRunnableInvoker {

    private static final Logger LOGGER = LoggerFactory.getLogger(TickBridgeProvider.class);

    private final Bridge        bridge = EventBridgeFactory.create(Bridge.class);

    /**
     * Creates a new tick bridge provider.
     */
    public TickBridgeProvider() {

        bridge.addModule(EventBridgeFactory.create(ReturnEventExtensionRequester.class));
        bridge.addModule(EventBridgeFactory.create(ReturnEventExtensionReturner.class));
        bridge.addModule(EventBridgeFactory.create(HandleInvocationProviderExtension.class));
        bridge.addModule(EventBridgeFactory.create(SBPIdentityExtension.class));
        bridge.addModule(EventBridgeFactory.create(SBPAwareHandlerExtension.class));

        bridge.getModule(HandleInvocationProviderExtension.class).setInvocationProvider(this);

        SBPIdentityService sbpIdentityService = ServiceRegistry.lookup(SBPIdentityService.class);
        bridge.getModule(SBPIdentityExtension.class).setIdentityService(sbpIdentityService);
        bridge.getModule(SBPAwareHandlerExtension.class).setIdentityService(sbpIdentityService);

        // Add exception catchers
        bridge.getModule(StandardHandlerModule.class).addExceptionCatcher(new EventHandlerExceptionCatcher() {

            @Override
            public void handle(RuntimeException exception, EventHandler<?> handler, Event event, BridgeConnector source) {

                logEventHandlingException(exception, handler.getClass(), event, source);
            }

        });
        bridge.getModule(SBPAwareHandlerExtension.class).addExceptionCatcher(new SBPAwareEventHandlerExceptionCatcher() {

            @Override
            public void handle(RuntimeException exception, SBPAwareEventHandler<?> handler, Event event, BridgeConnector source) {

                logEventHandlingException(exception, handler.getClass(), event, source);
            }

        });
    }

    private void logEventHandlingException(RuntimeException exception, Class<?> handlerType, Event event, BridgeConnector source) {

        SBPIdentity sender = ServiceRegistry.lookup(SBPIdentityService.class).getIdentity(source);
        LOGGER.warn("SBP '{}' sent event '{}' which caused an exception in handler '{}'", sender, event, handlerType.getName(), exception);
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
