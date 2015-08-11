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

package com.quartercode.disconnected.server.world.comp.net;

import java.util.LinkedList;
import java.util.Queue;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.apache.commons.lang3.Validate;
import com.quartercode.disconnected.server.world.comp.net.nodes.DownlinkRouterNode;
import com.quartercode.disconnected.server.world.comp.net.nodes.UplinkRouterNode;
import com.quartercode.disconnected.server.world.util.InterfaceAdapter;
import com.quartercode.disconnected.server.world.util.WorldNode;
import com.quartercode.disconnected.shared.world.comp.net.Address;
import com.quartercode.disconnected.shared.world.comp.net.NetId;
import com.quartercode.jtimber.api.node.Node;
import com.quartercode.jtimber.api.node.wrapper.SubstituteWithWrapper;
import com.quartercode.jtimber.api.node.wrapper.collection.QueueWrapper;

/**
 * This class represents a {@link Packet} whose route <b>between {@link NetNode}s inside one {@link Network}</b> has already been calculated.
 * It therefore contains some routing information, while wrapping around a real packet (e.g. the {@link StandardPacket}).<br>
 * Note, however, that routes through multiple network tiers (see {@link NetId}) are not precomputed.
 * Instead, a routed packet might just contain the path to an uplink or downlink router, which in turn transports the packet to another tier.
 * Then, a new route can be computed inside that other upper/lower-tier network.
 *
 * @see #getPath()
 */
public class RoutedPacket extends WorldNode<Node<?>> implements Packet {

    @XmlElement
    @XmlJavaTypeAdapter (InterfaceAdapter.class)
    private Packet               wrappedPacket;
    @XmlAttribute
    @XmlList
    @SubstituteWithWrapper (QueueWrapper.class)
    private final Queue<Integer> path;

    // JAXB constructor
    protected RoutedPacket() {

        path = new LinkedList<>();
    }

    /**
     * Creates a new routed packet.
     *
     * @param packet The network {@link Packet} that should be wrapped by the new routed packet.
     * @param path A queue that contains the {@link NetId#getNodeIdsAtTiers() node ids} (not {@link NetId}s!) of the {@link NetNode}s which should transport the new routed packet.
     *        See {@link #getPath()} for a very detailed description of this queue, its function, and some special values.
     */
    public RoutedPacket(Packet packet, Queue<Integer> path) {

        Validate.notNull(packet, "Cannot route a null packet by putting it in a RoutedPacket");
        Validate.notEmpty(path, "Routed packet path cannot be null or empty");

        wrappedPacket = packet;
        this.path = path;
    }

    /**
     * Returns the network {@link Packet} that is wrapped by the routed packet.
     * It should be extracted when the routed packet reaches its destination.
     *
     * @return The wrapped packet which is actually sent.
     */
    public Packet getWrappedPacket() {

        return wrappedPacket;
    }

    @Override
    public Address getSource() {

        return wrappedPacket.getSource();
    }

    @Override
    public Address getDestination() {

        return wrappedPacket.getDestination();
    }

    @Override
    public Object getData() {

        return wrappedPacket.getData();
    }

    @Override
    public long getSize() {

        return wrappedPacket.getSize();
    }

