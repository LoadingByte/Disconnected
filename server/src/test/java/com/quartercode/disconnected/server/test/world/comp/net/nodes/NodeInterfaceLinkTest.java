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

package com.quartercode.disconnected.server.test.world.comp.net.nodes;

import static org.junit.Assert.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Rule;
import org.junit.Test;
import com.quartercode.disconnected.server.world.comp.hardware.NetInterface;
import com.quartercode.disconnected.server.world.comp.net.Packet;
import com.quartercode.disconnected.server.world.comp.net.nodes.ComputerConnectedNode;

public class NodeInterfaceLinkTest {

    @Rule
    public JUnitRuleMockery                  context       = new JUnitRuleMockery();

    private final DummyComputerConnectedNode netNode1      = new DummyComputerConnectedNode(0);
    private final DummyNetInterface          netInterface1 = new DummyNetInterface("i0");
    private final DummyNetInterface          netInterface2 = new DummyNetInterface("i1");

    @Mock
    private Packet                           mockPacket;

    /*
     * This test just links one node/interface to a partner and checks whether the link has been established on both sides.
     */
    @Test
    public void testLink() {

        netInterface1.setNetNode(netNode1);

        assertTrue("Net node hasn't been linked to net interface", netInterface1 == netNode1.getNetInterface());
        assertTrue("Net interface hasn't been linked to net node", netNode1 == netInterface1.getNetNode());
    }

    /*
     * This test just unlinks one node/interface from its partner and checks whether the link has been removed on both sides.
     */
    @Test
    public void testRemoveLink() {

        netInterface1.setNetNode(netNode1);
        netInterface1.setNetNode(null);

        assertNull("Net node hasn't been unlinked", netNode1.getNetInterface());
        assertNull("Net interface hasn't been unlinked", netInterface1.getNetNode());
    }

    /*
     * This test assures that a net node cannot be linked to two net interfaces at the same time.
     */
    @Test (expected = IllegalStateException.class)
    public void testDoubleLinkException() {

        // Add the first links
        netInterface1.setNetNode(netNode1);
        netInterface2.setNetNode(netNode1);
    }

    /*
     * These tests assure that the packet deliver methods, which take packets and deliver them to the linked partner, work as expected.
     */

    @Test
    public void testNetNodeProcess() {

        netInterface1.setNetNode(netNode1);

        netNode1.deliverPacketToNetInterface(mockPacket);
        assertEquals("Number of packets processed by net interface 1", 1, netInterface1.processedCount);
    }

    @Test
    public void testNetInterfaceProcess() {

        netInterface1.setNetNode(netNode1);

        netInterface1.deliverOutgoing(mockPacket);
        assertEquals("Number of packets processed by net node 1", 1, netNode1.processedCount);
    }

    @Test (expected = IllegalStateException.class)
    public void testNetNodeProcessWhileUnlinked() {

        netNode1.deliverPacketToNetInterface(mockPacket);
    }

    @Test (expected = IllegalStateException.class)
    public void testNetInterfaceProcessWhileUnlinked() {

        netInterface1.deliverOutgoing(mockPacket);
    }

    @RequiredArgsConstructor
    private static class DummyComputerConnectedNode extends ComputerConnectedNode {

        @Getter
        private final int nodeId;

        private int       processedCount;

        // Make this method public
        @Override
        public void deliverPacketToNetInterface(Packet packet) {

            super.deliverPacketToNetInterface(packet);
        }

        @Override
        public void process(Packet packet) {

            processedCount++;
        }

    }

    private static class DummyNetInterface extends NetInterface {

        private int processedCount;

        private DummyNetInterface(String name) {

            super(name);
        }

        @Override
        public void deliverIncoming(Packet packet) {

            processedCount++;
        }

    }

}
