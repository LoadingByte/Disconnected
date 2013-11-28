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

package com.quartercode.disconnected.test.mocl.base.def;

import java.util.HashSet;
import java.util.Set;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import com.quartercode.disconnected.mocl.base.FeatureDefinition;
import com.quartercode.disconnected.mocl.base.FeatureHolder;
import com.quartercode.disconnected.mocl.base.def.AbstractFeature;
import com.quartercode.disconnected.mocl.base.def.AbstractFeatureDefinition;
import com.quartercode.disconnected.mocl.base.def.DefaultFeatureHolder;
import com.quartercode.disconnected.mocl.extra.Persistent;

public class DefaultFeatureHolderTest {

    private static FeatureDefinition<TestFeature1>    TEST_FEATURE_1;

    private static FeatureDefinition<AbstractFeature> TEST_FEATURE_2;

    @BeforeClass
    public static void setUpBeforeClass() {

        TEST_FEATURE_1 = new AbstractFeatureDefinition<TestFeature1>("testFeature1") {

            @Override
            public TestFeature1 create(FeatureHolder holder) {

                return new TestFeature1(getName(), holder);
            }

        };

        TEST_FEATURE_2 = new AbstractFeatureDefinition<AbstractFeature>("testFeature2") {

            @Override
            public AbstractFeature create(FeatureHolder holder) {

                return new AbstractFeature(getName(), holder);
            }

        };
    }

    private DefaultFeatureHolder featureHolder;

    @Before
    public void setUp() {

        featureHolder = new DefaultFeatureHolder();
    }

    @Test
    public void testGet() {

        Assert.assertEquals("Name of TEST_FEATURE_1", "testFeature1", featureHolder.get(TEST_FEATURE_1).getName());
        Assert.assertEquals("Name of TEST_FEATURE_2", "testFeature2", featureHolder.get(TEST_FEATURE_2).getName());
    }

    @Test
    public void testGetPersistentFeatures() {

        // Add feature objects
        featureHolder.get(TEST_FEATURE_1);
        featureHolder.get(TEST_FEATURE_2);

        Assert.assertTrue("Persistent features list doesn't contain TEST_FEATURE_1", featureHolder.getPersistentFeatures().contains(featureHolder.get(TEST_FEATURE_1)));
        Assert.assertFalse("Persistent features list contains TEST_FEATURE_2", featureHolder.getPersistentFeatures().contains(featureHolder.get(TEST_FEATURE_2)));
    }

    @Test
    public void testSetPersistentFeatures() {

        Set<Object> features = new HashSet<Object>();
        features.add(new AbstractFeature("testFeature", featureHolder));
        featureHolder.setPersistentFeatures(features);

        Set<Object> actualFeatures = new HashSet<Object>();
        for (Object feature : featureHolder) {
            actualFeatures.add(feature);
        }

        Assert.assertEquals("Added features", features, actualFeatures);
    }

    @Persistent
    private static class TestFeature1 extends AbstractFeature {

        public TestFeature1(String name, FeatureHolder holder) {

            super(name, holder);
        }

    }

}
