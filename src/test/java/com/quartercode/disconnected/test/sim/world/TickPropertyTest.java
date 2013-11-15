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

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.quartercode.disconnected.sim.world.ObjectProperty;
import com.quartercode.disconnected.sim.world.PropertyDefinition;
import com.quartercode.disconnected.sim.world.TickProperty;
import com.quartercode.disconnected.sim.world.TickProperty.UpdateTask;
import com.quartercode.disconnected.sim.world.World;
import com.quartercode.disconnected.sim.world.WorldObject;

public class TickPropertyTest {

    private static final PropertyDefinition<ObjectProperty<WorldObject>> TEST_OBJECT_PROPERTY;
    private static final PropertyDefinition<TickProperty>                TICK_PROPERTY;

    static {

        TEST_OBJECT_PROPERTY = new PropertyDefinition<ObjectProperty<WorldObject>>("testObject") {

            @Override
            public ObjectProperty<WorldObject> createProperty(WorldObject parent) {

                return new ObjectProperty<WorldObject>(getName(), parent);
            }

        };

        TICK_PROPERTY = new PropertyDefinition<TickProperty>("tick") {

            @Override
            public TickProperty createProperty(WorldObject parent) {

                return new TickProperty(getName(), parent);
            };

        };

    }

    private World                                                        world;
    private WorldObject                                                  worldObject;

    @Before
    public void setUp() {

        world = new World(null);

        worldObject = new WorldObject(world.getRoot());
        world.getRoot().get(TEST_OBJECT_PROPERTY).set(worldObject);
    }

    @Test
    public void testDelay() {

        final AtomicBoolean invoked1 = new AtomicBoolean();
        final AtomicBoolean invoked2 = new AtomicBoolean();
        worldObject.get(TICK_PROPERTY).add(new UpdateTask(new Runnable() {

            @Override
            public void run() {

                invoked1.set(true);
            }
        }, 2));
        worldObject.get(TICK_PROPERTY).add(new UpdateTask(new Runnable() {

            @Override
            public void run() {

                invoked2.set(true);
            }
        }, 5));
        for (int tick = 0; tick < 3; tick++) {
            worldObject.get(TICK_PROPERTY).update();
        }

        Assert.assertEquals("Update task 1 was invoked", true, invoked1.get());
        Assert.assertEquals("Update task 2 wasn't invoked", false, invoked2.get());
    }

    @Test
    public void testPeriod() {

        final AtomicInteger counter = new AtomicInteger();
        worldObject.get(TICK_PROPERTY).add(new UpdateTask(new Runnable() {

            @Override
            public void run() {

                counter.incrementAndGet();
            }
        }, 0, 2));
        for (int tick = 0; tick < 10; tick++) {
            worldObject.get(TICK_PROPERTY).update();
        }

        Assert.assertEquals("Update task was invoked correct amount of times", 5, counter.get());
    }

}
