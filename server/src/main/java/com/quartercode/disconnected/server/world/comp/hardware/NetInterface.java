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

package com.quartercode.disconnected.server.world.comp.hardware;

import javax.xml.bind.annotation.XmlElement;
import org.apache.commons.lang3.Validate;
import com.quartercode.disconnected.server.world.comp.hardware.Mainboard.NeedsMainboardSlot;
import com.quartercode.disconnected.server.world.comp.net.NetNode;
import com.quartercode.disconnected.server.world.comp.net.Network;
import com.quartercode.disconnected.server.world.comp.net.NetworkModule;
import com.quartercode.disconnected.server.world.comp.net.Packet;
import com.quartercode.disconnected.server.world.comp.net.nodes.ComputerConnectedNode;
import com.quartercode.disconnected.server.world.comp.net.socket.Socket;

/**
 * This class represents a network interface hardware piece that may be used by any computer.
 * It is responsible for sending and receiving network {@link Packet}s.<br>
 * <br>
 * Internally, the network interface hardware part provides a {@link ComputerConnectedNode} which represents the network interface in the global Internet.
 * Any packet communication is actually done through the {@link Network} of net nodes, and not a network of net interfaces.
 * Remember that the net node of a new network interface has to be set manually using {@link #setNetNode(ComputerConnectedNode)}.<br>
 * <br>
 * Note that the link between a net interface and a net node is automatically established on both sides as soon as {@link #setNetNode(ComputerConnectedNode)} is called.
 * Moreover, it is impossible to create inconsistent states.
 * For example, if you connect an already connected CC node to another net interface, an {@link IllegalStateException} will be thrown.<br>
 * While it is allowed to temporarily leave a net interface unlinked for changing things around, exceptions will be thrown if any packet processing
 * is attempted with such an unlinked interface.
 * Therefore, such a state should be resolved as fast as possible.
 *
 * @see Packet
 * @see NetNode
 * @see Hardware
 */
@NeedsMainboardSlot
public class NetInterface extends Hardware {

    @XmlElement
    private ComputerConnectedNode netNode;

    // JAXB constructor
    protected NetInterface() {

    }

    /**
     * Creates a new network interface.
     *
     * @param name The "model" name of the new network interface.
     *        See {@link #getName()} for more details.
     */
    public NetInterface(String name) {

        super(name);
    }

    /**
     * Returns the {@link ComputerConnectedNode} that represents the network interface in a {@link Network} and handles all the packet processing.
     * If you want to change the net node link, use the {@link #setNetNode(ComputerConnectedNode)} method.
     *
     * @return The net node that represents the network interface.
     */
    public ComputerConnectedNode getNetNode() {

        return netNode;
    }

    /**
     * Links the network interface with the given {@link ComputerConnectedNode}.
     * Afterwards, the net node will represent the net interface in a {@link Network} and handle all the packet processing.
     * Note that the link between the net interface and the node is automatically established on both sides.<br>
     * <br>
     * It is possible to temporarily set the net node of a net interface to {@code null} (unlinked state) for changing things around.
     * However, the net node needs to be set to a valid object again before any packet processing is attempted, or an exception will be thrown.
     *
     * @param netNode The net node that should represent the network interface.
     */
    public void setNetNode(ComputerConnectedNode netNode) {

        this.netNode = netNode;
    }

    /**
     * This is an internal method that immediately sends a provided {@link Packet} through the background {@link NetNode} network to its target net interface.
     * For doing that, it just calls the {@link NetNode#process(Packet)} method on the net node which is liked with this net interface.<br>
     * Note that this method should only be called by other parts of the OS and isn't intended for public use.
     *
     * @param packet The packet that should be delivered to its target network interface.
     */
    public void deliverOutgoing(Packet packet) {

        Validate.validState(netNode != null, "A network interface cannot send any packets through the network while it is temporarily unlinked (not linked with any net node)");

        netNode.process(packet);
    }

    /**
     * This is an internal method that immediately delivers a provided {@link Packet} to the OS, which in turn calls some sort of handler.
     * For example, a TCP packet could delivered to the {@link Socket} which has opened the connection.
     * However, this method doesn't know about any of those mechanics and just hands the packet over to the OS via {@link NetworkModule#handle(Packet)}.<br>
     * Note that this method should only be called by other parts of the OS and isn't intended for public use.
     *
     * @param packet The packet that has been received and should now be delivered to the OS.
     */
    public void deliverIncoming(Packet packet) {

        getSingleParent().getOs().getNetModule().handlePacket(packet);
    }

}
