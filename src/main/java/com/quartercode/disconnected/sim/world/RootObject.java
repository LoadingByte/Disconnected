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

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.Unmarshaller;
import com.quartercode.disconnected.sim.comp.Computer;
import com.quartercode.disconnected.sim.member.Member;
import com.quartercode.disconnected.sim.member.MemberGroup;
import com.quartercode.disconnected.sim.member.ai.AIController;
import com.quartercode.disconnected.sim.member.ai.PlayerController;

/**
 * The root object of a world can house first level world objects.
 * 
 * @see WorldObject
 */
public class RootObject extends WorldObject {

    // ----- Property Definitions -----

    /**
     * The members property stores a list of {@link Member}s.
     */
    public static final PropertyDefinition<MemberListProperty>      MEMBERS_PROPERTY;

    /**
     * The member groups property stores a list of {@link MemberGroup}s.
     */
    public static final PropertyDefinition<MemberGroupListProperty> MEMBER_GROUPS_PROPERTY;

    /**
     * The computers property stores a list of {@link Computer}s.
     */
    public static final PropertyDefinition<ListProperty<Computer>>  COMPUTERS_PROPERTY;

    static {

        MEMBERS_PROPERTY = new PropertyDefinition<MemberListProperty>("members") {

            @Override
            public MemberListProperty createProperty(WorldObject parent) {

                return new MemberListProperty(getName(), parent);
            }

        };

        MEMBER_GROUPS_PROPERTY = new PropertyDefinition<MemberGroupListProperty>("memberGroups") {

            @Override
            public MemberGroupListProperty createProperty(WorldObject parent) {

                return new MemberGroupListProperty(getName(), parent);
            }

        };

        COMPUTERS_PROPERTY = new PropertyDefinition<ListProperty<Computer>>("computers") {

            @Override
            public ListProperty<Computer> createProperty(WorldObject parent) {

                return new ListProperty<Computer>(getName(), parent);
            }

        };

    }

    /**
     * The member {@link ListProperty} is used for storing a list of {@link Member}s.
     * It also offers some extra functionality for accessing the property.
     * 
     * @see ListProperty
     */
    public static class MemberListProperty extends ListProperty<Member> {

        private Member localPlayerCache;

        /**
         * Creates a new empty member list property.
         * This is only recommended for direct field access (e.g. for serialization).
         */
        public MemberListProperty() {

            super();
        }

        /**
         * Creates a new member list property with the given name and parent object.
         * 
         * @param name The name the new list property will have.
         * @param parent The parent object which has the new list property.
         */
        public MemberListProperty(String name, WorldObject parent) {

            super(name, parent);
        }

        /**
         * Returns the {@link Member} which has the given name.
         * Returns null if there's no {@link Member} with the given name.
         * 
         * @param name The name of the {@link Member} to return.
         * @return The {@link Member} which has the given name.
         */
        public Member get(String name) {

            for (Member member : this) {
                if (member.getName().equals(name)) {
                    return member;
                }
            }

            return null;
        }

        /**
         * Returns a list of {@link Member}s which have an {@link AIController} with the given type as a superclass.
         * 
         * @param controllerType The type to use for the {@link AIController}-selection.
         * @return The {@link Member}s which have an {@link AIController} with the given type as a superclass.
         */
        public List<Member> getByController(Class<? extends AIController> controllerType) {

            List<Member> members = new ArrayList<Member>();
            for (Member member : this) {
                if (controllerType.isAssignableFrom(member.getAiController().getClass())) {
                    members.add(member);
                }
            }

            return members;
        }

        /**
         * Returns the player who is interacting with the real computer this program runs on.
         * 
         * @return the player on the real local computer.
         */
        public Member getLocalPlayer() {

            if (localPlayerCache == null) {
                for (Member member : getByController(PlayerController.class)) {
                    if ( ((PlayerController) member.getAiController()).isLocal()) {
                        localPlayerCache = member;
                        break;
                    }
                }
            }

            return localPlayerCache;
        }

    }

    /**
     * The member group {@link ListProperty} is used for storing a list of {@link MemberGroup}s.
     * It also offers some extra functionality for accessing the property.
     * 
     * @see ListProperty
     */
    public static class MemberGroupListProperty extends ListProperty<MemberGroup> {

        /**
         * Creates a new empty member group list property.
         * This is only recommended for direct field access (e.g. for serialization).
         */
        public MemberGroupListProperty() {

            super();
        }

        /**
         * Creates a new member group list property with the given name and parent object.
         * 
         * @param name The name the new list property will have.
         * @param parent The parent object which has the new list property.
         */
        public MemberGroupListProperty(String name, WorldObject parent) {

            super(name, parent);
        }

        /**
         * Returns the {@link MemberGroup} which contains the given {@link Member}.
         * Returns null if the given {@link Member} isn't set into any {@link MemberGroup}.
         * 
         * @param member The {@link Member} which is set into the {@link MemberGroup} to return.
         * @return The {@link MemberGroup} which contains the given {@link Member}.
         */
        public MemberGroup get(Member member) {

            for (MemberGroup group : this) {
                if (group.getMembers().contains(member)) {
                    return group;
                }
            }

            return null;
        }

    }

    // ----- Property Definitions End -----

    private World world;

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
