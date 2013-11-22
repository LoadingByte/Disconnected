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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import com.quartercode.disconnected.util.InfoString;
import com.quartercode.mocl.base.Feature;
import com.quartercode.mocl.base.FeatureDefinition;
import com.quartercode.mocl.base.FeatureHolder;
import com.quartercode.mocl.extra.Persistent;

/**
 * A default feature holder is a class which is modifiable through {@link Feature}s.
 * It is just an implementation of {@link FeatureHolder}.
 * A user can get {@link Feature}s through the central access method {@link #get(FeatureDefinition)}.
 * Such {@link Feature}s are defined by {@link FeatureDefinition} which describe how a feature looks like.
 * 
 * @see FeatureHolder
 * @see Feature
 * @see FeatureDefinition
 */
public class DefaultFeatureHolder implements FeatureHolder, InfoString {

    private final List<Feature> features = new ArrayList<Feature>();

    /**
     * Creates a new default feature holder.
     */
    public DefaultFeatureHolder() {

    }

    @SuppressWarnings ("unchecked")
    @Override
    public <F extends Feature> F get(FeatureDefinition<F> definition) {

        for (Feature feature : features) {
            if (feature.getName().equals(definition.getName())) {
                return (F) feature;
            }
        }

        F feature = definition.create(this);
        features.add(feature);
        return feature;
    }

    /**
     * Returns a list of all {@link Persistent} {@link Feature}s of the default feature holder.
     * This uses an object list since JAXB can't handle interfaces.
     * 
     * @return All {@link Persistent} {@link Feature}s of the default feature holder.
     */
    @XmlElement (name = "features")
    public List<Object> getPersistentFeatures() {

        List<Object> persistentFeatures = new ArrayList<Object>();
        for (Feature feature : features) {
            if (feature.getClass().isAnnotationPresent(Persistent.class)) {
                persistentFeatures.add(feature);
            }
        }

        return persistentFeatures;
    }

    /**
     * Adds the given list of {@link Persistent} {@link Feature}s to the default feature holder.
     * This uses an object list since JAXB can't handle interfaces.
     * 
     * @param persistentFeatures The {@link Persistent} {@link Feature}s to add.
     */
    public void setPersistentFeatures(List<Object> persistentFeatures) {

        for (Object persistentFeature : persistentFeatures) {
            if (persistentFeature instanceof Feature) {
                features.add((Feature) persistentFeature);
            }
        }
    }

    @Override
    public Iterator<Feature> iterator() {

        return features.iterator();
    }

    /**
     * Returns the unique serialization id for the default feature holder.
     * The id is just the identy hash code ({@link System#identityHashCode(Object)}) of the object as a hexadecimal string.
     * 
     * @return The unique serialization id for the default feature holder.
     */
    @XmlAttribute
    @XmlID
    public String getId() {

        return Integer.toHexString(System.identityHashCode(this));
    }

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = prime * result + (features == null ? 0 : features.hashCode());
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
        DefaultFeatureHolder other = (DefaultFeatureHolder) obj;
        if (features == null) {
            if (other.features != null) {
                return false;
            }
        } else if (!features.equals(other.features)) {
            return false;
        }
        return true;
    }

    @Override
    public String toInfoString() {

        String featureString = "";
        for (Feature feature : features) {
            featureString += ", " + (feature instanceof InfoString ? ((InfoString) feature).toInfoString() : feature.toString());
        }
        featureString = featureString.substring(2);

        return featureString;
    }

    @Override
    public String toString() {

        return getClass().getName() + " [" + toInfoString() + "]";
    }

}
