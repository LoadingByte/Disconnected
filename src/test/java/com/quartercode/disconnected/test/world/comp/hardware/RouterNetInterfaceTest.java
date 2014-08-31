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

package com.quartercode.disconnected.test.world.comp.hardware;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import com.quartercode.disconnected.world.comp.hardware.NodeNetInterface;
import com.quartercode.disconnected.world.comp.hardware.RouterNetInterface;
import com.quartercode.disconnected.world.comp.net.Backbone;
import com.quartercode.disconnected.world.comp.net.NetID;

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
        routerInterface.get(RouterNetInterface.SUBNET).set(10);

        backbone = new Backbone();

        neighbour1 = new RouterNetInterface();
        neighbour1.get(RouterNetInterface.SUBNET).set(15);

        neighbour2 = new RouterNetInterface();
        neighbour2.get(RouterNetInterface.SUBNET).set(20);

        child1 = new NodeNetInterface();
        child1.get(NodeNetInterface.NET_ID).set(generateNetID(10, 1));

        child2 = new NodeNetInterface();
        child2.get(NodeNetInterface.NET_ID).set(generateNetID(10, 2));
    }

    private NetID generateNetID(int subnet, int id) {

        NetID netId = new NetID();
        netId.get(NetID.SUBNET).set(subnet);
        netId.get(NetID.ID).set(id);
        return netId;
    }

    @Test
    public void testSetBackboneAddReverse() {

        routerInterface.get(RouterNetInterface.BACKBONE_CONNECTION).set(backbone);

        assertEquals("Router -> Backbone connection", backbone, routerInterface.get(RouterNetInterface.BACKBONE_CONNECTION).get());
        assertTrue("Backbone -> Router connection not added", backbone.get(Backbone.CHILDREN).get().contains(routerInterface));
    }

    @Test
    public void testSetBackboneRemoveReverse() {

        routerInterface.get(RouterNetInterface.BACKBONE_CONNECTION).set(backbone);
        routerInterface.get(RouterNetInterface.BACKBONE_CONNECTION).set(null);

        assertNull("Router -> Backbone connection", routerInterface.get(RouterNetInterface.BACKBONE_CONNECTION).get());
        assertFalse("Backbone -> Router connection not removed", backbone.get(Backbone.CHILDREN).get().contains(routerInterface));
    }

    @Test
    public void testAddNeighbourAddReverse() {

        routerInterface.get(RouterNetInterface.NEIGHBOURS).add(neighbour1);

        assertConnected(routerInterface, neighbour1, true);
    }

    @Test
    public void testRemoveNeighbourRemoveReverse() {

        routerInterface.get(RouterNetInterface.NEIGHBOURS).add(neighbour1);
        routerInterface.get(RouterNetInterface.NEIGHBOURS).remove(neighbour1);

        assertConnected(routerInterface, neighbour1, false);
    }

    @Test
    public void testAddMultipleNeighboursAddReverse() {

        routerInterface.get(RouterNetInterface.NEIGHBOURS).add(neighbour1);
        routerInterface.get(RouterNetInterface.NEIGHBOURS).add(neighbour2);

        assertConnected(routerInterface, neighbour1, true);
        assertConnected(routerInterface, neighbour2, true);
    }

    @Test
    public void testRemoveMultipleNeighboursRemoveReverse() {

        routerInterface.get(RouterNetInterface.NEIGHBOURS).add(neighbour1);
        routerInterface.get(RouterNetInterface.NEIGHBOURS).add(neighbour2);
        routerInterface.get(RouterNetInterface.NEIGHBOURS).remove(neighbour1);
        routerInterface.get(RouterNetInterface.NEIGHBOURS).remove(neighbour2);

        assertConnected(routerInterface, neighbour1, false);
        assertConnected(routerInterface, neighbour2, false);
    }

    @Test
    public void testAddRemoveMixedMultipleNeighboursAddRemoveReverse() {

        routerInterface.get(RouterNetInterface.NEIGHBOURS).add(neighbour1);
        routerInterface.get(RouterNetInterface.NEIGHBOURS).add(neighbour2);
        routerInterface.get(RouterNetInterface.NEIGHBOURS).remove(neighbour1);

        assertConnected(routerInterface, neighbour1, false);
        assertConnected(routerInterface, neighbour2, true);
    }

    private void assertConnected(RouterNetInterface routerInterface, RouterNetInterface neighbour, boolean connected) {

        String messageEnd = " connection" + (connected ? " " : " not ") + "available";
        assertTrue("Router -> Neighbour" + messageEnd, routerInterface.get(RouterNetInterface.NEIGHBOURS).get().contains(neighbour) == connected);
        assertTrue("Neighbour -> Router" + messageEnd, neighbour.get(RouterNetInterface.NEIGHBOURS).get().contains(routerInterface) == connected);
    }

    @Test
    public void testAddChildAddReverse() {

        routerInterface.get(RouterNetInterface.CHILDREN).add(child1);
        child1.get(NodeNetInterface.NET_ID).set(generateNetID(10, 1));

        assertConnected(routerInterface, child1, true);
    }

    @Test
    public void testRemoveChildRemoveReverse() {

        routerInterface.get(RouterNetInterface.CHILDREN).add(child1);
        child1.get(NodeNetInterface.NET_ID).set(generateNetID(10, 1));

        routerInterface.get(RouterNetInterface.CHILDREN).remove(child1);
        child1.get(NodeNetInterface.NET_ID).set(generateNetID(10, 1));

        assertConnected(routerInterface, child1, false);
    }

    @Test
    public void testAddMultipleChildrenAddReverse() {

        routerInterface.get(RouterNetInterface.CHILDREN).add(child1);
        child1.get(NodeNetInterface.NET_ID).set(generateNetID(10, 1));
        routerInterface.get(RouterNetInterface.CHILDREN).add(child2);
        child2.get(NodeNetInterface.NET_ID).set(generateNetID(10, 2));

        assertConnected(routerInterface, child1, true);
        assertConnected(routerInterface, child2, true);
    }

    @Test
    public void testRemoveMultipleChildrenRemoveReverse() {

        routerInterface.get(RouterNetInterface.CHILDREN).add(child1);
        child1.get(NodeNetInterface.NET_ID).set(generateNetID(10, 1));
        routerInterface.get(RouterNetInterface.CHILDREN).add(child2);
        child2.get(NodeNetInterface.NET_ID).set(generateNetID(10, 2));

        routerInterface.get(RouterNetInterface.CHILDREN).remove(child1);
        child1.get(NodeNetInterface.NET_ID).set(generateNetID(10, 1));
        routerInterface.get(RouterNetInterface.CHILDREN).remove(child2);
        child2.get(NodeNetInterface.NET_ID).set(generateNetID(10, 2));

        assertConnected(routerInterface, child1, false);
        assertConnected(routerInterface, child2, false);
    }

    @Test
    public void testAddRemoveMixedMultipleChildrenAddRemoveReverse() {

        routerInterface.get(RouterNetInterface.CHILDREN).add(child1);
        child1.get(NodeNetInterface.NET_ID).set(generateNetID(10, 1));
        routerInterface.get(RouterNetInterface.CHILDREN).add(child2);
        child2.get(NodeNetInterface.NET_ID).set(generateNetID(10, 2));

        routerInterface.get(RouterNetInterface.CHILDREN).remove(child1);
        child1.get(NodeNetInterface.NET_ID).set(generateNetID(10, 1));

        assertConnected(routerInterface, child1, false);
        assertConnected(routerInterface, child2, true);
    }

    private void assertConnected(RouterNetInterface routerInterface, NodeNetInterface child, boolean connected) {

        String messageEnd = " connection" + (connected ? " " : " not ") + "available";

        assertTrue("Router -> Child" + messageEnd, routerInterface.get(RouterNetInterface.CHILDREN).get().contains(child) == connected);
        assertTrue("Child -> Router" + messageEnd, routerInterface.equals(child.get(NodeNetInterface.CONNECTION).get()) == connected);
    }

}
