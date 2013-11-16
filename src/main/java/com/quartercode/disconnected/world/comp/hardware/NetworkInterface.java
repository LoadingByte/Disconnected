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

package com.quartercode.disconnected.world.comp.hardware;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import javax.xml.bind.annotation.XmlElement;
import com.quartercode.disconnected.world.comp.Computer;
import com.quartercode.disconnected.world.comp.Version;
import com.quartercode.disconnected.world.comp.Vulnerability;
import com.quartercode.disconnected.world.comp.hardware.Mainboard.NeedsMainboardSlot;
import com.quartercode.disconnected.world.comp.net.IP;
import com.quartercode.disconnected.world.comp.net.Packet;

/**
 * This class represents a network interface of a computer.
 * Every network interface has a speed a byte needs to get transfered through it.
 * Network interfaces are a central compontent of the internet infrastructure as they provide a connection to the internet.
 * 
 * @see Hardware
 * @see Packet
 */
@NeedsMainboardSlot
public class NetworkInterface extends Hardware {

    private IP                  ip;
    @XmlElement (name = "remainingPacket")
    private final Queue<Packet> remainingPackets = new LinkedList<Packet>();

    /**
     * Creates a new empty network interface.
     * This is only recommended for direct field access (e.g. for serialization).
     */
    protected NetworkInterface() {

    }

    /**
     * Creates a new network interface and sets the host computer, the name, the version and the vulnerabilities.
     * 
     * @param host The host computer this part is built in.
     * @param name The name the network interface has.
     * @param version The current version the network interface has.
     * @param vulnerabilities The vulnerabilities the network interface has.
     */
    public NetworkInterface(Computer host, String name, Version version, List<Vulnerability> vulnerabilities) {

        super(host, name, version, vulnerabilities);
    }

    /**
     * Returns the ip this interface can be found under.
     * 
     * @return The ip this interface can be found under.
     */
    @XmlElement
    public IP getIp() {

        return ip;
    }

    /**
     * Changes the ip this interface can be found under to a new one.
     * 
     * @param ip The new ip this interface can be found under.
     */
    public void setIp(IP ip) {

        this.ip = ip;
    }

    /**
     * Adds a new packet to the list of all remaining packets which should be sent soon.
     * 
     * @param packet The new packet for delivery.
     */
    public void sendPacket(Packet packet) {

        remainingPackets.offer(packet);
    }

    /**
     * Returns the packet which should be sent next.
     * 
     * @param remove True if the returned packet should be removed from the queue.
     * @return The packet which should be sent next.
     */
    public Packet nextDeliveryPacket(boolean remove) {

        if (remove) {
            return remainingPackets.poll();
        } else {
            return remainingPackets.peek();
        }
    }

    /**
     * This method handles an incoming packet.
     * 
     * @param packet The packet which just came in.
     */
    public void receivePacket(Packet packet) {

        getHost().get(Computer.OS).get().getNetworkManager().handlePacket(packet);
    }

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (ip == null ? 0 : ip.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        NetworkInterface other = (NetworkInterface) obj;
        if (ip == null) {
            if (other.ip != null) {
                return false;
            }
        } else if (!ip.equals(other.ip)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {

        return getClass().getName() + " [" + toInfoString() + ", " + ip + ", " + remainingPackets.size() + " remaining packets]";
    }

}
