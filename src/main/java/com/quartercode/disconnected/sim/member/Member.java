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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import com.quartercode.disconnected.sim.comp.Computer;
import com.quartercode.disconnected.sim.member.interest.Interest;
import com.quartercode.disconnected.util.InfoString;

/**
 * This class represents a member of a simulation (someone who have specific interests).
 * The member doesn't know his simulation, so you could use one member in multiple simulations.
 * A member also has interests and stores the reputation of a group to him.
 * 
 * @see Interest
 * @see Reputation
 */
@XmlAccessorType (XmlAccessType.FIELD)
public class Member implements InfoString {

    @XmlAttribute
    @XmlID
    private String               name;
    @XmlElement (name = "interest")
    private final List<Interest> interests = new CopyOnWriteArrayList<Interest>();
    @XmlIDREF
    private Computer             computer;

    /**
     * Creates a new empty member.
     * This is only recommended for direct field access (e.g. for serialization).
     */
    protected Member() {

    }

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

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = prime * result + (computer == null ? 0 : computer.hashCode());
        result = prime * result + (interests == null ? 0 : interests.hashCode());
        result = prime * result + (name == null ? 0 : name.hashCode());
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
        Member other = (Member) obj;
        if (computer == null) {
            if (other.computer != null) {
                return false;
            }
        } else if (!computer.equals(other.computer)) {
            return false;
        }
        if (interests == null) {
            if (other.interests != null) {
                return false;
            }
        } else if (!interests.equals(other.interests)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }

    @Override
    public String toInfoString() {

        return name + ", " + interests.size() + "interests, computer" + computer.getId();
    }

    @Override
    public String toString() {

        return getClass().getName() + " [" + toInfoString() + "]";
    }

}
