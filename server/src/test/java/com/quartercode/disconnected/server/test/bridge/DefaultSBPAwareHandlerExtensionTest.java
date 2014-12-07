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

package com.quartercode.disconnected.server.test.bridge;

import static com.quartercode.disconnected.server.test.ExtraActions.storeArgument;
import static com.quartercode.disconnected.server.test.ExtraAssert.assertListEquals;
import static com.quartercode.disconnected.server.test.ExtraAssert.assertMapEquals;
import static com.quartercode.disconnected.server.test.ExtraMatchers.aLowLevelHandlerWithThePredicate;
import static org.junit.Assert.assertTrue;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;
import org.apache.commons.lang3.tuple.Pair;
import org.jmock.Expectations;
import org.jmock.Sequence;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import com.quartercode.disconnected.server.bridge.DefaultSBPAwareHandlerExtension;
import com.quartercode.disconnected.server.bridge.SBPAwareEventHandler;
import com.quartercode.disconnected.server.bridge.SBPAwareEventHandlerExceptionCatcher;
import com.quartercode.disconnected.server.bridge.SBPAwareHandlerExtension.ModifySBPAwareExceptionCatcherListListener;
import com.quartercode.disconnected.server.bridge.SBPAwareHandlerExtension.ModifySBPAwareHandlerListListener;
import com.quartercode.disconnected.server.bridge.SBPAwareHandlerExtension.SBPAwareHandleExceptionInterceptor;
import com.quartercode.disconnected.server.bridge.SBPAwareHandlerExtension.SBPAwareHandleInterceptor;
import com.quartercode.disconnected.server.identity.SBPIdentityService;
import com.quartercode.disconnected.server.test.bridge.DummyEvents.CallableEvent;
import com.quartercode.disconnected.server.test.bridge.DummyEvents.Event1;
import com.quartercode.disconnected.server.test.bridge.DummyEvents.Event2;
import com.quartercode.disconnected.shared.identity.ClientIdentity;
import com.quartercode.disconnected.shared.identity.SBPIdentity;
import com.quartercode.eventbridge.bridge.Bridge;
import com.quartercode.eventbridge.bridge.BridgeConnector;
import com.quartercode.eventbridge.bridge.Event;
import com.quartercode.eventbridge.bridge.EventPredicate;
import com.quartercode.eventbridge.bridge.module.LowLevelHandler;
import com.quartercode.eventbridge.bridge.module.LowLevelHandlerModule;
import com.quartercode.eventbridge.channel.ChannelInvocation;

public class DefaultSBPAwareHandlerExtensionTest {

    private static final SBPIdentity        SBP_1   = new ClientIdentity("sbp1");
    private static final SBPIdentity        SBP_2   = new ClientIdentity("sbp2");

    @Rule
    public JUnitRuleMockery                 context = new JUnitRuleMockery();

    @Mock
    private Bridge                          bridge;
    @Mock
    private BridgeConnector                 source1;
    @Mock
    private BridgeConnector                 source2;
    @Mock
    private LowLevelHandlerModule           lowLevelHandlerModule;
    @Mock
    private SBPIdentityService              sbpIdentityService;

    private DefaultSBPAwareHandlerExtension extension;

    @Before
    public void setUp() {

        extension = new DefaultSBPAwareHandlerExtension();
        extension.setIdentityService(sbpIdentityService);

        // @formatter:off
        context.checking(new Expectations() {{

            allowing(bridge).getModule(LowLevelHandlerModule.class);
                will(returnValue(lowLevelHandlerModule));

            // Initialize the SBP identities for the bridge connectors
            allowing(sbpIdentityService).getIdentity(source1);
                will(returnValue(SBP_1));
            allowing(sbpIdentityService).getIdentity(source2);
                will(returnValue(SBP_2));

        }});
        // @formatter:on

        extension.add(bridge);
    }

