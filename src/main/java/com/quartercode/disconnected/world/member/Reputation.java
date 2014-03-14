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

package com.quartercode.disconnected.world.member;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlValue;

/**
 * This class represents the reputation of a member of the simulation as an integer.
 * A positive integer means a good reputation, a negative integer a bad one.
 * 
 * @see Member
 */
public class Reputation {

    @XmlIDREF
    @XmlAttribute
    private Member member;
    private int    value;

    /**
     * Creates a new empty reputation object.
     * This is only recommended for direct field access (e.g. for serialization).
     */
    protected Reputation() {

    }

    /**
     * Creates a new reputation object and sets the member whose reputation should be stored.
     * 
     * @param member The member to set.
     */
    public Reputation(Member member) {

        this.member = member;
    }

    /**
     * Creates a new reputation object and sets the member whose reputation should be stored and his reputation.
     * 
     * @param member The member to set.
     * @param value The reputation to set.
     */
    public Reputation(Member member, int value) {

        this.member = member;
        this.value = value;
    }

    /**
     * Returns the member whose reputation is stored.
     * 
     * @return The member whose reputation is stored.
     */
    public Member getMember() {

        return member;
    }

    /**
     * Returns the current stored reputation value.
     * 
     * @return The current stored reputation value.
     */
    @XmlValue
    public int getValue() {

        return value;
    }

    /**
     * Sets the reputation value to a new one.
     * 
     * @param value The reputation value to set.
     */
    public void setValue(int value) {

        this.value = value;
    }

    /**
     * Adds a delta to the reputation value.
     * 
     * @param delta The delta to add to the reputation value.
     */
    public void addValue(int delta) {

        value += delta;
    }

    /**
     * Subtracts a delta from the reputation value.
     * 
     * @param delta The delta to subtract from the reputation value.
     */
    public void subtractValue(int delta) {

        value -= delta;
    }

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = prime * result + (member == null ? 0 : member.hashCode());
        result = prime * result + value;
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
        Reputation other = (Reputation) obj;
        if (member == null) {
            if (other.member != null) {
                return false;
            }
        } else if (!member.equals(other.member)) {
            return false;
        }
        if (value != other.value) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {

        return getClass().getName() + " [member=" + member.getName() + ", value=" + value + "]";
    }

}
