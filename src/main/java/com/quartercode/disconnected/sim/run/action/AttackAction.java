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
import com.quartercode.disconnected.sim.run.attack.Attack;
import com.quartercode.disconnected.sim.run.attack.Exploit;
import com.quartercode.disconnected.sim.run.attack.Payload;
import com.quartercode.disconnected.sim.run.util.ScriptExecutor;

/**
 * This is a simple attack action which executes an attack to a given target using an exploit and payload.
 * 
 * @see Action
 * @see Member
 * @see Exploit
 * @see Payload
 */
public class AttackAction implements Action {

    private Member  target;
    private Exploit exploit;
    private Payload payload;

    /**
     * Creates a new attack action and sets the vulnerability to exploit and the payload.
     * 
     * @param target The target to attack using the given exploit and payload.
     * @param exploit The exploit to use for exploiting a vulnerability.
     * @param payload The payload to execute after exploiting the given vulnerability.
     */
    public AttackAction(Member target, Exploit exploit, Payload payload) {

        this.target = target;
        this.exploit = exploit;
        this.payload = payload;
    }

    /**
     * Returns the target to attack using the given exploit and payload.
     * 
     * @return the target to attack using the given exploit and payload.
     */
    public Member getTarget() {

        return target;
    }

    /**
     * Returns the exploit to use for exploiting a vulnerability.
     * 
     * @return The exploit to use for exploiting a vulnerability.
     */
    public Exploit getExploit() {

        return exploit;
    }

    /**
     * Returns the payload to execute after exploiting the given vulnerability.
     * 
     * @return The payload to execute after exploiting the given vulnerability.
     */
    public Payload getPayload() {

        return payload;
    }

    @Override
    public void execute(Simulation simulation, Member member) {

        Attack attack = new Attack(member, target, exploit, payload);

        for (String script : attack.getExploit().getVulnerability().getScripts()) {
            ScriptExecutor.execute(script, simulation, attack.getTarget(), attack.getAttacker());
        }

        if (attack.getPayload() != null) {
            for (String script : attack.getPayload().getScripts()) {
                ScriptExecutor.execute(script, simulation, attack.getTarget(), attack.getAttacker());
            }
        }
    }

}
