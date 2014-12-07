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

package com.quartercode.disconnected.shared.test.bridge;

import static com.quartercode.disconnected.shared.test.ExtraActions.storeArgument;
import static org.junit.Assert.assertEquals;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jmock.Expectations;
import org.jmock.Sequence;
import org.jmock.api.Action;
import org.jmock.api.Invocation;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.action.CustomAction;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import com.quartercode.disconnected.shared.bridge.DefaultHandleInvocationProviderExtension;
import com.quartercode.disconnected.shared.test.bridge.DummyEvents.Event1;
import com.quartercode.disconnected.shared.util.RunnableInvocationProvider;
import com.quartercode.eventbridge.bridge.Bridge;
import com.quartercode.eventbridge.bridge.BridgeConnector;
import com.quartercode.eventbridge.bridge.Event;
import com.quartercode.eventbridge.bridge.module.HandlerModule;
import com.quartercode.eventbridge.bridge.module.HandlerModule.HandleInterceptor;
import com.quartercode.eventbridge.channel.Channel;
import com.quartercode.eventbridge.channel.ChannelInvocation;
import com.quartercode.eventbridge.def.channel.DefaultChannel;

public class DefaultHandleInvocationProviderExtensionTest {

    private static Action invokeRunnable() {

        return new InvokeRunnableAction();
    }

    @Rule
    public JUnitRuleMockery                          context         = new JUnitRuleMockery();

    @Mock
    private Bridge                                   bridge;
    @Mock
    private HandlerModule                            handlerModule;
    @Mock
    private Channel<HandleInterceptor>               handlerModuleChannel;

    private DefaultHandleInvocationProviderExtension extension;
    private final Mutable<HandleInterceptor>         hookInterceptor = new MutableObject<>();

    @Before
    public void setUp() {

        extension = new DefaultHandleInvocationProviderExtension();

        // @formatter:off
        context.checking(new Expectations() {{

            allowing(bridge).getModule(HandlerModule.class);
                will(returnValue(handlerModule));
            allowing(handlerModule).getChannel();
                will(returnValue(handlerModuleChannel));

            // The module should add a hook to the handler module's channel
            allowing(handlerModuleChannel).addInterceptor(with(any(HandleInterceptor.class)), with(1000));
                will(storeArgument(0).in(hookInterceptor));

        }});
        // @formatter:on

        extension.add(bridge);
    }

    @Test
    public void testRemove() {

        // @formatter:off
        context.checking(new Expectations() {{

            // The module should remove its hook from the handler module's channel
            oneOf(handlerModuleChannel).removeInterceptor(with(any(HandleInterceptor.class)));

        }});
        // @formatter:on

        extension.remove();
    }

    @Test
    public void testGetSetInvocationProvider() {

        RunnableInvocationProvider invocationProvider = context.mock(RunnableInvocationProvider.class);

        extension.setInvocationProvider(invocationProvider);
        assertEquals("The extension's invocation provider", invocationProvider, extension.getInvocationProvider());
    }

    private void invokeHook(Event event, BridgeConnector source, HandleInterceptor lastInterceptor) {

        // Create a dummy channel for the hook interceptor
        Channel<HandleInterceptor> dummyChannel = new DefaultChannel<>(HandleInterceptor.class);
        dummyChannel.addInterceptor(hookInterceptor.getValue(), Integer.MAX_VALUE);

        // Add the custom interceptor
        dummyChannel.addInterceptor(lastInterceptor, 0);

        // Invoke the hook
        ChannelInvocation<HandleInterceptor> dummyChannelInvocation = dummyChannel.invoke();
        dummyChannelInvocation.next().handle(dummyChannelInvocation, event, source);
    }

    @SuppressWarnings ("unchecked")
    @Test
    public void testHandleWithNoInvocationProvider() {

        final Event event = new Event1();
        final BridgeConnector source = context.mock(BridgeConnector.class);

        final HandleInterceptor interceptor = context.mock(HandleInterceptor.class);

        // @formatter:off
        context.checking(new Expectations() {{

            oneOf(interceptor).handle(with(any(ChannelInvocation.class)), with(event), with(source));

        }});
        // @formatter:on

        extension.setInvocationProvider(null);
        invokeHook(event, source, interceptor);
    }

    @SuppressWarnings ("unchecked")
    @Test
    public void testHandleWithInvocationProvider() {

        final Event event = new Event1();
        final BridgeConnector source = context.mock(BridgeConnector.class);

        final RunnableInvocationProvider invocationProvider = context.mock(RunnableInvocationProvider.class);
        final HandleInterceptor interceptor = context.mock(HandleInterceptor.class);

        // @formatter:off
        context.checking(new Expectations() {{

            final Sequence calls = context.sequence("calls");
            oneOf(invocationProvider).invoke(with(any(Runnable.class))); inSequence(calls);
                will(invokeRunnable());
            oneOf(interceptor).handle(with(any(ChannelInvocation.class)), with(event), with(source)); inSequence(calls);

        }});
        // @formatter:on

        extension.setInvocationProvider(invocationProvider);
        invokeHook(event, source, interceptor);
    }

    private static class InvokeRunnableAction extends CustomAction {

        public InvokeRunnableAction() {

            super("invokes the provided runnable");
        }

        @Override
        public Object invoke(Invocation invocation) {

            Runnable runnable = (Runnable) invocation.getParameter(0);
            runnable.run();
            return null;
        }

    }

}
