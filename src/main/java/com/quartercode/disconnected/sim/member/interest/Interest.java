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

package com.quartercode.disconnected.sim.member.interest;

import javax.xml.bind.annotation.XmlAttribute;
import org.apache.commons.lang.Validate;
import com.quartercode.disconnected.sim.Simulation;
import com.quartercode.disconnected.sim.member.BrainData;
import com.quartercode.disconnected.sim.member.Member;
import com.quartercode.disconnected.sim.member.MemberGroup;
import com.quartercode.disconnected.sim.member.action.Action;

/**
 * This abstract class represents an interest of a member or a global one from a member group.
 * The interest only stores a general idea. For the actual execution, you need to generate an action object.
 * 
 * @see Member
 * @see MemberGroup
 * @see Action
 */
public abstract class Interest extends BrainData implements ReputationChangeProvider {

    private float priority;

    /**
     * Creates a new empty interest.
     */
    public Interest() {

    }

    /**
     * Creates a new interest and sets the priority.
     * 
     * @param priority The priority this interest has for the group or member (must be between 0 and 1, both inclusive).
     */
    public Interest(float priority) {

        setPriority(priority);
    }

    /**
     * Returns the priority the interest has for the group or member.
     * The priority value is located between 0 and 1, both inclusive.
     * 
     * @return The priority the interest has for the group or member.
     */
    @XmlAttribute
    public float getPriority() {

        return priority;
    }

    /**
     * Sets the priority the interest has for the group or member to a new one.
     * The priority value must be located between 0 and 1, both inclusive.
     * 
     * @param priority The new priority the interest has.
     */
    public void setPriority(float priority) {

        Validate.isTrue(priority >= 0 && priority <= 1, "Priority must be in range 0 <= priority <= 1: " + priority);
        this.priority = priority;
    }

    /**
     * Calculates the best avaiable action the given member would use for ecexuting the interest.
     * The calculated action may not be the perfect one, this method should also implement some human roughness.
     * This may also return null if no good action can be found, e.g. if every possibility is to risky.
     * 
     * @param simulation The simulation which contains the given member.
     * @param member The member which should execute the calculated action later.
     * @return The best avaiable action the given member would use for executing the interest. This may be null.
     */
    public abstract Action getAction(Simulation simulation, Member member);

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = prime * result + Float.floatToIntBits(priority);
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
        Interest other = (Interest) obj;
        if (Float.floatToIntBits(priority) != Float.floatToIntBits(other.priority)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {

        return getClass().getName() + " [priority=" + priority + "]";
    }

}
