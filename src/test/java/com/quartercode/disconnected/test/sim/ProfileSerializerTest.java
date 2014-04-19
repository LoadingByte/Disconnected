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

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collection;
import java.util.List;
import javax.xml.bind.JAXBException;
import org.apache.commons.io.input.ReaderInputStream;
import org.apache.commons.io.output.WriterOutputStream;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import com.quartercode.classmod.base.Feature;
import com.quartercode.classmod.base.def.DefaultFeatureHolder;
import com.quartercode.classmod.extra.Property;
import com.quartercode.disconnected.Main;
import com.quartercode.disconnected.sim.ProfileSerializer;
import com.quartercode.disconnected.sim.gen.SimulationGenerator;
import com.quartercode.disconnected.util.RandomPool;
import com.quartercode.disconnected.world.World;

public class ProfileSerializerTest {

    private World world;

    @BeforeClass
    public static void setUpBeforeClass() throws IOException {

        Main.fillRegistry();
        Main.fillResourceStore();
    }

    @Before
    public void setUp() {

        world = SimulationGenerator.generateWorld(10, null, new RandomPool(10));
    }

    @Test
    public void testSerializeWorld() throws IOException, JAXBException {

        StringWriter serialized = new StringWriter();
        WriterOutputStream outputStream = new WriterOutputStream(serialized);
        ProfileSerializer.serializeWorld(outputStream, world);
        outputStream.flush();

        World copy = ProfileSerializer.deserializeWorld(new ReaderInputStream(new StringReader(serialized.toString())));
        Assert.assertTrue("Serialized-deserialized copy of world equals original", equalsPersistent(world, copy));
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

        // Only check for persistent properties
        if (! (feature1 instanceof Property) || ! (feature2 instanceof Property)) {
            return true;
        }

        return equalsPersistent( ((Property<?>) feature1).get(), ((Property<?>) feature2).get());
    }

    private boolean equalsPersistent(Object o1, Object o2) {

        if (o1 instanceof Collection<?> && o2 instanceof Collection<?>) {
            Object[] collection1 = ((Collection<?>) o1).toArray(new Object[ ((Collection<?>) o1).size()]);
            Object[] collection2 = ((Collection<?>) o2).toArray(new Object[ ((Collection<?>) o2).size()]);

            if (collection1.length != collection2.length) {
                return false;
            } else {
                for (int index = 0; index < collection1.length; index++) {
                    if (!equalsPersistent(collection1[index], collection2[index])) {
                        return false;
                    }
                }
            }

            return true;
        } else if (o1 instanceof DefaultFeatureHolder && o2 instanceof DefaultFeatureHolder) {
            return equalsPersistent((DefaultFeatureHolder) o1, (DefaultFeatureHolder) o2);
        } else if (o1 == null && o2 == null) {
            return true;
        } else {
            return o1 != null && o1.equals(o2);
        }
    }

}
