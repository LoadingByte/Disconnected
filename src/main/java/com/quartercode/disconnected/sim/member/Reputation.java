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

/**
 * This class represents the reputation of a group to a member of the simulation as an integer.
 * A positive integer means a good reputation, a negative integer a bad one.
 * 
 * @see Member
 * @see MemberGroup
 */
public class Reputation {

    private int value;

    /**
     * Creates a new empty reputation object.
     */
    public Reputation() {

    }

    /**
     * Creates a new reputation object and sets the reputation.
     * 
     * @param value The reputation to set.
     */
    public Reputation(int value) {

        this.value = value;
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
