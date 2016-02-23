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
///*
// * This file is part of Disconnected.
// * Copyright (c) 2013 QuarterCode <http://quartercode.com/>
// *
// * Disconnected is free software: you can redistribute it and/or modify
// * it under the terms of the GNU General Public License as published by
// * the Free Software Foundation, either version 3 of the License, or
// * (at your option) any later version.
// *
// * Disconnected is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// * GNU General Public License for more details.
// *
// * You should have received a copy of the GNU General Public License
// * along with Disconnected. If not, see <http://www.gnu.org/licenses/>.
// */
//package com.quartercode.disconnected.server.world.comp.prog.net.web;
//
//import static com.quartercode.classmod.extra.func.Priorities.LEVEL_3;
//import static com.quartercode.classmod.factory.ClassmodFactory.factory;
//import static com.quartercode.disconnected.server.world.comp.prog.util.ProgNetUtils.addInterruptionSocketDisconnectorRegisteringExecutor;
//import static com.quartercode.disconnected.server.world.comp.prog.util.ProgNetUtils.registerInterruptionSocketConnectionListenerRemover;
//import static com.quartercode.disconnected.server.world.comp.prog.util.ProgStateUtils.addInterruptionStopperRegisteringExecutor;
//import com.quartercode.classmod.extra.conv.CFeatureHolder;
//import com.quartercode.classmod.extra.func.FunctionDefinition;
//import com.quartercode.classmod.extra.func.FunctionExecutor;
//import com.quartercode.classmod.extra.func.FunctionInvocation;
//import com.quartercode.classmod.extra.prop.PropertyDefinition;
//import com.quartercode.classmod.extra.storage.ReferenceStorage;
//import com.quartercode.classmod.factory.PropertyDefinitionFactory;
//import com.quartercode.classmod.util.FeatureDefinitionReference;
//import com.quartercode.disconnected.server.sim.TickService;
//import com.quartercode.disconnected.server.sim.scheduler.FunctionCallSchedulerTask;
//import com.quartercode.disconnected.server.world.comp.net.NetworkModule;
//import com.quartercode.disconnected.server.world.comp.net.socket.PacketHandler;
//import com.quartercode.disconnected.server.world.comp.net.socket.PortSocketConnectionListener;
//import com.quartercode.disconnected.server.world.comp.net.socket.Socket;
//import com.quartercode.disconnected.server.world.comp.os.OperatingSystem;
//import com.quartercode.disconnected.server.world.comp.prog.PayloadAttack;
//import com.quartercode.disconnected.server.world.comp.prog.PayloadAttack.Payload;
//import com.quartercode.disconnected.server.world.comp.prog.Process;
//import com.quartercode.disconnected.server.world.comp.prog.Program;
//import com.quartercode.disconnected.server.world.comp.prog.ProgramExecutor;
//import com.quartercode.disconnected.server.world.comp.vuln.Attack;
//import com.quartercode.disconnected.server.world.comp.vuln.VulnerabilityContainer;
//import com.quartercode.disconnected.server.world.util.WorldFeatureHolder;
//
//public class WebServerProgram extends ProgramExecutor {
//
//    // TODO: Make these fields dynamic
//    public static final int                                        PORT            = 80;
//    public static final int                                        REQUEST_TIMEOUT = 1 * TickService.DEFAULT_TICKS_PER_SECOND;
//
//    // ----- Properties -----
//
//    public static final PropertyDefinition<HTTPConnectionListener> HTTP_CONNECTION_LISTENER;
//
//    static {
//
//        HTTP_CONNECTION_LISTENER = factory(PropertyDefinitionFactory.class).create("httpConnectionListener", new ReferenceStorage<>());
//
//    }
//
//    // ----- Functions -----
//
//    static {
//
//        addInterruptionStopperRegisteringExecutor(WebServerProgram.class);
//        addInterruptionSocketDisconnectorRegisteringExecutor(WebServerProgram.class, PORT);
//
//        RUN.addExecutor("registerHTTPConnectionListener", WebServerProgram.class, new FunctionExecutor<Void>() {
//
//            @Override
//            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {
//
//                WebServerProgram program = (WebServerProgram) invocation.getCHolder();
//
//                HTTPConnectionListener listener = new HTTPConnectionListener();
//                listener.setObj(HTTPConnectionListener.PROGRAM, program);
//                program.setObj(HTTP_CONNECTION_LISTENER, listener);
//
//                NetworkModule netModule = program.getParent().invoke(Process.GET_OS).getObj(OperatingSystem.NET_MODULE);
//                netModule.addToColl(NetworkModule.CONNECTION_LISTENERS, listener);
//
//                // Register a process state listener to remove the new HTTP connection listener on process interruption
//                registerInterruptionSocketConnectionListenerRemover(program, listener);
//
//                return invocation.next(arguments);
//            }
//
//        });
//
//    }
//
//    public static interface ProgramDependentFH extends CFeatureHolder {
//
//        public static final PropertyDefinition<WebServerProgram> PROGRAM = factory(PropertyDefinitionFactory.class).create("program", new ReferenceStorage<>());
//
//    }
//
//    public static class HTTPConnectionListener extends PortSocketConnectionListener implements ProgramDependentFH {
//
//        static {
//
//            // Adds a packet handler that answers the first request and then immediately disconnects the socket connection
//            ON_ESTABLISH.addExecutor("addPacketHandler", HTTPConnectionListener.class, new FunctionExecutor<Void>() {
//
//                @Override
//                public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {
//
//                    CFeatureHolder listener = invocation.getCHolder();
//                    Socket socket = (Socket) arguments[0];
//
//                    HTTPPacketHandler packetHandler = new HTTPPacketHandler();
//                    packetHandler.setObj(HTTPPacketHandler.PROGRAM, listener.getObj(PROGRAM));
//                    socket.addToColl(Socket.PACKET_HANDLERS, packetHandler);
//
//                    return invocation.next(arguments);
//                }
//
//            });
//
//            // The request timeout disconnects the socket if the request packet doesn't arrive inside a specified timeframe
//            ON_ESTABLISH.addExecutor("addRequestTimeout", HTTPConnectionListener.class, new FunctionExecutor<Void>() {
//
//                @Override
//                public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {
//
//                    Socket socket = (Socket) arguments[0];
//
//                    FunctionCallSchedulerTask timeoutTask = new FunctionCallSchedulerTask();
//                    timeoutTask.setObj(FunctionCallSchedulerTask.FUNCTION_DEFINITION, new FeatureDefinitionReference<FunctionDefinition<?>>(Socket.class, Socket.DISCONNECT));
//                    // By adding the task to the socket, it's garbage-collected once the socket is disconnected (if it received a regular request).
//                    // That way, the program does not have to manage that by itself.
//                    socket.get(Socket.SCHEDULER).schedule("httpRequestTimeout", "networkUpdate", REQUEST_TIMEOUT, timeoutTask);
//
//                    return invocation.next(arguments);
//                }
//
//            });
//
//        }
//
//        public HTTPConnectionListener() {
//
//            setObj(LOCAL_PORT, PORT);
//        }
//
//    }
//
//    public static class HTTPPacketHandler extends WorldFeatureHolder implements PacketHandler, ProgramDependentFH {
//
//        static {
//
//            HANDLE.addExecutor("processAttack", HTTPPacketHandler.class, new FunctionExecutor<Void>() {
//
//                @Override
//                public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {
//
//                    CFeatureHolder handler = invocation.getCHolder();
//                    Process<?> process = handler.getObj(PROGRAM).getParent();
//                    Object data = arguments[1];
//
//                    if (data instanceof Attack) {
//                        String action = process.invoke(Process.GET_PROGRAM).getObj(Program.VULN_CONTAINER).invoke(VulnerabilityContainer.PROCESS_ATTACK, data);
//
//                        if (action.equals("executePayload") && data instanceof PayloadAttack) {
//                            ((PayloadAttack) data).getObj(PayloadAttack.PAYLOAD).invoke(Payload.RUN, process);
//                        } else if (action.equals("crash")) {
//                            process.invoke(Process.STOP, false);
//                        }
//                    }
//
//                    return invocation.next(arguments);
//                }
//
//            });
//            HANDLE.addExecutor("disconnect", HTTPPacketHandler.class, new FunctionExecutor<Void>() {
//
//                @Override
//                public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {
//
//                    Socket socket = (Socket) arguments[0];
//                    socket.invoke(Socket.DISCONNECT);
//
//                    return invocation.next(arguments);
//                }
//
//            }, LEVEL_3);
//
//        }
//
//    }
//
// }
