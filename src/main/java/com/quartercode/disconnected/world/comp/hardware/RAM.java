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

import com.quartercode.disconnected.mocl.base.FeatureDefinition;
import com.quartercode.disconnected.mocl.base.FeatureHolder;
import com.quartercode.disconnected.mocl.base.def.AbstractFeatureDefinition;
import com.quartercode.disconnected.mocl.extra.FunctionDefinition;
import com.quartercode.disconnected.mocl.extra.def.LockableFEWrapper;
import com.quartercode.disconnected.mocl.extra.def.ObjectProperty;
import com.quartercode.disconnected.mocl.util.FunctionDefinitionFactory;
import com.quartercode.disconnected.mocl.util.PropertyAccessorFactory;
import com.quartercode.disconnected.world.comp.hardware.Mainboard.NeedsMainboardSlot;

/**
 * This class represents a ram module of a computer.
 * A ram module has a size (given in bytes) and an access frequency (given in hertz).
 * 
 * @see Hardware
 */
@NeedsMainboardSlot
public class RAM extends Hardware {

    // ----- Properties -----

    /**
     * The size of the ram module, given in bytes.
     */
    protected static final FeatureDefinition<ObjectProperty<Long>> SIZE;

    /**
     * The access frequency of the ram module, given in hertz.
     */
    protected static final FeatureDefinition<ObjectProperty<Long>> FREQUENCY;

    static {

        SIZE = new AbstractFeatureDefinition<ObjectProperty<Long>>("size") {

            @Override
            public ObjectProperty<Long> create(FeatureHolder holder) {

                return new ObjectProperty<Long>(getName(), holder);
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
     * Returns the size of the ram module, given in bytes.
     */
    public static final FunctionDefinition<Long>                   GET_SIZE;

    /**
     * Changes the size of the ram module, given in bytes.
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
     * <td>size</td>
     * <td>The new size.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>                   SET_SIZE;

    /**
     * Returns the access frequency of the ram module, given in hertz.
     */
    public static final FunctionDefinition<Long>                   GET_FREQUENCY;

    /**
     * Changes the access frequency of the ram module, given in hertz.
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
     * <td>The new access frequency.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>                   SET_FREQUENCY;

    static {

        GET_SIZE = FunctionDefinitionFactory.create("getSize", RAM.class, PropertyAccessorFactory.createGet(SIZE));
        SET_SIZE = FunctionDefinitionFactory.create("setSize", RAM.class, new LockableFEWrapper<Void>(PropertyAccessorFactory.createSet(SIZE)), Long.class);

        GET_FREQUENCY = FunctionDefinitionFactory.create("getFrequency", RAM.class, PropertyAccessorFactory.createGet(FREQUENCY));
        SET_FREQUENCY = FunctionDefinitionFactory.create("setFrequency", RAM.class, new LockableFEWrapper<Void>(PropertyAccessorFactory.createSet(FREQUENCY)), Long.class);

    }

    /**
     * Creates a new ram module.
     */
    public RAM() {

    }

}