    @SuppressWarnings ("unchecked")
    @Test
    public void testRemove() {

        SBPAwareEventHandler<Event1> handler1 = context.mock(SBPAwareEventHandler.class, "handler1");
        SBPAwareEventHandler<Event2> handler2 = context.mock(SBPAwareEventHandler.class, "handler2");
        final EventPredicate<Event1> dummyPredicate1 = context.mock(EventPredicate.class, "dummyPredicate1");
        final EventPredicate<Event2> dummyPredicate2 = context.mock(EventPredicate.class, "dummyPredicate2");

        // @formatter:off
        context.checking(new Expectations() {{

            // Add
            oneOf(lowLevelHandlerModule).addHandler(with(aLowLevelHandlerWithThePredicate(dummyPredicate1)));
            oneOf(lowLevelHandlerModule).addHandler(with(aLowLevelHandlerWithThePredicate(dummyPredicate2)));
            // Automatic removal
            oneOf(lowLevelHandlerModule).removeHandler(with(aLowLevelHandlerWithThePredicate(dummyPredicate1)));
            oneOf(lowLevelHandlerModule).removeHandler(with(aLowLevelHandlerWithThePredicate(dummyPredicate2)));

        }});
        // @formatter:on

        extension.addHandler(handler1, dummyPredicate1);
        extension.addHandler(handler2, dummyPredicate2);

        extension.remove();
    }

    @SuppressWarnings ("unchecked")
    @Test
    public void testHandlerStorage() {

        SBPAwareEventHandler<Event1> handler1 = context.mock(SBPAwareEventHandler.class, "handler1");
        SBPAwareEventHandler<Event2> handler2 = context.mock(SBPAwareEventHandler.class, "handler2");
        SBPAwareEventHandler<Event2> handler3 = context.mock(SBPAwareEventHandler.class, "handler3");
        final EventPredicate<Event1> dummyPredicate1 = context.mock(EventPredicate.class, "dummyPredicate1");
        final EventPredicate<Event2> dummyPredicate2 = context.mock(EventPredicate.class, "dummyPredicate2");
        final EventPredicate<Event2> dummyPredicate3 = context.mock(EventPredicate.class, "dummyPredicate3");

        Pair<SBPAwareEventHandler<Event1>, EventPredicate<Event1>> pair1 = Pair.of(handler1, dummyPredicate1);
        Pair<SBPAwareEventHandler<Event2>, EventPredicate<Event2>> pair2 = Pair.of(handler2, dummyPredicate2);
        Pair<SBPAwareEventHandler<Event2>, EventPredicate<Event2>> pair3 = Pair.of(handler3, dummyPredicate3);

        // @formatter:off
        context.checking(new Expectations() {{

            final Sequence handlerListModifications = context.sequence("handlerListModifications");
            oneOf(lowLevelHandlerModule).addHandler(with(aLowLevelHandlerWithThePredicate(dummyPredicate1))); inSequence(handlerListModifications);
            oneOf(lowLevelHandlerModule).addHandler(with(aLowLevelHandlerWithThePredicate(dummyPredicate2))); inSequence(handlerListModifications);
            oneOf(lowLevelHandlerModule).addHandler(with(aLowLevelHandlerWithThePredicate(dummyPredicate3))); inSequence(handlerListModifications);
            oneOf(lowLevelHandlerModule).removeHandler(with(aLowLevelHandlerWithThePredicate(dummyPredicate2))); inSequence(handlerListModifications);

        }});
        // @formatter:on

        assertHandlerListEmpty();

        extension.removeHandler(handler1);
        assertHandlerListEmpty();

        extension.addHandler(handler1, dummyPredicate1);
        assertMapEquals("Handlers that are stored inside the extension are not correct", extension.getHandlers(), pair1);
        assertMapEquals("Handlers that are stored inside the extension changed on the second retrieval", extension.getHandlers(), pair1);

        extension.addHandler(handler2, dummyPredicate2);
        assertMapEquals("Handlers that are stored inside the extension are not correct", extension.getHandlers(), pair1, pair2);
        assertMapEquals("Handlers that are stored inside the extension changed on the second retrieval", extension.getHandlers(), pair1, pair2);

        extension.addHandler(handler3, dummyPredicate3);
        assertMapEquals("Handlers that are stored inside the extension are not correct", extension.getHandlers(), pair1, pair2, pair3);
        assertMapEquals("Handlers that are stored inside the extension changed on the second retrieval", extension.getHandlers(), pair1, pair2, pair3);

        extension.removeHandler(handler2);
        assertMapEquals("Handlers that are stored inside the extension are not correct", extension.getHandlers(), pair1, pair3);
        assertMapEquals("Handlers that are stored inside the extension changed on the second retrieval", extension.getHandlers(), pair1, pair3);
    }

    private void assertHandlerListEmpty() {

        assertTrue("There are handlers stored inside the extension although none were added", extension.getHandlers().isEmpty());
        assertTrue("Handlers that are stored inside the extension changed on the second retrieval", extension.getHandlers().isEmpty());
    }

