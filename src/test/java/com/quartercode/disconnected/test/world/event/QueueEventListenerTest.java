
package com.quartercode.disconnected.test.world.event;

import java.util.Arrays;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.quartercode.classmod.base.FeatureDefinition;
import com.quartercode.classmod.extra.ExecutorInvocationException;
import com.quartercode.classmod.extra.def.ObjectProperty;
import com.quartercode.disconnected.world.event.Event;
import com.quartercode.disconnected.world.event.EventListener;
import com.quartercode.disconnected.world.event.EventMatcher;
import com.quartercode.disconnected.world.event.QueueEventListener;
import com.quartercode.disconnected.world.event.TrueEventMatcher;
import com.quartercode.disconnected.world.event.TypeEventMatcher;

public class QueueEventListenerTest {

    private QueueEventListener listener;

    @Before
    public void setUp() {

        listener = new QueueEventListener();
    }

    @Test
    public void testNextPoll() throws ExecutorInvocationException {

        TestEvent event = sendTestEvent("testString", listener);

        // Note that we rely on the TrueEventMatcher here
        Assert.assertEquals("Queued event", event, listener.get(QueueEventListener.NEXT_EVENT).invoke(new TrueEventMatcher()));
        Assert.assertNull("Next event after all events were polled", listener.get(QueueEventListener.NEXT_EVENT).invoke(new TrueEventMatcher()));
    }

    @Test
    public void testNextSelect() throws ExecutorInvocationException {

        TestEvent event1 = sendTestEvent("testString1", listener);
        TestEvent event2 = sendTestEvent("testString2", listener);
        TestEvent event3 = sendTestEvent("testString3", listener);

        Assert.assertEquals("First queued event with 'testString2'", event2, listener.get(QueueEventListener.NEXT_EVENT).invoke(new TestDataEventMatcher("testString2")));
        Assert.assertEquals("Second queued event", event1, listener.get(QueueEventListener.NEXT_EVENT).invoke(new TrueEventMatcher()));
        Assert.assertEquals("Third queued event", event3, listener.get(QueueEventListener.NEXT_EVENT).invoke(new TrueEventMatcher()));
    }

    private TestEvent sendTestEvent(String testData, EventListener listener) throws ExecutorInvocationException {

        TestEvent event = new TestEvent();
        event.get(TestEvent.TEST_DATA).set(testData);
        event.get(Event.SEND).invoke(Arrays.asList(listener));
        return event;
    }

    private static class TestEvent extends Event {

        public static final FeatureDefinition<ObjectProperty<String>> TEST_DATA = ObjectProperty.createDefinition("testData");

    }

    private static class TestDataEventMatcher implements EventMatcher {

        private final String testDataValue;

        public TestDataEventMatcher(String testDataValue) {

            this.testDataValue = testDataValue;
        }

        @Override
        public boolean matches(Event event) throws ExecutorInvocationException {

            return new TypeEventMatcher(TestEvent.class).matches(event) && event.get(TestEvent.TEST_DATA).get().equals(testDataValue);
        }

    }

}
