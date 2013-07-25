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
import com.quartercode.disconnected.sim.member.interest.ReputationChangeProvider;

/**
 * This abstract class defines an action which a member of a simulation can execute.
 * The actual execution is implemented by a simulator class.
 * 
 * @see Interest
 * @see Member
 */
public abstract class Action {

    private ReputationChangeProvider reputationChangeProvider;

    /**
     * Creates a new abstract action and sets the reputation change provider.
     * 
     * @param reputationChangeProvider The reputation change provider which provides reputation deltas.
     */
    public Action(ReputationChangeProvider reputationChangeProvider) {

        this.reputationChangeProvider = reputationChangeProvider;
    }

    /**
     * Returns the reputation change provider which provides reputation deltas.
     * 
     * @return The reputation change provider which provides reputation deltas.
     */
    public ReputationChangeProvider getReputationChangeProvider() {

        return reputationChangeProvider;
    }

    /**
     * Lets a member execute the defined action in the given simulation.
     * In the case of a failure, this returns false, in the other case true.
     * 
     * @param simulation The simulation the execute the defined action in.
     * @param member The member which should execute the defined action.
     * @return False in the case of a failure, true in the other case.
     */
    public abstract boolean execute(Simulation simulation, Member member);

}
