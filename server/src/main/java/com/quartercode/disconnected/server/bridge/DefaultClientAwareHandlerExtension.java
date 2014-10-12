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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.quartercode.disconnected.server.client.ClientIdentityService;
import com.quartercode.disconnected.shared.client.ClientIdentity;
import com.quartercode.eventbridge.basic.AbstractBridgeModule;
import com.quartercode.eventbridge.bridge.BridgeConnector;
import com.quartercode.eventbridge.bridge.Event;
import com.quartercode.eventbridge.bridge.EventPredicate;
import com.quartercode.eventbridge.bridge.module.LowLevelHandler;
import com.quartercode.eventbridge.bridge.module.LowLevelHandlerModule;
import com.quartercode.eventbridge.channel.Channel;
import com.quartercode.eventbridge.channel.ChannelInvocation;
import com.quartercode.eventbridge.def.channel.DefaultChannel;
import com.quartercode.eventbridge.factory.Factory;

/**
 * The default default implementation of the {@link ClientAwareHandlerExtension} interface.
 * 
 * @see ClientAwareHandlerExtension
 */
public class DefaultClientAwareHandlerExtension extends AbstractBridgeModule implements ClientAwareHandlerExtension {

    private final Channel<ClientAwareHandleInterceptor>              channel                    = new DefaultChannel<>(ClientAwareHandleInterceptor.class);

    private ClientIdentityService                                    identityService;
    private final Map<ClientAwareEventHandler<?>, EventPredicate<?>> handlers                   = new ConcurrentHashMap<>();
    private final Map<ClientAwareEventHandler<?>, LowLevelHandler>   lowLevelHandlers           = new ConcurrentHashMap<>();
    private final List<ModifyClientAwareHandlerListListener>         modifyHandlerListListeners = new ArrayList<>();
    private Map<ClientAwareEventHandler<?>, EventPredicate<?>>       handlersUnmodifiableCache;

    /**
     * Creates a new default client-aware handler extension.
     */
    public DefaultClientAwareHandlerExtension() {

        channel.addInterceptor(new LastClientAwareHandleInterceptor(), 0);
    }

    @Override
    public void remove() {

        for (LowLevelHandler lowLevelHandler : lowLevelHandlers.values()) {
            getBridge().getModule(LowLevelHandlerModule.class).removeHandler(lowLevelHandler);
        }

        super.remove();
    }

    @Override
    public ClientIdentityService getIdentityService() {

        return identityService;
    }

    @Override
    public void setIdentityService(ClientIdentityService identityService) {

        this.identityService = identityService;
    }

    @Override
    public Map<ClientAwareEventHandler<?>, EventPredicate<?>> getHandlers() {

        if (handlersUnmodifiableCache == null) {
            handlersUnmodifiableCache = Collections.unmodifiableMap(handlers);
        }

        return handlersUnmodifiableCache;
    }

    @Override
    public void addHandler(ClientAwareEventHandler<?> handler, EventPredicate<?> predicate) {

        handlers.put(handler, predicate);
        handlersUnmodifiableCache = null;

        LowLevelHandler lowLevelHandler = new LowLevelHandlerAdapter(handler, predicate);
        lowLevelHandlers.put(handler, lowLevelHandler);
        getBridge().getModule(LowLevelHandlerModule.class).addHandler(lowLevelHandler);

        for (ModifyClientAwareHandlerListListener listener : modifyHandlerListListeners) {
            listener.onAddHandler(handler, predicate, this);
        }
    }

    @Override
    public void removeHandler(ClientAwareEventHandler<?> handler) {

        if (handlers.containsKey(handler)) {
            if (!modifyHandlerListListeners.isEmpty()) {
                EventPredicate<?> predicate = handlers.get(handler);
                for (ModifyClientAwareHandlerListListener listener : modifyHandlerListListeners) {
                    listener.onRemoveHandler(handler, predicate, this);
                }
            }

            LowLevelHandler lowLevelHandler = lowLevelHandlers.get(handler);
            lowLevelHandlers.remove(handler);
            getBridge().getModule(LowLevelHandlerModule.class).removeHandler(lowLevelHandler);

            handlers.remove(handler);
            handlersUnmodifiableCache = null;
        }
    }

    @Override
    public void addModifyHandlerListListener(ModifyClientAwareHandlerListListener listener) {

        modifyHandlerListListeners.add(listener);
    }

    @Override
    public void removeModifyHandlerListListener(ModifyClientAwareHandlerListListener listener) {

        modifyHandlerListListeners.remove(listener);
    }

    @Override
    public Channel<ClientAwareHandleInterceptor> getChannel() {

        return channel;
    }

    private void handle(Event event, BridgeConnector source, ClientAwareEventHandler<?> handler) {

        ClientIdentity client = identityService.getIdentity(source);

        ChannelInvocation<ClientAwareHandleInterceptor> invocation = channel.invoke();
        invocation.next().handle(invocation, event, source, client, handler);
    }

    private class LowLevelHandlerAdapter implements LowLevelHandler {

        private final ClientAwareEventHandler<?> handler;
        private final EventPredicate<?>          predicate;

        private LowLevelHandlerAdapter(ClientAwareEventHandler<?> handler, EventPredicate<?> predicate) {

            this.handler = handler;
            this.predicate = predicate;
        }

        @Override
        public EventPredicate<?> getPredicate() {

            return predicate;
        }

        @Override
        public void handle(Event event, BridgeConnector source) {

            DefaultClientAwareHandlerExtension.this.handle(event, source, handler);
        }

    }

    private class LastClientAwareHandleInterceptor implements ClientAwareHandleInterceptor {

        @Override
        public void handle(ChannelInvocation<ClientAwareHandleInterceptor> invocation, Event event, BridgeConnector source, ClientIdentity client, ClientAwareEventHandler<?> handler) {

            tryHandle(handler, event, client);

            invocation.next().handle(invocation, event, source, client, handler);
        }

        private <T extends Event> void tryHandle(ClientAwareEventHandler<T> handler, Event event, ClientIdentity client) {

            try {
                @SuppressWarnings ("unchecked")
                T castedEvent = (T) event;
                handler.handle(castedEvent, client);
            } catch (ClassCastException e) {
                // Do nothing
            }
        }

    }

    /**
     * A {@link Factory} for the {@link DefaultClientAwareHandlerExtension} object.
     */
    public static class DefaultClientAwareHandlerExtensionFactory implements Factory {

        @Override
        public Object create() {

            return new DefaultClientAwareHandlerExtension();
        }

    }

}
