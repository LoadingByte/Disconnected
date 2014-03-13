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

package com.quartercode.disconnected.world;

import com.quartercode.classmod.base.FeatureDefinition;
import com.quartercode.classmod.base.FeatureHolder;
import com.quartercode.classmod.base.def.AbstractFeatureDefinition;
import com.quartercode.classmod.base.def.DefaultFeatureHolder;
import com.quartercode.classmod.extra.FunctionDefinition;
import com.quartercode.classmod.extra.def.LockableFEWrapper;
import com.quartercode.classmod.extra.def.ObjectProperty;
import com.quartercode.classmod.util.FunctionDefinitionFactory;
import com.quartercode.classmod.util.PropertyAccessorFactory;

/**
 * This class stores a location using x and y relative coordinates.
 */
public class Location extends DefaultFeatureHolder {

    // ----- Properties -----

    /**
     * The relative relative x coordinate of the location.
     */
    protected static final FeatureDefinition<ObjectProperty<Float>> X;

    /**
     * The relative y coordinate of the location.
     */
    protected static final FeatureDefinition<ObjectProperty<Float>> Y;

    static {

        X = new AbstractFeatureDefinition<ObjectProperty<Float>>("x") {

            @Override
            public ObjectProperty<Float> create(FeatureHolder holder) {

                return new ObjectProperty<Float>(getName(), holder);
            }

        };

        Y = new AbstractFeatureDefinition<ObjectProperty<Float>>("y") {

            @Override
            public ObjectProperty<Float> create(FeatureHolder holder) {

                return new ObjectProperty<Float>(getName(), holder);
            }

        };

    }

    // ----- Properties End -----

    // ----- Functions -----

    /**
     * Returns the relative x coordinate of the location.
     */
    public static final FunctionDefinition<Float>                   GET_X;

    /**
     * Changes the relative x coordinate of the location.
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
     * <td>{@link Float}</td>
     * <td>x</td>
     * <td>The new relative x coordinate.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>                    SET_X;

    /**
     * Returns the relative y coordinate of the location.
     */
    public static final FunctionDefinition<Float>                   GET_Y;

    /**
     * Changes the relative y coordinate of the location.
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
     * <td>{@link Float}</td>
     * <td>y</td>
     * <td>The new relative y coordinate.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>                    SET_Y;

    static {

        GET_X = FunctionDefinitionFactory.create("getX", Location.class, PropertyAccessorFactory.createGet(X));
        SET_X = FunctionDefinitionFactory.create("setX", Location.class, new LockableFEWrapper<Void>(PropertyAccessorFactory.createSet(X)), Float.class);

        GET_Y = FunctionDefinitionFactory.create("getY", Location.class, PropertyAccessorFactory.createGet(Y));
        SET_Y = FunctionDefinitionFactory.create("setY", Location.class, new LockableFEWrapper<Void>(PropertyAccessorFactory.createSet(Y)), Float.class);

    }

    // ----- Functions End -----

    /**
     * Creates a new relative location.
     */
    public Location() {

    }

}
