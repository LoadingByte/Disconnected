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

package com.quartercode.disconnected.sim.world;

/**
 * The root object of a world can house first level world objects in the children property.
 * 
 * @see WorldObject
 */
public class RootObject extends WorldObject {

    /**
     * A property which can house first level world objects in the root object.
     */
    public static final PropertyDefinition<ListProperty<WorldObject>> CHILDREN_PROPERTY = new PropertyDefinition<ListProperty<WorldObject>>("children") {

                                                                                            @Override
                                                                                            public ListProperty<WorldObject> createProperty(WorldObject parent) {

                                                                                                return new ListProperty<WorldObject>(getName(), parent);
                                                                                            }

                                                                                        };

    /**
     * Creates a new empty root objects.
     */
    public RootObject() {

    }

}
