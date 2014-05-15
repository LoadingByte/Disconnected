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

package com.quartercode.disconnected.test.bridge.connector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.quartercode.disconnected.bridge.AbstractEventHandler;
import com.quartercode.disconnected.bridge.Bridge;
import com.quartercode.disconnected.bridge.BridgeConnectorException;
import com.quartercode.disconnected.bridge.Event;
import com.quartercode.disconnected.bridge.connector.LocalBridgeConnector;

public class LocalBridgeConnectorTest {

    private Bridge                 local;
    private Bridge                 remote;

    private final List<TestEvent2> localHandlerCalls  = new ArrayList<>();
    private final List<TestEvent1> remoteHandlerCalls = new ArrayList<>();

    @Before
    public void setUp() {

        local = new Bridge();
        remote = new Bridge();

        local.addHandler(new AbstractEventHandler<TestEvent2>(TestEvent2.class) {

            @Override
            public void handle(TestEvent2 event) {

                localHandlerCalls.add(event);
            }

        });

        remote.addHandler(new AbstractEventHandler<TestEvent1>(TestEvent1.class) {

            @Override
            public void handle(TestEvent1 event) {

                remoteHandlerCalls.add(event);
            }

        });
    }

    @Test
    public void test() throws BridgeConnectorException {

        LocalBridgeConnector connector = new LocalBridgeConnector(remote);
        local.connect(connector);

        List<TestEvent1> localToRemoteEvents = new ArrayList<>(Arrays.asList(new TestEvent1(), new TestEvent1(), new TestEvent1()));
        List<TestEvent2> remoteToLocalEvents = new ArrayList<>(Arrays.asList(new TestEvent2(), new TestEvent2(), new TestEvent2()));

        for (TestEvent1 event : localToRemoteEvents) {
            local.send(event);
        }
        for (TestEvent2 event : remoteToLocalEvents) {
            remote.send(event);
        }

        Assert.assertEquals("Events that were handled by local handler", remoteToLocalEvents, localHandlerCalls);
        Assert.assertEquals("Events that were handled by remote handler", localToRemoteEvents, remoteHandlerCalls);

        local.disconnect(connector);

        Assert.assertTrue("Bridge connector wasn't removed from local bridge", local.getConnections().isEmpty());
        Assert.assertTrue("Bridge connector wasn't removed from remote bridge", local.getConnections().isEmpty());
    }

    @SuppressWarnings ("serial")
    private static class TestEvent1 implements Event {

    }

    @SuppressWarnings ("serial")
    private static class TestEvent2 implements Event {

    }

}
