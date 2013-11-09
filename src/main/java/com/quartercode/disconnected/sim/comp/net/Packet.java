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

package com.quartercode.disconnected.sim.comp.net;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import com.quartercode.disconnected.sim.comp.SizeUtil;
import com.quartercode.disconnected.sim.comp.SizeUtil.SizeObject;
import com.quartercode.disconnected.util.InfoString;

/**
 * This class represents a packet which can be sent between network interfaces.
 * A packet contains a sender, a receiver (both represented by addresses) and a data map which holds the data which should be sent.
 */
public class Packet implements SizeObject, InfoString {

    @XmlElement
    private Address      sender;
    @XmlElement
    private Address      receiver;
    @XmlElement
    private Object       data;
    @XmlElement
    private List<String> target;
    @XmlElement
    private int          targetIndex;

    /**
     * Creates a new empty packet.
     * This is only recommended for direct field access (e.g. for serialization).
     */
    protected Packet() {

    }

    /**
     * Creates a new packet and sets the addresses and the data payload. Also sets the target array.
     * 
     * @param sender The address which is sending the packet.
     * @param receiver The address which will receive the packet.
     * @param data The data payload object which should be sent.
     * @param target The target array which is used by the receiver to resolve the purpose of the packet.
     * @throws IllegalArgumentException Can't derive size type from one of the given data values.
     */
    public Packet(Address sender, Address receiver, Object data, String... target) {

        this.sender = sender;
        this.receiver = receiver;
        this.data = data;
        this.target = Arrays.asList(target);
    }

    /**
     * Returns the address which is sending the packet.
     * 
     * @return The address which is sending the packet.
     */
    public Address getSender() {

        return sender;
    }

    /**
     * Returns the address which will receive the packet.
     * 
     * @return The address which will receive the packet.
     */
    public Address getReceiver() {

        return receiver;
    }

    /**
     * Returns the data payload object which should be sent.
     * The payload can't be modified after construction.
     * 
     * @return The data payload object which should be sent.
     */
    public Object getData() {

        return data;
    }

    /**
     * Returns the whole target array (or list) which is used by the receiver to resolve the purpose of the packet.
     * 
     * @return The target array which is used by the receiver to resolve the purpose of the packet.
     */
    public List<String> getTarget() {

        return Collections.unmodifiableList(target);
    }

    /**
     * Returns the next target string from the target array and optional increments the index for the next request.
     * Returns null if there's no element with the current index.
     * 
     * @param increment True if the target index should be incremented. This will return the next target string on the next request.
     * @return The next target string from the target array.
     */
    public String nextTarget(boolean increment) {

        if (targetIndex < target.size()) {
            int requestIndex = targetIndex;
            if (increment) {
                targetIndex++;
            }
            return target.get(requestIndex);
        } else {
            return null;
        }
    }

    /**
     * Resets the target index which is used by {@link #nextTarget(boolean)} for storing the last target string.
     */
    public void resetTargetIndex() {

        targetIndex = 0;
    }

    /**
     * Returns the size this packet has in bytes.
     * Every char in every key is equal to one byte.
     * 
     * @return The size this packet has in bytes.
     */
    @Override
    public long getSize() {

        long size = SizeUtil.getSize(data) + SizeUtil.getSize(target);
        return size;
    }

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = prime * result + (data == null ? 0 : data.hashCode());
        result = prime * result + (receiver == null ? 0 : receiver.hashCode());
        result = prime * result + (sender == null ? 0 : sender.hashCode());
        result = prime * result + (target == null ? 0 : target.hashCode());
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
        if (! (obj instanceof Packet)) {
            return false;
        }
        Packet other = (Packet) obj;
        if (data == null) {
            if (other.data != null) {
                return false;
            }
        } else if (!data.equals(other.data)) {
            return false;
        }
        if (receiver == null) {
            if (other.receiver != null) {
                return false;
            }
        } else if (!receiver.equals(other.receiver)) {
            return false;
        }
        if (sender == null) {
            if (other.sender != null) {
                return false;
            }
        } else if (!sender.equals(other.sender)) {
            return false;
        }
        if (target == null) {
            if (other.target != null) {
                return false;
            }
        } else if (!target.equals(other.target)) {
            return false;
        }
        return true;
    }

    @Override
    public String toInfoString() {

        return sender.toInfoString() + " to " + receiver.toInfoString() + ", payload " + data + " with first target " + target.get(0);
    }

    @Override
    public String toString() {

        return getClass().getName() + " [" + toInfoString() + "]";
    }

}
