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
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import com.quartercode.disconnected.shared.util.XmlPersistent;
import com.quartercode.disconnected.shared.world.comp.net.Address.AddressAdapter;

/**
 * This class represents an address which locates a specific service or socket which is available through a specific network interface.
 * The network interface is defined by a {@link NetId}, the service/socket by a port on which it's listening and sending.
 *
 * @see NetId
 */
@XmlPersistent
@XmlJavaTypeAdapter (AddressAdapter.class)
public class Address implements Serializable {

    private static final long serialVersionUID = 7239039771146867938L;

    @XmlElement
    private NetId             netId;
    @XmlElement
    private int               port;

    // JAXB constructor
    protected Address() {

    }

    /**
     * Creates a new address object.
     *
     * @param netId The target {@link NetId} that represents the network interface which holds the service or socket.
     *        Note that the net id cannot be {@code null}.
     * @param port The target port which specifies the service or socket.
     *        Note that the port must be {@code >= 1} and {@code <= 65535}.
     */
    public Address(NetId netId, int port) {

        Validate.notNull(netId, "Net id of address cannot be null");
        Validate.isTrue(port >= 1 && port <= 65535, "The port (%d) must be in range 1 <= port <= 65535 (e.g. 8080)", port);

        this.netId = netId;
        this.port = port;
    }

    /**
     * Creates a new address object using the address that is stored in the given address string.
     * The string must be using the format {@code netId:port} (e.g. {@code 4353.8.53:80}).
     * Note that the port must be {@code >= 1} and {@code <= 65535}.
     *
     * @param string The address string to parse.
     */
    public Address(String string) {

        String[] stringParts = StringUtils.split(string, ':');
        Validate.isTrue(stringParts.length == 2, "The address string (%s) must be provided in the format netId:port", string);

        netId = new NetId(stringParts[0]);
        port = Integer.parseInt(stringParts[1]);

        Validate.isTrue(port >= 1 && port <= 65535, "The port (%d) must be in range 1 <= port <= 65535 (e.g. 8080)", port);
    }

    /**
     * Returns the target {@link NetId} that represents the network interface which holds the service or socket.
     * Note that the net id cannot be {@code null}.
     *
     * @return The net id that defines the device.
     */
    public NetId getNetId() {

        return netId;
    }

    /**
     * Creates a new address that is based of this address and has the given {@link NetId}.
     * The id represents the network interface which holds the service or socket.
     * Note that the new net id cannot be {@code null}.
     *
     * @param netId The new net id.
     * @return The new address object with the given net id.
     */
    public Address withNetId(NetId netId) {

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

        final int prime = 31;
        int result = 1;
        result = prime * result + (netId == null ? 0 : netId.hashCode());
        result = prime * result + port;
        return result;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        } else if (obj == null || ! (obj instanceof Address)) {
            return false;
        } else {
            Address other = (Address) obj;
            return netId.equals(other.netId) && port == other.port;
        }
    }

    /**
     * Returns the stored address as a string.
     * The returned string is using the format {@code netId:port} (e.g. {@code 4353.8.53:80}).
     *
     * @return A string representation of the address.
     */
    @Override
    public String toString() {

        return new StringBuilder().append(netId).append(":").append(port).toString();
    }

    /**
     * An {@link XmlAdapter} that binds {@link Address} objects using their {@link Address#toString() string representation}.
     * If a JAXB property references a address object and doesn't specify a custom XML adapter, this adapter is used by default.
     *
     * @see Address
     */
    public static class AddressAdapter extends XmlAdapter<String, Address> {

        @Override
        public String marshal(Address v) {

            return v.toString();
        }

        @Override
        public Address unmarshal(String v) {

            return new Address(v);
        }

    }

}
