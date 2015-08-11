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

package com.quartercode.disconnected.server.bridge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import com.quartercode.disconnected.server.identity.SBPIdentityService;
import com.quartercode.disconnected.shared.identity.SBPIdentity;
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
 * The default default implementation of the {@link SBPAwareHandlerExtension} interface.
 *
 * @see SBPAwareHandlerExtension
 */
public class DefaultSBPAwareHandlerExtension extends AbstractBridgeModule implements SBPAwareHandlerExtension {

    private final Channel<SBPAwareHandleInterceptor>               handleChannel                       = new DefaultChannel<>(SBPAwareHandleInterceptor.class);
    private final Channel<SBPAwareHandleExceptionInterceptor>      exceptionChannel                    = new DefaultChannel<>(SBPAwareHandleExceptionInterceptor.class);

    private SBPIdentityService                                     identityService;
    private final Map<SBPAwareEventHandler<?>, EventPredicate<?>>  handlers                            = new ConcurrentHashMap<>();
    private final Map<SBPAwareEventHandler<?>, LowLevelHandler>    lowLevelHandlers                    = new ConcurrentHashMap<>();
    private final List<ModifySBPAwareHandlerListListener>          modifyHandlerListListeners          = new ArrayList<>();
    private Map<SBPAwareEventHandler<?>, EventPredicate<?>>        handlersUnmodifiableCache;

    private final List<SBPAwareEventHandlerExceptionCatcher>       exceptionCatchers                   = new CopyOnWriteArrayList<>();
    private final List<ModifySBPAwareExceptionCatcherListListener> modifyExceptionCatcherListListeners = new ArrayList<>();

    /**
     * Creates a new default SBP-aware handler extension.
     */
    public DefaultSBPAwareHandlerExtension() {

        handleChannel.addInterceptor(new LastSBPAwareHandleInterceptor(), 0);
        exceptionChannel.addInterceptor(new LastSBPAwareHandleExceptionInterceptor(), 0);
    }

    @Override
    public void remove() {

        for (LowLevelHandler lowLevelHandler : lowLevelHandlers.values()) {
            getBridge().getModule(LowLevelHandlerModule.class).removeHandler(lowLevelHandler);
        }

        super.remove();
    }

    @Override
    public SBPIdentityService getIdentityService() {

        return identityService;
    }

    @Override
    public void setIdentityService(SBPIdentityService identityService) {

        this.identityService = identityService;
    }

    @Override
    public Map<SBPAwareEventHandler<?>, EventPredicate<?>> getHandlers() {

        if (handlersUnmodifiableCache == null) {
            handlersUnmodifiableCache = Collections.unmodifiableMap(handlers);
        }

        return handlersUnmodifiableCache;
    }

    @Override
    public void addHandler(SBPAwareEventHandler<?> handler, EventPredicate<?> predicate) {

        handlers.put(handler, predicate);
        handlersUnmodifiableCache = null;

        LowLevelHandler lowLevelHandler = new LowLevelHandlerAdapter(handler, predicate);
        lowLevelHandlers.put(handler, lowLevelHandler);
        getBridge().getModule(LowLevelHandlerModule.class).addHandler(lowLevelHandler);

        for (ModifySBPAwareHandlerListListener listener : modifyHandlerListListeners) {
            listener.onAddHandler(handler, predicate, this);
        }
    }

