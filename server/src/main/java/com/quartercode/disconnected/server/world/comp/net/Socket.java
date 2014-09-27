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

package com.quartercode.disconnected.server.world.comp.net;

import static com.quartercode.classmod.ClassmodFactory.create;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.reflect.TypeLiteral;
import com.quartercode.classmod.base.FeatureHolder;
import com.quartercode.classmod.extra.CollectionPropertyDefinition;
import com.quartercode.classmod.extra.FunctionDefinition;
import com.quartercode.classmod.extra.FunctionExecutor;
import com.quartercode.classmod.extra.FunctionInvocation;
import com.quartercode.classmod.extra.Prioritized;
import com.quartercode.classmod.extra.PropertyDefinition;
import com.quartercode.classmod.extra.storage.StandardStorage;
import com.quartercode.classmod.extra.valuefactory.CloneValueFactory;
import com.quartercode.classmod.extra.valuefactory.ConstantValueFactory;
import com.quartercode.disconnected.server.sim.TickService;
import com.quartercode.disconnected.server.sim.scheduler.SchedulerTask;
import com.quartercode.disconnected.server.sim.scheduler.SchedulerTaskAdapter;
import com.quartercode.disconnected.server.sim.scheduler.SchedulerUser;
import com.quartercode.disconnected.server.util.ObjArray;
import com.quartercode.disconnected.server.util.RandomPool;
import com.quartercode.disconnected.server.world.WorldChildFeatureHolder;
import com.quartercode.disconnected.server.world.general.StringRepresentable;

/**
 * Sockets allow the sending of {@link Packet}s between two {@link Address}es.
 * For doing that, they must establish a connection between the addresses before any packets can be sent/received.
 * Note that a random free port is chosen by the os if a client, which does not have any open ports, connects to some other computer.
 */
public class Socket extends WorldChildFeatureHolder<NetworkModule> implements SchedulerUser, StringRepresentable {

    /**
     * The amount of ticks after which a socket whose {@link #CONNECT} method had been called is terminated if no connection was established.
     * By default, this value is equal to one second.
     */
    public static final int CONNECTION_TIMEOUT        = 1 * TickService.DEFAULT_TICKS_PER_SECOND;

    /**
     * The amount of ticks after which a keepalive request is sent periodically.
     * After such a request was sent, the remote socket has a limited time ({@link #KEEPALIVE_REPONSE_TIMEOUT}) to send the response.
     */
    public static final int KEEPALIVE_PERIOD          = 10 * TickService.DEFAULT_TICKS_PER_SECOND;

    /**
     * The amount of ticks after which a sent keepalive request must have been answered.
     */
    public static final int KEEPALIVE_REPONSE_TIMEOUT = 1 * TickService.DEFAULT_TICKS_PER_SECOND;

    /**
     * An enumeration which describes the different states a {@link Socket} can be in.
     * 
     * @see Socket#STATE
     */
    public static enum SocketState {

        /**
         * The socket was just created and hasn't been connected yet.
         */
        INACTIVE,
        /**
         * The socket sent its {@code syn} or {@code syn-ack} {@link Packet} as part of the handshake.
         * The handshake is triggered by the {@link Socket#CONNECT} method.
         */
        HANDSHAKE_SYN,
        /**
         * The socket is ready for sending and receiving packets.
         * That means that it received the other socket's {@code ack} or {@code syn-ack} {@link Packet} as part of the handshake.
         */
        CONNECTED,
        /**
         * The socket received a teardown {@link Packet} and is about to disconnect by calling its own {@link Socket#DISCONNECT} method.
         */
        RECEIVED_TEARDOWN,
        /**
         * The socket is fully disconnected and can no longer be used for anything.
         */
        DISCONNECTED;

    }

    // ----- Properties -----

