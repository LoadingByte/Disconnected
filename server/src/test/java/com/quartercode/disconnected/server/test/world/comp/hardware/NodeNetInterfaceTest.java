/*
 * This file is part of Disconnected.
 * Copyright (c) 2013 QuarterCode <http://quartercode.com/>
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

package com.quartercode.disconnected.server.test.world.comp.hardware;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import com.quartercode.disconnected.server.world.comp.hardware.NodeNetInterface;
import com.quartercode.disconnected.server.world.comp.hardware.RouterNetInterface;
import com.quartercode.disconnected.shared.world.comp.net.NetID;

public class NodeNetInterfaceTest {

    private NodeNetInterface   nodeInterface;
    private RouterNetInterface connection;

    @Before
    public void setUp() {

        nodeInterface = new NodeNetInterface();
        nodeInterface.setObj(NodeNetInterface.NET_ID, new NetID());

        connection = new RouterNetInterface();
        connection.setObj(RouterNetInterface.SUBNET, 10);
    }

    @Test
    public void testSetConnectionNullFromNull() {

        nodeInterface.setObj(NodeNetInterface.CONNECTION, null);
    }

    @Test
    public void testSetConnectionAddReverse() {

        nodeInterface.setObj(NodeNetInterface.CONNECTION, connection);

        assertNull("Net id that should've been cleared", nodeInterface.getObj(NodeNetInterface.NET_ID));
        assertEquals("Child -> Router connection", connection, nodeInterface.getObj(NodeNetInterface.CONNECTION));
        assertTrue("Router -> Child connection not added", connection.getColl(RouterNetInterface.CHILDREN).contains(nodeInterface));
    }

    @Test
    public void testSetConnectionRemoveReverse() {

        nodeInterface.setObj(NodeNetInterface.CONNECTION, connection);
        nodeInterface.setObj(NodeNetInterface.CONNECTION, null);

        assertNull("Child -> Router connection", nodeInterface.getObj(NodeNetInterface.CONNECTION));
        assertFalse("Router -> Child connection not removed", connection.getColl(RouterNetInterface.CHILDREN).contains(nodeInterface));
    }

    @Test
    public void testChangeNetIDWithConnection() {

        nodeInterface.setObj(NodeNetInterface.CONNECTION, connection);
        nodeInterface.setObj(NodeNetInterface.NET_ID, new NetID(10, 0));
    }

    @Test (expected = IllegalArgumentException.class)
    public void testChangeNetIDWithConnectionWrongSubnet() {

        nodeInterface.setObj(NodeNetInterface.CONNECTION, connection);
        nodeInterface.setObj(NodeNetInterface.NET_ID, new NetID(11, 0));
    }

}
