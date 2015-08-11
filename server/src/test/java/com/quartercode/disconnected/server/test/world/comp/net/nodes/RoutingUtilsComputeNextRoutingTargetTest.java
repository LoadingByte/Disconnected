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

import static org.junit.Assert.assertEquals;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import com.quartercode.disconnected.server.world.comp.net.Packet;
import com.quartercode.disconnected.server.world.comp.net.nodes.RoutingUtils;
import com.quartercode.disconnected.shared.world.comp.net.Address;
import com.quartercode.disconnected.shared.world.comp.net.NetId;

@RequiredArgsConstructor
@RunWith (Parameterized.class)
public class RoutingUtilsComputeNextRoutingTargetTest {

    @Parameters
    public static Collection<Object[]> data() {

        List<Object[]> data = new ArrayList<>();

        // My computer is the receiver of the packet
        data.add(new Object[] { new NetId(1, 2, 3), new NetId(1, 2, 3), -1 });

        // Route the packet to the uplink router
        data.add(new Object[] { new NetId(1, 2, 3, 4), new NetId(1, 2, 3), -2 });
        data.add(new Object[] { new NetId(1, 2, 3, 4), new NetId(1, 2), -2 });
        data.add(new Object[] { new NetId(1, 2, 3, 4), new NetId(1, 2, 5), -2 });
        data.add(new Object[] { new NetId(1, 2, 3, 4), new NetId(1, 2, 5, 6), -2 });
        data.add(new Object[] { new NetId(1, 2, 3, 4), new NetId(1, 2, 5, 6, 7), -2 });

        // Route the packet to the net node with a node id in my network
        data.add(new Object[] { new NetId(1, 2, 3), new NetId(1, 2, 4), 4 });
        data.add(new Object[] { new NetId(1, 2, 3), new NetId(1, 2, 4, 5), 4 });
        // I'm the downlink
        data.add(new Object[] { new NetId(1, 2, 3), new NetId(1, 2, 3, 5), -3 });

        return data;
    }

    @Rule
    public JUnitRuleMockery context = new JUnitRuleMockery();

    private final NetId     netNodeNetId;
    private final NetId     packetDestNetId;
    private final int       expectedResult;

    @Test
    public void testComputeNextRoutingTarget() {

        final Packet packet = context.mock(Packet.class);

        // @formatter:off
        context.checking(new Expectations() {{

            allowing(packet).getDestination();
                will(returnValue(new Address(packetDestNetId, 1)));

        }});
        // @formatter:on

        int result = RoutingUtils.computeNextRoutingTarget(netNodeNetId, packet);
        assertEquals("Returned int code (net node net id = '" + netNodeNetId + "'; packet dest id = '" + packetDestNetId + "')", expectedResult, result);
    }

}
