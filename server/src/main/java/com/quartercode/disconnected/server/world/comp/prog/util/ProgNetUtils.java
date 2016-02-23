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

package com.quartercode.disconnected.server.world.comp.prog.util;

import com.quartercode.disconnected.server.world.comp.net.NetworkModule;
import com.quartercode.disconnected.server.world.comp.net.socket.Socket;
import com.quartercode.disconnected.server.world.comp.net.socket.SocketConnectionListener;
import com.quartercode.disconnected.server.world.comp.os.OperatingSystem;
import com.quartercode.disconnected.server.world.comp.prog.Process;
import com.quartercode.disconnected.server.world.comp.prog.ProcessStateListener;
import com.quartercode.disconnected.server.world.comp.prog.ProgramExecutor;
import com.quartercode.disconnected.shared.world.comp.proc.WorldProcessState;

public class ProgNetUtils {

    public static void addInterruptionSocketDisconnectorRegisteringExecutor(Class<? extends ProgramExecutor> executorType, final int port) {

        ProgramExecutor.RUN.addExecutor("registerInterruptionSocketDisconnector", executorType, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                ProcessStateListener listener = new DisconnectSocketsOnInterruptPSListener();
                listener.setObj(DisconnectSocketsOnInterruptPSListener.PORT, port);

                Process<?> process = ((ProgramExecutor) invocation.getCHolder()).getParent();
                process.addToColl(Process.STATE_LISTENERS, listener);

                return invocation.next(arguments);
            }

        }, LEVEL_3);
    }

    public static void registerInterruptionSocketConnectionListenerRemover(ProgramExecutor program, SocketConnectionListener connectionListener) {

        ProcessStateListener listener = new RemoveSocketConnectionListenerOnInterruptPSListener();
        listener.setObj(RemoveSocketConnectionListenerOnInterruptPSListener.CONNECTION_LISTENER, connectionListener);

        Process<?> process = program.getParent();
        process.addToColl(Process.STATE_LISTENERS, listener);
    }

    private ProgNetUtils() {

    }

    public static class DisconnectSocketsOnInterruptPSListener extends WorldFeatureHolder implements ProcessStateListener {

        // ----- Properties -----

        public static final PropertyDefinition<Integer> PORT;

        static {

            PORT = factory(PropertyDefinitionFactory.class).create("port", new StandardStorage<>());

        }

        // ----- Functions -----

        static {

            ON_STATE_CHANGE.addExecutor("disconnectSocketsOnInterrupt", DisconnectSocketsOnInterruptPSListener.class, new FunctionExecutor<Void>() {

                @Override
                public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                    Object newState = arguments[2];

                    if (newState == WorldProcessState.INTERRUPTED) {
                        int port = invocation.getCHolder().getObj(PORT);
                        NetworkModule netModule = ((Process<?>) arguments[0]).invoke(Process.GET_OS).getObj(OperatingSystem.NET_MODULE);

                        // Remove all sockets which are bound to the program's port
                        for (Socket socket : netModule.getColl(NetworkModule.SOCKETS)) {
                            if (socket.getObj(Socket.LOCAL_PORT) == port) {
                                socket.invoke(Socket.DISCONNECT);
                            }
                        }
                    }

                    return invocation.next(arguments);
                }

            });

        }

    }

    public static class RemoveSocketConnectionListenerOnInterruptPSListener extends WorldFeatureHolder implements ProcessStateListener {

        // ----- Properties -----

        public static final PropertyDefinition<SocketConnectionListener> CONNECTION_LISTENER;

        static {

            CONNECTION_LISTENER = factory(PropertyDefinitionFactory.class).create("connectionListener", new ReferenceStorage<>());

        }

        // ----- Functions -----

        static {

            ON_STATE_CHANGE.addExecutor("removeSocketConnectionListenerOnInterrupt", DisconnectSocketsOnInterruptPSListener.class, new FunctionExecutor<Void>() {

                @Override
                public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                    Object newState = arguments[2];

                    if (newState == WorldProcessState.INTERRUPTED) {
                        Process<?> process = (Process<?>) arguments[0];
                        NetworkModule netModule = process.invoke(Process.GET_OS).getObj(OperatingSystem.NET_MODULE);

                        netModule.removeFromColl(NetworkModule.CONNECTION_LISTENERS, invocation.getCHolder().getObj(CONNECTION_LISTENER));
                    }

                    return invocation.next(arguments);
                }

            });

        }

    }

}
