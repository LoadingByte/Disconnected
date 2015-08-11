/*
 * This file is part of Disconnected.
 * Copyright (c) 2013 QuarterCode <http://quartercode.com/>
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

package com.quartercode.disconnected.server.world.comp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import org.apache.commons.lang3.Validate;
import com.quartercode.disconnected.server.world.World;
import com.quartercode.disconnected.server.world.comp.hardware.Hardware;
import com.quartercode.disconnected.server.world.comp.os.OperatingSystem;
import com.quartercode.disconnected.server.world.util.WorldNode;
import com.quartercode.disconnected.shared.world.general.Location;
import com.quartercode.jtimber.api.node.wrapper.SubstituteWithWrapper;
import com.quartercode.jtimber.api.node.wrapper.collection.ListWrapper;

/**
 * This class stores information about a computer, like the {@link Location} or the {@link Hardware} parts.
 *
 * @see Location
 * @see Hardware
 */
public class Computer extends WorldNode<World> {

    @XmlAttribute
    private Location             location;
    @XmlElementWrapper
    @XmlElementRef
    @SubstituteWithWrapper (ListWrapper.class)
    private final List<Hardware> hardware = new ArrayList<>();
    @XmlElement
    private OperatingSystem      os;

    // JAXB constructor
    protected Computer() {

    }

    /**
     * Creates a new computer.
     *
     * @param location The geographical {@link Location} of the computer.
     * @param os The {@link OperatingSystem} instance which runs the computer.
     */
    public Computer(Location location, OperatingSystem os) {

        Validate.notNull(location, "Computer location cannot be null");
        Validate.notNull(os, "Computer operating system cannot be null");

        this.location = location;
        this.os = os;
    }

    /**
     * Returns the geographical {@link Location} of the computer.
     *
     * @return The location of the computer.
     */
    public Location getLocation() {

        return location;
    }

    /**
     * Returns the {@link Hardware} parts the computer contains.
     *
     * @return The hardware of the computer.
     */
    public List<Hardware> getHardware() {

        return Collections.unmodifiableList(hardware);
    }

    /**
     * Returns all the {@link Hardware} which are an instance of the given class and used by this computer.
     * If no matching hardware part is found, this method returns an empty list.<br>
     * If you want to retrieve <b>exactly</b> one hardware part of a given type, try {@link #getSingleHardwareByType(Class)}.
     *
     * @param type The type the returned hardware parts must be instances of.
     * @return All hardware parts which are an instance of the given type. May be empty.
     */
    @SuppressWarnings ("unchecked")
    public <T extends Hardware> List<T> getHardwareByType(Class<T> type) {

        List<T> hardwareByType = new ArrayList<>();

        for (Hardware hardwarePart : hardware) {
            if (type.isInstance(hardwarePart)) {
                hardwareByType.add((T) hardwarePart);
            }
        }

        return hardwareByType;
    }

    /**
     * If this computer contains <b>exactly</b> one {@link Hardware} part of the given type, returns that part. Otherwise, throws an {@link IllegalStateException}.
     * Note that such exceptions are also thrown if no matching hardware part can be found.
     *
     * @param type The type the returned hardware part must be an instance of.
     * @return The hardware part which is an instance of the given type. May not be {@code null}.
     * @throws IllegalStateException If there are 0 or multiple hardware parts that match the given type.
     */
    public <T extends Hardware> T getSingleHardwareByType(Class<T> type) {

        List<T> hardwareByType = getHardwareByType(type);

        Validate.validState(hardwareByType.size() != 0, "This computer contains no '%s'", type.getName());
        Validate.validState(hardwareByType.size() <= 1, "This computer contains more than one '%s'", type.getName());

        return hardwareByType.get(0);
    }

    /**
     * Adds a {@link Hardware} part to the computer.
     *
     * @param hardware The hardware part to add to the computer.
     */
    public void addHardware(Hardware hardware) {

        Validate.notNull(hardware, "Cannot add null hardware to computer");

        this.hardware.add(hardware);
    }

    /**
     * Removes a {@link Hardware} part from the computer.
     *
     * @param hardware The hardware part to remove from the computer.
     */
    public void removeHardware(Hardware hardware) {

        this.hardware.remove(hardware);
    }

    /**
     * Returns the {@link OperatingSystem} instance which runs the computer.
     * Note that it is <b>not</b> {@code null} if the computer is turned of.
     * Instead, {@link OperatingSystem#isRunning()} returns {@code false};
     *
     * @return The operating system which runs the computer.
     */
    public OperatingSystem getOs() {

        return os;
    }

}
