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
import com.quartercode.disconnected.server.world.comp.net.Backbone;
import com.quartercode.disconnected.shared.world.comp.net.NetID;

public class RouterNetInterfaceTest {

    private RouterNetInterface routerInterface;
    private Backbone           backbone;
    private RouterNetInterface neighbour1;
    private RouterNetInterface neighbour2;
    private NodeNetInterface   child1;
    private NodeNetInterface   child2;

    @Before
    public void setUp() {

        routerInterface = new RouterNetInterface();
        routerInterface.setObj(RouterNetInterface.SUBNET, 10);

        backbone = new Backbone();

        neighbour1 = new RouterNetInterface();
        neighbour1.setObj(RouterNetInterface.SUBNET, 15);

        neighbour2 = new RouterNetInterface();
        neighbour2.setObj(RouterNetInterface.SUBNET, 20);

        child1 = new NodeNetInterface();
        child1.setObj(NodeNetInterface.NET_ID, new NetID(10, 1));

        child2 = new NodeNetInterface();
        child2.setObj(NodeNetInterface.NET_ID, new NetID(10, 2));
    }

    @Test
    public void testSetBackboneAddReverse() {

        routerInterface.setObj(RouterNetInterface.BACKBONE_CONNECTION, backbone);

        assertEquals("Router -> Backbone connection", backbone, routerInterface.getObj(RouterNetInterface.BACKBONE_CONNECTION));
        assertTrue("Backbone -> Router connection not added", backbone.getColl(Backbone.CHILDREN).contains(routerInterface));
    }

    @Test
    public void testSetBackboneRemoveReverse() {

        routerInterface.setObj(RouterNetInterface.BACKBONE_CONNECTION, backbone);
        routerInterface.setObj(RouterNetInterface.BACKBONE_CONNECTION, null);

        assertNull("Router -> Backbone connection", routerInterface.getObj(RouterNetInterface.BACKBONE_CONNECTION));
        assertFalse("Backbone -> Router connection not removed", backbone.getColl(Backbone.CHILDREN).contains(routerInterface));
    }

    @Test
    public void testAddNeighbourAddReverse() {

        routerInterface.addToColl(RouterNetInterface.NEIGHBOURS, neighbour1);

        assertConnected(routerInterface, neighbour1, true);
    }

    @Test
    public void testRemoveNeighbourRemoveReverse() {

        routerInterface.addToColl(RouterNetInterface.NEIGHBOURS, neighbour1);
        routerInterface.removeFromColl(RouterNetInterface.NEIGHBOURS, neighbour1);

        assertConnected(routerInterface, neighbour1, false);
    }

    @Test
    public void testAddMultipleNeighboursAddReverse() {

        routerInterface.addToColl(RouterNetInterface.NEIGHBOURS, neighbour1);
        routerInterface.addToColl(RouterNetInterface.NEIGHBOURS, neighbour2);

        assertConnected(routerInterface, neighbour1, true);
        assertConnected(routerInterface, neighbour2, true);
    }

    @Test
    public void testRemoveMultipleNeighboursRemoveReverse() {

        routerInterface.addToColl(RouterNetInterface.NEIGHBOURS, neighbour1);
        routerInterface.addToColl(RouterNetInterface.NEIGHBOURS, neighbour2);
        routerInterface.removeFromColl(RouterNetInterface.NEIGHBOURS, neighbour1);
        routerInterface.removeFromColl(RouterNetInterface.NEIGHBOURS, neighbour2);

        assertConnected(routerInterface, neighbour1, false);
        assertConnected(routerInterface, neighbour2, false);
    }

    @Test
    public void testAddRemoveMixedMultipleNeighboursAddRemoveReverse() {

        routerInterface.addToColl(RouterNetInterface.NEIGHBOURS, neighbour1);
        routerInterface.addToColl(RouterNetInterface.NEIGHBOURS, neighbour2);
        routerInterface.removeFromColl(RouterNetInterface.NEIGHBOURS, neighbour1);

        assertConnected(routerInterface, neighbour1, false);
        assertConnected(routerInterface, neighbour2, true);
    }

