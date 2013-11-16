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

package com.quartercode.disconnected.world.comp.net;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlIDREF;
import org.apache.commons.lang.Validate;
import com.quartercode.disconnected.util.InfoString;
import com.quartercode.disconnected.world.comp.hardware.NetworkInterface;

/**
 * This class represents an address which locates a specific service which is avaiable through a specific network interface.
 * The network interface is defined by an ip, the service by a port on which it's listening.
 * 
 * @see IP
 * @see NetworkInterface
 */
public class Address implements InfoString {

    @XmlIDREF
    @XmlAttribute
    private IP  ip;
    @XmlAttribute
    private int port;

    /**
     * Creates a new empty address.
     * This is only recommended for direct field access (e.g. for serialization).
     */
    protected Address() {

    }

    /**
     * Creates a new address and sets the target ip and the target port.
     * The port must be in range 0 <= port <= 65535.
     * 
     * @param ip The target ip which represents the network interface which holds the service.
     * @param port The target port which specifies the service.
     */
    public Address(IP ip, int port) {

        Validate.isTrue(port >= 0 || port <= 65535, "The port must be in range 0 <= port <= 65535 (e.g. 8080): ", port);

        this.ip = ip;
        this.port = port;
    }

    /**
     * Returns the target ip which represents the network interface which holds the service.
     * 
     * @return The target ip which represents the network interface which holds the service.
     */
    public IP getIp() {

        return ip;
    }

    /**
     * Returns the target port which specifies the service.
     * 
     * @return The target port which specifies the service.
     */
    public int getPort() {

        return port;
    }

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = prime * result + (ip == null ? 0 : ip.hashCode());
        result = prime * result + port;
        return result;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (! (obj instanceof Address)) {
            return false;
        }
        Address other = (Address) obj;
        if (ip == null) {
            if (other.ip != null) {
                return false;
            }
        } else if (!ip.equals(other.ip)) {
            return false;
        }
        if (port != other.port) {
            return false;
        }
        return true;
    }

    @Override
    public String toInfoString() {

        return ip + ":" + port;
    }

    @Override
    public String toString() {

        return getClass().getName() + " [" + toInfoString() + "]";
    }

}
