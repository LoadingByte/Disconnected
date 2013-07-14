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

package com.quartercode.disconnected.sim.run.action;

import com.quartercode.disconnected.sim.Simulation;
import com.quartercode.disconnected.sim.member.Member;
import com.quartercode.disconnected.sim.member.interest.Interest;

/**
 * This interface defines an action which a member of a simulation can execute.
 * The actual execution is implemented by a simulator class.
 * 
 * @see Interest
 * @see Member
 */
public interface Action {

    /**
     * Lets a member execute the defined action in the given simulation.
     * 
     * @param simulation The simulation the execute the defined action in.
     * @param member The member which should execute the defined actionn.
     */
    public void execute(Simulation simulation, Member member);

}