    /**
     * Returns the queue that contains the {@link NetId#getNodeIdsAtTiers() node ids} (not {@link NetId}s!) of the {@link NetNode}s which should transport the routed packet.
     * The node ids are related to the {@link Network} the packet is currently in and are provided in the correct order of transportation.
     * For example, if this queue contains the ids {@code 5}, {@code 7} and {@code 10}, the net node the packet originated from (the first one) will send it to node {@code 5}.
     * That node will then send it to node {@code 7}, which in turn sends it even further to the receiving node {@code 10} that might forward the packet to its computer.<br>
     * <br>
     * However, there often occur cases where the destination node isn't actually a part of the same (sub)network the sending node is part of.
     * In such a case, the packet is send to a router (uplink or downlink) which in turn delivers it to another network.
     * Sometimes, multiple of the network hops are necessary in order to reach the network that contains the destination node.<br>
     * <br>
     * When any node (including routers) receives any {@link Packet}, it should apply the following routing algorithm:
     *
     * <table border="1">
     * <tr>
     * <td colspan="4">Is the packet a routed packet and is it's path queue <b>not</b> empty?</td>
     * </tr>
     * <tr>
     * <td colspan="2">Yes</td>
     * <td colspan="2">No</td>
     * </tr>
     * <tr>
     * <td colspan="2">Am I connected to the next node in the path queue?</td>
     * <td colspan="2">Calculate a new route for the packet (algorithm below).</td>
     * </tr>
     * <tr>
     * <td>Yes</td>
     * <td>No</td>
     * <td rowspan="2"></td>
     * </tr>
     * <tr>
     * <td>Send the packet to the next node in the path queue.</td>
     * <td>Calculate a new route for the packet (algorithm below).</td>
     * </tr>
     * </table>
     * <br>
     * A new route for a packet is calculated by firstly determinating the next target of the packet inside the current network using the following algorithm.
     * It essentially compares the net id of the net node that handles the packet ("me") with the destination net id of the packet.
     *
     * <table border="1">
     * <tr>
     * <td colspan="5">Am I the receiver of the packet?<br>
     * <i>Does my net id match the packet's destination net id?<br>
     * (e.g. my net id = {@code 1.2.3}, dest. net id = {@code 1.2.3} =&gt; net ids match)</i></td>
     * </tr>
     * <tr>
     * <td>Yes</td>
     * <td colspan="4">No</td>
     * </tr>
     * <tr>
     * <td>Hand over the packet's data payload to my computer without calculating a new route</td>.
     * <td colspan="4">Is the destination located in an upper-tier network compared to the network I am located in?<br>
     * <i>Is my net id longer than the packet's destination net id?<br>
     * (e.g. my net id = {@code 1.2.3.4}, dest. net id = {@code 5.6} =&gt; my net id is longer)</i></td>
     * </tr>
     * <td rowspan="6"></td>
     * <td>Yes</td>
     * <td colspan="3">No</td>
     * </tr>
     * <tr>
     * <td>Use the network's {@link UplinkRouterNode} as next target.</td>
     * <td colspan="3">Is the destination located in my network or a lower-tier network that is reachable from my network?<br>
     * <i>Does the beginning of my net id (apart from the last element) match the beginning of the destination net id?<br>
     * (e.g. my net id = {@code 1.2.3}, dest. net id = {@code 1.4.5.6} =&gt; my beginning last node id ({@code 1.2}) doesn't match the dest's beginning ({@code 1.4})</i></td>
     * </tr>
     * <tr>
     * <td rowspan="4"></td>
     * <td colspan="2">Yes</td>
     * <td>No</td>
     * </tr>
     * <tr>
     * <td colspan="2">Is the destination located in a lower-tier network?<br>
     * <i>Is my net id shorter that the packet's destination net id?<br>
     * (e.g. my net id = {@code 1.2.3}, dest. net id = {@code 1.2.4.5} =&gt; my net id is shorter)</i></td>
     * <td>Use the network's {@link UplinkRouterNode} as next target.</td>
     * </tr>
     * <tr>
     * <td>Yes</td>
     * <td>No</td>
     * <td rowspan="2"></td>
     * </tr>
     * <tr>
     * <td>Use the correct {@link DownlinkRouterNode}, which leads into the next lower-tier network on the path to the destination's network, as next target.<br>
     * <i>Cut off the packet's destination net id after the tier of my net id and use the downlink router with that net id as next target.<br>
     * (e.g. my net id = {@code 1.2.3}, dest. net id = {@code 1.2.4.5.6} =&gt; net id of target = {@code 1.2.4})</i></td>
     * <td>Use the packet's destination net id as next target (the destination is in my network).</td>
     * </tr>
     * </table>
     * <br>
     * Note that the returned queue is modifiable; therefore, you can {@link Queue#poll() poll} from it.
     * Sadly, there are no built-in unmodifiable queues available.
     * Just make sure that you do not execute any other modifying operation apart from poll (e.g. {@link Queue#offer(Object)}) on it.
     *
     * @return A queue that contains the node ids of the nodes which should transport the routed packet.
     */
    public Queue<Integer> getPath() {

        return path;
    }

}