    /**
     * The local port the socket is bound to. By setting the port to 0, the os chooses a random free port for the new socket.
     * In combination with the {@link #DESTINATION}, this field defines exactly one possible socket connection.
     * Note that two sockets can run on the same port as long as they have different destinations.<br>
     * <br>
     * Exceptions that can occur when setting:
     * 
     * <table>
     * <tr>
     * <th>Exception</th>
     * <th>When?</th>
     * </tr>
     * <tr>
     * <td>{@link IllegalArgumentException}</td>
     * <td>The provided port is lesser than 0 or greater than 65535.</td>
     * </tr>
     * </table>
     */
    public static final PropertyDefinition<Integer>                                      LOCAL_PORT;

    /**
     * The {@link Address} of the destination socket on the destination computer the local socket sends packets to.
     */
    public static final PropertyDefinition<Address>                                      DESTINATION;

    /**
     * A collection of {@link PacketHandler}s that are called when a packet arrives.
     */
    public static final CollectionPropertyDefinition<PacketHandler, List<PacketHandler>> PACKET_HANDLERS;

    /**
     * The current {@link SocketState} of the socket.
     * The state describes whether the socket is currently inactive, inside a handshake phase, connected or has already been disconnected.
     */
    public static final PropertyDefinition<SocketState>                                  STATE;

    /**
     * A temporary field which stores the sequence number that is currently used for an internal activity.
     * For example, this property is used for storing the local sequence number during the handshake.<br>
     * <br>
     * By setting this value to -1 or any other value lesser than 0, the property automatically generates a random sequence number.
     * However, this field should not be modified from the outside!
     */
    public static final PropertyDefinition<Integer>                                      CURRENT_SEQ_NUMBER;

    static {

        LOCAL_PORT = create(new TypeLiteral<PropertyDefinition<Integer>>() {}, "name", "localPort", "storage", new StandardStorage<>());
        LOCAL_PORT.addSetterExecutor("checkRange", Socket.class, new FunctionExecutor<Void>() {

            @Override
            @Prioritized (Prioritized.LEVEL_6)
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                int port = (Integer) arguments[0];
                Validate.isTrue(port >= 0 && port <= 65535, "The port (%d) must be in range 0 (random) <= port <= 65535 (e.g. 8080)", port);

                return invocation.next(arguments);
            }

        });

        DESTINATION = create(new TypeLiteral<PropertyDefinition<Address>>() {}, "name", "destination", "storage", new StandardStorage<>());
        PACKET_HANDLERS = create(new TypeLiteral<CollectionPropertyDefinition<PacketHandler, List<PacketHandler>>>() {}, "name", "packetHandlers", "storage", new StandardStorage<>(), "collection", new CloneValueFactory<>(new ArrayList<>()));

