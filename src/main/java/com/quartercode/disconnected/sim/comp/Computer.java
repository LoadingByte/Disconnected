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
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;

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
@XmlAccessorType (XmlAccessType.FIELD)
public class Computer {

    @XmlAttribute
    @XmlID
    private String                      id;

    private Location                    location;
    private Mainboard                   mainboard;
    @XmlElement (name = "hardware")
    private final List<Hardware>        hardware         = new CopyOnWriteArrayList<Hardware>();
    @XmlElement (name = "operatingSystem")
    private final List<OperatingSystem> operatingSystems = new CopyOnWriteArrayList<OperatingSystem>();
    @XmlElement (name = "program")
    private final List<Program>         programs         = new CopyOnWriteArrayList<Program>();

    /**
     * Creates a new empty computer.
     * This is only recommended for direct field access (e.g. for serialization).
     */
    public Computer() {

    }

    /**
     * Creates a new computer and sets the final id.
     * 
     * @param id The final id for the computer.
     */
    public Computer(String id) {

        this.id = id;
    }

    /**
     * Returns the final id the computer has.
     * 
     * @return The final id the computer has.
     */
    public String getId() {

        return id;
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
     * Returns the mainboard of the computer which every other hardware part needs.
     * 
     * @return The mainboard of the computer.
     */
    public Mainboard getMainboard() {

        return mainboard;
    }

    /**
     * Sets the mainboard of the computer which every other hardware part needs to a new one.
     * 
     * @param mainboard The new mainboard of the computer.
     */
    public void setMainboard(Mainboard mainboard) {

        this.mainboard = mainboard;
    }

    /**
     * Returns the hardware the computer contains.
     * The mainboard is not classified as hardware and stored seperately.
     * 
     * @return The hardware the computer contains, except for the mainboards.
     */
    public List<Hardware> getHardware() {

        return Collections.unmodifiableList(hardware);
    }

    /**
     * Adds a hardware part to the computer.
     * The mainboard is not classified as hardware and stored seperately.
     * 
     * @param hardware The hardware part to add to the computer.
     */
    public void addHardware(Hardware hardware) {

        this.hardware.add(hardware);
    }

    /**
     * Removes a hardware part from the computer.
     * The mainboard is not classified as hardware and stored seperately.
     * 
     * @param hardware The hardware part to from from the computer.
     */
    public void removeHardware(Hardware hardware) {

        this.hardware.remove(hardware);
    }

    /**
     * Returns all opertating systems which are installed on the computer.
     * 
     * @return All opertating systems which are installed on the computer.
     */
    public List<OperatingSystem> getOperatingSystems() {

        return Collections.unmodifiableList(operatingSystems);
    }

    /**
     * Adds an operating system to the computer.
     * 
     * @param operatingSystem The operating system to add to the computer.
     */
    public void addOperatingSystem(OperatingSystem operatingSystem) {

        operatingSystems.add(operatingSystem);
    }

    /**
     * Removes an operating system from the computer.
     * 
     * @param operatingSystem The operating system to remove from the computer.
     */
    public void removeOperatingSystem(OperatingSystem operatingSystem) {

        operatingSystems.remove(operatingSystem);
    }

    /**
     * Returns all programs which are installed on the computer.
     * 
     * @return All programs which are installed on the computer.
     */
    public List<Program> getPrograms() {

        return Collections.unmodifiableList(programs);
    }

    /**
     * Adds a program to the computer.
     * 
     * @param program The program to add to the computer.
     */
    public void addProgram(Program program) {

        programs.add(program);
    }

    /**
     * Removes a program from the computer.
     * 
     * @param program The program to remove from the computer.
     */
    public void removeProgram(Program program) {

        programs.remove(program);
    }

    /**
     * Returns all computer parts this computer currently contains.
     * This collects the objects from every sublist and creates a new list out of them.
     * 
     * @return All computer parts this computer currently contains.
     */
    public List<ComputerPart> getParts() {

        List<ComputerPart> parts = new ArrayList<ComputerPart>();

        parts.add(mainboard);
        parts.addAll(hardware);
        parts.addAll(operatingSystems);
        parts.addAll(programs);

        return parts;
    }

}
