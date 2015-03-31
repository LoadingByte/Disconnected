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

package com.quartercode.disconnected.shared.bridge;

import com.quartercode.disconnected.shared.util.RunnableInvocationProvider;
import com.quartercode.eventbridge.bridge.BridgeConnector;
import com.quartercode.eventbridge.bridge.BridgeModule;
import com.quartercode.eventbridge.bridge.Event;
import com.quartercode.eventbridge.bridge.module.HandlerModule;

/**
 * The handle invocation provider extension redirects all {@link HandlerModule#handle(Event, BridgeConnector)} calls through a {@link RunnableInvocationProvider}.<br>
 * <br>
 * Since the handle invocation provider extension is a {@link BridgeModule}, it can be added to a bridge as follows:
 * 
 * <pre>
 * Bridge bridge = ...
 * HandleInvocationProviderExtension extension = ...
 * bridge.addModule(extension);
 * </pre>
 * 
 * Please note that the extension also can be removed from a bridge:
 * 
 * <pre>
 * Bridge bridge = ...
 * HandleInvocationProviderExtension extension = ...
 * bridge.addModule(extension);
 * ...
 * bridge.removeModule(bridge.getModule(HandleInvocationProviderExtension.class));
 * </pre>
 * 
 * @see HandlerModule
 * @see RunnableInvocationProvider
 */
public interface HandleInvocationProviderExtension extends BridgeModule {

    /**
     * Returns the {@link RunnableInvocationProvider} all {@link HandlerModule#handle(Event, BridgeConnector)} calls are redirected through.
     * 
     * @return The runnable invocation provider which is used to redirect all event handle calls.
     */
    public RunnableInvocationProvider getInvocationProvider();

    /**
     * Sets the {@link RunnableInvocationProvider} all {@link HandlerModule#handle(Event, BridgeConnector)} calls are redirected through.
     * If the invocation provider is set to {@code null}, event handle calls are not redirected.
     * 
     * @param invocationProvider The new runnable invocation provider which is used to redirect all event handle calls.
     */
    public void setInvocationProvider(RunnableInvocationProvider invocationProvider);

}
