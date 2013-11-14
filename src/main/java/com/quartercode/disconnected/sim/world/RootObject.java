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

import javax.xml.bind.Unmarshaller;
import com.quartercode.disconnected.sim.comp.Computer;
import com.quartercode.disconnected.sim.member.Member;

/**
 * The root object of a world can house first level world objects.
 * 
 * @see WorldObject
 */
public class RootObject extends WorldObject {

    /**
     * The members property stores a list of {@link Member}s.
     */
    public static final PropertyDefinition<ListProperty<Member>>   MEMBERS_PROPERTY;

    /**
     * The computers property stores a list of {@link Computer}s.
     */
    public static final PropertyDefinition<ListProperty<Computer>> COMPUTERS_PROPERTY;

    static {

        MEMBERS_PROPERTY = new PropertyDefinition<ListProperty<Member>>("members") {

            @Override
            public ListProperty<Member> createProperty(WorldObject parent) {

                return new ListProperty<Member>(getName(), parent);
            }

        };

        COMPUTERS_PROPERTY = new PropertyDefinition<ListProperty<Computer>>("computers") {

            @Override
            public ListProperty<Computer> createProperty(WorldObject parent) {

                return new ListProperty<Computer>(getName(), parent);
            }

        };

    }

    private World                                                  world;

    /**
     * Creates a new empty root object.
     * This is only recommended for direct field access (e.g. for serialization).
     */
    protected RootObject() {

        super();
    }

    /**
     * Creates a new root object which is used in the given world.
     * 
     * @param world The world the new object is used in.
     */
    public RootObject(World world) {

        super(null);

        this.world = world;
    }

    @Override
    public World getWorld() {

        return world;
    }

    @Override
    protected void beforeUnmarshal(Unmarshaller unmarshaller, Object parent) {

        if (parent instanceof World) {
            world = (World) parent;
        }
    }

}
