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

package com.quartercode.disconnected.shared.world.comp.net;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlElement;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import com.quartercode.disconnected.shared.util.XmlPersistent;

/**
 * This class represents a network id which is used to define the "location of a computer in the internet".
 * It contains a subnet part which defines the LAN in which the using device is located in,
 * as well as a device id part which defines the location of the device inside that LAN.<br>
 * <br>
 * Example:
 * 
 * <pre>
 * NetID:               4353.8
 * Subnet (LAN or WAN): 4353
 * Location in Subnet:  8
 * </pre>
 * 
 * @see Address
 */
@XmlPersistent
public class NetID implements Serializable {

    @XmlElement
    private final int subnet;
    @XmlElement
    private final int id;

    /**
     * Creates a new empty net id object with the subnet and device id being set to 0.
     */
    public NetID() {

        subnet = 0;
        id = 0;
    }

    /**
     * Creates a new net id object with the given subnet and device id.
     * Note that both parts must be {@code >= 0}.
     * 
     * @param subnet The subnet id of the subnet the net id is placed in.
     * @param id The actual net id that is used to identify a device inside a subnet.
     */
    public NetID(int subnet, int id) {

        Validate.isTrue(subnet >= 0, "Subnet id (%d) must be >= 0", subnet);
        Validate.isTrue(id >= 0, "Device id (%d) must be >= 0", id);

        this.subnet = subnet;
        this.id = id;
    }

    /**
     * Creates a new net id object using the net id that is stored in the given net id string.
     * The string must be using the format {@code subnet.id} (e.g. {@code 4353.8}).
     * Note that the two parts must be {@code >= 0}.
     * 
     * @param string The net id string to parse.
     */
    public NetID(String string) {

        String[] stringParts = StringUtils.split(string, '.');
        Validate.isTrue(stringParts.length == 2, "The net id string (%s) must be provided in the format subnet.id", string);

        subnet = Integer.parseInt(stringParts[0]);
        id = Integer.parseInt(stringParts[1]);

        Validate.isTrue(subnet >= 0, "Subnet id (%d) must be >= 0", subnet);
        Validate.isTrue(id >= 0, "Device id (%d) must be >= 0", id);
    }

    /**
     * Returns the subnet id of the subnet the net id is placed in.
     * A subnet might contain multiple net ids that are connected to each other through a router.
     * Note that the subnet id must always be {@code >= 0}.
     * 
     * @return The subnet id.
     */
    public int getSubnet() {

        return subnet;
    }

    /**
     * Creates a new net id that is based of this net id and has the given subnet id.
     * The subnet described by the id might contain multiple net ids that are connected to each other through a router.
     * Note that the new subnet id must be {@code >= 0}.
     * 
     * @param subnet The new subnet id.
     * @return The new net id object with the given subnet id.
     */
    public NetID withSubnet(int subnet) {

        Validate.isTrue(subnet >= 0, "Subnet id (%d) must be >= 0", subnet);

        return new NetID(subnet, id);
    }

    /**
     * Returns the actual net id that is used to identify a device inside a subnet.
     * The id {@code 0} identifies the main router of a subnet.
     * Note that the device id must always be {@code >= 0}.
     * 
     * @return The device id.
     */
    public int getId() {

        return id;
    }

    /**
     * Creates a new net id that is based of this net id and has the given device net id.
     * The device id is used to identify a device inside a subnet.
     * Note that the new device id must be {@code >= 0}.
     * 
     * @param id The new device id.
     * @return The new net id object with the given device id.
     */
    public NetID withId(int id) {

        Validate.isTrue(id >= 0, "Device id (%d) must be >= 0", id);

        return new NetID(subnet, id);
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
     * Returns the stored net id as a string.
     * The returned string is using the format {@code subnet.id} (e.g. {@code 4353.8}).
     * 
     * @return A string representation of the net id.
     */
    @Override
    public String toString() {

        return new StringBuilder().append(subnet).append(".").append(id).toString();
    }

}
