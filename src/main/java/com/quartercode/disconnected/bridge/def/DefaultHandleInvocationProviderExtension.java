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

package com.quartercode.disconnected.bridge.def;

import com.quartercode.disconnected.bridge.HandleInvocationProviderExtension;
import com.quartercode.disconnected.util.RunnableInvocationProvider;
import com.quartercode.eventbridge.basic.AbstractBridgeModule;
import com.quartercode.eventbridge.bridge.Bridge;
import com.quartercode.eventbridge.bridge.BridgeConnector;
import com.quartercode.eventbridge.bridge.Event;
import com.quartercode.eventbridge.bridge.module.HandlerModule;
import com.quartercode.eventbridge.bridge.module.HandlerModule.HandleInterceptor;
import com.quartercode.eventbridge.channel.ChannelInvocation;
import com.quartercode.eventbridge.factory.Factory;

/**
 * The default default implementation of the {@link HandleInvocationProviderExtension} interface.
 * 
 * @see HandleInvocationProviderExtension
 */
public class DefaultHandleInvocationProviderExtension extends AbstractBridgeModule implements HandleInvocationProviderExtension {

    private final HIPEHandleInterceptor handleInterceptor = new HIPEHandleInterceptor();

    private RunnableInvocationProvider  invocationProvider;

    @Override
    public void add(Bridge bridge) {

        super.add(bridge);

        bridge.getModule(HandlerModule.class).getChannel().addInterceptor(handleInterceptor, 40);
    }

    @Override
    public void remove() {

        getBridge().getModule(HandlerModule.class).getChannel().removeInterceptor(handleInterceptor);

        super.remove();
    }

    @Override
    public RunnableInvocationProvider getInvocationProvider() {

        return invocationProvider;
    }

    @Override
    public void setInvocationProvider(RunnableInvocationProvider invocationProvider) {

        this.invocationProvider = invocationProvider;
    }

    private class HIPEHandleInterceptor implements HandleInterceptor {

        @Override
        public void handle(ChannelInvocation<HandleInterceptor> invocation, Event event, BridgeConnector source) {

            if (invocationProvider == null) {
                invocation.next().handle(invocation, event, source);
            } else {
                invocationProvider.invoke(new InvokeNextHandleInterceptorRunnable(invocation, event, source));
            }
        }

        private class InvokeNextHandleInterceptorRunnable implements Runnable {

            private final ChannelInvocation<HandleInterceptor> invocation;
            private final Event                                event;
            private final BridgeConnector                      source;

            private InvokeNextHandleInterceptorRunnable(ChannelInvocation<HandleInterceptor> invocation, Event event, BridgeConnector source) {

                this.invocation = invocation;
                this.event = event;
                this.source = source;
            }

            @Override
            public void run() {

                invocation.next().handle(invocation, event, source);
            }

        }

    }

    /**
     * A {@link Factory} for the {@link DefaultHandleInvocationProviderExtension} object.
     */
    public static class DefaultHandleInvocationProviderExtensionFactory implements Factory {

        @Override
        public Object create() {

            return new DefaultHandleInvocationProviderExtension();
        }

    }

}
