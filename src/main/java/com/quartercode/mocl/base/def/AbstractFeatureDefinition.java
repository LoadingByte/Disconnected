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

package com.quartercode.mocl.base.def;

import com.quartercode.mocl.base.Feature;
import com.quartercode.mocl.base.FeatureDefinition;
import com.quartercode.mocl.base.FeatureHolder;

/**
 * An abstract feature definition is used to get a {@link Feature} from a {@link FeatureHolder}.
 * It's an implementation of the {@link FeatureDefinition} interface.
 * It contains the name of the {@link Feature} and the type it has as a generic parameter.
 * You can use an abstract feature definition to construct a new instance of the defined {@link Feature} through {@link #create(FeatureHolder)}.
 * 
 * @param <F> The type the defined {@link Feature} has.
 * @see FeatureDefinition
 * @see Feature
 */
public abstract class AbstractFeatureDefinition<F extends Feature> implements FeatureDefinition<F> {

    private final String name;

    /**
     * Creates a new abstract feature definition for defining a {@link Feature} with the given name.
     * 
     * @param name The name of the defined {@link Feature}.
     */
    public AbstractFeatureDefinition(String name) {

        this.name = name;
    }

    @Override
    public String getName() {

        return name;
    }

}
