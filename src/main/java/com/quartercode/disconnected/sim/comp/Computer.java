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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import com.quartercode.disconnected.sim.Location;
import com.quartercode.disconnected.sim.comp.hardware.Hardware;
import com.quartercode.disconnected.sim.comp.hardware.Mainboard;
import com.quartercode.disconnected.sim.comp.os.OperatingSystem;
import com.quartercode.disconnected.sim.comp.program.Program;

/**
 * This class stores information about a computer, like the mainboard, other hardware, programs etc.
 * 
 * @see Location
 * 
 * @see ComputerPart
 * @see Mainboard
 * @see Hardware
 * @see OperatingSystem
 * @see Program
 */
public class Computer {

    private Location             location;
    @XmlElement (name = "hardware")
    private final List<Hardware> hardware = new CopyOnWriteArrayList<Hardware>();
    private OperatingSystem      operatingSystem;

    /**
     * Creates a new computer.
     */
    public Computer() {

    }

    /**
     * Returns the unique serialization id for the computer.
     * The id is the identy hash code of the computer object.
     * 
     * @return The unique serialization id for the computer.
     */
    @XmlID
    @XmlAttribute
    public String getId() {

        return Integer.toHexString(System.identityHashCode(this));
    }

    /**
     * Returns the location of the computer.
     * 
     * @return The location of the computer.
     */
    public Location getLocation() {

        return location;
    }

    /**
     * Sets the location of the computer to a new one.
     * 
     * @param location The new location of the computer.
     */
    public void setLocation(Location location) {

        this.location = location;
    }

    /**
     * Returns the hardware the computer contains.
     * 
     * @return The hardware the computer contains.
     */
    public List<Hardware> getHardware() {

        return Collections.unmodifiableList(hardware);
    }

    /**
     * Returns the hardware parts in the computer which have the given type as a superclass.
     * 
     * @param type The type to use for the selection.
     * @return The hardware parts in the computer which have the given type as a superclass.
     */
    public <T> List<T> getHardware(Class<T> type) {

        List<T> hardware = new ArrayList<T>();
        for (Hardware hardwarePart : this.hardware) {
            if (type.isAssignableFrom(hardwarePart.getClass())) {
                hardware.add(type.cast(hardwarePart));
            }
        }
        return hardware;
    }

    /**
     * Adds a hardware part to the computer.
     * 
     * @param hardware The hardware part to add to the computer.
     */
    public void addHardware(Hardware hardware) {

        this.hardware.add(hardware);
    }

    /**
     * Removes a hardware part from the computer.
     * 
     * @param hardware The hardware part to from from the computer.
     */
    public void removeHardware(Hardware hardware) {

        this.hardware.remove(hardware);
    }

    /**
     * Returns the opertating system which is installed on the computer.
     * 
     * @return The opertating system which is installed on the computer.
     */
    public OperatingSystem getOperatingSystem() {

        return operatingSystem;
    }

    /**
     * Sets the opertating system which is installed on the computer to a new one.
     * 
     * @param operatingSystem The new opertating system which is installed on the computer.
     */
    public void setOperatingSystem(OperatingSystem operatingSystem) {

        this.operatingSystem = operatingSystem;
    }

    /**
     * Returns all computer parts this computer currently contains.
     * This collects the objects from every sublist and creates a new list out of them.
     * 
     * @return All computer parts this computer currently contains.
     */
    public List<ComputerPart> getParts() {

        List<ComputerPart> parts = new ArrayList<ComputerPart>();

        parts.addAll(hardware);
        parts.add(operatingSystem);

        return parts;
    }

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = prime * result + (hardware == null ? 0 : hardware.hashCode());
        result = prime * result + (location == null ? 0 : location.hashCode());
        result = prime * result + (operatingSystem == null ? 0 : operatingSystem.hashCode());
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
        Computer other = (Computer) obj;
        if (hardware == null) {
            if (other.hardware != null) {
                return false;
            }
        } else if (!hardware.equals(other.hardware)) {
            return false;
        }
        if (location == null) {
            if (other.location != null) {
                return false;
            }
        } else if (!location.equals(other.location)) {
            return false;
        }
        if (operatingSystem == null) {
            if (other.operatingSystem != null) {
                return false;
            }
        } else if (!operatingSystem.equals(other.operatingSystem)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {

        List<String> hardwareInfo = new ArrayList<String>();
        for (Hardware hardwarePart : hardware) {
            hardwareInfo.add(hardwarePart.toInfoString());
        }
        return getClass().getName() + " [location=" + location + ", hardware=" + hardwareInfo + ", operatingSystem=" + operatingSystem.toInfoString() + "]";
    }

}
