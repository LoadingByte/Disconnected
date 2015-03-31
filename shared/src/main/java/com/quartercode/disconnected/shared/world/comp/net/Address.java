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

package com.quartercode.disconnected.shared.world.comp.net;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlElement;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import com.quartercode.disconnected.shared.util.XmlPersistent;

/**
 * This class represents an address which locates a specific service or socket which is available through a specific network interface.
 * The network interface is defined by a {@link NetID}, the service/socket by a port on which it's listening and sending.
 * 
 * @see NetID
 */
@XmlPersistent
public class Address implements Serializable {

    private static final long serialVersionUID = 1589075859879897650L;

    @XmlElement
    private final NetID       netId;
    @XmlElement
    private final int         port;

    /**
     * Creates a new empty address object with an empty {@link NetID} and the port 1.
     */
    public Address() {

        netId = new NetID();
        port = 1;
    }

    /**
     * Creates a new address object.
     * 
     * @param netId The target {@link NetID} that represents the network interface which holds the service or socket.
     *        Note that the net id cannot be {@code null}.
     * @param port The target port which specifies the service or socket.
     *        Note that the port must be {@code >= 1} and {@code <= 65535}.
     */
    public Address(NetID netId, int port) {

        Validate.notNull(netId, "Net id of address cannot be null");
        Validate.isTrue(port >= 1 && port <= 65535, "The port (%d) must be in range 1 <= port <= 65535 (e.g. 8080)", port);

        this.netId = netId;
        this.port = port;
    }

    /**
     * Creates a new address object using the address that is stored in the given address string.
     * The string must be using the format {@code netId:port} (e.g. {@code 4353.8:80}).
     * Note that the port must be {@code >= 1} and {@code <= 65535}.
     * 
     * @param string The address string to parse.
     */
    public Address(String string) {

        String[] stringParts = StringUtils.split(string, ':');
        Validate.isTrue(stringParts.length == 2, "The address string (%s) must be provided in the format netId:port", string);

        netId = new NetID(stringParts[0]);
        port = Integer.parseInt(stringParts[1]);

        Validate.isTrue(port >= 1 && port <= 65535, "The port (%d) must be in range 1 <= port <= 65535 (e.g. 8080)", port);
    }

    /**
     * Returns the target {@link NetID} that represents the network interface which holds the service or socket.
     * Note that the net id cannot be {@code null}.
     * 
     * @return The net id that defines the device.
     */
    public NetID getNetId() {

        return netId;
    }

    /**
     * Creates a new address that is based of this address and has the given {@link NetID}.
     * The id represents the network interface which holds the service or socket.
     * Note that the new net id cannot be {@code null}.
     * 
     * @param netId The new net id.
     * @return The new address object with the given net id.
     */
    public Address withNetId(NetID netId) {

        Validate.notNull(netId, "Net id of address cannot be null");

        return new Address(netId, port);
    }

    /**
     * Returns the target port which specifies the service or socket.
     * Note that the port must always be {@code >= 1} and {@code <= 65535}.
     * 
     * @return The port of the service or socket.
     */
    public int getPort() {

        return port;
    }

    /**
     * Creates a new address that is based of this address and has the given port.
     * The port specifies the service or socket.
     * Note that the new port must be {@code >= 1} and {@code <= 65535}.
     * 
     * @param port The new port.
     * @return The new address object with the given port.
     */
    public Address withPort(int port) {

        Validate.isTrue(port >= 1 && port <= 65535, "The port (%d) must be in range 1 <= port <= 65535 (e.g. 8080)", port);

        return new Address(netId, port);
    }

    @Override
    public int hashCode() {

        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(Object obj) {

        return EqualsBuilder.reflectionEquals(this, obj);
    }

    /**
     * Returns the stored address as a string.
     * The returned string is using the format {@code netId:port} (e.g. {@code 4353.8:80}).
     * 
     * @return A string representation of the address.
     */
    @Override
    public String toString() {

        return new StringBuilder().append(netId).append(":").append(port).toString();
    }

}
