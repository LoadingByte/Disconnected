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

package com.quartercode.disconnected.test.sim;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import javax.xml.bind.JAXBException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import com.quartercode.classmod.base.Feature;
import com.quartercode.classmod.base.def.DefaultFeatureHolder;
import com.quartercode.classmod.extra.CollectionProperty;
import com.quartercode.classmod.extra.Storage;
import com.quartercode.classmod.extra.ValueSupplier;
import com.quartercode.classmod.extra.def.DefaultCollectionProperty;
import com.quartercode.classmod.extra.def.DefaultProperty;
import com.quartercode.disconnected.Main;
import com.quartercode.disconnected.sim.profile.Profile;
import com.quartercode.disconnected.sim.profile.ProfileSerializer;
import com.quartercode.disconnected.util.RandomPool;
import com.quartercode.disconnected.world.World;
import com.quartercode.disconnected.world.gen.WorldGenerator;

public class ProfileSerializerTest {

    private World world;

    @BeforeClass
    public static void setUpBeforeClass() throws IOException {

        Main.fillGlobalStorage();
        Main.fillResourceStore();
    }

    @Before
    public void setUp() {

        world = WorldGenerator.generateWorld(new RandomPool(Profile.DEFAULT_RANDOM_POOL_SIZE), 10);
    }

    @Test
    public void testSerializeWorld() throws IOException, JAXBException {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ProfileSerializer.serializeWorld(outputStream, world);
        outputStream.flush();
        String serialized = new String(outputStream.toByteArray(), "UTF-8");

        World copy = ProfileSerializer.deserializeWorld(new ByteArrayInputStream(serialized.getBytes("UTF-8")));
        Assert.assertTrue("Serialized-deserialized copy of world does not equal original", equalsPersistent(world, copy));
    }

    /*
     * Method for checking whether the persistent features of the given feature holders are equal to each other.
     */
    private boolean equalsPersistent(DefaultFeatureHolder holder1, DefaultFeatureHolder holder2) {

        List<Feature> features1 = holder1.getPersistentFeatures();
        List<Feature> features2 = holder2.getPersistentFeatures();

        if (features1.size() != features2.size()) {
            return false;
        } else {
            for (Feature feature1 : features1) {
                boolean contains = false;
                for (Feature feature2 : features2) {
                    if (equalsPersistent(feature1, feature2)) {
                        contains = true;
                        break;
                    }
                }

                if (!contains) {
                    return false;
                }
            }

            return true;
        }
    }

    private boolean equalsPersistent(Feature feature1, Feature feature2) {

        // Only check for value suppliers
        if (! (feature1 instanceof ValueSupplier) || ! (feature2 instanceof ValueSupplier)) {
            return true;
        }
        // Don't check features that are excluded from equality checks
        else if (isIgnoreEquals(feature1) || isIgnoreEquals(feature2)) {
            return true;
        }

        Object value1 = getStorage((ValueSupplier<?>) feature1).get();
        Object value2 = getStorage((ValueSupplier<?>) feature2).get();

        // Return true if the feature is a collection property and the collection is empty
        if (feature1 instanceof CollectionProperty && feature2 instanceof CollectionProperty && value2 == null) {
            return true;
        }

        return equalsPersistent(value1, value2);
    }

    private boolean isIgnoreEquals(Feature feature) {

        try {
            Field ignoreEquals;
            if (feature instanceof DefaultProperty) {
                ignoreEquals = DefaultProperty.class.getDeclaredField("ignoreEquals");
            } else if (feature instanceof DefaultCollectionProperty) {
                ignoreEquals = DefaultCollectionProperty.class.getDeclaredField("ignoreEquals");
            } else {
                return false;
            }

            ignoreEquals.setAccessible(true);
            boolean value = (boolean) ignoreEquals.get(feature);
            ignoreEquals.setAccessible(false);
            return value;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Storage<?> getStorage(ValueSupplier<?> valueSupplier) {

        try {
            Field storageField = valueSupplier.getClass().getDeclaredField("storage");
            storageField.setAccessible(true);
            Storage<?> storage = (Storage<?>) storageField.get(valueSupplier);
            storageField.setAccessible(false);
            return storage;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean equalsPersistent(Object o1, Object o2) {

        if (o1 instanceof Object[] && o2 instanceof Object[]) {
            return equalsPersistent((Object[]) o1, (Object[]) o2);
        } else if (o1 instanceof Collection<?> && o2 instanceof Collection<?>) {
            Object[] collection1 = ((Collection<?>) o1).toArray(new Object[ ((Collection<?>) o1).size()]);
            Object[] collection2 = ((Collection<?>) o2).toArray(new Object[ ((Collection<?>) o2).size()]);
            return equalsPersistent(collection1, collection2);
        } else if (o1 instanceof DefaultFeatureHolder && o2 instanceof DefaultFeatureHolder) {
            return equalsPersistent((DefaultFeatureHolder) o1, (DefaultFeatureHolder) o2);
        } else if (o1 == null && o2 == null) {
            return true;
        } else {
            return o1 != null && o1.equals(o2);
        }
    }

    private boolean equalsPersistent(Object[] array1, Object[] array2) {

        if (array1.length != array2.length) {
            return false;
        } else {
            for (int index = 0; index < array1.length; index++) {
                if (!equalsPersistent(array1[index], array2[index])) {
                    return false;
                }
            }
        }

        return true;
    }

}
