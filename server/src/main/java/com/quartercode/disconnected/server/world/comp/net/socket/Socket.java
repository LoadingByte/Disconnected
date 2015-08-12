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
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import org.apache.commons.lang3.Validate;
import com.quartercode.disconnected.server.sim.TickService;
import com.quartercode.disconnected.server.sim.scheduler.DefaultScheduler;
import com.quartercode.disconnected.server.sim.scheduler.Scheduler;
import com.quartercode.disconnected.server.sim.scheduler.SchedulerTask;
import com.quartercode.disconnected.server.sim.scheduler.SchedulerTaskAdapter;
import com.quartercode.disconnected.server.util.ObjArray;
import com.quartercode.disconnected.server.world.comp.net.Packet;
import com.quartercode.disconnected.server.world.util.WorldNode;
import com.quartercode.disconnected.shared.world.comp.net.Address;
import com.quartercode.jtimber.api.node.wrapper.SubstituteWithWrapper;
import com.quartercode.jtimber.api.node.wrapper.collection.ListWrapper;

/**
 * Sockets allow the sending of {@link Packet}s between two {@link Address}es.
 * For doing that, they must establish a connection between the addresses before any packets can be sent/received.
 * Note that a random free port is chosen by the operating system if a client, which does not have any open ports, connects to some other computer.
 */
public class Socket extends WorldNode<SocketRegistry> {

    /**
     * The amount of ticks after which a socket whose {@link #connect()} method had been called is terminated if no connection was established.
     * By default, this value is equal to one second.
     */
    public static final int           CONNECTION_TIMEOUT        = 1 * TickService.DEFAULT_TICKS_PER_SECOND;

    /**
     * The amount of ticks after which a keepalive request is sent periodically.
     * After such a request was sent, the remote socket has a limited time ({@link #KEEPALIVE_REPONSE_TIMEOUT}) to send the response.
     */
    public static final int           KEEPALIVE_PERIOD          = 10 * TickService.DEFAULT_TICKS_PER_SECOND;

    /**
     * The amount of ticks after which a sent keepalive request must have been answered.
     */
    public static final int           KEEPALIVE_REPONSE_TIMEOUT = 1 * TickService.DEFAULT_TICKS_PER_SECOND;

    @XmlElement (type = DefaultScheduler.class)
    private final Scheduler<Socket>   scheduler                 = new DefaultScheduler<>();

    @XmlAttribute
    private int                       localPort;
    @XmlElement
    private Address                   destination;

    @XmlElementWrapper
    @XmlElement (name = "packetHandler")
    @SubstituteWithWrapper (ListWrapper.class)
    private final List<PacketHandler> packetHandlers            = new ArrayList<>();

    @XmlAttribute
    private SocketState               state                     = SocketState.INACTIVE;
    @XmlAttribute
    private int                       currentSeqNumber;

    /*
     * A buffer with all received packet data payloads that need to be processed by the user of the socket.
     * All incoming packet payloads are added to the this buffer.
     * When the scheduler calls the group "computer.processUpdate", all these payloads are polled and handed over to the socket's packet handlers.
     */
    @XmlElementWrapper
    @XmlElement (name = "packet")
    @SubstituteWithWrapper (ListWrapper.class)
    private final List<Object>        incomingPacketBuffer      = new LinkedList<>();

    /**
     * Creates a new socket.
     * This constructor should only be called by a {@link SocketRegistry}.
     * Don't forget to call {@link #initialize(int, Address)} after the socket has been added to the socket registry's internal socket list.
     */
    protected Socket() {

    }

