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

package com.quartercode.disconnected.test.world.comp.net;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.quartercode.disconnected.world.comp.hardware.NodeNetInterface;
import com.quartercode.disconnected.world.comp.hardware.RouterNetInterface;
import com.quartercode.disconnected.world.comp.net.NetID;

public class NodeNetInterfaceTest {

    private NodeNetInterface   nodeInterface;
    private RouterNetInterface connection;

    @Before
    public void setUp() {

        nodeInterface = new NodeNetInterface();
        nodeInterface.get(NodeNetInterface.NET_ID).set(new NetID());

        connection = new RouterNetInterface();
        connection.get(RouterNetInterface.SUBNET).set(10);
    }

    @Test
    public void testSetConnectionNullFromNull() {

        nodeInterface.get(NodeNetInterface.CONNECTION).set(null);
    }

    @Test
    public void testSetConnectionAddReverse() {

        nodeInterface.get(NodeNetInterface.CONNECTION).set(connection);

        Assert.assertNull("Net id that should've been cleared", nodeInterface.get(NodeNetInterface.NET_ID).get());
        Assert.assertEquals("Child -> Router connection", connection, nodeInterface.get(NodeNetInterface.CONNECTION).get());
        Assert.assertTrue("Router -> Child connection not added", connection.get(RouterNetInterface.CHILDREN).get().contains(nodeInterface));
    }

    @Test
    public void testSetConnectionRemoveReverse() {

        nodeInterface.get(NodeNetInterface.CONNECTION).set(connection);
        nodeInterface.get(NodeNetInterface.CONNECTION).set(null);

        Assert.assertNull("Child -> Router connection", nodeInterface.get(NodeNetInterface.CONNECTION).get());
        Assert.assertFalse("Router -> Child connection not removed", connection.get(RouterNetInterface.CHILDREN).get().contains(nodeInterface));
    }

    @Test
    public void testChangeNetIDWithConnection() {

        nodeInterface.get(NodeNetInterface.CONNECTION).set(connection);

        NetID netID = new NetID();
        netID.get(NetID.SUBNET).set(10);
        nodeInterface.get(NodeNetInterface.NET_ID).set(netID);
    }

    @Test (expected = IllegalArgumentException.class)
    public void testChangeNetIDWithConnectionWrongSubnet() {

        nodeInterface.get(NodeNetInterface.CONNECTION).set(connection);

        NetID netID = new NetID();
        netID.get(NetID.SUBNET).set(11);
        nodeInterface.get(NodeNetInterface.NET_ID).set(netID);
    }

}