    @SuppressWarnings ("unchecked")
    @Test
    public void testHandlerStorageListeners() {

        final SBPAwareEventHandler<Event1> handler = context.mock(SBPAwareEventHandler.class, "handler");
        final EventPredicate<Event1> dummyPredicate = context.mock(EventPredicate.class, "dummyPredicate");
        final ModifySBPAwareHandlerListListener listener = context.mock(ModifySBPAwareHandlerListListener.class);

        // @formatter:off
        context.checking(new Expectations() {{

            allowing(lowLevelHandlerModule).addHandler(with(any(LowLevelHandler.class)));
            allowing(lowLevelHandlerModule).removeHandler(with(any(LowLevelHandler.class)));

            final Sequence listenerCalls = context.sequence("listenerCalls");
            oneOf(listener).onAddHandler(handler, dummyPredicate, extension); inSequence(listenerCalls);
            oneOf(listener).onRemoveHandler(handler, dummyPredicate, extension); inSequence(listenerCalls);

        }});
        // @formatter:on

        // Calls with listener
        extension.addModifyHandlerListListener(listener);
        extension.addHandler(handler, dummyPredicate);
        extension.removeHandler(handler);

        // Calls without listener
        extension.removeModifyHandlerListListener(listener);
        extension.addHandler(handler, dummyPredicate);
        extension.removeHandler(handler);
    }

    @SuppressWarnings ("unchecked")
    @Test
    public void testCallHandler() {

        final Event1 event1 = new Event1();
        final Event2 event2 = new Event2();

        final SBPAwareEventHandler<Event> handler = context.mock(SBPAwareEventHandler.class, "handler");
        final EventPredicate<Event> dummyPredicate = context.mock(EventPredicate.class, "dummyPredicate");

        final SBPAwareHandleInterceptor interceptor = context.mock(SBPAwareHandleInterceptor.class);
        extension.getHandleChannel().addInterceptor(new DummySBPAwareHandleInterceptor(interceptor), 1);

        final Mutable<LowLevelHandler> lowLevelHandler = new MutableObject<>();

        // @formatter:off
        context.checking(new Expectations() {{

            // Expect the low-level handler to be added
            oneOf(lowLevelHandlerModule).addHandler(with(aLowLevelHandlerWithThePredicate(dummyPredicate)));
                will(storeArgument(0).in(lowLevelHandler));

            final Sequence handleChain = context.sequence("handleChain");
            // Event 1 fired by SBP 1
            oneOf(interceptor).handle(with(any(ChannelInvocation.class)), with(event1), with(source1), with(SBP_1), with(handler)); inSequence(handleChain);
            oneOf(handler).handle(event1, SBP_1); inSequence(handleChain);
            // Event 2 fired by SBP 2
            oneOf(interceptor).handle(with(any(ChannelInvocation.class)), with(event2), with(source2), with(SBP_2), with(handler)); inSequence(handleChain);
            oneOf(handler).handle(event2, SBP_2); inSequence(handleChain);

        }});
        // @formatter:on

        extension.addHandler(handler, dummyPredicate);

        lowLevelHandler.getValue().handle(event1, source1);
        lowLevelHandler.getValue().handle(event2, source2);
    }

    @SuppressWarnings ("unchecked")
    @Test
    public void testCallHandlerWithPredicate() {

        final Event1 regularEvent = new Event1();
        final Event2 otherEvent = new Event2();

        final SBPAwareEventHandler<Event> handler = context.mock(SBPAwareEventHandler.class, "handler");
        final EventPredicate<Event> predicate = context.mock(EventPredicate.class, "predicate");

        final SBPAwareHandleInterceptor interceptor = context.mock(SBPAwareHandleInterceptor.class);
        extension.getHandleChannel().addInterceptor(new DummySBPAwareHandleInterceptor(interceptor), 1);

        final Mutable<LowLevelHandler> lowLevelHandler = new MutableObject<>();

        // @formatter:off
        context.checking(new Expectations() {{

            // Predicate
            allowing(predicate).test(regularEvent);
                will(returnValue(true));
            allowing(predicate).test(otherEvent);
                will(returnValue(false));

            // Expect the low-level handler to be added
            oneOf(lowLevelHandlerModule).addHandler(with(aLowLevelHandlerWithThePredicate(predicate)));
                will(storeArgument(0).in(lowLevelHandler));

            final Sequence handleChain = context.sequence("handleChain");
            // Regular event
            oneOf(interceptor).handle(with(any(ChannelInvocation.class)), with(regularEvent), with(source1), with(SBP_1), with(handler)); inSequence(handleChain);
            oneOf(handler).handle(regularEvent, SBP_1); inSequence(handleChain);
            // Other event
            // Expect the unwanted event to be invoked since the predicate is not tested by the StandardHandlerModule
            // In fact, the predicate is tested by the LowLevelHandlerModule
            oneOf(interceptor).handle(with(any(ChannelInvocation.class)), with(otherEvent), with(source1), with(SBP_1), with(handler)); inSequence(handleChain);
            oneOf(handler).handle(otherEvent, SBP_1); inSequence(handleChain);

        }});
        // @formatter:on

        extension.addHandler(handler, predicate);

        lowLevelHandler.getValue().handle(regularEvent, source1);
        lowLevelHandler.getValue().handle(otherEvent, source1);
    }

