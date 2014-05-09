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

package com.quartercode.disconnected.bridge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bridges allow to connect two parts of an application without specifying the way of transport between multiple bridges.
 * Generally, bridges communicate with {@link Event}s that are sent between the bridges.
 * Events are sent by the first bridge, received by the second bridge and finally processed by some {@link EventHandler}s.<br>
 * <br>
 * Bridges use the concept of {@link EventPredicate}s:
 * Since every event handler supplies such a predicate, they can be used to determine whether an event should be sent between some bridges.
 * That means that a bridge only sends an event if the connected bridge has a handler that might want to process the event.
 * That limitation reduces the load on the event transport mechanism without the event senders having to worry about the event receivers.<br>
 * <br>
 * {@link BridgeConnector}s are used for connecting a bridge with some other bridges.
 * The {@link #connect(BridgeConnector)} method activates a connector and binds it,
 * while the {@link #disconnect(BridgeConnector)} method does the opposite.
 * The usage of bridge connectors inverts the dependency on details and decouples the bridge from the event transport logic.
 * 
 * @see Event
 * @see EventHandler
 * @see BridgeConnector
 */
public class Bridge {

    private static final Logger                                 LOGGER                    = LoggerFactory.getLogger(Bridge.class);

    private final List<EventHandler<?>>                         handlers                  = new ArrayList<>();
    private final List<BridgeConnector>                         connections               = new ArrayList<>();

    private final Map<BridgeConnector, List<EventPredicate<?>>> remoteAcceptionPredicates = new HashMap<>();

    /**
     * Creates a new empty bridge.
     */
    public Bridge() {

    }

    // ----- Handlers -----

    /**
     * Returns an unmodifiable list that contains all {@link EventHandler}s which are listening for incoming {@link Event}s.
     * 
     * @return The event handlers that are listening on the bridge.
     */
    public List<EventHandler<?>> getHandlers() {

        return Collections.unmodifiableList(handlers);
    }

    /**
     * Adds the given {@link EventHandler} to bridge. It'll start listening for incoming {@link Event}s.
     * Furthermore, the predicate of the handler is added to all connected bridges.
     * That might cause the sending of events which weren't sent previously.
     * 
     * @param handler The new event handler that should start listening on the bridge.
     */
    public void addHandler(EventHandler<?> handler) {

        handlers.add(handler);

        // Add handler's acception predicate to remote bridge
        send(new AddRemovePredicateEvent(handler.getPredicate(), true));
    }

    /**
     * Removes the given {@link EventHandler} from the bridge. It'll stop listening for incoming {@link Event}s.
     * Furthermore, the predicate of the handler is removed from all connected bridges.
     * That might cause events that were sent previously not to be sent anymore.
     * 
     * @param handler The event handler that should stop listening on the bridge.
     */
    public void removeHandler(EventHandler<?> handler) {

        handlers.remove(handler);

        // Remove handler's acception predicate from remote bridge
        send(new AddRemovePredicateEvent(handler.getPredicate(), false));
    }

    // ----- Connections -----

    /**
     * Returns an unmodifiable list that contains all active and bound {@link BridgeConnector}s that connect to other bridges.
     * 
     * @return All bound and connected bridge connectors.
     */
    public List<BridgeConnector> getConnections() {

        return Collections.unmodifiableList(connections);
    }

    /**
     * Starts up the given {@link BridgeConnector} and connects this bridge with another bridge which is defined by the connector.
     * Furthermore, the predicates of all active {@link EventHandler}s are added to the newly connected bridge.
     * That might cause the sending of {@link Event}s which weren't sent previously.
     * 
     * @param connector The bridge connector that should connect this bride with another bridge.
     * @throws BridgeConnectorException Something goes wrong while connecting the two bridges or transmitting the predicates.
     */
    public void connect(BridgeConnector connector) throws BridgeConnectorException {

        connections.add(connector);
        remoteAcceptionPredicates.put(connector, new ArrayList<EventPredicate<?>>());

        connector.start(this);

        // Add acception predicates of all handlers to remote bridge
        for (EventHandler<?> handler : handlers) {
            connector.send(new AddRemovePredicateEvent(handler.getPredicate(), true));
        }
    }

    /**
     * Shuts down the given {@link BridgeConnector} and disconnects this bridge from the bridge the connector connected it to.
     * Furthermore, the predicates of all active {@link EventHandler}s are removed from the newly disconnected bridge.
     * That might cause {@link Event}s that were sent previously not to be sent anymore.<br>
     * <br>
     * Please note that the connector on the other side must call this method on its bridge with itself once the connection is destroyed.
     * 
     * @param connector The bridge connector that should disconnect this bride from another bridge.
     * @throws BridgeConnectorException Something goes wrong while disconnecting the two bridges or transmitting the predicate removal.
     */
    public void disconnect(BridgeConnector connector) throws BridgeConnectorException {

        connector.stop();

        connections.remove(connector);
        remoteAcceptionPredicates.remove(connector);
    }

    // ----- Events -----

    /**
     * Sends the given {@link Event} to all connected bridges which have an {@link EventHandler} whose predicate allows the event.
     * If no connected bridge is interested, this method does nothing.
     * 
     * @param event The event that should be tried to send to any interested connected bridge.
     */
    public void send(Event event) {

        for (BridgeConnector connection : connections) {
            // Check whether the connected bridge is interested in the event
            if (isInteresting(connection, event)) {
                try {
                    connection.send(event);
                } catch (BridgeConnectorException e) {
                    LOGGER.error("Can't send event '" + event + "' through bridge connector", e);
                }
            }
        }
    }

    private boolean isInteresting(BridgeConnector connection, Event event) {

        for (EventPredicate<? extends Event> predicate : remoteAcceptionPredicates.get(connection)) {
            if (tryTest(predicate, event)) {
                return true;
            }
        }

        return false;
    }

    private <T extends Event> boolean tryTest(EventPredicate<T> predicate, Event event) {

        try {
            @SuppressWarnings ("unchecked")
            T castedEvent = (T) event;
            return predicate.test(castedEvent);
        } catch (ClassCastException e) {
            return false;
        }
    }

    /**
     * {@link BridgeConnector}s can call this method when they receive an event from a connected bridge.
     * The method requires the connector that received the event as an argument.
     * It calls all {@link EventHandler}s whose expected type is equal to the event's type and whose predicate accepts the event.
     * 
     * @param connection The bridge connector that received the event and likely called this method.
     * @param event The actual event object that was received by the given connector.
     *        It'll be handed over to all interested event handlers.
     */
    public void handle(BridgeConnector connection, Event event) {

        if (event instanceof AddRemovePredicateEvent) {
            AddRemovePredicateEvent predicateEvent = (AddRemovePredicateEvent) event;
            EventPredicate<?> predicate = predicateEvent.getPredicate();
            if (predicateEvent.isAdd()) {
                remoteAcceptionPredicates.get(connection).add(predicate);
            } else {
                remoteAcceptionPredicates.get(connection).remove(predicate);
            }
            return;
        }

        for (EventHandler<?> handler : handlers) {
            if (tryTest(handler.getPredicate(), event)) {
                tryHandle(handler, event);
            }
        }
    }

    private <T extends Event> void tryHandle(EventHandler<T> handler, Event event) {

        try {
            @SuppressWarnings ("unchecked")
            T castedEvent = (T) event;
            handler.handle(castedEvent);
        } catch (ClassCastException e) {}
    }

}
