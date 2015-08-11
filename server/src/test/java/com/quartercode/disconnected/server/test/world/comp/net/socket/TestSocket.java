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

package com.quartercode.disconnected.server.test.world.comp.net.socket;

import java.lang.reflect.Field;
import java.util.List;
import org.apache.commons.lang3.reflect.FieldUtils;
import com.quartercode.disconnected.server.sim.scheduler.Scheduler;
import com.quartercode.disconnected.server.world.comp.net.socket.Socket;
import com.quartercode.disconnected.server.world.comp.net.socket.SocketState;
import com.quartercode.disconnected.shared.world.comp.net.Address;

@SuppressWarnings ("unchecked")
public class TestSocket extends Socket {

    // ----- Static Methods ----

    private static Field getField(String name) {

        return FieldUtils.getField(Socket.class, name, true);
    }

    public static Scheduler<Socket> getScheduler(Socket socket) {

        try {
            return (Scheduler<Socket>) getField("scheduler").get(socket);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void sneakyStateChange(Socket socket, SocketState state) {

        try {
            getField("state").set(socket, state);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<Object> getIncomingPacketBuffer(Socket socket) {

        try {
            return (List<Object>) getField("incomingPacketBuffer").get(socket);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    // ----- Member Methods -----

    @Override
    public void initialize(int localPort, Address destination) {

        super.initialize(localPort, destination);
    }

    public Scheduler<Socket> getScheduler() {

        return getScheduler(this);
    }

    @Override
    public void setState(SocketState state) {

        super.setState(state);
    }

    public void sneakyStateChange(SocketState state) {

        sneakyStateChange(this, state);
    }

    public List<Object> getIncomingPacketBuffer() {

        return getIncomingPacketBuffer(this);
    }

    @Override
    public void handle(Object data) {

        super.handle(data);
    }

}
