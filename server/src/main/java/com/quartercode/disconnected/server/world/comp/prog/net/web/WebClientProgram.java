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
//import static com.quartercode.disconnected.server.world.comp.prog.util.ProgEventUtils.registerSBPAwareEventHandler;
//import static com.quartercode.disconnected.server.world.comp.prog.util.ProgStateUtils.addInterruptionStopperRegisteringExecutor;
//import lombok.RequiredArgsConstructor;
//import com.quartercode.classmod.extra.conv.CFeatureHolder;
//import com.quartercode.classmod.extra.func.FunctionExecutor;
//import com.quartercode.classmod.extra.func.FunctionInvocation;
//import com.quartercode.classmod.extra.prop.PropertyDefinition;
//import com.quartercode.classmod.extra.storage.ReferenceStorage;
//import com.quartercode.classmod.extra.storage.StandardStorage;
//import com.quartercode.classmod.factory.PropertyDefinitionFactory;
//import com.quartercode.disconnected.server.bridge.SBPAwareEventHandler;
//import com.quartercode.disconnected.server.sim.TickService;
//import com.quartercode.disconnected.server.sim.scheduler.SchedulerTaskAdapter;
//import com.quartercode.disconnected.server.world.World;
//import com.quartercode.disconnected.server.world.comp.net.Backbone;
//import com.quartercode.disconnected.server.world.comp.net.NetworkModule;
//import com.quartercode.disconnected.server.world.comp.net.socket.PacketHandler;
//import com.quartercode.disconnected.server.world.comp.net.socket.Socket;
//import com.quartercode.disconnected.server.world.comp.net.socket.SocketState;
//import com.quartercode.disconnected.server.world.comp.os.OperatingSystem;
//import com.quartercode.disconnected.server.world.comp.prog.Process;
//import com.quartercode.disconnected.server.world.comp.prog.ProgramExecutor;
//import com.quartercode.disconnected.server.world.util.WorldFeatureHolder;
//import com.quartercode.disconnected.shared.event.comp.prog.generic.GP_SBPWPU_ErrorEvent;
//import com.quartercode.disconnected.shared.event.comp.prog.net.web.WCP_SBPWPU_DisplayWebpageCommand;
//import com.quartercode.disconnected.shared.event.comp.prog.net.web.WCP_WP_OpenWebpageCommand;
//import com.quartercode.disconnected.shared.identity.SBPIdentity;
//import com.quartercode.disconnected.shared.world.comp.net.Address;
//import com.quartercode.disconnected.shared.world.comp.net.NetID;
//import com.quartercode.disconnected.shared.world.comp.net.web.URL;
//import com.quartercode.disconnected.shared.world.comp.net.web.Webpage;
//import com.quartercode.disconnected.shared.world.comp.prog.SBPWorldProcessUserId;
//import com.quartercode.eventbridge.bridge.Bridge;
//
//public class WebClientProgram extends ProgramExecutor {
//
//    // TODO: Make these fields dynamic
//    public static final int                     DEFAULT_PORT     = 80;
//    public static final int                     RESPONSE_TIMEOUT = 1 * TickService.DEFAULT_TICKS_PER_SECOND;
//
//    // ----- Properties -----
//
//    public static final PropertyDefinition<URL> CURRENT_URL;
//
//    static {
//
//        CURRENT_URL = factory(PropertyDefinitionFactory.class).create("currentURL", new StandardStorage<>());
//
//    }
//
//    // ----- Functions -----
//
//    static {
//
//        addInterruptionStopperRegisteringExecutor(WebClientProgram.class);
//
//        RUN.addExecutor("registerOpenURLCommandHandler", WebClientProgram.class, new FunctionExecutor<Void>() {
//
//            @Override
//            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {
//
//                WebClientProgram program = (WebClientProgram) invocation.getCHolder();
//                registerSBPAwareEventHandler(program, WCP_WP_OpenWebpageCommand.class, new OpenWebpageCommandHandler(program), true);
//
//                return invocation.next(arguments);
//            }
//
//        });
//
//    }
//
//    @RequiredArgsConstructor
//    public static class OpenWebpageCommandHandler implements SBPAwareEventHandler<WCP_WP_OpenWebpageCommand> {
//
//        private final WebClientProgram program;
//
//        @Override
//        public void handle(WCP_WP_OpenWebpageCommand event, SBPIdentity sender) {
//
//            URL url = event.getUrl();
//
//            Backbone backbone = program.getParent().invoke(Process.GET_OS).getParent().getParent().getObj(World.BACKBONE);
//            NetID netId = backbone.getObj(Backbone.DNS).get(url.getDomain());
//
//            Address host = new Address(netId, url.getPort());
//
//            Socket socket = program.getParent().invoke(Process.GET_OS).getObj(OperatingSystem.NET_MODULE).invoke(NetworkModule.CREATE_SOCKET);
//            socket.setObj(Socket.LOCAL_PORT, 0);
//            socket.setObj(Socket.DESTINATION, host);
//
//            HTTPPacketHandler packetHandler = new HTTPPacketHandler();
//            packetHandler.setObj(HTTPPacketHandler.PROGRAM, program);
//            socket.addToColl(Socket.PACKET_HANDLERS, packetHandler);
//
//            // Add a task to timeout the socket connection and send an error to the WPU if that happens
//            HTTPTimeoutTask timeoutTask = new HTTPTimeoutTask();
//            timeoutTask.setObj(HTTPTimeoutTask.SOCKET, socket);
//            socket.get(Socket.SCHEDULER).schedule("httpResponseTimeout", "networkUpdate", RESPONSE_TIMEOUT, timeoutTask);
//        }
//
//    }
//
//    public static class HTTPPacketHandler extends WorldFeatureHolder implements PacketHandler {
//
//        public static final PropertyDefinition<WebClientProgram> PROGRAM = factory(PropertyDefinitionFactory.class).create("program", new ReferenceStorage<>());
//
//        static {
//
//            HANDLE.addExecutor("forwardWebpage", HTTPPacketHandler.class, new FunctionExecutor<Void>() {
//
//                @Override
//                public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {
//
//                    CFeatureHolder handler = invocation.getCHolder();
//                    WebClientProgram program = handler.getObj(PROGRAM);
//                    Webpage data = (Webpage) arguments[1];
//
//                    // Only forward the received webpage if the URL matches the one which is currently opened
//                    if (data.getUrl().equals(program.getObj(CURRENT_URL))) {
//                        Bridge bridge = program.getBridge();
//                        SBPWorldProcessUserId wpuId = program.getParent().getObj(Process.WORLD_PROCESS_USER);
//
//                        bridge.send(new WCP_SBPWPU_DisplayWebpageCommand(wpuId, data));
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
//    public static class HTTPTimeoutTask extends SchedulerTaskAdapter {
//
//        public static final PropertyDefinition<Socket> SOCKET = factory(PropertyDefinitionFactory.class).create("socket", new ReferenceStorage<>());
//
//        static {
//
//            EXECUTE.addExecutor("default", HTTPTimeoutTask.class, new FunctionExecutor<Void>() {
//
//                @Override
//                public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {
//
//                    WebClientProgram program = (WebClientProgram) arguments[0];
//                    Socket socket = invocation.getCHolder().getObj(SOCKET);
//
//                    if (socket != null && socket.getObj(Socket.STATE) != SocketState.DISCONNECTED) {
//                        // Disconnect the socket
//                        socket.invoke(Socket.DISCONNECT);
//
//                        // Send an error message to the client
//                        Bridge bridge = program.getBridge();
//                        SBPWorldProcessUserId wpuId = program.getParent().getObj(Process.WORLD_PROCESS_USER);
//                        bridge.send(new GP_SBPWPU_ErrorEvent(wpuId, "timeout", program.getObj(CURRENT_URL).toString()));
//                    }
//
//                    return invocation.next(arguments);
//                }
//
//            });
//
//        }
//
//    }
//
// }
