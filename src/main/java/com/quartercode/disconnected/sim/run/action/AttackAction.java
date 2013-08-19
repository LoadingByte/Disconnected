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
import com.quartercode.disconnected.sim.member.interest.ReputationChangeProvider;
import com.quartercode.disconnected.sim.run.attack.Attack;
import com.quartercode.disconnected.sim.run.attack.Exploit;
import com.quartercode.disconnected.sim.run.attack.Payload;
import com.quartercode.disconnected.sim.run.util.ScriptExecutor;
import com.quartercode.disconnected.util.ProbabilityUtil;

/**
 * This is a simple attack action which executes an attack to a given target using an exploit and payload.
 * 
 * @see Action
 * @see Member
 * @see Exploit
 * @see Payload
 */
public class AttackAction extends Action {

    private final Attack attack;

    /**
     * Creates a new attack action and sets the reputation changer and the attack to use.
     * 
     * @param reputationChangeProvider The reputation change provider which provides reputation deltas.
     * @param attack The attack to use for executing the action.
     */
    public AttackAction(ReputationChangeProvider reputationChangeProvider, Attack attack) {

        super(reputationChangeProvider);

        this.attack = attack;
    }

    /**
     * Returns the attack to use for executing the action.
     * 
     * @return The attack to use for executing the action.
     */
    public Attack getAttack() {

        return attack;
    }

    @Override
    public boolean execute(Simulation simulation, Member member) {

        // Execute the exploit
        simulation.getGroup(member).getReputation(member).addValue(getReputationChangeProvider().getReputationChange(simulation, member, simulation.getGroup(member)));
        ScriptExecutor.execute(attack.getExploit().getVulnerability().getScripts(), simulation, attack.getTarget(), member);

        // Calculate the success (of course, this is not final)
        if (ProbabilityUtil.gen(0.2F)) {
            // Execute the payload
            simulation.getGroup(attack.getTarget()).getReputation(member).addValue(getReputationChangeProvider().getReputationChange(simulation, member, simulation.getGroup(attack.getTarget())));
            ScriptExecutor.execute(attack.getPayload().getScripts(), simulation, attack.getTarget(), member);

            return true;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = prime * result + (attack == null ? 0 : attack.hashCode());
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
        AttackAction other = (AttackAction) obj;
        if (attack == null) {
            if (other.attack != null) {
                return false;
            }
        } else if (!attack.equals(other.attack)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {

        return getClass().getName() + " [attack=" + attack + "]";
    }

}
