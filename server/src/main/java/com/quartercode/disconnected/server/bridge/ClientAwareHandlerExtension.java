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

package com.quartercode.disconnected.server.bridge;

import java.util.Map;
import com.quartercode.disconnected.server.client.ClientIdentityService;
import com.quartercode.disconnected.shared.client.ClientIdentity;
import com.quartercode.eventbridge.bridge.BridgeConnector;
import com.quartercode.eventbridge.bridge.BridgeModule;
import com.quartercode.eventbridge.bridge.Event;
import com.quartercode.eventbridge.bridge.EventPredicate;
import com.quartercode.eventbridge.bridge.module.StandardHandlerModule;
import com.quartercode.eventbridge.channel.Channel;
import com.quartercode.eventbridge.channel.ChannelInvocation;

/**
 * An extension that allows to register {@link ClientAwareEventHandler}s, which provide {@link ClientIdentity}s alongside with each {@link Event}.
 * That client identities represent the clients that sent the events.
 * See {@link StandardHandlerModule} for more information on how it works internally.
 * 
 * @see Event
 * @see ClientAwareEventHandler
 * @see ClientIdentity
 */
public interface ClientAwareHandlerExtension extends BridgeModule {

    /**
     * Returns the {@link ClientIdentityService} the extension is linked with.
     * All actions are performed with that service.
     * 
     * @return The linked client identity service.
     */
    public ClientIdentityService getIdentityService();

    /**
     * Changes the {@link ClientIdentityService} the extension is linked with.
     * All actions will be performed with that service.
     * 
     * @param identityService The new linked client identity service.
     */
    public void setIdentityService(ClientIdentityService identityService);

    /**
     * Returns all {@link ClientAwareEventHandler}s which are listening for incoming {@link Event}s, alongside with their {@link EventPredicate} matchers.
     * A client-aware event handler should only be invoked if its event predicate returns {@code true} for the event.
     * 
     * @return The client-aware event handlers that are listening on the client-aware handler extension.
     */
    public Map<ClientAwareEventHandler<?>, EventPredicate<?>> getHandlers();

    /**
     * Adds the given {@link ClientAwareEventHandler} to the client-aware handler extension.
     * It'll start listening for incoming {@link Event}s that match the given {@link EventPredicate}.
     * 
     * @param handler The new client-aware event handler that should start listening on the client-aware handler extension.
     * @param predicate An event predicate that decides which events are passed into the handler.
     */
    public void addHandler(ClientAwareEventHandler<?> handler, EventPredicate<?> predicate);

    /**
     * Removes the given {@link ClientAwareEventHandler} from the client-aware handler extension.
     * It'll stop listening for incoming {@link Event}s.
     * 
     * @param handler The client-aware event handler that should stop listening on the client-aware handler extension.
     */
    public void removeHandler(ClientAwareEventHandler<?> handler);

    /**
     * Adds the given {@link ModifyClientAwareHandlerListListener} that is called when a {@link ClientAwareEventHandler} is added or removed.
     * 
     * @param listener The listener that should be added.
     * @see #addHandler(ClientAwareEventHandler, EventPredicate)
     * @see #removeHandler(ClientAwareEventHandler)
     */
    public void addModifyHandlerListListener(ModifyClientAwareHandlerListListener listener);

    /**
     * Removes the given {@link ModifyClientAwareHandlerListListener} that is called when a {@link ClientAwareEventHandler} is added or removed.
     * 
     * @param listener The listener that should be removed.
     * @see #addHandler(ClientAwareEventHandler, EventPredicate)
     * @see #removeHandler(ClientAwareEventHandler)
     */
    public void removeModifyHandlerListListener(ModifyClientAwareHandlerListListener listener);

    /**
     * Returns the {@link Channel} which delivers {@link Event}s to a specific {@link ClientAwareEventHandler}s.
     * 
     * @return The channel which delivers events to a specific client-aware event handler.
     */
    public Channel<ClientAwareHandleInterceptor> getChannel();

    /**
     * A modify client-aware handler list listener is called when a {@link ClientAwareEventHandler} is added to or removed from a {@link ClientAwareHandlerExtension}.
     */
    public static interface ModifyClientAwareHandlerListListener {

        /**
         * This method is invoked when the given {@link ClientAwareEventHandler} is being added to the given {@link ClientAwareHandlerExtension}.
         * It is called after the handler was added.
         * 
         * @param handler The client-aware event handler that is added to the client-aware handler extension.
         * @param predicate The {@link EventPredicate} matcher that belongs to the client-aware handler.
         * @param extension The client-aware handler extension the given client-aware handler is added to.
         */
        public void onAddHandler(ClientAwareEventHandler<?> handler, EventPredicate<?> predicate, ClientAwareHandlerExtension extension);

        /**
         * This method is invoked when the given {@link ClientAwareEventHandler} is being removed from the given {@link ClientAwareHandlerExtension}.
         * It is called before the handler is removed.
         * 
         * @param handler The client-aware event handler that is removed from the client-aware handler extension.
         * @param predicate The {@link EventPredicate} matcher that belonged to the client-aware handler.
         * @param extension The client-aware handler extension the given client-aware handler is removed from.
         */
        public void onRemoveHandler(ClientAwareEventHandler<?> handler, EventPredicate<?> predicate, ClientAwareHandlerExtension extension);

    }

    /**
     * The interceptor which is used in the client-aware handle channel of a {@link ClientAwareHandlerExtension}.
     * 
     * @see ClientAwareHandlerExtension#getChannel()
     */
    public static interface ClientAwareHandleInterceptor {

        /**
         * Intercepts the delivery process of the given {@link Event} to the given {@link ClientAwareEventHandler}.
         * 
         * @param invocation The {@link ChannelInvocation} object for the current invocation chain.
         * @param event The event which is transported through the channel.
         *        It should be delivered the given client-aware event handler.
         * @param source The {@link BridgeConnector} which received the event.
         *        May be {@code null} if the handled event was sent from the same bridge as the one which is handling it.
         * @param client The {@link ClientIdentity} of the client which sent the event.
         *        May be {@code null} if the handled event was not sent from a client bridge.
         * @param handler The client-aware event handler the given event is delivered to.
         */
        public void handle(ChannelInvocation<ClientAwareHandleInterceptor> invocation, Event event, BridgeConnector source, ClientIdentity client, ClientAwareEventHandler<?> handler);

    }

}
