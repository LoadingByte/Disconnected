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

package com.quartercode.disconnected.test.sim.world;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.quartercode.disconnected.sim.world.ObjectProperty;
import com.quartercode.disconnected.sim.world.PropertyDefinition;
import com.quartercode.disconnected.sim.world.World;
import com.quartercode.disconnected.sim.world.WorldObject;

public class WorldObjectTest {

    private static final PropertyDefinition<ObjectProperty<WorldObject>> TEST_OBJECT_PROPERTY;
    private static final PropertyDefinition<ObjectProperty<String>>      STRING_PROPERTY;

    static {

        TEST_OBJECT_PROPERTY = new PropertyDefinition<ObjectProperty<WorldObject>>("testObject") {

            @Override
            public ObjectProperty<WorldObject> createProperty(WorldObject parent) {

                return new ObjectProperty<WorldObject>(getName(), parent);
            }

        };

        STRING_PROPERTY = new PropertyDefinition<ObjectProperty<String>>("string") {

            @Override
            public ObjectProperty<String> createProperty(WorldObject parent) {

                return new ObjectProperty<String>(getName(), parent);
            };

        };

    }

    private World                                                        world;
    private WorldObject                                                  worldObject;

    @Before
    public void setUp() {

        world = new World();

        worldObject = new WorldObject(world.getRoot());
        world.getRoot().get(TEST_OBJECT_PROPERTY).set(worldObject);
    }

    @Test
    public void testGet() {

        worldObject.get(STRING_PROPERTY).set("Test");
        Assert.assertEquals("Content of string property is correct", "Test", worldObject.get(STRING_PROPERTY).get());
    }

    }

}
