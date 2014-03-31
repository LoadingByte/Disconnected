
package com.quartercode.disconnected.test.world.event;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Assert;
import org.junit.Test;
import com.quartercode.classmod.base.def.DefaultFeatureHolder;
import com.quartercode.classmod.extra.ExecutorInvocationException;
import com.quartercode.classmod.extra.FunctionExecutor;
import com.quartercode.classmod.extra.FunctionInvocation;
import com.quartercode.classmod.extra.PropertyDefinition;
import com.quartercode.classmod.extra.def.ObjectProperty;
import com.quartercode.disconnected.world.event.Event;
import com.quartercode.disconnected.world.event.EventListener;

public class EventTest {

    @Test
    public void testSend() throws ExecutorInvocationException {

        TestEvent event = new TestEvent();
        event.get(TestEvent.TEST_DATA).set("test");

        final AtomicReference<Event> receivedEvent = new AtomicReference<Event>();
        EventListener.HANDLE_EVENT.addExecutor("default", SendListener.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) throws ExecutorInvocationException {

                receivedEvent.set((Event) arguments[0]);
                return invocation.next(arguments);
            }
        });
        event.get(Event.SEND).invoke(Arrays.asList(new SendListener()));
        Assert.assertEquals("Received event", event, receivedEvent.get());
    }

    private static class TestEvent extends Event {

        public static final PropertyDefinition<String> TEST_DATA = ObjectProperty.createDefinition("testData");

    }

    private static class SendListener extends DefaultFeatureHolder implements EventListener {

    }

}
