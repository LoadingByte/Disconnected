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

import static com.quartercode.classmod.extra.func.Priorities.*;
import static com.quartercode.classmod.factory.ClassmodFactory.factory;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.Validate;
import com.quartercode.classmod.extra.conv.CFeatureHolder;
import com.quartercode.classmod.extra.func.FunctionDefinition;
import com.quartercode.classmod.extra.func.FunctionExecutor;
import com.quartercode.classmod.extra.func.FunctionInvocation;
import com.quartercode.classmod.extra.prop.CollectionPropertyDefinition;
import com.quartercode.classmod.extra.prop.PropertyDefinition;
import com.quartercode.classmod.extra.storage.StandardStorage;
import com.quartercode.classmod.extra.valuefactory.CloneValueFactory;
import com.quartercode.classmod.extra.valuefactory.ConstantValueFactory;
import com.quartercode.classmod.factory.CollectionPropertyDefinitionFactory;
import com.quartercode.classmod.factory.FunctionDefinitionFactory;
import com.quartercode.classmod.factory.PropertyDefinitionFactory;
import com.quartercode.disconnected.server.sim.TickService;
import com.quartercode.disconnected.server.sim.scheduler.SchedulerTask;
import com.quartercode.disconnected.server.sim.scheduler.SchedulerTaskAdapter;
import com.quartercode.disconnected.server.sim.scheduler.SchedulerUser;
import com.quartercode.disconnected.server.util.ObjArray;
import com.quartercode.disconnected.server.world.util.StringRepresentable;
import com.quartercode.disconnected.server.world.util.WorldChildFeatureHolder;
import com.quartercode.disconnected.shared.world.comp.net.Address;

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

        LOCAL_PORT = factory(PropertyDefinitionFactory.class).create("localPort", new StandardStorage<>());
        LOCAL_PORT.addSetterExecutor("checkRange", Socket.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                int port = (Integer) arguments[0];
                Validate.isTrue(port >= 0 && port <= 65535, "The port (%d) must be in range 0 (random) <= port <= 65535 (e.g. 8080)", port);

                return invocation.next(arguments);
            }

        }, LEVEL_6);

        DESTINATION = factory(PropertyDefinitionFactory.class).create("destination", new StandardStorage<>());
        PACKET_HANDLERS = factory(CollectionPropertyDefinitionFactory.class).create("packetHandlers", new StandardStorage<>(), new CloneValueFactory<>(new ArrayList<>()));

        STATE = factory(PropertyDefinitionFactory.class).create("state", new StandardStorage<>(), new ConstantValueFactory<>(SocketState.INACTIVE));
        STATE.addSetterExecutor("scheduleConnectionTimeout", Socket.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                CFeatureHolder holder = invocation.getCHolder();
                boolean stateChangesToHandshakeSyn = arguments[0] == SocketState.HANDSHAKE_SYN && holder.getObj(Socket.STATE) != SocketState.HANDSHAKE_SYN;

                invocation.next(arguments);

                if (stateChangesToHandshakeSyn) {
                    holder.get(SCHEDULER).schedule(new SchedulerTaskAdapter("connectionTimeout", "computerNetworkUpdate", CONNECTION_TIMEOUT) {

                        @Override
                        public void execute(CFeatureHolder holder) {

                            if (holder.getObj(STATE) != SocketState.CONNECTED) {
                                holder.invoke(DISCONNECT);
                            }
                        }

                    });
                }

                return null;
            }

        }, LEVEL_7);
        STATE.addSetterExecutor("scheduleKeepalive", Socket.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                CFeatureHolder holder = invocation.getCHolder();
                boolean stateChangesToConnected = arguments[0] == SocketState.CONNECTED && holder.getObj(Socket.STATE) != SocketState.CONNECTED;

                invocation.next(arguments);

                if (stateChangesToConnected) {
                    holder.get(SCHEDULER).schedule(new SchedulerTaskAdapter("scheduleKeepalive", "computerNetworkUpdate", KEEPALIVE_PERIOD, KEEPALIVE_PERIOD) {

                        @Override
                        public void execute(CFeatureHolder holder) {

                            scheduleKeepaliveResponseCheck(holder);

                            holder.invoke(SEND, new ObjArray("$_keepalive", "req"));
                        }

                    });
                }

                return null;
            }

            /*
             * This method schedules a task that disconnects the connection after some time.
             * It is cancelled when a keepalive response packet arrives.
             */
            private void scheduleKeepaliveResponseCheck(CFeatureHolder holder) {

                holder.get(SCHEDULER).schedule(new SchedulerTaskAdapter("keepaliveTimeout", "computerNetworkUpdate", KEEPALIVE_REPONSE_TIMEOUT) {

                    @Override
                    public void execute(CFeatureHolder holder) {

                        holder.invoke(DISCONNECT);
                    }

                });
            }

        }, LEVEL_7);

        CURRENT_SEQ_NUMBER = factory(PropertyDefinitionFactory.class).create("currentSeqNumber", new StandardStorage<>());
        CURRENT_SEQ_NUMBER.addSetterExecutor("generate", Socket.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                Socket holder = (Socket) invocation.getCHolder();

                if ((int) arguments[0] < 0) {
                    // Generate a new sequence number
                    holder.setObj(CURRENT_SEQ_NUMBER, holder.getRandom().nextInt());
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

        CONNECT = factory(FunctionDefinitionFactory.class).create("connect", new Class[0]);
        CONNECT.addExecutor("validateSettings", Socket.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                CFeatureHolder holder = invocation.getCHolder();

                Validate.notNull(holder.getObj(DESTINATION), "Socket destination cannot be null");

                return invocation.next(arguments);
            }

        }, LEVEL_8);
        CONNECT.addExecutor("default", Socket.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                Socket holder = (Socket) invocation.getCHolder();

                Validate.validState(holder.getObj(STATE) == SocketState.INACTIVE, "Cannot connect socket because it is not in state INACTIVE (current state is '%s')", holder.getObj(STATE));

                holder.setObj(STATE, SocketState.HANDSHAKE_SYN);
                // Generate a new sequence number
                holder.setObj(CURRENT_SEQ_NUMBER, -1);
                // Send the first handshake packet (syn) with the local sequence number
                holder.invoke(SEND, new ObjArray("$_handshake", "syn", holder.getObj(CURRENT_SEQ_NUMBER)));

                return invocation.next(arguments);
            }

        });

        DISCONNECT = factory(FunctionDefinitionFactory.class).create("disconnect", new Class[0]);
        DISCONNECT.addExecutor("default", Socket.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                Socket holder = (Socket) invocation.getCHolder();

                if (holder.getObj(STATE) != SocketState.RECEIVED_TEARDOWN && holder.getObj(STATE) != SocketState.DISCONNECTED) {
                    holder.invoke(SEND, "$_teardown");
                }

                holder.setObj(STATE, SocketState.DISCONNECTED);

                return invocation.next(arguments);
            }

        });

        SEND = factory(FunctionDefinitionFactory.class).create("send", new Class[] { Object.class });
        SEND.addExecutor("default", Socket.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                Socket holder = (Socket) invocation.getCHolder();

                if (holder.getParent() != null) {
                    holder.getParent().invoke(NetworkModule.SEND_TCP, holder, arguments[0]);
                }

                return invocation.next(arguments);
            }

        });

        HANDLE = factory(FunctionDefinitionFactory.class).create("handle", new Class[] { Packet.class });
        HANDLE.addExecutor("processHandshake", Socket.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                CFeatureHolder holder = invocation.getCHolder();
                Object data = ((Packet) arguments[0]).getObj(Packet.DATA);

                if (data instanceof ObjArray) {
                    Object[] dataArray = ((ObjArray) data).getArray();

                    if (dataArray.length >= 1 && dataArray[0].equals("$_handshake")) {
                        // If a syn packet was received
                        if (holder.getObj(STATE) == SocketState.INACTIVE && dataArray.length == 3 && dataArray[1].equals("syn") && dataArray[2] instanceof Integer) {
                            int destinationSeqNumber = (int) dataArray[2];

                            holder.setObj(STATE, SocketState.HANDSHAKE_SYN);
                            // Generate a new sequence number
                            holder.setObj(CURRENT_SEQ_NUMBER, -1);
                            // Send the second handshake packet (syn-ack) with the local sequence number and the ack sequence number
                            holder.invoke(SEND, new ObjArray("$_handshake", "syn-ack", holder.getObj(CURRENT_SEQ_NUMBER), destinationSeqNumber + 1));
                        }
                        // If a syn-ack packet was received
                        else if (holder.getObj(STATE) == SocketState.HANDSHAKE_SYN && dataArray.length == 4 && dataArray[1].equals("syn-ack") && dataArray[2] instanceof Integer && dataArray[3] instanceof Integer) {
                            int destinationSeqNumber = (int) dataArray[2];
                            int ackSeqNumber = (int) dataArray[3];

                            // Check whether the ack sequence number is correct
                            if (ackSeqNumber == holder.getObj(CURRENT_SEQ_NUMBER) + 1) {
                                // Mark the connection as established
                                holder.setObj(STATE, SocketState.CONNECTED);
                                // Send the final handshake packet (ack) with the destination sequence number increased by 1
                                holder.invoke(SEND, new ObjArray("$_handshake", "ack", destinationSeqNumber + 1));
                            } else {
                                // Terminate the connection
                                holder.invoke(DISCONNECT);
                            }
                        }
                        // If an ack packet was received
                        else if (holder.getObj(STATE) == SocketState.HANDSHAKE_SYN && dataArray.length == 3 && dataArray[1].equals("ack") && dataArray[2] instanceof Integer) {
                            int ackId = (int) dataArray[2];

                            // Check whether the ack sequence number is correct
                            if (ackId == holder.getObj(CURRENT_SEQ_NUMBER) + 1) {
                                // Mark the connection as established
                                holder.setObj(STATE, SocketState.CONNECTED);
                            } else {
                                // Terminate the connection
                                holder.invoke(DISCONNECT);
                            }
                        }
                        // If an invalid handshake packet was received
                        else {
                            // Terminate the connection
                            holder.invoke(DISCONNECT);
                        }

                        return null;
                    }
                }

                return invocation.next(arguments);
            }

        }, LEVEL_7);
        HANDLE.addExecutor("processKeepalive", Socket.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                CFeatureHolder holder = invocation.getCHolder();

                if (holder.getObj(STATE) == SocketState.CONNECTED) {
                    Object data = ((Packet) arguments[0]).getObj(Packet.DATA);

                    if (data instanceof ObjArray) {
                        Object[] dataArray = ((ObjArray) data).getArray();

                        if (dataArray.length == 2 && dataArray[0].equals("$_keepalive")) {
                            // If a request packet was received
                            if (dataArray[1].equals("req")) {
                                // Answer with a response packet
                                holder.invoke(SEND, new ObjArray("$_keepalive", "rsp"));
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
                                holder.invoke(DISCONNECT);
                            }

                            return null;
                        }
                    }
                }

                return invocation.next(arguments);
            }

        }, LEVEL_7);
        HANDLE.addExecutor("processTeardown", Socket.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                CFeatureHolder holder = invocation.getCHolder();
                Object data = ((Packet) arguments[0]).getObj(Packet.DATA);

                if (data instanceof String && data.equals("$_teardown")) {
                    holder.setObj(STATE, SocketState.RECEIVED_TEARDOWN);
                    holder.invoke(DISCONNECT);
                    return null;
                }

                return invocation.next(arguments);
            }

        }, LEVEL_7);
        HANDLE.addExecutor("default", Socket.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                CFeatureHolder holder = invocation.getCHolder();

                // Terminate the connection if a non-handshake and non-teardown packet comes through although the connection hasn't been established yet
                if (holder.getObj(STATE) != SocketState.CONNECTED) {
                    holder.invoke(DISCONNECT);
                    return null;
                }

                Object data = ((Packet) arguments[0]).getObj(Packet.DATA);

                for (PacketHandler packetHandler : holder.getColl(PACKET_HANDLERS)) {
                    packetHandler.invoke(PacketHandler.HANDLE, holder, data);
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

}
