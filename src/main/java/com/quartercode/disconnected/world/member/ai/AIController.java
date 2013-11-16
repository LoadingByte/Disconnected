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

package com.quartercode.disconnected.world.member.ai;

import javax.xml.bind.Unmarshaller;
import com.quartercode.disconnected.sim.Simulation;
import com.quartercode.disconnected.world.member.Member;

/**
 * An ai controller executes tick updates for a member type.
 */
public abstract class AIController {

    private Member member;

    /**
     * Creates a new empty ai controller.
     * This is only recommended for direct field access (e.g. for serialization).
     */
    protected AIController() {

    }

    /**
     * Creates a new ai controller and sets the member which should be simulated.
     * 
     * @param member The member which should be simulated using this controller.
     */
    public AIController(Member member) {

        this.member = member;
    }

    /**
     * Returns the member which should be simulated using this ai controller.
     * 
     * @return The member which should be simulated using this ai controller.
     */
    public Member getMember() {

        return member;
    }

    /**
     * Executes a tick update for the member the controller is built for.
     * 
     * @param simulation The simulation the member is located in.
     */
    public abstract void update(Simulation simulation);

    public void beforeUnmarshal(Unmarshaller unmarshaller, Object parent) {

        member = (Member) parent;
    }

}
