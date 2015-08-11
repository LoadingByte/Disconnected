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

package com.quartercode.disconnected.server.world.comp.net.nodes;

import com.quartercode.disconnected.server.world.comp.net.NetNode;
import com.quartercode.disconnected.server.world.comp.net.Packet;
import com.quartercode.disconnected.server.world.comp.net.RoutedPacket;

/**
 * An abstract {@link ComputerConnectedNode} which implements the bare bones of {@link Packet} processing, but leaves the rest to subclasses.
 * Essentially, the class checks on whether an incoming packet is a {@link RoutedPacket} and the next "hop" is reachable.
 * If that's the case, the packet is "hopped" the next {@link NetNode} in the routing queue.
 * Otherwise, the <b>abstract</b> method {@link #computeRouteAndExecute(Packet)} is called in order to calculate a new route for the packet and execute it.
 * Of course, that method should be implemented by subclasses.
 */
public abstract class TemplateNode extends ComputerConnectedNode {

    @Override
    public void process(Packet packet) {

        // Ignore any null packets (might be results of previously dropped packets)
        if (packet == null) {
            return;
        }

        // If the packet is a routed one, try to deliver it to the next node on the precomputed path
        NetNode nextNode = RoutingUtils.getNextNetNodeWithRoutedPacketInfo(this, packet);
        if (nextNode != null) {
            deliverPacketToNetNode(packet, nextNode);
        }
        // If that's not possible, recompute the packet's route and execute the next step on that route
        else {
            computeRouteAndExecute(RoutingUtils.unwrapRoutedPacket(packet));
        }
    }

    /**
     * This method is implemented by subclasses and should calculate a new route for the given {@link Packet}, as well as directly executing it.
     * However, the word "route" might be a bit inappropriate in this context since there are many things this method could do.
     * For example, it is also responsible for delivering the packet to the connected {@link #getNetInterface() net interface} if it has reached its destination.
     * Still, in most cases this method will just plot a new course for the packet and send it away.
     * The {@link RoutingUtils} class might be useful for that.
     *
     * @param packet The packet a new "route" should be calculated and executed for.
     *        It doesn't have to be {@link RoutingUtils#unwrapRoutedPacket(Packet) unwrapped} manually since that has been done automatically.
     */
    protected abstract void computeRouteAndExecute(Packet packet);

    /**
     * This is an internal utility method which delivers the given {@link Packet} to the given {@link NetNode} for further processing.
     * That might be the case because the given node is the next one on the {@link RoutedPacket#getPath() route} which has been computed for the packet.
     * Internally, this method calls {@link NetNode#process(Packet)}.
     * Note that the method should only be called by subclasses of this class and isn't intended for public use.
     *
     * @param packet The packet that should be delivered to the given net node for further handling.
     * @param netNode The net node which should handle the given packet.
     */
    protected void deliverPacketToNetNode(Packet packet, NetNode netNode) {

        netNode.process(packet);
    }

}
