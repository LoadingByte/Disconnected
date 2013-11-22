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

import java.util.Iterator;
import java.util.List;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import com.quartercode.mocl.base.Feature;
import com.quartercode.mocl.base.FeatureDefinition;
import com.quartercode.mocl.base.FeatureHolder;
import com.quartercode.mocl.base.def.AbstractFeature;
import com.quartercode.mocl.base.def.DefaultFeatureHolder;
import com.quartercode.mocl.extra.Persistent;

/**
 * A feature holder feature is a {@link Feature} which offers the functionality of an additional {@link FeatureHolder}.
 * That allows making nested {@link Feature}s like function groups etc.
 */
public class FeatureHolderFeature extends AbstractFeature implements FeatureHolder {

    private final DefaultFeatureHolder featureHolder = new DefaultFeatureHolder();

    /**
     * Creates a new empty feature holder feature.
     * This is only recommended for direct field access (e.g. for serialization).
     */
    protected FeatureHolderFeature() {

    }

    /**
     * Creates a new feature holder feature with the given name and parent {@link FeatureHolder}.
     * 
     * @param name The name of the feature holder feature.
     * @param holder The feature holder which has and uses the new feature holder feature.
     */
    public FeatureHolderFeature(String name, FeatureHolder holder) {

        super(name, holder);
    }

    @Override
    public <F extends Feature> F get(FeatureDefinition<F> definition) {

        return featureHolder.get(definition);
    }

    /**
     * Returns a list of all {@link Persistent} {@link Feature}s of the feature holder feature.
     * This uses an object list since JAXB can't handle interfaces.
     * 
     * @return All {@link Persistent} {@link Feature}s of the feature holder feature.
     */
    @XmlElement (name = "features")
    public List<Object> getPersistentFeatures() {

        return featureHolder.getPersistentFeatures();
    }

    /**
     * Adds the given list of {@link Persistent} {@link Feature}s to the feature holder feature.
     * This uses an object list since JAXB can't handle interfaces.
     * 
     * @param persistentFeatures The {@link Persistent} {@link Feature}s to add.
     */
    public void setPersistentFeatures(List<Object> persistentFeatures) {

        featureHolder.setPersistentFeatures(persistentFeatures);
    }

    @Override
    public Iterator<Feature> iterator() {

        return featureHolder.iterator();
    }

    /**
     * Returns the unique serialization id for the feature holder feature.
     * The id is just the identy hash code ({@link System#identityHashCode(Object)}) of the object as a hexadecimal string.
     * 
     * @return The unique serialization id for the feature holder feature.
     */
    @XmlAttribute
    @XmlID
    public String getId() {

        return Integer.toHexString(System.identityHashCode(this));
    }

}
