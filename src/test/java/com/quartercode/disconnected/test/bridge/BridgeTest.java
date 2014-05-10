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

package com.quartercode.disconnected.test.bridge;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.quartercode.disconnected.bridge.AbstractBridgeConnector;
import com.quartercode.disconnected.bridge.AbstractEventHandler;
import com.quartercode.disconnected.bridge.AddRemovePredicateEvent;
import com.quartercode.disconnected.bridge.Bridge;
import com.quartercode.disconnected.bridge.BridgeConnector;
import com.quartercode.disconnected.bridge.BridgeConnectorException;
import com.quartercode.disconnected.bridge.Event;
import com.quartercode.disconnected.bridge.EventPredicate;
import com.quartercode.disconnected.bridge.predicate.TruePredicate;

public class BridgeTest {

    private Bridge bridge;

    @Before
    public void setUp() {

        bridge = new Bridge();
    }

    @Test
    public void testSendToLocalHandler() {

        final List<Event1> handler1Calls = new ArrayList<>();
        bridge.addHandler(new AbstractEventHandler<Event1>(new TruePredicate<Event1>()) {

            @Override
            public void handle(Event1 event) {

                handler1Calls.add(event);
            }

        });

        final List<Event2> handler2Calls = new ArrayList<>();
        bridge.addHandler(new AbstractEventHandler<Event2>(new TruePredicate<Event2>()) {

            @Override
            public void handle(Event2 event) {

                handler2Calls.add(event);
            }

        });

        Event event1 = new Event1();
        Event event2 = new Event2();
        Event event3 = new Event2();
        Event event4 = new Event1();
        Event event5 = new Event2();
        bridge.send(event1);
        bridge.send(event2);
        bridge.send(event3);
        bridge.send(event4);
        bridge.send(event5);

        Assert.assertEquals("Events that were handled by handler 1", new ArrayList<>(Arrays.asList(event1, event4)), handler1Calls);
        Assert.assertEquals("Events that were handled by handler 2", new ArrayList<>(Arrays.asList(event2, event3, event5)), handler2Calls);
    }

    @Test
    public void testSendToConnector() throws BridgeConnectorException {

        final List<Event> connectorCalls = new ArrayList<>();
        BridgeConnector connector = new AbstractBridgeConnector() {

            @Override
            public void send(Event event) {

                connectorCalls.add(event);
            }

        };
        bridge.connect(connector);

        // Send all events through the bridge
        bridge.handle(connector, new AddRemovePredicateEvent(new TruePredicate<>(), true));

        List<Event> events = new ArrayList<>(Arrays.asList(new Event1(), new Event2(), new Event2(), new Event1(), new Event2()));
        for (Event event : events) {
            bridge.send(event);
        }

        Assert.assertEquals("Events that were sent through the dummy connector", events, connectorCalls);
    }

    @SuppressWarnings ("serial")
    @Test
    public void testSendToConnectorWithPredicate() throws BridgeConnectorException {

        final List<Event> connectorCalls = new ArrayList<>();
        BridgeConnector connector = new AbstractBridgeConnector() {

            @Override
            public void send(Event event) {

                connectorCalls.add(event);
            }

        };
        bridge.connect(connector);

        // Send all events through the bridge
        bridge.handle(connector, new AddRemovePredicateEvent(new EventPredicate<Event>() {

            @Override
            public boolean test(Event event) {

                return event instanceof Event1;
            }

        }, true));

        List<Event> events = new ArrayList<>(Arrays.asList(new Event1(), new Event2(), new Event2(), new Event1(), new Event2()));
        for (Event event : events) {
            bridge.send(event);
        }

        Assert.assertEquals("Events that were sent through the dummy connector", new ArrayList<>(Arrays.asList(events.get(0), events.get(3))), connectorCalls);
    }

    @Test
    public void testHandle() {

        final List<Event1> handler1Calls = new ArrayList<>();
        bridge.addHandler(new AbstractEventHandler<Event1>(new TruePredicate<Event1>()) {

            @Override
            public void handle(Event1 event) {

                handler1Calls.add(event);
            }

        });

        final List<Event2> handler2Calls = new ArrayList<>();
        bridge.addHandler(new AbstractEventHandler<Event2>(new TruePredicate<Event2>()) {

            @Override
            public void handle(Event2 event) {

                handler2Calls.add(event);
            }

        });

        Event event1 = new Event1();
        Event event2 = new Event2();
        Event event3 = new Event2();
        Event event4 = new Event1();
        Event event5 = new Event2();
        bridge.handle(null, event1);
        bridge.handle(null, event2);
        bridge.handle(null, event3);
        bridge.handle(null, event4);
        bridge.handle(null, event5);

        Assert.assertEquals("Events that were handled by handler 1", new ArrayList<>(Arrays.asList(event1, event4)), handler1Calls);
        Assert.assertEquals("Events that were handled by handler 2", new ArrayList<>(Arrays.asList(event2, event3, event5)), handler2Calls);
    }

    @SuppressWarnings ("serial")
    private static class Event1 implements Event {

    }

    @SuppressWarnings ("serial")
    private static class Event2 implements Event {

    }

}