    @Override
    public void removeHandler(SBPAwareEventHandler<?> handler) {

        if (handlers.containsKey(handler)) {
            if (!modifyHandlerListListeners.isEmpty()) {
                EventPredicate<?> predicate = handlers.get(handler);
                for (ModifySBPAwareHandlerListListener listener : modifyHandlerListListeners) {
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
    public List<SBPAwareEventHandlerExceptionCatcher> getExceptionCatchers() {

        return Collections.unmodifiableList(exceptionCatchers);
    }

    @Override
    public void addExceptionCatcher(SBPAwareEventHandlerExceptionCatcher catcher) {

        exceptionCatchers.add(catcher);

        for (ModifySBPAwareExceptionCatcherListListener listener : modifyExceptionCatcherListListeners) {
            listener.onAddCatcher(catcher, this);
        }
    }

    @Override
    public void removeExceptionCatcher(SBPAwareEventHandlerExceptionCatcher catcher) {

        for (ModifySBPAwareExceptionCatcherListListener listener : modifyExceptionCatcherListListeners) {
            listener.onRemoveCatcher(catcher, this);
        }

        exceptionCatchers.remove(catcher);
    }

    @Override
    public void addModifyHandlerListListener(ModifySBPAwareHandlerListListener listener) {

        modifyHandlerListListeners.add(listener);
    }

    @Override
    public void removeModifyHandlerListListener(ModifySBPAwareHandlerListListener listener) {

        modifyHandlerListListeners.remove(listener);
    }

    @Override
    public void addModifyExceptionCatcherListListener(ModifySBPAwareExceptionCatcherListListener listener) {

        modifyExceptionCatcherListListeners.add(listener);
    }

    @Override
    public void removeModifyExceptionCatcherListListener(ModifySBPAwareExceptionCatcherListListener listener) {

        modifyExceptionCatcherListListeners.remove(listener);
    }

    @Override
    public Channel<SBPAwareHandleInterceptor> getHandleChannel() {

        return handleChannel;
    }

    @Override
    public Channel<SBPAwareHandleExceptionInterceptor> getExceptionChannel() {

        return exceptionChannel;
    }

    private void handle(Event event, BridgeConnector source, SBPAwareEventHandler<?> handler) {

        SBPIdentity sender = identityService.getIdentity(source);

        ChannelInvocation<SBPAwareHandleInterceptor> invocation = handleChannel.invoke();
        invocation.next().handle(invocation, event, source, sender, handler);
    }

    private class LowLevelHandlerAdapter implements LowLevelHandler {

        private final SBPAwareEventHandler<?> handler;
        private final EventPredicate<?>       predicate;

        private LowLevelHandlerAdapter(SBPAwareEventHandler<?> handler, EventPredicate<?> predicate) {

            this.handler = handler;
            this.predicate = predicate;
        }

        @Override
        public EventPredicate<?> getPredicate() {

            return predicate;
        }

        @Override
        public void handle(Event event, BridgeConnector source) {

            DefaultSBPAwareHandlerExtension.this.handle(event, source, handler);
        }

    }

    private class LastSBPAwareHandleInterceptor implements SBPAwareHandleInterceptor {

        @Override
        public void handle(ChannelInvocation<SBPAwareHandleInterceptor> invocation, Event event, BridgeConnector source, SBPIdentity sender, SBPAwareEventHandler<?> handler) {

            try {
                tryHandle(handler, event, sender);
            } catch (RuntimeException e) {
                invokeExceptionChannel(e, handler, event, source);
            }

            invocation.next().handle(invocation, event, source, sender, handler);
        }

        private <T extends Event> void tryHandle(SBPAwareEventHandler<T> handler, Event event, SBPIdentity sender) {

            try {
                @SuppressWarnings ("unchecked")
                T castedEvent = (T) event;
                handler.handle(castedEvent, sender);
            } catch (ClassCastException e) {
                // Do nothing
            }
        }

        private void invokeExceptionChannel(RuntimeException exception, SBPAwareEventHandler<?> handler, Event event, BridgeConnector source) {

            ChannelInvocation<SBPAwareHandleExceptionInterceptor> invocation = exceptionChannel.invoke();
            invocation.next().handle(invocation, exception, handler, event, source);
        }

    }

    private class LastSBPAwareHandleExceptionInterceptor implements SBPAwareHandleExceptionInterceptor {

        @Override
        public void handle(ChannelInvocation<SBPAwareHandleExceptionInterceptor> invocation, RuntimeException exception, SBPAwareEventHandler<?> handler, Event event, BridgeConnector source) {

            for (SBPAwareEventHandlerExceptionCatcher catcher : exceptionCatchers) {
                catcher.handle(exception, handler, event, source);
            }

            invocation.next().handle(invocation, exception, handler, event, source);
        }

    }

    /**
     * A {@link Factory} for the {@link DefaultSBPAwareHandlerExtension} object.
     */
    public static class DefaultSBPAwareHandlerExtensionFactory implements Factory {

        @Override
        public Object create() {

            return new DefaultSBPAwareHandlerExtension();
        }

    }

}
