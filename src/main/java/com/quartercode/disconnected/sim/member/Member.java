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

package com.quartercode.disconnected.sim.member;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import com.quartercode.disconnected.sim.comp.Computer;
import com.quartercode.disconnected.sim.member.interest.Interest;

/**
 * This class represents a member of a simulation (someone who have specific interests).
 * The member doesn't know his simulation, so you could use one member in multiple simulations.
 * A member also has interests and stores the reputation of a group to him.
 * 
 * @see Interest
 * @see Reputation
 */
public class Member {

    private String                       name;
    private List<Interest>               interests   = new CopyOnWriteArrayList<Interest>();
    private Map<MemberGroup, Reputation> reputations = new ConcurrentHashMap<MemberGroup, Reputation>();
    private Computer                     computer;

    /**
     * Creates a new member and sets the name.
     * 
     * @param name The name for the new member.
     */
    public Member(String name) {

        this.name = name;
    }

    /**
     * Returns the name of this member.
     * 
     * @return The name of this member.
     */
    public String getName() {

        return name;
    }

    /**
     * Returns the reputation of this member from the perspective of the given member group.
     * If no reputation is set, an empty one will be created.
     * 
     * @param group The member group which has the returned reputation of this member.
     * @return The reputation of this member from the perspective of the given member group.
     */
    public Reputation getReputation(MemberGroup group) {

        if (!reputations.containsKey(group)) {
            reputations.put(group, new Reputation());
        }
        return reputations.get(group);
    }

    /**
     * Returns the interests of this member.
     * 
     * @return The interests of this member.
     */
    public List<Interest> getInterests() {

        return Collections.unmodifiableList(interests);
    }

    /**
     * Adds a interest to the member.
     * 
     * @param interest The interest to add to the member.
     */
    public void addInterest(Interest interest) {

        interests.add(interest);
    }

    /**
     * Removes a interest from the member.
     * 
     * @param interest The interest to remove from the member.
     */
    public void removeInterest(Interest interest) {

        interests.remove(interest);
    }

    /**
     * Returns the computer the member has access on.
     * 
     * @return The computer the member has access on.
     */
    public Computer getComputer() {

        return computer;
    }

    /**
     * Sets the computer the member has access on to a new one.
     * 
     * @param computer The new computer the member will have access on
     */
    public void setComputer(Computer computer) {

        this.computer = computer;
    }

}
