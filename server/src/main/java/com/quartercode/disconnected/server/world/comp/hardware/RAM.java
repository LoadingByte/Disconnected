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
import com.quartercode.disconnected.server.world.comp.hardware.Mainboard.NeedsMainboardSlot;

/**
 * This class represents a RAM module of a computer.
 * A RAM module has a size in bytes and an access frequency in hertz.
 *
 * @see Hardware
 */
@NeedsMainboardSlot
public class RAM extends Hardware {

    @XmlAttribute
    private long size;
    @XmlAttribute
    private long accessFrequency;

    // JAXB constructor
    protected RAM() {

    }

    /**
     * Creates a new RAM module.
     *
     * @param name The "model" name of the new RAM module.
     *        See {@link #getName()} for more details.
     * @param size The size of the RAM module in bytes.
     * @param accessFrequency The frequency at which the RAM module can be accessed in hertz.
     */
    public RAM(String name, long size, long accessFrequency) {

        super(name);

        Validate.isTrue(size > 0, "RAM module size must be > 0");
        Validate.isTrue(accessFrequency > 0, "RAM module access frequency must be > 0");

        this.size = size;
        this.accessFrequency = accessFrequency;
    }

    /**
     * Returns the size of the RAM module in bytes.
     *
     * @return The size of the RAM module in bytes.
     */
    public long getSize() {

        return size;
    }

    /**
     * Returns the frequency at which the RAM module can be accessed in hertz.
     *
     * @return The access frequency of the RAM module in hertz.
     */
    public long getAccessFrequency() {

        return accessFrequency;
    }

}
