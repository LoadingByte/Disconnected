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

import org.apache.commons.lang3.Validate;
import com.quartercode.disconnected.server.world.comp.hardware.NetInterface;
import com.quartercode.disconnected.server.world.comp.net.NetNode;
import com.quartercode.disconnected.server.world.comp.net.Network;
import com.quartercode.disconnected.server.world.comp.net.Packet;
import com.quartercode.jtimber.api.node.Node;

/**
 * An abstract {@link NetNode} that is linked with a {@link NetInterface}; that way, it is able to represent a computer inside a {@link Network}.
 * The CC (computer-connected) node delivers outgoing packets, which are sent by the net interface, and hands over incoming packets to the net interface.
 * It is the base class for all the net nodes which exist by default.
 * Of course, non-computer-connected nodes could be used to represent net nodes which operate without computers (if that's needed).<br>
 * <br>
 * Note that the link between a net interface and a CC node is automatically established on both sides as soon as {@link NetInterface#setNetNode(ComputerConnectedNode)} is called.
 * Moreover, it is impossible to create inconsistent states.
 * For example, if you connect an already connected CC node to another net interface, an {@link IllegalStateException} will be thrown.<br>
 * While it is allowed to temporarily leave a CC node unlinked for changing things around, exceptions will be thrown if any packet processing
 * is attempted with such an unlinked node.
 * Therefore, such a state should be resolved as fast as possible.
 *
 * @see NetNode
 * @see NetInterface
 */
public abstract class ComputerConnectedNode extends NetNode {

    /**
     * Returns the {@link NetInterface} this computer-connected net node represents inside a {@link Network}.
     * This node delivers outgoing packets, which are sent by the net interface, and hands over incoming packets to the net interface.
     * If you want to change the net interface link, firstly call {@link NetInterface#setNetNode(ComputerConnectedNode)} with {@code null} on the old linked net interface
     * and then call that method with this net node on the new net interface.
     * If you omit the first (unlinking) call, an {@link IllegalStateException} will be thrown.
     *
     * @return The network interface which is currently represented by this net node.
     */
    public NetInterface getNetInterface() {

        for (Node<?> parent : getParents()) {
            if (parent instanceof NetInterface) {
                return (NetInterface) parent;
            }
        }

        return null;
    }

    /*
     * Ensure that the net node isn't already linked to another net interface when a new link is established.
     */
    @Override
    protected void onAddParent(Node<?> parent) {

        super.onAddParent(parent);

        if (parent instanceof NetInterface) {
            for (Node<?> existingParent : getParents()) {
                if (existingParent != parent && existingParent instanceof NetInterface) {
                    throw new IllegalStateException("A net node cannot be linked to two net interfaces at the same time");
                }
            }
        }
    }

    /**
     * This is an internal utility method which hands over the given {@link Packet} to the linked {@link #getNetInterface() network interface} for being processed by the OS.
     * For doing that, it internally calls the {@link NetInterface#deliverIncoming(Packet)} method.
     * Moreover, it invokes some checks to ensure that the link is valid.
     * Note that the method should only be called by subclasses of this class and isn't intended for public use.
     *
     * @param packet The packet that has been received and should now be delivered to the OS via the {@link NetInterface}.
     */
    protected void deliverPacketToNetInterface(Packet packet) {

        Validate.validState(getNetInterface() != null, "A computer connected net node cannot deliver any packets to the OS while it is temporarily unlinked (not linked with any net interface)");
        getNetInterface().deliverIncoming(packet);
    }

}
