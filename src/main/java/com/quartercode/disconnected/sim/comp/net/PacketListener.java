
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