    @SuppressWarnings ("unchecked")
    @Test
    public void testCallHandlerWrongTypeInPredicate() {

        final EventPredicate<Event> predicate = context.mock(EventPredicate.class);
        final SBPAwareEventHandler<CallableEvent> handler = new SBPAwareEventHandler<CallableEvent>() {

            @Override
            public void handle(CallableEvent event, SBPIdentity sender) {

                event.call();
            }

        };

        final Mutable<LowLevelHandler> lowLevelHandler = new MutableObject<>();

        // @formatter:off
        context.checking(new Expectations() {{

            oneOf(lowLevelHandlerModule).addHandler(with(aLowLevelHandlerWithThePredicate(predicate)));
                will(storeArgument(0).in(lowLevelHandler));

        }});
        // @formatter:on

        extension.addHandler(handler, predicate);

        lowLevelHandler.getValue().handle(new Event1(), source1);
    }

    @Test
    public void testExceptionCatcherStorage() {

        final SBPAwareEventHandlerExceptionCatcher catcher1 = context.mock(SBPAwareEventHandlerExceptionCatcher.class, "catcher1");
        final SBPAwareEventHandlerExceptionCatcher catcher2 = context.mock(SBPAwareEventHandlerExceptionCatcher.class, "catcher2");
        final SBPAwareEventHandlerExceptionCatcher catcher3 = context.mock(SBPAwareEventHandlerExceptionCatcher.class, "catcher3");

        assertExceptionCatcherListEmpty();

        extension.removeExceptionCatcher(catcher1);
        assertExceptionCatcherListEmpty();

        extension.addExceptionCatcher(catcher1);
        assertListEquals("Exception catchers that are stored inside the extension are not correct", extension.getExceptionCatchers(), catcher1);
        assertListEquals("Exception catchers that are stored inside the extension changed on the second retrieval", extension.getExceptionCatchers(), catcher1);

        extension.addExceptionCatcher(catcher2);
        assertListEquals("Exception catchers that are stored inside the extension are not correct", extension.getExceptionCatchers(), catcher1, catcher2);
        assertListEquals("Exception catchers that are stored inside the extension changed on the second retrieval", extension.getExceptionCatchers(), catcher1, catcher2);

        extension.addExceptionCatcher(catcher3);
        assertListEquals("Exception catchers that are stored inside the extension are not correct", extension.getExceptionCatchers(), catcher1, catcher2, catcher3);
        assertListEquals("Exception catchers that are stored inside the extension changed on the second retrieval", extension.getExceptionCatchers(), catcher1, catcher2, catcher3);

        extension.removeExceptionCatcher(catcher2);
        assertListEquals("Exception catchers that are stored inside the extension are not correct", extension.getExceptionCatchers(), catcher1, catcher3);
        assertListEquals("Exception catchers that are stored inside the extension changed on the second retrieval", extension.getExceptionCatchers(), catcher1, catcher3);
    }

    private void assertExceptionCatcherListEmpty() {

        assertTrue("There are exception catchers stored inside the extension although none were added", extension.getExceptionCatchers().isEmpty());
        assertTrue("Exception catchers that are stored inside the extension changed on the second retrieval", extension.getExceptionCatchers().isEmpty());
    }

