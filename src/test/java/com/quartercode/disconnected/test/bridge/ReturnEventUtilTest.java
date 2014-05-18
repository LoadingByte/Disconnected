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
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.quartercode.disconnected.bridge.AbstractEventHandler;
import com.quartercode.disconnected.bridge.Bridge;
import com.quartercode.disconnected.bridge.EventHandler;
import com.quartercode.disconnected.bridge.ReturnEventUtil;
import com.quartercode.disconnected.bridge.ReturnEventUtil.BooleanCloseChecker;
import com.quartercode.disconnected.bridge.ReturnEventUtil.Return;
import com.quartercode.disconnected.bridge.ReturnEventUtil.Returnable;

public class ReturnEventUtilTest {

    private Bridge bridge;

    @Before
    public void setUp() {

        bridge = new Bridge();
    }

    @Test
    public void testSend() {

        final AtomicReference<Event1> lastHandlerCall = new AtomicReference<>();
        bridge.addHandler(new AbstractEventHandler<Event1>(Event1.class) {

            @Override
            public void handle(Event1 event) {

                lastHandlerCall.set(event);
            }

        });

        Event1 event = new Event1("teststring");
        ReturnEventUtil.send(bridge, event, new AbstractEventHandler<Event2>(Event2.class) {

            @Override
            public void handle(Event2 event) {

                // Empty
            }

        });

        // Remove the "nextReturnId" to make the handled event comparable
        if (lastHandlerCall.get() != null) {
            lastHandlerCall.set(lastHandlerCall.get().withNextReturnId(null));
        }

        Assert.assertEquals("Event that was handled by the 'responding' handler (sent by ReturnEventUtil)", event, lastHandlerCall.get());
    }

    @Test
    public void testSendAndRespond() {

        // Receiving handler
        final AtomicReference<Event2> lastHandlerCall = new AtomicReference<>();
        EventHandler<Event2> handler = new AbstractEventHandler<Event2>(Event2.class) {

            @Override
            public void handle(Event2 event) {

                lastHandlerCall.set(event);
            }

        };

        // Responding handler
        final AtomicReference<Event2> sentResponse = new AtomicReference<>();
        bridge.addHandler(new AbstractEventHandler<Event1>(Event1.class) {

            @Override
            public void handle(Event1 event) {

                sentResponse.set(new Event2(event.getNextReturnId()));
                bridge.send(sentResponse.get());
            }

        });

        ReturnEventUtil.send(bridge, new Event1("teststring"), handler);

        Event2 response = lastHandlerCall.get();
        Assert.assertNotNull("No event was handled by the return-awaiting handler", response);
        Assert.assertEquals("The event that was handled by the return-awaiting handler", sentResponse.get(), response);
        Assert.assertTrue("Temporary handler for receiving the return event wasn't removed", bridge.getHandlers().size() == 1);
    }

    @Test
    public void testSendAndMultiRespond() {

        // Receiving handler
        final List<Event2> handlerCalls = new ArrayList<>();
        final AtomicBoolean closeFlag = new AtomicBoolean();
        final EventHandler<Event2> handler = new AbstractEventHandler<Event2>(Event2.class) {

            @Override
            public void handle(Event2 event) {

                handlerCalls.add(event);

                if (handlerCalls.size() == 3) {
                    closeFlag.set(true);
                }
            }

        };

        // Responding handler
        final List<Event2> sentResponses = new ArrayList<>();
        bridge.addHandler(new AbstractEventHandler<Event1>(Event1.class) {

            @Override
            public void handle(Event1 event) {

                for (int counter = 0; counter < 3; counter++) {
                    Assert.assertTrue("Temporary handler for receiving the return event was removed before it should be", bridge.getHandlers().size() == 2);
                    Event2 response = new Event2(event.getNextReturnId());
                    sentResponses.add(response);
                    bridge.send(response);
                }
            }

        });

        ReturnEventUtil.send(bridge, new Event1("teststring"), handler, new BooleanCloseChecker(closeFlag));

        Assert.assertTrue("The return-awaiting handler didn't receive any return events", handlerCalls.size() == 3);
        Assert.assertEquals("The events that were handled by the return-awaiting handler", sentResponses, handlerCalls);
        Assert.assertTrue("Temporary handler for receiving the return event wasn't removed", bridge.getHandlers().size() == 1);
    }

    @SuppressWarnings ("serial")
    private static class Event1 implements Returnable {

        private final String testData;
        private final String nextReturnId;

        public Event1(String testData) {

            this.testData = testData;
            nextReturnId = null;
        }

        private Event1(String testData, String nextReturnId) {

            this.testData = testData;
            this.nextReturnId = nextReturnId;
        }

        public String getTestData() {

            return testData;
        }

        @Override
        public String getNextReturnId() {

            return nextReturnId;
        }

        @Override
        public Event1 withNextReturnId(String nextReturnId) {

            return new Event1(testData, nextReturnId);
        }

        @Override
        public boolean equals(Object obj) {

            return obj instanceof Event1 && testData.equals( ((Event1) obj).getTestData());
        }

    }

    @SuppressWarnings ("serial")
    private static class Event2 implements Return {

        private final String returnId;

        public Event2(String returnId) {

            this.returnId = returnId;
        }

        @Override
        public String getReturnId() {

            return returnId;
        }

    }

}
