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

package com.quartercode.mocl.extra.def;

import javax.xml.bind.Unmarshaller;
import com.quartercode.mocl.base.Feature;
import com.quartercode.mocl.base.FeatureDefinition;
import com.quartercode.mocl.base.FeatureHolder;
import com.quartercode.mocl.base.def.DefaultFeatureHolder;
import com.quartercode.mocl.extra.ChildFeatureHolder;

/**
 * A child feature holder is a {@link FeatureHolder} which stores its parent {@link FeatureHolder}.
 * It uses the {@link DefaultFeatureHolder} implementation.
 * A user can get {@link Feature}s through the central access method {@link #get(FeatureDefinition)}.
 * Such {@link Feature}s are defined by {@link FeatureDefinition} which describe how a feature looks like.
 * 
 * @param <P> The type the parent {@link FeatureHolder} has to have.
 * @see FeatureHolder
 * @see Feature
 * @see FeatureDefinition
 */
public class DefaultChildFeatureHolder<P extends FeatureHolder> extends DefaultFeatureHolder implements ChildFeatureHolder<P> {

    private P parent;

    /**
     * Creates a new empty default child feature holder.
     * This is only recommended for direct field access (e.g. for serialization).
     */
    protected DefaultChildFeatureHolder() {

    }

    /**
     * Creates a new default child feature holder which has the given parent {@link FeatureHolder}.
     * 
     * @param parent The parent {@link FeatureHolder} which is storing this default child feature holder.
     */
    public DefaultChildFeatureHolder(P parent) {

        this.parent = parent;
    }

    /**
     * Returns The parent {@link FeatureHolder} which is storing this default child feature holder.
     * 
     * @return The parent {@link FeatureHolder}.
     */
    @Override
    public P getParent() {

        return parent;
    }

    /**
     * Resolves the parent {@link FeatureHolder} which is storing this feature holder during umarshalling.
     * 
     * @param unmarshaller The unmarshaller which unmarshals this objects.
     * @param parent The object which was unmarshalled as the parent {@link FeatureHolder} from the xml structure.
     */
    @SuppressWarnings ("unchecked")
    protected void beforeUnmarshal(Unmarshaller unmarshaller, Object parent) {

        if (parent instanceof Feature) {
            this.parent = (P) ((Feature) parent).getHolder();
        }
    }

}
