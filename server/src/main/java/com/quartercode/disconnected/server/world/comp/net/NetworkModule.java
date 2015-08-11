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
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import org.apache.commons.lang3.Validate;
import com.quartercode.disconnected.server.sim.scheduler.DefaultScheduler;
import com.quartercode.disconnected.server.sim.scheduler.Scheduler;
import com.quartercode.disconnected.server.sim.scheduler.SchedulerTask;
import com.quartercode.disconnected.server.sim.scheduler.SchedulerTaskAdapter;
import com.quartercode.disconnected.server.world.comp.hardware.NetInterface;
import com.quartercode.disconnected.server.world.comp.net.socket.Socket;
import com.quartercode.disconnected.server.world.comp.net.socket.SocketConnectionListener;
import com.quartercode.disconnected.server.world.comp.net.socket.SocketRegistry;
import com.quartercode.disconnected.server.world.comp.net.socket.SocketRegistryAPI;
import com.quartercode.disconnected.server.world.comp.os.OperatingSystem;
import com.quartercode.disconnected.server.world.comp.os.mod.OSModule;
import com.quartercode.disconnected.server.world.util.WorldNode;
import com.quartercode.disconnected.shared.world.comp.net.Address;
import com.quartercode.jtimber.api.node.wrapper.SubstituteWithWrapper;
import com.quartercode.jtimber.api.node.wrapper.collection.ListWrapper;

/**
 * This class represents an {@link OSModule operating system module} which is used to send and receive network {@link Packet}s.
 * The module also abstracts the concept of packets and introduces {@link Socket}s for easier data transfer.
 * It is an essential part of the {@link OperatingSystem} and is directly used by it.<br>
 * <br>
 * Internally, the network module uses the {@link SocketRegistry} class for managing and organizing sockets.
 *
 * @see Packet
 * @see Socket
 * @see OSModule
 */
public class NetworkModule extends WorldNode<OperatingSystem> implements OSModule, SocketRegistryAPI {

    @XmlElement (type = DefaultScheduler.class)
    private final Scheduler<NetworkModule> scheduler            = new DefaultScheduler<>();

    @XmlElement
    private final SocketRegistry           socketRegistry       = new SocketRegistry();

    /*
     * A buffer with all packets that should be sent at the end of the tick (scheduler group "networkUpdate").
     * All packets that are sent using the sendPacket() method are added to this buffer.
     * When the scheduler calls the group "networkUpdate", all these packets are polled and handed over to the NetInterface of the computer.
     */
    @XmlElementWrapper
    @XmlElementRef
    @SubstituteWithWrapper (ListWrapper.class)
    private final List<Packet>             outgoingPacketBuffer = new LinkedList<>();

    /**
     * Creates a new network module.
     */
    public NetworkModule() {

        // By default, the OS module is not running
        scheduler.setActive(false);

        // Add a scheduler task for regularly sending all packets from the outgoing packet buffer
        scheduler.schedule("sendBufferedPackets", "networkUpdate", 1, 1, new SendBufferedPacketsTask());
    }

    /**
     * Returns the {@link NetInterface} which is internally used for sending {@link Packet}s over the {@link Network}.
     *
     * @return The net interface used by this network module.
     */
    public NetInterface getNetInterface() {

        return getSingleParent().getSingleParent().getSingleHardwareByType(NetInterface.class);
    }

    /**
     * An internal method for sending the given {@link Packet} over the network.
     * It allows to specify whether the packet should be delivered instantly or at the end of the current tick.
     * Instant packets are useful for internal socket packets, e.g. the packets involved in the handshake.
     * By sending those packets instantly, the user doesn't have to wait three ticks until the socket connection is established.
     *
     * @param packet The packet which should be sent.
     * @param instantly Whether the packet should be sent instantly ({@code true}) or at the end of the current tick ({@code false}).
     */
    public void sendPacket(Packet packet, boolean instantly) {

        Validate.notNull(packet, "A network module cannot send a null packet");

        if (instantly) {
            deliverPacketToNetInterface(packet);
        } else {
            outgoingPacketBuffer.add(packet);
        }
    }

    /*
     * An internal utility method that immediately sends the given packet over the network.
     * It is protected so that it can be overridden by test subclasses.
     */
    protected void deliverPacketToNetInterface(Packet packet) {

        getNetInterface().deliverOutgoing(packet);
    }

    /**
     * This internal method <b>immediately</b> hands over the given {@link Packet} to some sort of responsible packet handler.
     * Currently, all packets are delivered to the {@link Socket}s which have opened the associated connections.
     *
     * @param packet The packet which should be delivered to a packet handler.
     */
    public void handlePacket(Packet packet) {

        Validate.notNull(packet, "A network module cannot handle a null packet");
        socketRegistry.handlePacket(packet);
    }

    @Override
    public void setRunning(boolean running) {

        setSchedulerActivity(running);

        socketRegistry.setRunning(running);
    }

    private void setSchedulerActivity(boolean running) {

        scheduler.setActive(running);
    }

    // ----- Delegates to SocketRegistry -----

    @Override
    public List<Socket> getSockets() {

        return socketRegistry.getSockets();
    }

    @Override
    public List<SocketConnectionListener> getConnectionListeners() {

        return socketRegistry.getConnectionListeners();
    }

    @Override
    public void addConnectionListener(SocketConnectionListener connectionListener) {

        Validate.notNull(connectionListener, "Cannot add a null socket connection listener to network module");
        socketRegistry.addConnectionListener(connectionListener);
    }

    @Override
    public void removeConnectionListener(SocketConnectionListener connectionListener) {

        Validate.notNull(connectionListener, "Cannot remove a null socket connection listener from network module");
        socketRegistry.removeConnectionListener(connectionListener);
    }

    @Override
    public Socket createSocket(Address destination) {

        return socketRegistry.createSocket(destination);
    }

    @Override
    public Socket createSocket(Address destination, int localPort) {

        return socketRegistry.createSocket(destination, localPort);
    }

    /**
     * This {@link SchedulerTask} reads and sends all packets from the outgoing packet buffer of a {@link NetworkModule}.
     *
     * @see NetworkModule
     */
    protected static class SendBufferedPacketsTask extends SchedulerTaskAdapter<NetworkModule> {

        @Override
        public void execute(Scheduler<? extends NetworkModule> scheduler, NetworkModule netModule) {

            // While at least one more packet needs to be sent
            while (!netModule.outgoingPacketBuffer.isEmpty()) {
                // Retrieve and send the next packet
                Packet packet = netModule.outgoingPacketBuffer.get(0);
                netModule.outgoingPacketBuffer.remove(0);
                netModule.deliverPacketToNetInterface(packet);
            }
        }

    }

}
