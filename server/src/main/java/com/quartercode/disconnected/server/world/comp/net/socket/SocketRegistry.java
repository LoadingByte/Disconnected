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

package com.quartercode.disconnected.server.world.comp.net.socket;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import com.quartercode.disconnected.server.world.comp.net.NetworkModule;
import com.quartercode.disconnected.server.world.comp.net.Packet;
import com.quartercode.disconnected.server.world.comp.net.StandardPacket;
import com.quartercode.disconnected.server.world.comp.net.socket.SocketConnectionListener.ConnectionAllowance;
import com.quartercode.disconnected.server.world.util.WorldNode;
import com.quartercode.disconnected.shared.world.comp.net.Address;
import com.quartercode.jtimber.api.node.wrapper.SubstituteWithWrapper;
import com.quartercode.jtimber.api.node.wrapper.collection.ListWrapper;

/**
 * This class is a part of the {@link NetworkModule} which is used to manage {@link Socket}s as well as sending and receiving {@link Packet}s for them.
 * It is able to organize all sockets in an internal {@link #getSockets() socket list}.
 *
 * @see Packet
 * @see Socket
 * @see NetworkModule
 */
public class SocketRegistry extends WorldNode<NetworkModule> implements SocketRegistryAPI {

    @XmlElement
    @SubstituteWithWrapper (ListWrapper.class)
    private final List<Socket>                   sockets             = new ArrayList<>();
    @XmlElement
    @SubstituteWithWrapper (ListWrapper.class)
    private final List<SocketConnectionListener> connectionListeners = new ArrayList<>();

    @Override
    public List<Socket> getSockets() {

        return Collections.unmodifiableList(sockets);
    }

    /**
     * If the given {@link Socket} has been {@link SocketState#DISCONNECTED disconnected}, this method removes it from the {@link #getSockets() socket list}.
     * Note that this method should always and only be called by the disconnected sockets themselves.
     *
     * @param socket The disconnected socket which should be removed.
     */
    protected void removeDisconnectedSocket(Socket socket) {

        if (socket.getState() == SocketState.DISCONNECTED) {
            sockets.remove(socket);
        }
    }

    @Override
    public List<SocketConnectionListener> getConnectionListeners() {

        return Collections.unmodifiableList(connectionListeners);
    }

    @Override
    public void addConnectionListener(SocketConnectionListener connectionListener) {

        connectionListeners.add(connectionListener);
    }

    @Override
    public void removeConnectionListener(SocketConnectionListener connectionListener) {

        connectionListeners.remove(connectionListener);
    }

    @Override
    public Socket createSocket(Address destination) {

        return createSocket(destination, -1);
    }

    @Override
    public Socket createSocket(Address destination, int localPort) {

        Socket socket = new Socket();
        sockets.add(socket);

        try {
            socket.initialize(localPort, destination);
        } catch (IllegalArgumentException | IllegalStateException e) {
            sockets.remove(socket);
            throw e;
        }

        return socket;
    }

    /**
     * Sends a new {@link Packet}, caused by the given {@link Socket}, with the given data object over the network.
     * This internal method allows to specify whether the packet should be delivered instantly or at the end of the current tick.
     * Instant packets are useful for internal socket packets, e.g. the packets involved in the handshake.
     * By sending those packets instantly, the user doesn't have to wait three ticks until the socket connection is established.
     *
     * @param socket The socket which calls the method and likes to send the packet.
     *        This socket is the "causer" of the packet.
     * @param data The data object which should be put into the packet's {@link Packet#getData() data payload attribute}.
     * @param instantly Whether the packet should be sent instantly ({@code true}) or at the end of the current tick ({@code false}).
     */
    protected void sendPacket(Socket socket, Object data, boolean instantly) {

        NetworkModule networkModule = getNetworkModule();

        // Construct the address of the sending socket
        Address source = new Address(networkModule.getNetInterface().getNetNode().getNetId(), socket.getLocalPort());

        // Construct a new packet
        Packet packet = new StandardPacket(source, socket.getDestination(), data);

        // Send the packet
        networkModule.sendPacket(packet, instantly);
    }

    /*
     * An internal utility method that returns the network module which uses this socket registry.
     * The returned network module can be used to send packets over the network.
     * This method is protected so that it can be overridden by test subclasses.
     */
    protected NetworkModule getNetworkModule() {

        return getSingleParent();
    }

    /**
     * This internal method <b>immediately</b> hands over the given {@link Packet} to the {@link Socket}s which has opened the associated connection.
     * It should only be called by the {@link NetworkModule} which uses this socket registry.
     * If there is no responsible socket for the received packet yet, a new one is created and all {@link #getConnectionListeners() connection listeners} are notified.
     *
     * @param packet The packet which should be delivered to the responsible socket.
     */
    public void handlePacket(Packet packet) {

        Address packetSource = packet.getSource();
        int packetDestinationPort = packet.getDestination().getPort();

        // Find the socket the packet was sent to
        Socket responsibleSocket = null;
        for (Socket socket : getSockets()) {
            if (socket.getLocalPort() == packetDestinationPort && socket.getDestination().equals(packetSource)) {
                responsibleSocket = socket;
                break;
            }
        }

        // Create a new socket if the destination of the packet is not yet bound
        // Note that this socket creation must be allowed by the connection listeners before it is able to succeed
        if (responsibleSocket == null) {
            responsibleSocket = tryCreateSocket(packetSource, packetDestinationPort);
        }

        // Hand the packet's data payload over to the socket so it can handle it
        if (responsibleSocket != null) {
            responsibleSocket.handle(packet.getData());
        }
    }

    private Socket tryCreateSocket(Address requester, int localPort) {

        // Iterate over the opinions of all connection listeners and cancel the connection attempt if it is disallowed
        boolean allowAfterAll = false;
        for (SocketConnectionListener connectionListener : connectionListeners) {
            ConnectionAllowance allowance = connectionListener.onRequest(requester, localPort);
            if (allowance == ConnectionAllowance.ALLOW_AFTER_ALL) {
                allowAfterAll = true;
            } else if (allowance == ConnectionAllowance.REJECT_IMMEDIATELY) {
                return null;
            }
        }
        if (!allowAfterAll) {
            return null;
        }

        // If the socket creation is allowed, do create the socket
        return createSocket(requester, localPort);
    }

    /**
     * Called on the bootstrap ({@code running = true}) or shutdown ({@code running = false}) of the {@link NetworkModule} which uses this socket registry.
     *
     * @param running Whether the socket registry should start up ({@code true}) or shut down ({@code false}).
     */
    public void setRunning(boolean running) {

        // Only invoke on shutdown
        if (!running) {
            disconnectAllSockets();
        }
    }

    private void disconnectAllSockets() {

        // Need to use a new list instance because the old one is modified when a socket is disconnected
        for (Socket socket : new ArrayList<>(getSockets())) {
            socket.disconnect();
        }
    }

}