        STATE = create(new TypeLiteral<PropertyDefinition<SocketState>>() {}, "name", "state", "storage", new StandardStorage<>(), "initialValue", new ConstantValueFactory<>(SocketState.INACTIVE));
        STATE.addSetterExecutor("scheduleKeepalive", Socket.class, new FunctionExecutor<Void>() {

            @Override
            @Prioritized (Prioritized.LEVEL_7)
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                FeatureHolder holder = invocation.getHolder();
                boolean stateChangesToConnected = arguments[0] == SocketState.CONNECTED && holder.get(Socket.STATE).get() != SocketState.CONNECTED;

                invocation.next(arguments);

                if (stateChangesToConnected) {
                    holder.get(SCHEDULER).schedule(new SchedulerTaskAdapter("scheduleKeepalive", "computerNetworkUpdate", KEEPALIVE_PERIOD, KEEPALIVE_PERIOD) {

                        @Override
                        public void execute(FeatureHolder holder) {

                            scheduleKeepaliveResponseCheck(holder);

                            holder.get(SEND).invoke(new ObjArray("$_keepalive", "req"));
                        }

                    });
                }

                return null;
            }

            /*
             * This method schedules a task that disconnects the connection after some time.
             * It is cancelled when a keepalive response packet arrives.
             */
            private void scheduleKeepaliveResponseCheck(FeatureHolder holder) {

                holder.get(SCHEDULER).schedule(new SchedulerTaskAdapter("keepaliveTimeout", "computerNetworkUpdate", KEEPALIVE_REPONSE_TIMEOUT) {

                    @Override
                    public void execute(FeatureHolder holder) {

                        holder.get(DISCONNECT).invoke();
                    }

                });
            }

        });

        CURRENT_SEQ_NUMBER = create(new TypeLiteral<PropertyDefinition<Integer>>() {}, "name", "currentSeqNumber", "storage", new StandardStorage<>());
        CURRENT_SEQ_NUMBER.addSetterExecutor("generate", Socket.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                Socket holder = (Socket) invocation.getHolder();

                if ((int) arguments[0] < 0) {
                    RandomPool random = holder.getWorld().getRandom();
                    int newSequenceNumber = random.nextInt();

                    holder.get(CURRENT_SEQ_NUMBER).set(newSequenceNumber);
                }

                return invocation.next(arguments);
            }

        });

    }

    // ----- Functions -----

    /**
     * Connects the socket using the data that was set before this call.
     * The {@link #SEND} method can only be used after this method was called.
     */
    public static final FunctionDefinition<Void>                                         CONNECT;

    /**
     * Disconnects the socket and makes it unusable.
     * The {@link #SEND} method can no longer be used after this method was called.
     */
    public static final FunctionDefinition<Void>                                         DISCONNECT;

    /**
     * Sends the given data object over the socket to the set {@link #DESTINATION} computer.
     * For doing that, this method wraps the data object inside a {@link Packet}.
     * 
     * <table>
     * <tr>
     * <th>Index</th>
     * <th>Type</th>
     * <th>Parameter</th>
     * <th>Description</th>
     * </tr>
     * <tr>
     * <td>0</td>
     * <td>{@link Object}</td>
     * <td>data</td>
     * <td>The data payload object which should be sent over the socket.</td>
     * </tr>
     * </table>
     * 
     * @see Packet#DATA
     */
    public static final FunctionDefinition<Void>                                         SEND;

    /**
     * Handles the given {@link Packet} which came in from the connected socket on the set {@link #DESTINATION} computer.
     * This method probably calls the {@link #PACKET_HANDLERS}.
     * 
     * <table>
     * <tr>
     * <th>Index</th>
     * <th>Type</th>
     * <th>Parameter</th>
     * <th>Description</th>
     * </tr>
     * <tr>
     * <td>0</td>
     * <td>{@link Packet}</td>
     * <td>packet</td>
     * <td>The packet that was received and should be handled by the socket.</td>
     * </tr>
     * </table>
     * 
     * @see Packet
     */
    public static final FunctionDefinition<Void>                                         HANDLE;

    static {

        CONNECT = create(new TypeLiteral<FunctionDefinition<Void>>() {}, "name", "connect", "parameters", new Class[0]);
        CONNECT.addExecutor("validateSettings", Socket.class, new FunctionExecutor<Void>() {

            @Override
            @Prioritized (Prioritized.LEVEL_8)
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                FeatureHolder holder = invocation.getHolder();

                Validate.notNull(holder.get(DESTINATION).get(), "Socket destination cannot be null");

                return invocation.next(arguments);
            }

        });
        CONNECT.addExecutor("scheduleTimeout", Socket.class, new FunctionExecutor<Void>() {

            @Override
            @Prioritized (Prioritized.LEVEL_6)
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                FeatureHolder holder = invocation.getHolder();

                holder.get(SCHEDULER).schedule(new SchedulerTaskAdapter("connectionTimeout", "computerNetworkUpdate", CONNECTION_TIMEOUT) {

                    @Override
                    public void execute(FeatureHolder holder) {

                        if (holder.get(STATE).get() != SocketState.CONNECTED) {
                            holder.get(DISCONNECT).invoke();
                        }
                    }

                });

                return invocation.next(arguments);
            }

        });
        CONNECT.addExecutor("default", Socket.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                Socket holder = (Socket) invocation.getHolder();

                if (holder.get(STATE).get() != SocketState.INACTIVE) {
                    throw new IllegalStateException("Cannot connect socket because it is not in state INACTIVE (current state is '" + holder.get(STATE).get() + "')");
                }

                holder.get(STATE).set(SocketState.HANDSHAKE_SYN);
                // Generate a new sequence number
                holder.get(CURRENT_SEQ_NUMBER).set(-1);
                // Send the first handshake packet (syn) with the local sequence number
                holder.get(SEND).invoke(new ObjArray("$_handshake", "syn", holder.get(CURRENT_SEQ_NUMBER).get()));

                return invocation.next(arguments);
            }

        });

        DISCONNECT = create(new TypeLiteral<FunctionDefinition<Void>>() {}, "name", "disconnect", "parameters", new Class[0]);
        DISCONNECT.addExecutor("default", Socket.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                Socket holder = (Socket) invocation.getHolder();

                if (holder.get(STATE).get() != SocketState.RECEIVED_TEARDOWN && holder.get(STATE).get() != SocketState.DISCONNECTED) {
                    holder.get(SEND).invoke("$_teardown");
                }

                holder.get(STATE).set(SocketState.DISCONNECTED);

                return invocation.next(arguments);
            }

        });

        SEND = create(new TypeLiteral<FunctionDefinition<Void>>() {}, "name", "send", "parameters", new Class[] { Object.class });
        SEND.addExecutor("default", Socket.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                Socket holder = (Socket) invocation.getHolder();

                if (holder.getParent() != null) {
                    holder.getParent().get(NetworkModule.SEND).invoke(holder, arguments[0]);
                }

                return invocation.next(arguments);
            }

        });

        HANDLE = create(new TypeLiteral<FunctionDefinition<Void>>() {}, "name", "handle", "parameters", new Class[] { Packet.class });
        HANDLE.addExecutor("processHandshake", Socket.class, new FunctionExecutor<Void>() {

            @Override
            @Prioritized (Prioritized.LEVEL_7)
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                FeatureHolder holder = invocation.getHolder();
                Object data = ((Packet) arguments[0]).get(Packet.DATA).get();

                if (data instanceof ObjArray) {
                    Object[] dataArray = ((ObjArray) data).getArray();

                    if (dataArray.length >= 1 && dataArray[0].equals("$_handshake")) {
                        // If a syn packet was received
                        if (holder.get(STATE).get() == SocketState.INACTIVE && dataArray.length == 3 && dataArray[1].equals("syn") && dataArray[2] instanceof Integer) {
                            int destinationSeqNumber = (int) dataArray[2];

                            holder.get(STATE).set(SocketState.HANDSHAKE_SYN);
                            // Generate a new sequence number
                            holder.get(CURRENT_SEQ_NUMBER).set(-1);
                            // Send the second handshake packet (syn-ack) with the local sequence number and the ack sequence number
                            holder.get(SEND).invoke(new ObjArray("$_handshake", "syn-ack", holder.get(CURRENT_SEQ_NUMBER).get(), destinationSeqNumber + 1));
                        }
                        // If a syn-ack packet was received
                        else if (holder.get(STATE).get() == SocketState.HANDSHAKE_SYN && dataArray.length == 4 && dataArray[1].equals("syn-ack") && dataArray[2] instanceof Integer && dataArray[3] instanceof Integer) {
                            int destinationSeqNumber = (int) dataArray[2];
                            int ackSeqNumber = (int) dataArray[3];

                            // Check whether the ack sequence number is correct
                            if (ackSeqNumber == holder.get(CURRENT_SEQ_NUMBER).get() + 1) {
                                // Mark the connection as established
                                holder.get(STATE).set(SocketState.CONNECTED);
                                // Send the final handshake packet (ack) with the destination sequence number increased by 1
                                holder.get(SEND).invoke(new ObjArray("$_handshake", "ack", destinationSeqNumber + 1));
                            } else {
                                // Terminate the connection
                                holder.get(DISCONNECT).invoke();
                            }
                        }
                        // If an ack packet was received
                        else if (holder.get(STATE).get() == SocketState.HANDSHAKE_SYN && dataArray.length == 3 && dataArray[1].equals("ack") && dataArray[2] instanceof Integer) {
                            int ackId = (int) dataArray[2];

                            // Check whether the ack sequence number is correct
                            if (ackId == holder.get(CURRENT_SEQ_NUMBER).get() + 1) {
                                // Mark the connection as established
                                holder.get(STATE).set(SocketState.CONNECTED);
                            } else {
                                // Terminate the connection
                                holder.get(DISCONNECT).invoke();
                            }
                        }
                        // If an invalid handshake packet was received
                        else {
                            // Terminate the connection
                            holder.get(DISCONNECT).invoke();
                        }

                        return null;
                    }
                }

                return invocation.next(arguments);
            }

        });
        HANDLE.addExecutor("processKeepalive", Socket.class, new FunctionExecutor<Void>() {

            @Override
            @Prioritized (Prioritized.LEVEL_7)
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                FeatureHolder holder = invocation.getHolder();

                if (holder.get(STATE).get() == SocketState.CONNECTED) {
                    Object data = ((Packet) arguments[0]).get(Packet.DATA).get();

                    if (data instanceof ObjArray) {
                        Object[] dataArray = ((ObjArray) data).getArray();

                        if (dataArray.length == 2 && dataArray[0].equals("$_keepalive")) {
                            // If a request packet was received
                            if (dataArray[1].equals("req")) {
                                // Answer with a response packet
                                holder.get(SEND).invoke(new ObjArray("$_keepalive", "rsp"));
                            }
                            // If a response packet was received
                            else if (dataArray[1].equals("rsp")) {
                                // Cancel the current keepalive timeout scheduler task if one exists
                                SchedulerTask keepaliveTimeoutTask = holder.get(SCHEDULER).getTask("keepaliveTimeout");
                                if (keepaliveTimeoutTask != null) {
                                    keepaliveTimeoutTask.cancel();
                                }
                            }
                            // If an invalid keepalive packet was received
                            else {
                                // Terminate the connection
                                holder.get(DISCONNECT).invoke();
                            }

                            return null;
                        }
                    }
                }

                return invocation.next(arguments);
            }

        });
        HANDLE.addExecutor("processTeardown", Socket.class, new FunctionExecutor<Void>() {

            @Override
            @Prioritized (Prioritized.LEVEL_7)
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                FeatureHolder holder = invocation.getHolder();
                Object data = ((Packet) arguments[0]).get(Packet.DATA).get();

                if (data instanceof String && data.equals("$_teardown")) {
                    holder.get(STATE).set(SocketState.RECEIVED_TEARDOWN);
                    holder.get(DISCONNECT).invoke();
                    return null;
                }

                return invocation.next(arguments);
            }

        });
        HANDLE.addExecutor("default", Socket.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                FeatureHolder holder = invocation.getHolder();

                // Terminate the connection if a non-handshake and non-teardown packet comes through although the connection hasn't been established yet
                if (holder.get(STATE).get() != SocketState.CONNECTED) {
                    holder.get(DISCONNECT).invoke();
                    return null;
                }

                Object data = ((Packet) arguments[0]).get(Packet.DATA).get();

                for (PacketHandler packetHandler : holder.get(PACKET_HANDLERS).get()) {
                    packetHandler.handle(data);
                }

                return invocation.next(arguments);
            }

        });

    }

    /**
     * Creates a new socket.
     */
    public Socket() {

        setParentType(NetworkModule.class);
    }

    /**
     * A packet handle is a simple functional class which is called when a {@link Packet} arrives at a {@link Socket}.
     * However, the handler only receives the carried data object and no further metadata.<br>
     * <br>
     * Sadly, this class must be abstract since JAXB can't handle interfaces.
     * 
     * @see Socket
     * @see Packet
     */
    public static abstract class PacketHandler {

        /**
         * This method is called when a {@link Packet} with the given data object arrives at the {@link Socket} the handler is added to.
         * 
         * @param data The data object that was carried by the received packet.
         */
        public abstract void handle(Object data);

    }

}