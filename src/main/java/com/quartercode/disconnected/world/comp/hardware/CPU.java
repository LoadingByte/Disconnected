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

package com.quartercode.disconnected.world.comp.hardware;

import com.quartercode.classmod.base.FeatureDefinition;
import com.quartercode.classmod.base.FeatureHolder;
import com.quartercode.classmod.base.def.AbstractFeatureDefinition;
import com.quartercode.classmod.extra.FunctionDefinition;
import com.quartercode.classmod.extra.def.LockableFEWrapper;
import com.quartercode.classmod.extra.def.ObjectProperty;
import com.quartercode.classmod.util.FunctionDefinitionFactory;
import com.quartercode.classmod.util.PropertyAccessorFactory;
import com.quartercode.disconnected.world.comp.Computer;
import com.quartercode.disconnected.world.comp.hardware.Mainboard.NeedsMainboardSlot;

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
    protected static final FeatureDefinition<ObjectProperty<Integer>> THREADS;

    /**
     * The tick frequency of the cpu, given in hertz.
     */
    protected static final FeatureDefinition<ObjectProperty<Long>>    FREQUENCY;

    static {

        THREADS = new AbstractFeatureDefinition<ObjectProperty<Integer>>("threads") {

            @Override
            public ObjectProperty<Integer> create(FeatureHolder holder) {

                return new ObjectProperty<Integer>(getName(), holder);
            }

        };

        FREQUENCY = new AbstractFeatureDefinition<ObjectProperty<Long>>("frequency") {

            @Override
            public ObjectProperty<Long> create(FeatureHolder holder) {

                return new ObjectProperty<Long>(getName(), holder);
            }

        };

    }

    // ----- Properties End -----

    // ----- Functions -----

    /**
     * Returns the amount of possible threads running at the same time (virtual cores).
     */
    public static final FunctionDefinition<Integer>                   GET_THREADS;

    /**
     * Changes the amount of possible threads running at the same time (virtual cores).
     * 
     * <table>
     * <tr>
     * <th>Index</th>
     * <th>Type</th>
     * <th>Parameter</th>
     * <th>Description</th>
     * </tr>
     * <tr>
     * <td>0</td>
     * <td>{@link Integer}</td>
     * <td>threads</td>
     * <td>The new thread amount.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>                      SET_THREADS;

    /**
     * Returns the tick frequency of the cpu, given in hertz.
     */
    public static final FunctionDefinition<Long>                      GET_FREQUENCY;

    /**
     * Changes the tick frequency of the cpu, given in hertz.
     * 
     * <table>
     * <tr>
     * <th>Index</th>
     * <th>Type</th>
     * <th>Parameter</th>
     * <th>Description</th>
     * </tr>
     * <tr>
     * <td>0</td>
     * <td>{@link Long}</td>
     * <td>frequency</td>
     * <td>The new tick frequency.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>                      SET_FREQUENCY;

    static {

        GET_THREADS = FunctionDefinitionFactory.create("getThreads", CPU.class, PropertyAccessorFactory.createGet(THREADS));
        SET_THREADS = FunctionDefinitionFactory.create("setThreads", CPU.class, new LockableFEWrapper<Void>(PropertyAccessorFactory.createSet(THREADS)), Integer.class);

        GET_FREQUENCY = FunctionDefinitionFactory.create("getFrequency", CPU.class, PropertyAccessorFactory.createGet(FREQUENCY));
        SET_FREQUENCY = FunctionDefinitionFactory.create("setFrequency", CPU.class, new LockableFEWrapper<Void>(PropertyAccessorFactory.createSet(FREQUENCY)), Long.class);

    }

    // ----- Functions End -----

    /**
     * Creates a new cpu.
     */
    public CPU() {

    }

}
