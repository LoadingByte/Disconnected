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

package com.quartercode.disconnected.server.test.world.comp.net;

import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import com.quartercode.disconnected.server.world.comp.hardware.RouterNetInterface;
import com.quartercode.disconnected.server.world.comp.net.Backbone;

public class BackboneTest {

    private Backbone           backbone;
    private RouterNetInterface child1;
    private RouterNetInterface child2;

    @Before
    public void setUp() {

        backbone = new Backbone();

        child1 = new RouterNetInterface();
        child1.setObj(RouterNetInterface.SUBNET, 10);

        child2 = new RouterNetInterface();
        child2.setObj(RouterNetInterface.SUBNET, 15);
    }

    @Test
    public void testAddChildAddReverse() {

        backbone.addCol(Backbone.CHILDREN, child1);

        assertConnected(backbone, child1, true);
    }

    @Test
    public void testRemoveChildRemoveReverse() {

        backbone.addCol(Backbone.CHILDREN, child1);
        backbone.removeCol(Backbone.CHILDREN, child1);

        assertConnected(backbone, child1, false);
    }

    @Test
    public void testAddMultipleChildrenAddReverse() {

        backbone.addCol(Backbone.CHILDREN, child1);
        backbone.addCol(Backbone.CHILDREN, child2);

        assertConnected(backbone, child1, true);
        assertConnected(backbone, child2, true);
    }

    @Test
    public void testRemoveMultipleChildrenRemoveReverse() {

        backbone.addCol(Backbone.CHILDREN, child1);
        backbone.addCol(Backbone.CHILDREN, child2);
        backbone.removeCol(Backbone.CHILDREN, child1);
        backbone.removeCol(Backbone.CHILDREN, child2);

        assertConnected(backbone, child1, false);
        assertConnected(backbone, child2, false);
    }

    @Test
    public void testAddRemoveMixedMultipleChildrenAddRemoveReverse() {

        backbone.addCol(Backbone.CHILDREN, child1);
        backbone.addCol(Backbone.CHILDREN, child2);
        backbone.removeCol(Backbone.CHILDREN, child1);

        assertConnected(backbone, child1, false);
        assertConnected(backbone, child2, true);
    }

    private void assertConnected(Backbone backbone, RouterNetInterface child, boolean connected) {

        String messageEnd = " connection" + (connected ? " " : " not ") + "available";

        assertTrue("Router -> Child" + messageEnd, backbone.getCol(Backbone.CHILDREN).contains(child) == connected);
        assertTrue("Child -> Router" + messageEnd, backbone.equals(child.getObj(RouterNetInterface.BACKBONE_CONNECTION)) == connected);
    }

}
