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

import javax.xml.bind.annotation.XmlAttribute;
import com.quartercode.disconnected.util.InfoString;
import com.quartercode.mocl.base.Feature;
import com.quartercode.mocl.base.FeatureHolder;

/**
 * An abstract feature is a part of a {@link FeatureHolder} and is used for creating modifiable classes.
 * It's an implementation of the {@link Feature} interface.
 * The content of a feature is not limited, but it has to provide a name and store its holder.
 * 
 * @see Feature
 */
public class AbstractFeature implements Feature, InfoString {

    @XmlAttribute
    private String        name;
    private FeatureHolder holder;

    /**
     * Creates a new empty abstract feature.
     * This is only recommended for direct field access (e.g. for serialization).
     */
    protected AbstractFeature() {

    }

    /**
     * Creates a new abstract feature with the given name and {@link FeatureHolder}.
     * 
     * @param name The name of the {@link Feature}.
     * @param holder The feature holder which has and uses the new {@link Feature}.
     */
    public AbstractFeature(String name, FeatureHolder holder) {

        this.name = name;
        this.holder = holder;
    }

    @Override
    public String getName() {

        return name;
    }

    @Override
    public FeatureHolder getHolder() {

        return holder;
    }

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = prime * result + (name == null ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        AbstractFeature other = (AbstractFeature) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }

    @Override
    public String toInfoString() {

        return getName();
    }

    @Override
    public String toString() {

        return getClass().getName() + " [" + toInfoString() + "]";
    }

}
