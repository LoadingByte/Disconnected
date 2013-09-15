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

package com.quartercode.disconnected.sim;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import com.quartercode.disconnected.sim.comp.Computer;
import com.quartercode.disconnected.sim.member.Member;
import com.quartercode.disconnected.sim.member.MemberGroup;

/**
 * This clas represents a simulation which stores information about the members, member groups and computers.
 * To actually run the simulation, you need to run a tick simulator.
 * 
 * @see Member
 * @see MemberGroup
 * @see Computer
 */
@XmlRootElement (namespace = "http://quartercode.com/")
public class Simulation {

    @XmlElementWrapper (name = "members")
    @XmlElement (name = "member")
    private final List<Member>      members   = new CopyOnWriteArrayList<Member>();
    @XmlElementWrapper (name = "groups")
    @XmlElement (name = "group")
    private final List<MemberGroup> groups    = new CopyOnWriteArrayList<MemberGroup>();
    @XmlElementWrapper (name = "computers")
    @XmlElement (name = "computer")
    private final List<Computer>    computers = new CopyOnWriteArrayList<Computer>();

    /**
     * Creates a new empty simulation.
     */
    public Simulation() {

    }

    /**
     * Returns all members of the simulation.
     * 
     * @return All members of the simulation.
     */
    public List<Member> getMembers() {

        return Collections.unmodifiableList(members);
    }

    /**
     * Returns the member of this simulation which has the given name.
     * Returns null if there's no member with the given name.
     * 
     * @param name The name of the member to return.
     * @return The member of this simulation which has the given name.
     */
    public Member getMember(String name) {

        for (Member member : members) {
            if (member.getName().equals(name)) {
                return member;
            }
        }

        return null;
    }

    /**
     * Returns the member of this simulation which controls the given computer.
     * Returns null if the given computer isn't controlled by any member.
     * 
     * @param computer The computer which is controlled by the member to return.
     * @return The member of this simulation which controls the given computer.
     */
    public Member getMember(Computer computer) {

        for (Member member : members) {
            if (member.getComputer().equals(computer)) {
                return member;
            }
        }

        return null;
    }

    /**
     * Adds a member to the simulation.
     * 
     * @param member The member to add to the simulation.
     */
    public void addMember(Member member) {

        members.add(member);
    }

    /**
     * Removes a member from the simulation.
     * 
     * @param member The member to remove from the simulation.
     */
    public void removeMember(Member member) {

        members.remove(member);
    }

    /**
     * Returns all member groups of the simulation.
     * 
     * @return All member groups of the simulation.
     */
    public List<MemberGroup> getGroups() {

        return Collections.unmodifiableList(groups);
    }

    /**
     * Returns the member group of this simulation which contains the given member.
     * Returns null if the given member isn't set into any group.
     * 
     * @param member The member which is set into the member group to return.
     * @return The member group of this simulation which contains the given member.
     */
    public MemberGroup getGroup(Member member) {

        for (MemberGroup group : groups) {
            if (group.getMembers().contains(member)) {
                return group;
            }
        }

        return null;
    }

    /**
     * Adds a member group to the simulation.
     * 
     * @param group The member group to add to the simulation.
     */
    public void addGroup(MemberGroup group) {

        groups.add(group);
    }

    /**
     * Removes a member group from the simulation.
     * 
     * @param group The member group to remove from the simulation.
     */
    public void removeGroup(MemberGroup group) {

        groups.remove(group);
    }

    /**
     * Returns all computers of the simulation.
     * 
     * @return All computers of the simulation.
     */
    public List<Computer> getComputers() {

        return Collections.unmodifiableList(computers);
    }

    /**
     * Adds a computer to the simulation.
     * 
     * @param computer The computer to add to the simulation.
     */
    public void addComputer(Computer computer) {

        computers.add(computer);
    }

    /**
     * Removes a computer from the simulation.
     * 
     * @param computer The computer to remove from the simulation.
     */
    public void removeComputer(Computer computer) {

        computers.remove(computer);
    }

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = prime * result + (computers == null ? 0 : computers.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Simulation other = (Simulation) obj;
        if (computers == null) {
            if (other.computers != null) {
                return false;
            }
        } else if (!computers.equals(other.computers)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {

        return getClass().getName() + " [members=" + members.size() + ", groups=" + groups.size() + ", computers=" + computers.size() + "]";
    }

}