    private void assertConnected(RouterNetInterface routerInterface, RouterNetInterface neighbour, boolean connected) {

        String messageEnd = " connection" + (connected ? " " : " not ") + "available";
        assertTrue("Router -> Neighbour" + messageEnd, routerInterface.getColl(RouterNetInterface.NEIGHBOURS).contains(neighbour) == connected);
        assertTrue("Neighbour -> Router" + messageEnd, neighbour.getColl(RouterNetInterface.NEIGHBOURS).contains(routerInterface) == connected);
    }

    @Test
    public void testAddChildAddReverse() {

        routerInterface.addToColl(RouterNetInterface.CHILDREN, child1);
        child1.setObj(NodeNetInterface.NET_ID, new NetID(10, 1));

        assertConnected(routerInterface, child1, true);
    }

    @Test
    public void testRemoveChildRemoveReverse() {

        routerInterface.addToColl(RouterNetInterface.CHILDREN, child1);
        child1.setObj(NodeNetInterface.NET_ID, new NetID(10, 1));

        routerInterface.removeFromColl(RouterNetInterface.CHILDREN, child1);
        child1.setObj(NodeNetInterface.NET_ID, new NetID(10, 1));

        assertConnected(routerInterface, child1, false);
    }

    @Test
    public void testAddMultipleChildrenAddReverse() {

        routerInterface.addToColl(RouterNetInterface.CHILDREN, child1);
        child1.setObj(NodeNetInterface.NET_ID, new NetID(10, 1));
        routerInterface.addToColl(RouterNetInterface.CHILDREN, child2);
        child2.setObj(NodeNetInterface.NET_ID, new NetID(10, 2));

        assertConnected(routerInterface, child1, true);
        assertConnected(routerInterface, child2, true);
    }

    @Test
    public void testRemoveMultipleChildrenRemoveReverse() {

        routerInterface.addToColl(RouterNetInterface.CHILDREN, child1);
        child1.setObj(NodeNetInterface.NET_ID, new NetID(10, 1));
        routerInterface.addToColl(RouterNetInterface.CHILDREN, child2);
        child2.setObj(NodeNetInterface.NET_ID, new NetID(10, 2));

        routerInterface.removeFromColl(RouterNetInterface.CHILDREN, child1);
        child1.setObj(NodeNetInterface.NET_ID, new NetID(10, 1));
        routerInterface.removeFromColl(RouterNetInterface.CHILDREN, child2);
        child2.setObj(NodeNetInterface.NET_ID, new NetID(10, 2));

        assertConnected(routerInterface, child1, false);
        assertConnected(routerInterface, child2, false);
    }

    @Test
    public void testAddRemoveMixedMultipleChildrenAddRemoveReverse() {

        routerInterface.addToColl(RouterNetInterface.CHILDREN, child1);
        child1.setObj(NodeNetInterface.NET_ID, new NetID(10, 1));
        routerInterface.addToColl(RouterNetInterface.CHILDREN, child2);
        child2.setObj(NodeNetInterface.NET_ID, new NetID(10, 2));

        routerInterface.removeFromColl(RouterNetInterface.CHILDREN, child1);
        child1.setObj(NodeNetInterface.NET_ID, new NetID(10, 1));

        assertConnected(routerInterface, child1, false);
        assertConnected(routerInterface, child2, true);
    }

    private void assertConnected(RouterNetInterface routerInterface, NodeNetInterface child, boolean connected) {

        String messageEnd = " connection" + (connected ? " " : " not ") + "available";

        assertTrue("Router -> Child" + messageEnd, routerInterface.getColl(RouterNetInterface.CHILDREN).contains(child) == connected);
        assertTrue("Child -> Router" + messageEnd, routerInterface.equals(child.getObj(NodeNetInterface.CONNECTION)) == connected);
    }

}
