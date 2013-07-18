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
import java.util.concurrent.CopyOnWriteArrayList;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import com.quartercode.disconnected.sim.member.interest.Interest;

/**
 * This class represents a group of members which may have interests of the same type.
 * A group also has a reputation on a member which is stored in the member's object. One member can't have an own reputation on another member.
 * Global interests which every member cares of are also defined in this class.
 * 
 * @see Member
 * @see Reputation
 * @see Interest
 */
@XmlAccessorType (XmlAccessType.FIELD)
public class MemberGroup {

    @XmlElement (name = "member")
    @XmlIDREF
    private List<Member>     members     = new CopyOnWriteArrayList<Member>();
    @XmlElement (name = "reputation")
    private List<Reputation> reputations = new CopyOnWriteArrayList<Reputation>();
    @XmlElement (name = "interest")
    private List<Interest>   interests   = new CopyOnWriteArrayList<Interest>();

    /**
     * Creates a new empty member group
     */
    public MemberGroup() {

    }

    /**
     * Returns all members of this group.
     * 
     * @return All members of this group.
     */
    public List<Member> getMembers() {

        return Collections.unmodifiableList(members);
    }

    /**
     * Returns the member of this group which has the given name.
     * Returns null if there's no member with the given name in this group.
     * 
     * @param name The name of the member to return.
     * @return The member of this group which has the given name
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
     * Adds a member to the group.
     * 
     * @param member The member to add to the group.
     */
    public void addMember(Member member) {

        members.add(member);
    }

    /**
     * Removes a member from the group.
     * 
     * @param member The member to remove from the group.
     */
    public void removeMember(Member member) {

        members.remove(member);
    }

    /**
     * Returns the reputation the given member has from the perspective of this.
     * If no reputation is set, an empty one will be created.
     * 
     * @param member The member whose reputation should be returned.
     * @return The reputation the given member has from the perspective of this.
     */
    public Reputation getReputation(Member member) {

        for (Reputation reputation : reputations) {
            if (reputation.getMember().equals(member)) {
                return reputation;
            }
        }

        Reputation reputation = new Reputation(member);
        reputations.add(reputation);
        return reputation;
    }

    /**
     * Returns the global interests of this group which every member cares of.
     * Apart from this interests, every member has his own ones.
     * 
     * @return The global interests of this group which every member cares of.
     */
    public List<Interest> getInterests() {

        return Collections.unmodifiableList(interests);
    }

    /**
     * Adds a global interest to the group.
     * 
     * @param interest The global interest to add to the group.
     */
    public void addInterest(Interest interest) {

        interests.add(interest);
    }

    /**
     * Removes a global interest from the group.
     * 
     * @param interest The global interest to remove from the group.
     */
    public void removeInterest(Interest interest) {

        interests.remove(interest);
    }

}
