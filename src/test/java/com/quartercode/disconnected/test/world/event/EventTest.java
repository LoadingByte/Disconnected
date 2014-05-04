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

package com.quartercode.disconnected.test.world.event;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Assert;
import org.junit.Test;
import com.quartercode.classmod.base.def.DefaultFeatureHolder;
import com.quartercode.classmod.extra.FunctionExecutor;
import com.quartercode.classmod.extra.FunctionInvocation;
import com.quartercode.classmod.extra.PropertyDefinition;
import com.quartercode.classmod.extra.def.ObjectProperty;
import com.quartercode.disconnected.world.event.Event;
import com.quartercode.disconnected.world.event.EventListener;

public class EventTest {

    @Test
    public void testSend() {

        TestEvent event = new TestEvent();
        event.get(TestEvent.TEST_DATA).set("test");

        final AtomicReference<Event> receivedEvent = new AtomicReference<>();
        EventListener.HANDLE_EVENT.addExecutor("default", SendListener.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

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
