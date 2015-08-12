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

/**
 * This exception occurs when a new {@link PortBoundSocketConnectionListener} tries to bind a port which has already been bound by another one.
 *
 * @see SocketRegistryAPI#addConnectionListener(SocketConnectionListener)
 */
public class PortAlreadyBoundException extends Exception {

    private static final long serialVersionUID = -6773608834560405429L;

    private final int         localPort;

    /**
     * Creates a new port already bound exception.
     *
     * @param localPort The {@link Socket#getLocalPort() local port} a new {@link PortBoundSocketConnectionListener} tried to bind although it has already been bound by another one.
     */
    public PortAlreadyBoundException(int localPort) {

        super("The port " + localPort + " is already bound by another port bound socket connection listener");

        this.localPort = localPort;
    }

    /**
     * Returns the {@link Socket#getLocalPort() local port} a new {@link PortBoundSocketConnectionListener} tried to bind although it has already been bound by another one.
     *
     * @return The already bound local port.
     */
    public int getLocalPort() {

        return localPort;
    }

}
