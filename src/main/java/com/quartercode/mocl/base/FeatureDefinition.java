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

package com.quartercode.mocl.base;

/**
 * A feature definition is used to get a {@link Feature} from a {@link FeatureHolder}.
 * It contains the name of the {@link Feature} and the type it has as a generic parameter.
 * You can use a feature definition to construct a new instance of the defined feature through {@link #create(FeatureHolder)}.
 * 
 * @param <F> The type the defined {@link Feature} has.
 * @see Feature
 * @see Named
 */
public interface FeatureDefinition<F extends Feature> extends Named {

    /**
     * Returns the name of the defined {@link Feature}.
     * The name is used for storing and accessing a created {@link Feature} in a {@link FeatureHolder}.
     * 
     * @return The name of the {@link Feature}.
     */
    @Override
    public String getName();

    /**
     * Creates a new {@link Feature} which is defined by this feature definition using the given holder.
     * The holder is a {@link FeatureHolder} which can have different features.
     * 
     * @param holder The {@link FeatureHolder} which holds the new {@link Feature}.
     * @return The created {@link Feature}.
     */
    public F create(FeatureHolder holder);

}
