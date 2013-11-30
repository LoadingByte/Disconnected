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

package com.quartercode.disconnected.mocl.extra.def;

import javax.xml.bind.Unmarshaller;
import com.quartercode.disconnected.mocl.base.Feature;
import com.quartercode.disconnected.mocl.base.FeatureDefinition;
import com.quartercode.disconnected.mocl.base.FeatureHolder;
import com.quartercode.disconnected.mocl.base.def.DefaultFeatureHolder;
import com.quartercode.disconnected.mocl.extra.ChildFeatureHolder;

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
     * Creates a new default child feature holder.
     */
    public DefaultChildFeatureHolder() {

    }

    @Override
    public P getParent() {

        return parent;
    }

    @Override
    public void setParent(P parent) {

        this.parent = parent;
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