    /**
     * Initializes the socket by setting the given two values in order to characterize the socket.
     * It is important that the socket has been added to the internal {@link SocketRegistry}'s socket list before this method is called.
     *
     * @param localPort The {@link #getLocalPort() local port} the socket is bound to.
     *        By setting it to {@code -1}, the OS chooses a random free port for the new socket.
     * @param destination The {@link Address} of the socket on the destination computer the new socket should send packets to and receive packets from.
     * @throws IllegalArgumentException If the provided local port is neither {@code -1} nor in range {@code 1 <= port <= 65535}.
     * @throws IllegalStateException If the provided combination of "localPort" and "destination" is already used by another socket.
     */
    protected void initialize(int localPort, Address destination) {

        Validate.isTrue(localPort == -1 || localPort >= 1 && localPort <= 65535,
                "The local socket port (%d) must be -1 (random) or in range 1 <= port <= 65535 (e.g. 8080)", localPort);
        Validate.notNull(destination, "Destination socket address of new socket cannot be null");

        this.localPort = localPort == -1 ? getRandomFreePort() : localPort;
        this.destination = destination;

        // Ensure that the socket connection is not already in use
        Validate.validState(isPortFree(this.localPort), "Another socket with local port '%d' and destination '%s' is already bound", localPort, destination);
    }

    /*
     * Returns a randomly chosen port which can be used by this socket because its not occupied by another socket.
     * That means that there's no other socket which uses the same combination of "localPort" and "destination".
     */
    private int getRandomFreePort() {

        int minPort = 49152;
        int maxPort = 65535;

        Random random = getRandom();

        int port;
        do {
            port = random.nextInt(maxPort + 1 - minPort) + minPort;
        } while (!isPortFree(port));

        return port;
    }

