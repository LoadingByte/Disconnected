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

import java.util.HashSet;
import java.util.Set;
import javax.xml.bind.annotation.XmlRootElement;
import com.quartercode.classmod.base.FeatureDefinition;
import com.quartercode.classmod.base.def.DefaultFeatureHolder;
import com.quartercode.classmod.extra.ExecutorInvocationException;
import com.quartercode.classmod.extra.FunctionDefinition;
import com.quartercode.classmod.extra.def.ObjectProperty;
import com.quartercode.classmod.util.CollectionPropertyAccessorFactory;
import com.quartercode.classmod.util.CollectionPropertyAccessorFactory.CriteriumMatcher;
import com.quartercode.classmod.util.FunctionDefinitionFactory;
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
    protected static final FeatureDefinition<ObjectProperty<Set<Member>>>      MEMBERS;

    /**
     * The {@link MemberGroup}s which are present in the world.
     */
    protected static final FeatureDefinition<ObjectProperty<Set<MemberGroup>>> GROUPS;

    /**
     * The {@link Computer}s which are present in the world.
     */
    protected static final FeatureDefinition<ObjectProperty<Set<Computer>>>    COMPUTERS;

    static {

        MEMBERS = ObjectProperty.<Set<Member>> createDefinition("members", new HashSet<Member>());
        GROUPS = ObjectProperty.<Set<MemberGroup>> createDefinition("memberGroups", new HashSet<MemberGroup>());
        COMPUTERS = ObjectProperty.<Set<Computer>> createDefinition("computers", new HashSet<Computer>());

    }

    // ----- Properties End -----

    // ----- Functions -----

    /**
     * Returns the {@link Member}s who are present in the world.
     * The returned list is an unmodifiable one.
     */
    public static final FunctionDefinition<Set<Member>>                        GET_MEMBERS;

    /**
     * Returns the {@link Member} who has the given name.
     * 
     * <table>
     * <tr>
     * <th>Index</th>
     * <th>Type</th>
     * <th>Parameter</th>
     * <th>Description</th>
     * </tr>
     * <tr>
     * <td>0</td>
     * <td>{@link String}</td>
     * <td>name</td>
     * <td>The name of the {@link Member} to return.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Member>                             GET_MEMBER_BY_NAME;

    /**
     * Adds {@link Member}s to the world.
     * 
     * <table>
     * <tr>
     * <th>Index</th>
     * <th>Type</th>
     * <th>Parameter</th>
     * <th>Description</th>
     * </tr>
     * <tr>
     * <td>0...</td>
     * <td>{@link Member}...</td>
     * <td>members</td>
     * <td>The {@link Member}s to add to the world.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>                               ADD_MEMBERS;

    /**
     * Removes {@link Member}s from the world.
     * 
     * <table>
     * <tr>
     * <th>Index</th>
     * <th>Type</th>
     * <th>Parameter</th>
     * <th>Description</th>
     * </tr>
     * <tr>
     * <td>0...</td>
     * <td>{@link Member}...</td>
     * <td>members</td>
     * <td>The {@link Member}s to remove from the world.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>                               REMOVE_MEMBERS;

    /**
     * Returns the {@link MemberGroup}s which are present in the world.
     * The returned list is an unmodifiable one.
     */
    public static final FunctionDefinition<Set<MemberGroup>>                   GET_GROUPS;

    /**
     * Returns the {@link MemberGroup} the given {@link Member} is in.
     * 
     * <table>
     * <tr>
     * <th>Index</th>
     * <th>Type</th>
     * <th>Parameter</th>
     * <th>Description</th>
     * </tr>
     * <tr>
     * <td>0</td>
     * <td>{@link Member}</td>
     * <td>member</td>
     * <td>The {@link Member} who is in the {@link MemberGroup} to return.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<MemberGroup>                        GET_GROUP_BY_MEMBER;

    /**
     * Adds {@link MemberGroup}s to the world.
     * 
     * <table>
     * <tr>
     * <th>Index</th>
     * <th>Type</th>
     * <th>Parameter</th>
     * <th>Description</th>
     * </tr>
     * <tr>
     * <td>0...</td>
     * <td>{@link MemberGroup}...</td>
     * <td>groups</td>
     * <td>The {@link MemberGroup}s to add to the world.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>                               ADD_GROUPS;

    /**
     * Removes {@link MemberGroup}s from the world.
     * 
     * <table>
     * <tr>
     * <th>Index</th>
     * <th>Type</th>
     * <th>Parameter</th>
     * <th>Description</th>
     * </tr>
     * <tr>
     * <td>0...</td>
     * <td>{@link MemberGroup}...</td>
     * <td>groups</td>
     * <td>The {@link MemberGroup}s to remove from the world.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>                               REMOVE_GROUPS;

    /**
     * Returns the {@link Computer}s which are present in the world.
     * The returned list is an unmodifiable one.
     */
    public static final FunctionDefinition<Set<Computer>>                      GET_COMPUTERS;

    /**
     * Adds {@link Computer}s to the world.
     * 
     * <table>
     * <tr>
     * <th>Index</th>
     * <th>Type</th>
     * <th>Parameter</th>
     * <th>Description</th>
     * </tr>
     * <tr>
     * <td>0...</td>
     * <td>{@link Computer}...</td>
     * <td>groups</td>
     * <td>The {@link Computer}s to add to the world.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>                               ADD_COMPUTERS;

    /**
     * Removes {@link Computer}s from the world.
     * 
     * <table>
     * <tr>
     * <th>Index</th>
     * <th>Type</th>
     * <th>Parameter</th>
     * <th>Description</th>
     * </tr>
     * <tr>
     * <td>0...</td>
     * <td>{@link Computer}...</td>
     * <td>groups</td>
     * <td>The {@link Computer}s to remove from the world.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>                               REMOVE_COMPUTERS;

    static {

        GET_MEMBERS = FunctionDefinitionFactory.create("getMembers", World.class, CollectionPropertyAccessorFactory.createGet(MEMBERS));
        GET_MEMBER_BY_NAME = FunctionDefinitionFactory.create("getMemberByName", World.class, CollectionPropertyAccessorFactory.createGetSingle(MEMBERS, new CriteriumMatcher<Member>() {

            @Override
            public boolean matches(Member element, Object... arguments) throws ExecutorInvocationException {

                return element.getName().equals(arguments[0]);
            }

        }), String.class);
        ADD_MEMBERS = FunctionDefinitionFactory.create("addMembers", World.class, CollectionPropertyAccessorFactory.createAdd(MEMBERS), Member[].class);
        REMOVE_MEMBERS = FunctionDefinitionFactory.create("removeMembers", World.class, CollectionPropertyAccessorFactory.createRemove(MEMBERS), Member[].class);

        GET_GROUPS = FunctionDefinitionFactory.create("getGroups", World.class, CollectionPropertyAccessorFactory.createGet(GROUPS));
        GET_GROUP_BY_MEMBER = FunctionDefinitionFactory.create("getGroupByMember", World.class, CollectionPropertyAccessorFactory.createGetSingle(GROUPS, new CriteriumMatcher<MemberGroup>() {

            @Override
            public boolean matches(MemberGroup element, Object... arguments) throws ExecutorInvocationException {

                return element.getMembers().contains(arguments[0]);
            }

        }), Member.class);
        ADD_GROUPS = FunctionDefinitionFactory.create("addGroups", World.class, CollectionPropertyAccessorFactory.createAdd(GROUPS), MemberGroup[].class);
        REMOVE_GROUPS = FunctionDefinitionFactory.create("removeGroups", World.class, CollectionPropertyAccessorFactory.createRemove(GROUPS), MemberGroup[].class);

        GET_COMPUTERS = FunctionDefinitionFactory.create("getComputers", World.class, CollectionPropertyAccessorFactory.createGet(COMPUTERS));
        ADD_COMPUTERS = FunctionDefinitionFactory.create("addComputers", World.class, CollectionPropertyAccessorFactory.createAdd(COMPUTERS), Computer[].class);
        REMOVE_COMPUTERS = FunctionDefinitionFactory.create("removeComputers", World.class, CollectionPropertyAccessorFactory.createRemove(COMPUTERS), Computer[].class);

    }

    // ----- Functions End -----

    private Simulation                                                         simulation;

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
