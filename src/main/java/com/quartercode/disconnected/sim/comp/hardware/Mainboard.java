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

package com.quartercode.disconnected.sim.comp.hardware;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import com.quartercode.disconnected.sim.comp.Computer;
import com.quartercode.disconnected.sim.comp.ComputerPart;
import com.quartercode.disconnected.sim.comp.Version;
import com.quartercode.disconnected.sim.comp.Vulnerability;

/**
 * This class stores information about a mainboard.
 * This also contains a list of all vulnerabilities this mainboard has.
 * 
 * @see ComputerPart
 * @see Hardware
 */
@XmlAccessorType (XmlAccessType.FIELD)
public class Mainboard extends Hardware {

    @XmlElement (name = "slot")
    private List<MainboradSlot> slots;

    /**
     * Creates a new empty mainboard.
     * This is only recommended for direct field access (e.g. for serialization).
     */
    protected Mainboard() {

    }

    /**
     * Creates a new mainboard and sets the host computer, the name, the version, the vulnerabilities and a list of all avaiable mainboard slots.
     * 
     * @param host The host computer this part is built in.
     * @param name The name the mainboard has.
     * @param version The current version the mainboard has.
     * @param vulnerabilities The vulnerabilities the mainboard has.
     * @param slots A list of all avaiable mainboard slots.
     */
    public Mainboard(Computer host, String name, Version version, List<Vulnerability> vulnerabilities, List<MainboradSlot> slots) {

        super(host, name, version, vulnerabilities);

        this.slots = slots;
    }

    /**
     * Returns all avaiable mainboard slots.
     * The slots may have a content on them, you have to check before you set the content to a new one.
     * 
     * @return All avaiable mainboard slots.
     */
    public List<MainboradSlot> getSlots() {

        return Collections.unmodifiableList(slots);
    }

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (slots == null ? 0 : slots.hashCode());
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
        if (! (obj instanceof Mainboard)) {
            return false;
        }
        Mainboard other = (Mainboard) obj;
        if (slots == null) {
            if (other.slots != null) {
                return false;
            }
        } else if (!slots.equals(other.slots)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {

        return getClass().getName() + " [" + toInfoString() + ", " + slots.size() + " slots]";
    }

    /**
     * This class represents a mainboard slot which can have a hardware part as content.
     * The hardware type a slot can accept is defined using generics and the type class.
     * A mainboard slot is not classiefied as a computer part and only used by the mainboard class.
     * 
     * @see Mainboard
     * @see Hardware
     */
    @XmlAccessorType (XmlAccessType.FIELD)
    public static class MainboradSlot {

        @XmlAttribute
        private Class<? extends Hardware> type;
        @XmlIDREF
        @XmlAttribute
        private Hardware                  content;

        /**
         * Creates a new empty mainboard slot.
         * This is only recommended for direct field access (e.g. for serialization).
         */
        protected MainboradSlot() {

        }

        /**
         * Creates a new mainboard slot and sets the hardware type the slot can accept.
         * 
         * @param type The hardware type the slot can accept.
         */
        public MainboradSlot(Class<? extends Hardware> type) {

            this.type = type;
        }

        /**
         * Returns the hardware type the slot can accept.
         * 
         * @return The hardware type the slot can accept.
         */
        public Class<? extends Hardware> getType() {

            return type;
        }

        /**
         * Returns if the slot actually accepts the given part of hardware.
         * 
         * @param hardware
         * @return If the slot actually accepts the given part of hardware.
         */
        public boolean accept(Hardware hardware) {

            return type.isAssignableFrom(hardware.getClass());
        }

        /**
         * Returns the content the slot currently holds.
         * 
         * @return The content the slot currently holds.
         */
        public Hardware getContent() {

            return content;
        }

        /**
         * Sets the content the slot holds to a new one. This has to be of the type the slot accepts.
         * 
         * @param content The new content the slot will hold
         */
        public void setContent(Hardware content) {

            this.content = content;
        }

        @Override
        public int hashCode() {

            final int prime = 31;
            int result = 1;
            result = prime * result + (content == null ? 0 : content.hashCode());
            result = prime * result + (type == null ? 0 : type.hashCode());
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
            if (! (obj instanceof MainboradSlot)) {
                return false;
            }
            MainboradSlot other = (MainboradSlot) obj;
            if (content == null) {
                if (other.content != null) {
                    return false;
                }
            } else if (!content.equals(other.content)) {
                return false;
            }
            if (type == null) {
                if (other.type != null) {
                    return false;
                }
            } else if (!type.equals(other.type)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {

            return getClass().getName() + " [type " + type + ", holding " + content.toInfoString() + "]";
        }

    }

    /**
     * This annotation marks hardware types which are compatible with a mainboard and need a slot to function.
     */
    @Target (ElementType.TYPE)
    @Retention (RetentionPolicy.RUNTIME)
    public static @interface NeedsMainboardSlot {

    }

}
