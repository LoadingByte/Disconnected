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

import java.util.List;
import java.util.Map;
import com.quartercode.disconnected.server.identity.SBPIdentityService;
import com.quartercode.disconnected.shared.identity.SBPIdentity;
import com.quartercode.eventbridge.bridge.BridgeConnector;
import com.quartercode.eventbridge.bridge.BridgeModule;
import com.quartercode.eventbridge.bridge.Event;
import com.quartercode.eventbridge.bridge.EventPredicate;
import com.quartercode.eventbridge.bridge.module.StandardHandlerModule;
import com.quartercode.eventbridge.channel.Channel;
import com.quartercode.eventbridge.channel.ChannelInvocation;

/**
 * An extension that allows to register {@link SBPAwareEventHandler}s, which provide {@link SBPIdentity}s alongside with each {@link Event}.
 * That SBP identities represent the server bridge partners who sent the events.
 * See {@link StandardHandlerModule} for more information on how it works internally.
 * 
 * @see Event
 * @see SBPAwareEventHandler
 * @see SBPIdentity
 */
public interface SBPAwareHandlerExtension extends BridgeModule {

    /**
     * Returns the {@link SBPIdentityService} the extension is linked with.
     * All actions are performed with that service.
     * 
     * @return The linked SBP identity service.
     */
    public SBPIdentityService getIdentityService();

    /**
     * Changes the {@link SBPIdentityService} the extension is linked with.
     * All actions will be performed with that service.
     * 
     * @param identityService The new linked SBP identity service.
     */
    public void setIdentityService(SBPIdentityService identityService);

    /**
     * Returns all {@link SBPAwareEventHandler}s which are listening for incoming {@link Event}s, alongside with their {@link EventPredicate} matchers.
     * A SBP-aware event handler should only be invoked if its event predicate returns {@code true} for the event.
     * 
     * @return The SBP-aware event handlers that are listening on the SBP-aware handler extension.
     */
    public Map<SBPAwareEventHandler<?>, EventPredicate<?>> getHandlers();

    /**
     * Adds the given {@link SBPAwareEventHandler} to the SBP-aware handler extension.
     * It'll start listening for incoming {@link Event}s that match the given {@link EventPredicate}.
     * 
     * @param handler The new SBP-aware event handler that should start listening on the SBP-aware handler extension.
     * @param predicate An event predicate that decides which events are passed into the handler.
     */
    public void addHandler(SBPAwareEventHandler<?> handler, EventPredicate<?> predicate);

    /**
     * Removes the given {@link SBPAwareEventHandler} from the SBP-aware handler extension.
     * It'll stop listening for incoming {@link Event}s.
     * 
     * @param handler The SBP-aware event handler that should stop listening on the SBP-aware handler extension.
     */
    public void removeHandler(SBPAwareEventHandler<?> handler);

    /**
     * Returns all {@link SBPAwareEventHandlerExceptionCatcher}s which are listening for {@link RuntimeException}s that occur during the handling of an {@link Event}.
     * When a registered {@link SBPAwareEventHandler} throws such an exception, all of these exception catchers are called.
     * 
     * @return The exception catchers that are listening on the SBP-aware handler extension.
     */
    public List<SBPAwareEventHandlerExceptionCatcher> getExceptionCatchers();

    /**
     * Adds the given {@link SBPAwareEventHandlerExceptionCatcher}s to the SBP-aware handler extension.
     * It'll start listening for {@link RuntimeException}s that occur during the handling of an {@link Event}.
     * 
     * @param catcher The new exception catcher that should start listening on the SBP-aware handler extension.
     */
    public void addExceptionCatcher(SBPAwareEventHandlerExceptionCatcher catcher);