    @Test
    public void testExceptionCatcherStorageListeners() {

        final SBPAwareEventHandlerExceptionCatcher catcher = context.mock(SBPAwareEventHandlerExceptionCatcher.class, "catcher");
        final ModifySBPAwareExceptionCatcherListListener listener = context.mock(ModifySBPAwareExceptionCatcherListListener.class);

        // @formatter:off
        context.checking(new Expectations() {{

            final Sequence listenerCalls = context.sequence("listenerCalls");
            oneOf(listener).onAddCatcher(catcher, extension); inSequence(listenerCalls);
            oneOf(listener).onRemoveCatcher(catcher, extension); inSequence(listenerCalls);

        }});
        // @formatter:on

        // Calls with listener
        extension.addModifyExceptionCatcherListListener(listener);
        extension.addExceptionCatcher(catcher);
        extension.removeExceptionCatcher(catcher);

        // Calls without listener
        extension.removeModifyExceptionCatcherListListener(listener);
        extension.addExceptionCatcher(catcher);
        extension.removeExceptionCatcher(catcher);
    }

    @SuppressWarnings ("unchecked")
    @Test
    public void testExceptionInHandler() {

        final Event1 regularEvent = new Event1();
        final Event2 exceptionEvent = new Event2();

        final SBPAwareEventHandler<Event> handler = context.mock(SBPAwareEventHandler.class, "handler");
        final EventPredicate<Event> predicate = context.mock(EventPredicate.class, "predicate");

        final SBPAwareEventHandlerExceptionCatcher catcher1 = context.mock(SBPAwareEventHandlerExceptionCatcher.class, "exceptionCatcher1");
        final SBPAwareEventHandlerExceptionCatcher catcher2 = context.mock(SBPAwareEventHandlerExceptionCatcher.class, "exceptionCatcher2");
        extension.addExceptionCatcher(catcher1);
        extension.addExceptionCatcher(catcher2);

        final SBPAwareHandleExceptionInterceptor interceptor = context.mock(SBPAwareHandleExceptionInterceptor.class);
        extension.getExceptionChannel().addInterceptor(new DummySBPAwareHandleExceptionInterceptor(interceptor), 1);

        final Mutable<LowLevelHandler> lowLevelHandler = new MutableObject<>();

        // @formatter:off
        context.checking(new Expectations() {{

            allowing(predicate).test(with(any(Event.class)));
                will(returnValue(true));

            oneOf(lowLevelHandlerModule).addHandler(with(aLowLevelHandlerWithThePredicate(predicate)));
                will(storeArgument(0).in(lowLevelHandler));

            final Sequence handleChain = context.sequence("handleChain");
            // Regular event
            oneOf(handler).handle(regularEvent, SBP_1); inSequence(handleChain);
            // Exception event
            final RuntimeException exception = new RuntimeException();
            oneOf(handler).handle(exceptionEvent, SBP_1); inSequence(handleChain);
                will(throwException(exception));
            oneOf(interceptor).handle(with(any(ChannelInvocation.class)), with(exception), with(handler), with(exceptionEvent), with(source1)); inSequence(handleChain);
            oneOf(catcher1).handle(exception, handler, exceptionEvent, source1); inSequence(handleChain);
            oneOf(catcher2).handle(exception, handler, exceptionEvent, source1); inSequence(handleChain);

        }});
        // @formatter:on

        extension.addHandler(handler, predicate);

        lowLevelHandler.getValue().handle(regularEvent, source1);
        lowLevelHandler.getValue().handle(exceptionEvent, source1);
    }

    private static class DummySBPAwareHandleInterceptor implements SBPAwareHandleInterceptor {

        private final SBPAwareHandleInterceptor dummy;

        private DummySBPAwareHandleInterceptor(SBPAwareHandleInterceptor dummy) {

            this.dummy = dummy;
        }

        @Override
        public void handle(ChannelInvocation<SBPAwareHandleInterceptor> invocation, Event event, BridgeConnector source, SBPIdentity sender, SBPAwareEventHandler<?> handler) {

            dummy.handle(invocation, event, source, sender, handler);
            invocation.next().handle(invocation, event, source, sender, handler);
        }

    }

    private static class DummySBPAwareHandleExceptionInterceptor implements SBPAwareHandleExceptionInterceptor {

        private final SBPAwareHandleExceptionInterceptor dummy;

        private DummySBPAwareHandleExceptionInterceptor(SBPAwareHandleExceptionInterceptor dummy) {

            this.dummy = dummy;
        }

        @Override
        public void handle(ChannelInvocation<SBPAwareHandleExceptionInterceptor> invocation, RuntimeException exception, SBPAwareEventHandler<?> handler, Event event, BridgeConnector source) {

            dummy.handle(invocation, exception, handler, event, source);
            invocation.next().handle(invocation, exception, handler, event, source);
        }

    }

}
