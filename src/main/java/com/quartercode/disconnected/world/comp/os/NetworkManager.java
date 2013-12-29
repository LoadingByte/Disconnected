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

package com.quartercode.disconnected.world.comp.os;

import javax.xml.bind.Unmarshaller;
import com.quartercode.disconnected.mocl.extra.FunctionExecutionException;
import com.quartercode.disconnected.world.comp.net.Packet;

/**
 * The network manager is a subclass the {@link OperatingSystem} uses for storing and delivering packets.
 * This class only gets used by the {@link OperatingSystem}.
 * 
 * @see Packet
 * @see OperatingSystem
 */
public class NetworkManager {

    private OperatingSystem host;

    /**
     * Creates a new empty network manager manager.
     * This is only recommended for direct field access (e.g. for serialization).
     */
    protected NetworkManager() {

    }

    /**
     * Creates a new network manager and sets the host the manager is running for.
     * 
     * @param host The {@link OperatingSystem} this network manager is running for.
     */
    public NetworkManager(OperatingSystem host) {

        this.host = host;
    }

    /**
     * Returns the {@link OperatingSystem} this network manager is running for.
     * 
     * @return The {@link OperatingSystem} this network manager is running for.
     */
    public OperatingSystem getHost() {

        return host;
    }

    /**
     * Sends a new packet from the sender to the receiver address of the given packet.
     * 
     * @param packet The packet to send.
     */
    public void sendPacket(Packet packet) {

        // TODO: Reimplement this when the new network system is created
        // Does nothing at the moment
        // packet.get(Packet.GET_SENDER).invoke().get(Address.GET_IP).invoke().getHost().get(NetworkInterface.SEND_PACKETS).invoke(packet);
    }

    /**
     * This method takes an incoming packet and distributes it to the target port.
     * 
     * @param packet The packet which came in and called the method.
     * @throws FunctionExecutionException Something goes wrong while resolving some data.
     */
    public void handlePacket(Packet packet) throws FunctionExecutionException {

        // TODO: Reimplement this when the new network system is created
        // if (host.getProcessManager().getProcess(packet.get(Packet.GET_RECEIVER).invoke()) != null) {
        // host.getProcessManager().getProcess(packet.get(Packet.GET_RECEIVER).invoke()).get(Process.GET_EXECUTOR).invoke().receivePacket(packet);
        // }
    }

    protected void beforeUnmarshal(Unmarshaller unmarshaller, Object parent) {

        host = (OperatingSystem) parent;
    }

}
