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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.quartercode.disconnected.bridge.QueueEventHandler;
import com.quartercode.disconnected.test.bridge.DummyEvents.Event1;
import com.quartercode.disconnected.test.bridge.DummyEvents.Event2;
import com.quartercode.eventbridge.bridge.Event;
import com.quartercode.eventbridge.bridge.EventPredicate;
import com.quartercode.eventbridge.extra.predicate.TypePredicate;

public class QueueEventHandlerTest {

    private QueueEventHandler<Event> handler;

    @Before
    public void setUp() {

        handler = new QueueEventHandler<>();
    }

    @Test
    public void testNext() {

        Event[] events = { new Event1(), new Event2(), new Event1(), new Event2(), new Event2() };

        for (Event event : events) {
            handler.handle(event);
        }

        for (Event event : events) {
            Assert.assertEquals("Polled event", event, handler.next());
        }
        Assert.assertNull("Next event after all events were polled", handler.next());
    }

    @Test
    public void testNextWithMatcher() {

        Event[] events = { new Event1(), new Event2(), new Event1(), new Event2(), new Event2() };
        Event[] pollOrder1 = { events[0], events[2] };
        Event[] pollOrder2 = { events[1], events[3], events[4] };

        for (Event event : events) {
            handler.handle(event);
        }

        EventPredicate<?> currentPredicate = new TypePredicate<>(Event1.class);
        for (Event event : pollOrder1) {
            Assert.assertEquals("Polled event", event, handler.next(currentPredicate));
        }
        Assert.assertNull("Next event after all events of type 1 were polled", handler.next(currentPredicate));

        currentPredicate = new TypePredicate<>(Event2.class);
        for (Event event : pollOrder2) {
            Assert.assertEquals("Polled event", event, handler.next(currentPredicate));
        }
        Assert.assertNull("Next event after all events of type 2 were polled", handler.next(currentPredicate));

        Assert.assertNull("Next event after all events of all types were polled", handler.next());
    }

}
