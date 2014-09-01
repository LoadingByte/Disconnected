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

package com.quartercode.disconnected.server.world.comp.hardware;

import static com.quartercode.classmod.ClassmodFactory.create;
import org.apache.commons.lang3.reflect.TypeLiteral;
import com.quartercode.classmod.extra.PropertyDefinition;
import com.quartercode.classmod.extra.storage.StandardStorage;
import com.quartercode.disconnected.server.world.comp.Computer;
import com.quartercode.disconnected.server.world.comp.hardware.Mainboard.NeedsMainboardSlot;

/**
 * This class represents a cpu of a {@link Computer}.
 * A cpu has a count of possible threads running at the same time and a frequency (given in hertz).
 * 
 * @see Hardware
 */
@NeedsMainboardSlot
public class CPU extends Hardware {

    // ----- Properties -----

    /**
     * The amount of possible threads running at the same time (virtual cores).
     */
    public static final PropertyDefinition<Integer> THREADS;

    /**
     * The tick frequency of the cpu, given in hertz.
     */
    public static final PropertyDefinition<Long>    FREQUENCY;

    static {

        THREADS = create(new TypeLiteral<PropertyDefinition<Integer>>() {}, "name", "threads", "storage", new StandardStorage<>());
        FREQUENCY = create(new TypeLiteral<PropertyDefinition<Long>>() {}, "name", "frequency", "storage", new StandardStorage<>());

    }

}
