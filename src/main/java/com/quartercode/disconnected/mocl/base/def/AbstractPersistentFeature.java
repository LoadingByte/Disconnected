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

package com.quartercode.disconnected.mocl.base.def;

import javax.xml.bind.Unmarshaller;
import com.quartercode.disconnected.mocl.base.Feature;
import com.quartercode.disconnected.mocl.base.FeatureHolder;
import com.quartercode.disconnected.mocl.base.Persistent;

/**
 * The abstract persistent feature is a {@link Feature} which can be serialized using JAXB.
 * 
 * @see Feature
 * @see AbstractFeature
 */
@Persistent
public class AbstractPersistentFeature extends AbstractFeature {

    /**
     * Creates a new empty abstract persistent feature.
     * This is only recommended for direct field access (e.g. for serialization).
     */
    protected AbstractPersistentFeature() {

        super(null, null);
    }

    /**
     * Creates a new abstract persistent feature with the given name and {@link FeatureHolder}.
     * 
     * @param name The name of the {@link Feature}.
     * @param holder The feature holder which has and uses the new {@link Feature}.
     */
    public AbstractPersistentFeature(String name, FeatureHolder holder) {

        super(name, holder);
    }

    /**
     * Resolves the {@link FeatureHolder} which houses the abstract persistent feature.
     * 
     * @param unmarshaller The unmarshaller which unmarshals this task.
     * @param parent The object which was unmarshalled as the parent one from the xml structure.
     */
    protected void beforeUnmarshal(Unmarshaller unmarshaller, Object parent) {

        if (parent instanceof FeatureHolder) {
            setHolder((FeatureHolder) parent);
        }
    }

}
