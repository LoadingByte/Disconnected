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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import com.quartercode.disconnected.sim.comp.program.ProgramExecutor;
import com.quartercode.disconnected.util.InfoString;

/**
 * This packet listener listens to a certain local binding adress for receiving packets related to the using process.
 * 
 * @see Packet
 * @see ProgramExecutor
 * @see Process
 */
@XmlAccessorType (XmlAccessType.FIELD)
public class PacketListener implements InfoString {

    @XmlAttribute
    private String  name;
    private Address binding;

    /**
     * Creates a new empty packet listener.
     * This is only recommended for direct field access (e.g. for serialization).
     */
    protected PacketListener() {

    }

    /**
     * Creates a new packet listener, sets the name and fixes the bound address.
     * 
     * @param name The name identifier for the listener. It's used to sort packets after they were received.
     * @param binding The address this listener is bound to.
     */
    public PacketListener(String name, Address binding) {

        this.name = name;
        this.binding = binding;
    }

    /**
     * Returns The name identifier for the listener.
     * It's used to sort packets after they were received.
     * 
     * @return The name identifier for the listener.
     */
    public String getName() {

        return name;
    }

    /**
     * Returns the address this listener is bound to.
     * 
     * @return The address this listener is bound to.
     */
    public Address getBinding() {

        return binding;
    }

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = prime * result + (binding == null ? 0 : binding.hashCode());
        result = prime * result + (name == null ? 0 : name.hashCode());
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
        if (! (obj instanceof PacketListener)) {
            return false;
        }
        PacketListener other = (PacketListener) obj;
        if (binding == null) {
            if (other.binding != null) {
                return false;
            }
        } else if (!binding.equals(other.binding)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }

    @Override
    public String toInfoString() {

        return name + ", bound to " + binding.toInfoString();
    }

    @Override
    public String toString() {

        return getClass().getName() + " [" + toInfoString() + "]";
    }

}
