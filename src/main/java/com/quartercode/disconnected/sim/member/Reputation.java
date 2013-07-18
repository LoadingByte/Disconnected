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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlValue;

/**
 * This class represents the reputation of a member of the simulation as an integer.
 * A positive integer means a good reputation, a negative integer a bad one.
 * 
 * @see Member
 */
@XmlAccessorType (XmlAccessType.FIELD)
public class Reputation {

    @XmlAttribute
    @XmlIDREF
    private Member member;
    @XmlValue
    private int    value;

    /**
     * Creates a new empty reputation object.
     * This is only recommended for direct field access (e.g. for serialization).
     */
    public Reputation() {

    }

    /**
     * Creates a new reputation object and sets the member which reputation should be stored.
     * 
     * @param member The member to set.
     */
    public Reputation(Member member) {

        this.member = member;
    }

    /**
     * Creates a new reputation object and sets the member which reputation should be stored and his reputation.
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
     * Removes a delta from the reputation value.
     * 
     * @param delta The delta to remove from the reputation value.
     */
    public void removeValue(int delta) {

        value -= delta;
    }

}