    /*
     * Returns whether the given port can be used by this socket because it's not already occupied by another socket.
     */
    private boolean isPortFree(int port) {

        for (Socket socket : getSingleParent().getSockets()) {
            if (socket != this && socket.getLocalPort() == port && socket.getDestination().equals(destination)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns the local port the socket is bound to.
     * In combination with the {@link #getDestination() destination} of the socket connection, this field defines exactly one possible socket connection.
     * Therefore, two sockets can be bound to the same local port as long as they have different destinations.
     *
     * @return The local port of the socket.
     */
    public int getLocalPort() {

        return localPort;
    }

    /**
     * Returns the {@link Address} of the socket on the destination computer this socket sends packets to and receives packets from.
     *
     * @return The destination socket's network address.
     */
    public Address getDestination() {

        return destination;
    }

    /**
     * Returns a collection of {@link PacketHandler}s that are called every time a new {@link Packet} arrives.<br>
     * <br>
     * Internally, these handlers are called when scheduler invokes the group {@code computer.processUpdate}.
     * When that happens, all buffered packets are polled from the internal incoming packet buffer and are handed over to these handlers.
     *
     * @return The packet handlers that are called whenever a packet arrives.
     */
    public List<PacketHandler> getPacketHandlers() {

        return Collections.unmodifiableList(packetHandlers);
    }

    /**
     * Adds a new {@link PacketHandler} that will be called every time a new {@link Packet} arrives.
     * See {@link #getPacketHandlers()} for more information on packet handlers.
     *
     * @param packetHandler The packet handler to add.
     */
    public void addPacketHandler(PacketHandler packetHandler) {

        packetHandlers.add(packetHandler);
    }

    /**
     * Removes the given {@link PacketHandler} so that it will no longer be called when new {@link Packet}s arrive.
     * See {@link #getPacketHandlers()} for more information on packet handlers.
     *
     * @param packetHandler The packet handler to remove.
     */
    public void removePacketHandler(PacketHandler packetHandler) {

        packetHandlers.remove(packetHandler);
    }

    /**
     * Returns the current {@link SocketState state} of the socket.
     * The state describes whether the socket is currently {@link SocketState#INACTIVE inactive}, inside a {@link SocketState#HANDSHAKE_SYN handshake phase},
     * is {@link SocketState#CONNECTED connected}, is currently {@link SocketState#RECEIVED_TEARDOWN tearing down}, or has already been {@link SocketState#DISCONNECTED disconnected}.
     *
     * @return The current socket state.
     */
    public SocketState getState() {

        return state;
    }

    /**
     * Changes the current {@link #getState() state} of the socket.
     * This method is necessary since it needs to execute some further actions depending on the new state of the socket.
     * For example, if the socket's state is changed to {@link SocketState#CONNECTED}, the keepalive task needs to be scheduled.
     *
     * @param state The new socket state.
     */
    protected void setState(SocketState state) {

        SocketState oldState = this.state;
        this.state = state;

        // If the state is changed to "HANDSHAKE_SYN"
        if (state == SocketState.HANDSHAKE_SYN && oldState != SocketState.HANDSHAKE_SYN) {
            scheduler.schedule("connectionTimeout", "networkUpdate", CONNECTION_TIMEOUT, new ConnectionTimeoutTask());
        }

        // If the state is changed to "CONNECTED"
        if (state == SocketState.CONNECTED && oldState != SocketState.CONNECTED) {
            // Note that these tasks do not need to be cancelled explicitly since the scheduler object is automatically unlinked once the socket is disconnected
            scheduler.schedule("scheduleKeepalive", "networkUpdate", KEEPALIVE_PERIOD, KEEPALIVE_PERIOD, new ScheduleKeepaliveTask());
            scheduler.schedule("handleIncomingBufferedPackets", "computer.processUpdate", 1, 1, new HandleBufferedPacketsTask());

            // Inform all socket connection listeners about the successfully established connection
            SocketRegistry socketRegistry = getSingleParent();
            if (socketRegistry != null) {
                for (SocketConnectionListener connectionListener : socketRegistry.getConnectionListeners()) {
                    connectionListener.onEstablish(this);
                }
            }
        }

        // If the state is changed to "DISCONNECTED"
        if (state == SocketState.DISCONNECTED && oldState != SocketState.DISCONNECTED) {
            SocketRegistry socketRegistry = getSingleParent();
            if (socketRegistry != null) {
                getSingleParent().removeDisconnectedSocket(this);
            }
        }
    }

    /**
     * Generates a new totally random sequence number.
     * The sequence number is internally used for socket management (e.g. handshakes).
     */
    protected void generateSeqNumber() {

        currentSeqNumber = getRandom().nextInt();
    }

    /**
     * Connects the socket using the data that was set during the construction of the socket.
     * The {@link #send()} method can only be used after this method has been called.
     * Note that the handshake packets that initiate the connection are sent immediately.
     * Therefore, <b>the entire handshake only takes one single tick</b> although multiple packets are sent back and forth.
     *
     * @throws IllegalStateException If the {@link #getState() state} of the socket is not {@link SocketState#INACTIVE}.
     */
    public void connect() {

        Validate.validState(state == SocketState.INACTIVE, "Cannot connect socket because it is not in state INACTIVE (current state is '%s')", state);

        // Start the handshake
        setState(SocketState.HANDSHAKE_SYN);
        generateSeqNumber();
        // Send the first handshake packet (syn) with the local sequence number
        send(new ObjArray("$_handshake", "syn", currentSeqNumber));
    }

    /**
     * Disconnects the socket and makes it unusable.
     * The {@link #send()} method can no longer be used after this method has been called.
     * Note that the handshake packets that initiate the connection are sent immediately.
     * Therefore, <b>the entire teardown only takes one single tick</b> although multiple packets are sent back and forth.
     */
    public void disconnect() {

        if (state != SocketState.RECEIVED_TEARDOWN && state != SocketState.DISCONNECTED) {
            send("$_teardown");
        }

        setState(SocketState.DISCONNECTED);
    }

    /**
     * Sends the given data payload object to the set {@link #getDestination() destination} socket <b>as soon as scheduler calls the group {@code computer.processUpdate}</b>.
     * For doing that, this method wraps the data object inside a {@link Packet}.
     * Note that a program which receives a packet cannot handle it during the same tick it was sent.
     * Instead, the received packet will be handled during the next tick.
     *
     * @param data The data payload object which should be sent over the socket to the destination.
     * @see Packet#getData()
     */
    public void send(Object data) {

        SocketRegistry socketRegistry = getSingleParent();

        if (socketRegistry != null) {
            boolean internal = isInternalPacketDefString(data) || data instanceof ObjArray && isInternalPacketDefString( ((ObjArray) data).getArray()[0]);

            // Internal packets can always be sent
            // Regular user packets can only be sent if the socket is connected
            if (internal || state == SocketState.CONNECTED) {
                // Only internal packets are sent instantly
                socketRegistry.sendPacket(this, data, internal);
            }
        }
    }

    private boolean isInternalPacketDefString(Object data) {

        return data instanceof String && ((String) data).startsWith("$_");
    }

    /**
     * Handles the given {@link Packet} data payload object which has been sent by the connected socket on the set {@link #getDestination() destination} computer.
     * If the received packet isn't an internal one (e.g. related to the handshake), this method will add it to the internal incoming packet buffer.
     * That means that the packet will be forwarded to all {@link #getPacketHandlers() packet handlers}.<br>
     * <br>
     * Note that this method should only be called by the {@link SocketRegistry} that stores the socket.
     *
     * @param packet The packet that was received and should be handled by the socket.
     * @see Packet
     */
    protected void handle(Object data) {

        /*
         * Process all internal packets.
         */
        if (data instanceof ObjArray) {
            Object[] dataArray = ((ObjArray) data).getArray();

            if (dataArray.length >= 1 && dataArray[0].equals("$_handshake")) {
                handleHandshake(dataArray);
                return;
            } else if (dataArray.length == 2 && dataArray[0].equals("$_keepalive")) {
                handleKeepalive(dataArray);
                return;
            }
        }

        if (data instanceof String && data.equals("$_teardown")) {
            handleTeardown();
            return;
        }

        // If the packet is not internal, process it as a regular user packet
        handleRegularPacket(data);
    }

    private void handleHandshake(Object[] dataArray) {

        // If a syn packet was received
        if (state == SocketState.INACTIVE && dataArray.length == 3 && dataArray[1].equals("syn") && dataArray[2] instanceof Integer) {
            int destinationSeqNumber = (int) dataArray[2];

            setState(SocketState.HANDSHAKE_SYN);
            // Generate a new sequence number
            generateSeqNumber();
            // Send the second handshake packet (syn-ack) with the local sequence number and the ack sequence number
            send(new ObjArray("$_handshake", "syn-ack", currentSeqNumber, destinationSeqNumber + 1));
        }
        // If a syn-ack packet was received
        else if (state == SocketState.HANDSHAKE_SYN && dataArray.length == 4 && dataArray[1].equals("syn-ack") && dataArray[2] instanceof Integer && dataArray[3] instanceof Integer) {
            int destinationSeqNumber = (int) dataArray[2];
            int ackSeqNumber = (int) dataArray[3];

            // Check whether the ack sequence number is correct
            if (ackSeqNumber == currentSeqNumber + 1) {
                // Mark the connection as established
                setState(SocketState.CONNECTED);
                // Send the final handshake packet (ack) with the destination sequence number increased by 1
                send(new ObjArray("$_handshake", "ack", destinationSeqNumber + 1));
            } else {
                // Terminate the connection
                disconnect();
            }
        }
        // If an ack packet was received
        else if (state == SocketState.HANDSHAKE_SYN && dataArray.length == 3 && dataArray[1].equals("ack") && dataArray[2] instanceof Integer) {
            int ackId = (int) dataArray[2];

            // Check whether the ack sequence number is correct
            if (ackId == currentSeqNumber + 1) {
                // Mark the connection as established
                setState(SocketState.CONNECTED);
            } else {
                // Terminate the connection
                disconnect();
            }
        }
        // If an invalid handshake packet was received
        else {
            // Terminate the connection
            disconnect();
        }
    }

    private void handleKeepalive(Object[] dataArray) {

        // Terminate the connection if a non-handshake and non-teardown packet comes through although the connection hasn't been established yet
        if (state != SocketState.CONNECTED) {
            disconnect();
        } else {
            // If a request packet was received
            if (dataArray[1].equals("req")) {
                // Answer with a response packet
                send(new ObjArray("$_keepalive", "rsp"));
            }
            // If a response packet was received
            else if (dataArray[1].equals("rsp")) {
                // Cancel the current keepalive timeout scheduler task if one exists
                SchedulerTask<?> keepaliveTimeoutTask = scheduler.getTaskByName("keepaliveTimeout");
                if (keepaliveTimeoutTask != null) {
                    keepaliveTimeoutTask.cancel();
                }
            }
            // If an invalid keepalive packet was received
            else {
                // Terminate the connection
                disconnect();
            }
        }
    }

    private void handleTeardown() {

        setState(SocketState.RECEIVED_TEARDOWN);
        disconnect();
    }

    private void handleRegularPacket(Object data) {

        // Terminate the connection if a non-handshake and non-teardown packet comes through although the connection hasn't been established yet
        if (state != SocketState.CONNECTED) {
            disconnect();
        } else {
            incomingPacketBuffer.add(data);
        }
    }

    /**
     * This {@link SchedulerTask} is called when a timeout occurs during the {@link Socket} handshake.
     * It basically disconnects the socket and therefore removes it.
     *
     * @see Socket
     */
    protected static class ConnectionTimeoutTask extends SchedulerTaskAdapter<Socket> {

        @Override
        public void execute(Scheduler<? extends Socket> scheduler, Socket socket) {

            if (socket.getState() != SocketState.CONNECTED) {
                socket.disconnect();
            }
        }

    }

    /**
     * This {@link SchedulerTask} is called on a regular basis to verify the availability of the connection.
     * It schedules a new task for calling {@link Socket#disconnect()} (name {@code keepaliveTimeout}) and sends a {@code keepalive} request packet to the remote {@link Socket}.
     * Once the keepalive response packet arrives, the timeout task is cancelled.
     *
     * @see Socket
     */
    protected static class ScheduleKeepaliveTask extends SchedulerTaskAdapter<Socket> {

        @Override
        public void execute(Scheduler<? extends Socket> scheduler, Socket socket) {

            // Schedule a new task that disconnects the connection after some time
            // That new task is cancelled once a keepalive response packet arrives
            scheduler.schedule("keepaliveTimeout", "networkUpdate", KEEPALIVE_REPONSE_TIMEOUT, new KeepaliveTimeoutTask());

            // Send the keepalive request
            socket.send(new ObjArray("$_keepalive", "req"));
        }

    }

    /**
     * This {@link SchedulerTask} calls {@link Socket#disconnect()} in order to terminate a socket connection if a keepalive failed.
     * Note that this timeout task is cancelled once the keepalive response packet arrives.
     *
     * @see Socket
     * @see ScheduleKeepaliveTask
     */
    protected static class KeepaliveTimeoutTask extends SchedulerTaskAdapter<Socket> {

        @Override
        public void execute(Scheduler<? extends Socket> scheduler, Socket socket) {

            socket.disconnect();
        }

    }

    /**
     * This {@link SchedulerTask} reads and delivers all packets from the incoming packet buffer to all {@link Socket#getPacketHandlers() packet handlers}.
     *
     * @see Socket
     */
    protected static class HandleBufferedPacketsTask extends SchedulerTaskAdapter<Socket> {

        @Override
        public void execute(Scheduler<? extends Socket> scheduler, Socket socket) {

            // While packets need to be handled
            while (!socket.incomingPacketBuffer.isEmpty()) {
                // Retrieve the next packet data payload
                Object packetData = socket.incomingPacketBuffer.get(0);
                socket.incomingPacketBuffer.remove(0);

                for (PacketHandler packetHandler : socket.getPacketHandlers()) {
                    // While the socket is marked as connected
                    if (socket.getState() != SocketState.CONNECTED) {
                        return;
                    }

                    // Handle the packet using the current packet handler
                    packetHandler.handle(socket, packetData);
                }
            }
        }

    }

}
