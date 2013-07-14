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

package com.quartercode.disconnected.sim.comp;

import java.util.Collections;
import java.util.List;

/**
 * This class stores information about a mainboard.
 * This also contains a list of all vulnerabilities this mainboard has.
 * The mainboard is not classified as a hardware part.
 * 
 * @see ComputerPart
 * @see Hardware
 */
public class Mainboard extends ComputerPart {

    private List<MainboradSlot> slots;

    /**
     * Creates a new mainboard and sets the name, the vulnerabilities and a list of all avaiable mainboard slots.
     * 
     * @param name The name the part has.
     * @param vulnerabilities The vulnerabilities the part has.
     * @param slots A list of all avaiable mainboard slots.
     */
    public Mainboard(String name, List<Vulnerability> vulnerabilities, List<MainboradSlot> slots) {

        super(name, vulnerabilities);

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

    /**
     * This class represents a mainboard slot which can have a hardware part as content.
     * The hardware type a slot can accept is defined using generics and the type class.
     * A mainboard slot is not classiefied as a computer part and only used by the mainboard class.
     * 
     * @see Mainboard
     * @see Hardware
     */
    public static class MainboradSlot {

        private Class<? extends Hardware> type;
        private Hardware                  content;

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

    }

}