    /**
     * Removes the given {@link SBPAwareEventHandlerExceptionCatcher}s from the SBP-aware handler extension.
     * It'll stop listening for {@link RuntimeException}s that occur during the handling of an {@link Event}.
     * 
     * @param catcher The exception catcher that should stop listening on the SBP-aware handler extension.
     */
    public void removeExceptionCatcher(SBPAwareEventHandlerExceptionCatcher catcher);

    /**
     * Adds the given {@link ModifySBPAwareHandlerListListener} that is called when an {@link SBPAwareEventHandler} is added or removed.
     * 
     * @param listener The listener that should be added.
     * @see #addHandler(SBPAwareEventHandler, EventPredicate)
     * @see #removeHandler(SBPAwareEventHandler)
     */
    public void addModifyHandlerListListener(ModifySBPAwareHandlerListListener listener);

    /**
     * Removes the given {@link ModifySBPAwareHandlerListListener} that is called when an {@link SBPAwareEventHandler} is added or removed.
     * 
     * @param listener The listener that should be removed.
     * @see #addHandler(SBPAwareEventHandler, EventPredicate)
     * @see #removeHandler(SBPAwareEventHandler)
     */
    public void removeModifyHandlerListListener(ModifySBPAwareHandlerListListener listener);

    /**
     * Adds the given {@link ModifySBPAwareExceptionCatcherListListener} that is called when an {@link SBPAwareEventHandlerExceptionCatcher} is added or removed.
     * 
     * @param listener The listener that should be added.
     * @see #addExceptionCatcher(SBPAwareEventHandlerExceptionCatcher)
     * @see #removeExceptionCatcher(SBPAwareEventHandlerExceptionCatcher)
     */
    public void addModifyExceptionCatcherListListener(ModifySBPAwareExceptionCatcherListListener listener);

    /**
     * Removes the given {@link ModifySBPAwareExceptionCatcherListListener} that is called when an {@link SBPAwareEventHandlerExceptionCatcher} is added or removed.
     * 
     * @param listener The listener that should be removed.
     * @see #addExceptionCatcher(SBPAwareEventHandlerExceptionCatcher)
     * @see #removeExceptionCatcher(SBPAwareEventHandlerExceptionCatcher)
     */
    public void removeModifyExceptionCatcherListListener(ModifySBPAwareExceptionCatcherListListener listener);

    /**
     * Returns the {@link Channel} which delivers {@link Event}s to a specific {@link SBPAwareEventHandler}s.
     * 
     * @return The channel which delivers events to a specific SBP-aware event handler.
     */
    public Channel<SBPAwareHandleInterceptor> getHandleChannel();

    /**
     * Returns the {@link Channel} which delivers {@link RuntimeException}s that occurred during the hanlding of events to all {@link SBPAwareEventHandlerExceptionCatcher}s.
     * 
     * @return The channel which delivers runtime exceptions to all exception catchers.
     */
    public Channel<SBPAwareHandleExceptionInterceptor> getExceptionChannel();

    /**
     * A modify SBP-aware handler list listener is called when an {@link SBPAwareEventHandler} is added to or removed from an {@link SBPAwareHandlerExtension}.
     */
    public static interface ModifySBPAwareHandlerListListener {

        /**
         * This method is invoked when the given {@link SBPAwareEventHandler} is being added to the given {@link SBPAwareHandlerExtension}.
         * It is called after the handler was added.
         * 
         * @param handler The SBP-aware event handler that is added to the SBP-aware handler extension.
         * @param predicate The {@link EventPredicate} matcher that belongs to the SBP-aware handler.
         * @param extension The SBP-aware handler extension the given SBP-aware handler is added to.
         */
        public void onAddHandler(SBPAwareEventHandler<?> handler, EventPredicate<?> predicate, SBPAwareHandlerExtension extension);

