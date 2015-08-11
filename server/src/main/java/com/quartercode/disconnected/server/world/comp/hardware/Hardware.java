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

package com.quartercode.disconnected.server.world.comp.hardware;

import javax.xml.bind.annotation.XmlAttribute;
import org.apache.commons.lang3.Validate;
import com.quartercode.disconnected.server.world.comp.Computer;
import com.quartercode.disconnected.server.world.util.WorldNode;

/**
 * This class is the base class for classes which store information about a part of hardware, like a {@link Mainboard}, a {@link CPU}, or a {@link RAM} module.
 *
 * @see Computer
 */
public class Hardware extends WorldNode<Computer> {

    @XmlAttribute
    private String name;

    // JAXB constructor
    protected Hardware() {

    }

    /**
     * Creates a new hardware part.
     *
     * @param name The "model" name of the new hardware part.
     *        See {@link #getName()} for more details.
     */
    public Hardware(String name) {

        Validate.notNull(name, "Cannot use null as hardware name");

        this.name = name;
    }

    /**
     * Returns the "model" name of the hardware part.
     * This name identifies a certain "model" of a hardware part type (e.g. a specific mainbord).
     * Therefore, all hardware parts of the same type (e.g. mainborads) and with the same specs should have the same name.
     *
     * @return The "model" name of the hardware part.
     */
    public String getName() {

        return name;
    }

}
