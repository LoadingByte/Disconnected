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

package com.quartercode.disconnected.world;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;
import com.quartercode.classmod.base.def.DefaultFeatureHolder;
import com.quartercode.classmod.extra.CollectionPropertyDefinition;
import com.quartercode.classmod.extra.def.ObjectCollectionProperty;
import com.quartercode.disconnected.sim.Simulation;
import com.quartercode.disconnected.world.comp.Computer;
import com.quartercode.disconnected.world.member.Member;
import com.quartercode.disconnected.world.member.MemberGroup;

/**
 * A world is a space which contains {@link DefaultFeatureHolder}s.
 * The actual world class contains first level {@link DefaultFeatureHolder}s.
 */
@XmlRootElement (namespace = "http://quartercode.com/")
public class World extends DefaultFeatureHolder {

    // ----- Properties -----

    /**
     * The {@link Member}s who are present in the world.
     */
    public static final CollectionPropertyDefinition<Member, List<Member>>           MEMBERS;

    /**
     * The {@link MemberGroup}s which are present in the world.
     */
    public static final CollectionPropertyDefinition<MemberGroup, List<MemberGroup>> GROUPS;

    /**
     * The {@link Computer}s which are present in the world.
     */
    public static final CollectionPropertyDefinition<Computer, List<Computer>>       COMPUTERS;

    static {

        MEMBERS = ObjectCollectionProperty.createDefinition("members", new ArrayList<Member>());
        GROUPS = ObjectCollectionProperty.createDefinition("memberGroups", new ArrayList<MemberGroup>());
        COMPUTERS = ObjectCollectionProperty.createDefinition("computers", new ArrayList<Computer>());

    }

    // ----- Properties End -----

    private Simulation                                                               simulation;

    /**
     * Creates a new empty world.
     * This is only recommended for direct field access (e.g. for serialization).
     */
    protected World() {

    }

    /**
     * Creates a new world which is placed in the given {@link Simulation}.
     * 
     * @param simulation The {@link Simulation} the new world is placed in.
     */
    public World(Simulation simulation) {

        this.simulation = simulation;
    }

    /**
     * Returns the {@link Simulation} the world is placed in.
     * 
     * @return The world's {@link Simulation}.
     */
    public Simulation getSimulation() {

        return simulation;
    }

}