        /**
         * This method is invoked when the given {@link SBPAwareEventHandler} is being removed from the given {@link SBPAwareHandlerExtension}.
         * It is called before the handler is removed.
         * 
         * @param handler The SBP-aware event handler that is removed from the SBP-aware handler extension.
         * @param predicate The {@link EventPredicate} matcher that belonged to the SBP-aware handler.
         * @param extension The SBP-aware handler extension the given SBP-aware handler is removed from.
         */
        public void onRemoveHandler(SBPAwareEventHandler<?> handler, EventPredicate<?> predicate, SBPAwareHandlerExtension extension);

    }

    /**
     * A modify SBP-aware exception catcher list listener is called when an {@link SBPAwareEventHandlerExceptionCatcher} is added to or removed from an {@link SBPAwareHandlerExtension}.
     */
    public static interface ModifySBPAwareExceptionCatcherListListener {

        /**
         * This method is invoked when the given {@link SBPAwareEventHandlerExceptionCatcher} is being added to the given {@link SBPAwareHandlerExtension}.
         * It is called after the catcher was added.
         * 
         * @param catcher The exception catcher that is added to the standard handler extension.
         * @param extension The SBP-aware handler extension the given catcher is added to.
         */
        public void onAddCatcher(SBPAwareEventHandlerExceptionCatcher catcher, SBPAwareHandlerExtension extension);

        /**
         * This method is invoked when the given {@link SBPAwareEventHandlerExceptionCatcher} is being removed from the given {@link SBPAwareHandlerExtension}.
         * It is called before the catcher is removed.
         * 
         * @param catcher The exception catcher that is removed from the SBP-aware handler extension.
         * @param extension The SBP-aware handler extension the given catcher is removed from.
         */
        public void onRemoveCatcher(SBPAwareEventHandlerExceptionCatcher catcher, SBPAwareHandlerExtension extension);

    }

    /**
     * The interceptor which is used in the SBP-aware handle channel of an {@link SBPAwareHandlerExtension}.
     * 
     * @see SBPAwareHandlerExtension#getHandleChannel()
     */
    public static interface SBPAwareHandleInterceptor {

        /**
         * Intercepts the delivery process of the given {@link Event} to the given {@link SBPAwareEventHandler}.
         * 
         * @param invocation The {@link ChannelInvocation} object for the current invocation chain.
         * @param event The event which is transported through the channel.
         *        It should be delivered the given SBP-aware event handler.
         * @param source The {@link BridgeConnector} which received the event.
         *        May be {@code null} if the handled event was sent from the same bridge as the one which is handling it.
         * @param sender The {@link SBPIdentity} of the SBP who sent the event.
         *        May be {@code null} if the handled event was not sent from an SBP bridge.
         * @param handler The SBP-aware event handler the given event is delivered to.
         */
        public void handle(ChannelInvocation<SBPAwareHandleInterceptor> invocation, Event event, BridgeConnector source, SBPIdentity sender, SBPAwareEventHandler<?> handler);

    }

    /**
     * The interceptor which is used in the SBP-aware exception handle channel of an {@link SBPAwareHandlerExtension}.
     * 
     * @see SBPAwareHandlerExtension#getExceptionChannel()
     */
    public static interface SBPAwareHandleExceptionInterceptor {

        /**
         * Intercepts the delivery process of the given {@link RuntimeException} to all {@link SBPAwareEventHandlerExceptionCatcher}s of the {@link SBPAwareHandlerExtension}.
         * 
         * @param invocation The {@link ChannelInvocation} object for the current invocation chain.
         * @param exception The runtime exception which is transported through the channel.
         *        It should be delivered all exception catchers of the extension.
         * @param handler The {@link SBPAwareEventHandler} which threw the exception.
         * @param event The {@link Event} which caused the given event handler to throw the given exception on handling.
         * @param source The {@link BridgeConnector} which received the given event.
         *        May be {@code null} if the handled event was sent from the same bridge as the one which is handling it.
         */
        public void handle(ChannelInvocation<SBPAwareHandleExceptionInterceptor> invocation, RuntimeException exception, SBPAwareEventHandler<?> handler, Event event, BridgeConnector source);

    }

}
