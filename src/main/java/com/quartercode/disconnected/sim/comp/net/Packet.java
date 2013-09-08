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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.apache.commons.lang.Validate;
import com.quartercode.disconnected.util.InfoString;
import com.quartercode.disconnected.util.MapAdapter;
import com.quartercode.disconnected.util.size.SizeObject;
import com.quartercode.disconnected.util.size.SizeUtil;

/**
 * This class represents a packet which can be sent between network interfaces.
 * A packet contains a sender, a receiver (both represented by addresses) and a data map which holds the data which should be sent.
 */
public class Packet implements SizeObject, InfoString {

    private static Map<String, Object> parseDataArray(Object... data) {

        if (data.length % 2 == 0) {
            Map<String, Object> dataMap = new HashMap<String, Object>();
            for (int counter = 0; counter < data.length; counter += 2) {
                dataMap.put(String.valueOf(data[counter]), data[counter + 1]);
            }
            return dataMap;
        } else {
            throw new IllegalArgumentException("Data array must have an equal amount of keys and values");
        }
    }

    @XmlElement
    private Address             sender;
    @XmlElement
    private Address             receiver;
    @XmlJavaTypeAdapter (MapAdapter.class)
    private Map<String, Object> data;

    /**
     * Creates a new empty packet.
     * This is only recommended for direct field access (e.g. for serialization).
     */
    protected Packet() {

    }

    /**
     * Creates a new packet and sets the addresses and the data map.
     * 
     * @param sender The address which is sending the packet.
     * @param receiver The address which will receive the packet.
     * @param data The data map which holds the data which should be sent.
     * @throws IllegalArgumentException Can't derive size type from one of the given data values.
     */
    public Packet(Address sender, Address receiver, Map<String, Object> data) {

        for (Object value : data.values()) {
            if (value != null) {
                Validate.isTrue(SizeUtil.accept(value), "Size of type " + value.getClass().getName() + " can't be derived");
            }
        }

        this.sender = sender;
        this.receiver = receiver;
        this.data = data;
    }

    /**
     * Creates a new packet and sets the addresses and the data map.
     * The data parameter must be an array with key-value-pairs (e.g. ["key1", value1, "key2", value2]).
     * 
     * @param sender The address which is sending the packet.
     * @param receiver The address which will receive the packet.
     * @param data The data which should be sent in key-value-pairs (e.g. ["key1", value1, "key2", value2]).
     * @throws IllegalArgumentException The data array hasn't an equal amount of keys and values or can't derive size type from one of the given data values.
     */
    public Packet(Address sender, Address receiver, Object... data) {

        this(sender, receiver, parseDataArray(data));
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
     * Returns the data map which holds the data which should be sent.
     * This can't be modified after construction.
     * 
     * @return The data map which holds the data which should be sent.
     */
    public Map<String, Object> getData() {

        return Collections.unmodifiableMap(data);
    }

    /**
     * Returns the value of a given key which should be sent.
     * This uses the data map which can't be modified after construction.
     * 
     * @param key The value of that key is returned.
     * @return The value of a given key which should be sent.
     */
    public Object get(String key) {

        return data.get(key);
    }

    /**
     * Returns true if the packet contains a data entry with the given key.
     * 
     * @param key The key to check for.
     * @return True if the packet contains a data entry with the given key.
     */
    public boolean contains(String key) {

        return data.containsKey(key);
    }

    /**
     * Returns true if the packet contains a data entry with the given key-value-pair.
     * 
     * @param key The key to check for.
     * @param value The value to check the key for.
     * @return True if the packet contains a data entry with the given key-value-pair.
     */
    public boolean contains(String key, Object value) {

        return data.containsKey(key);
    }

    /**
     * Returns the size this packet has in bytes.
     * Every char in every key is equal to one byte.
     * 
     * @return The size this packet has in bytes.
     */
    @Override
    public long getSize() {

        long size = 0;
        for (Entry<String, Object> entry : data.entrySet()) {
            size += SizeUtil.getSize(entry.getKey());
            size += SizeUtil.getSize(entry.getValue());
        }
        return size;
    }

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = prime * result + (data == null ? 0 : data.hashCode());
        result = prime * result + (receiver == null ? 0 : receiver.hashCode());
        result = prime * result + (sender == null ? 0 : sender.hashCode());
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
        return true;
    }

    @Override
    public String toInfoString() {

        return sender.toInfoString() + " to " + receiver.toInfoString() + ", payload " + data;
    }

    @Override
    public String toString() {

        return getClass().getName() + " [" + toInfoString() + "]";
    }

}
