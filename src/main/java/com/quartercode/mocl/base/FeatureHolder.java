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
 * A feature holder is a class which is modifiable through {@link Feature}s.
 * A user can get {@link Feature}s through the central access method {@link #get(FeatureDefinition)}.
 * Such {@link Feature}s are defined by {@link FeatureDefinition} which describe how a feature looks like.
 * 
 * @see Feature
 * @see FeatureDefinition
 */
public interface FeatureHolder extends Iterable<Feature> {

    /**
     * Returns the {@link Feature} which is defined by the given {@link FeatureDefinition}.
     * The method should also create a new {@link Feature} from the {@link FeatureDefinition} if the requested one doesn't exist.
     * 
     * @param definition The {@link FeatureDefinition} which describes the requested {@link Feature}.
     * @return The {@link Feature} which is requestes.
     */
    public <F extends Feature> F get(FeatureDefinition<F> definition);

}
