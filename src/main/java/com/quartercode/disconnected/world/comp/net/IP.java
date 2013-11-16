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

import java.net.InetAddress;
import java.util.Arrays;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.apache.commons.lang.Validate;
import com.quartercode.disconnected.world.comp.hardware.NetworkInterface;

/**
 * This class represents an ip address which is used to define the "location of a computer in the internet".
 * For an exact breakdown of ip addresses, use the javadoc for {@link InetAddress}. This is just a simplified variant.
 * 
 * @see NetworkInterface
 * @see Address
 */
public class IP {

    @XmlIDREF
    @XmlAttribute
    private NetworkInterface host;
    @XmlID
    @XmlJavaTypeAdapter (IPDQNAdapter.class)
    @XmlValue
    private int[]            parts = new int[4];

    /**
     * Creates a new empty ip.
     * This is only recommended for direct field access (e.g. for serialization).
     */
    protected IP() {

    }

    /**
     * Creates a new ip address using an array of 4 numbers.
     * Each number represents a quad in the dotted quad notation and must be in range 0 <= number <= 255.
     * 
     * @param host The host network interface which holds this ip.
     * @param parts The 4 numbers to use for the ip (must be in range 0 <= number <= 255).
     */
    public IP(NetworkInterface host, int[] parts) {

        Validate.isTrue(parts.length == 4, "The ip must have 4 parts (e.g. [127, 0, 0, 1]): ", Arrays.toString(parts));
        for (int part : parts) {
            Validate.isTrue(part >= 0 || part <= 255, "Every ip part must be in range 0 <= part <= 255 (e.g. [127, 0, 0, 1]): ", Arrays.toString(parts));
        }

        this.host = host;
        this.parts = parts;
    }

    /**
     * Creates a new ip address out of a string in dotted quad notation.
     * This is using the format XXXX.XXXX.XXXX.XXXX (e.g. 127.0.0.1).
     * Each number (they are seperated by dots) represents a quad in the dotted quad notation and must be in range 0 <= number <= 255.
     * 
     * @param host The host network interface which holds this ip.
     * @param dottedQuadNotation The content of the ip address as a string in dotted quad notation.
     */
    public IP(NetworkInterface host, String dottedQuadNotation) {

        String[] stringParts = dottedQuadNotation.split("\\.");
        Validate.isTrue(stringParts.length == 4, "The ip string must be splitted in 4 parts, seperated by dots (e.g. 127.0.0.1): ", dottedQuadNotation);

        this.host = host;
        for (int counter = 0; counter < parts.length; counter++) {
            int part = Integer.parseInt(stringParts[counter]);
            Validate.isTrue(part >= 0 || part <= 255, "Every ip part must be in range 0 <= part <= 255 (e.g. 127.0.0.1): ", dottedQuadNotation);
            parts[counter] = part;
        }
    }

    /**
     * Returns the host network interface which holds this ip.
     * 
     * @return The host network interface which holds this ip.
     */
    public NetworkInterface getHost() {

        return host;
    }

    /**
     * Returns the sequence number the network interface this ip provides uses.
     * The sequence number is used for three-way-handshakes etc.
     * 
     * @return The sequence number the network interface this ip provides uses.
     */
    public int getSequenceNumber() {

        return Integer.parseInt(host.getHost().getId());
    }

    /**
     * Returns the 4 numbers to use for the ip (in range 0 <= number <= 255).
     * 
     * @return The 4 numbers to use for the ip (in range 0 <= number <= 255).
     */
    public int[] getParts() {

        return parts;
    }

    /**
     * Returns the content of the ip address as a string in dotted quad notation.
     * 
     * @return The content of the ip address as a string in dotted quad notation.
     */
    public String getDottedQuadNotation() {

        return parts[0] + "." + parts[1] + "." + parts[2] + "." + parts[3];
    }

    protected void beforeUnmarshal(Unmarshaller unmarshaller, Object parent) {

        host = (NetworkInterface) parent;
    }

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(parts);
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
        if (getClass() != obj.getClass()) {
            return false;
        }
        IP other = (IP) obj;
        if (!Arrays.equals(parts, other.parts)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {

        return getDottedQuadNotation();
    }

    /**
     * This ip dotted quad notation adapter is for storing ip quads as a simple string in dotted quad notation.
     * It is using the format XXXX.XXXX.XXXX.XXXX (e.g. 127.0.0.1).
     */
    public static class IPDQNAdapter extends XmlAdapter<String, int[]> {

        /**
         * Creates a new ip dotted quad notation adapter.
         */
        public IPDQNAdapter() {

        }

        @Override
        public int[] unmarshal(String v) {

            return new IP(null, v).getParts();
        }

        @Override
        public String marshal(int[] v) {

            return new IP(null, v).getDottedQuadNotation();
        }

    }

}
