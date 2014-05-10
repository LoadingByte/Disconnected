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

import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Assert;
import org.junit.Test;
import com.quartercode.disconnected.bridge.AbstractEventHandler;
import com.quartercode.disconnected.bridge.Event;
import com.quartercode.disconnected.bridge.EventHandler;
import com.quartercode.disconnected.bridge.EventPredicate;
import com.quartercode.disconnected.bridge.EventUtils;
import com.quartercode.disconnected.bridge.predicate.TypePredicate;

public class EventUtilsTest {

    @SuppressWarnings ("serial")
    @Test
    public void testTryTest() {

        final AtomicBoolean predicateInvoked = new AtomicBoolean();
        EventPredicate<Event2> predicate = new EventPredicate<Event2>() {

            @Override
            public boolean test(Event2 event) {

                predicateInvoked.set(true);
                return event instanceof Event3;
            }

        };

        checkTest(false, false, predicateInvoked, EventUtils.tryTest(predicate, new Event1()));
        checkTest(true, false, predicateInvoked, EventUtils.tryTest(predicate, new Event2()));
        checkTest(true, true, predicateInvoked, EventUtils.tryTest(predicate, new Event3()));
    }

    private void checkTest(boolean expectedPredicateInvoked, boolean expectedTestResult, AtomicBoolean predicateInvoked, boolean testResult) {

        Assert.assertTrue("Test predicate was" + (expectedPredicateInvoked ? "n't" : "") + " invoked", predicateInvoked.get() == expectedPredicateInvoked);
        Assert.assertTrue("Test result is " + testResult + " although it should be " + expectedTestResult, testResult == expectedTestResult);

        // Reset
        predicateInvoked.set(false);
    }

    @Test
    public void testTryHandle() {

        final AtomicBoolean handlerInvoked = new AtomicBoolean();
        EventHandler<Event2> handler = new AbstractEventHandler<Event2>(new TypePredicate<Event2>(Event3.class)) {

            @Override
            public void handle(Event2 event) {

                handlerInvoked.set(true);
            }

        };

        EventUtils.tryHandle(handler, new Event1(), false);
        checkHandle(false, handlerInvoked);
        EventUtils.tryHandle(handler, new Event2(), false);
        checkHandle(true, handlerInvoked);
        EventUtils.tryHandle(handler, new Event3(), false);
        checkHandle(true, handlerInvoked);

        EventUtils.tryHandle(handler, new Event1(), true);
        checkHandle(false, handlerInvoked);
        EventUtils.tryHandle(handler, new Event2(), true);
        checkHandle(false, handlerInvoked);
        EventUtils.tryHandle(handler, new Event3(), true);
        checkHandle(true, handlerInvoked);
    }

    private void checkHandle(boolean expectedHandlerInvoked, AtomicBoolean handlerInvoked) {

        Assert.assertTrue("Test handler was" + (expectedHandlerInvoked ? "n't" : "") + " invoked", handlerInvoked.get() == expectedHandlerInvoked);

        // Reset
        handlerInvoked.set(false);
    }

    @SuppressWarnings ("serial")
    private static class Event1 implements Event {

    }

    @SuppressWarnings ("serial")
    private static class Event2 implements Event {

    }

    @SuppressWarnings ("serial")
    private static class Event3 extends Event2 {

    }

}
