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

package com.quartercode.disconnected.sim.run.attack;

import com.quartercode.disconnected.sim.member.Member;

/**
 * This class represents an attack to a vulnerable system component.
 * An attack always contains an attacker, a target, and exploit and a payload.
 * The payload gets executed after the attacker exploited the vulnerability.
 * 
 * @see Member
 * @see Exploit
 * @see Payload
 */
public class Attack {

    private Member  attacker;
    private Member  target;
    private Exploit exploit;
    private Payload payload;

    /**
     * Creates a new attack and sets the attacker, the target, the exploit and the payload.
     * 
     * @param attacker The attacker who attacks the target using the given exploit and payload.
     * @param target The target who gets attacked by the attacker.
     * @param exploit The exploit which exploits the vulnerability.
     * @param payload The payload which gets executed after exploiting the vulnerability.
     */
    public Attack(Member attacker, Member target, Exploit exploit, Payload payload) {

        this.attacker = attacker;
        this.target = target;
        this.exploit = exploit;
        this.payload = payload;
    }

    /**
     * Returns the attacker who attacks the target using the given exploit and payload.
     * 
     * @return The attacker who attacks the target using the given exploit and payload.
     */
    public Member getAttacker() {

        return attacker;
    }

    /**
     * Returns the target who gets attacked by the attacker.
     * 
     * @return The target who gets attacked by the attacker.
     */
    public Member getTarget() {

        return target;
    }

    /**
     * Returns the exploit which exploits the vulnerability.
     * 
     * @return The exploit which exploits the vulnerability.
     */
    public Exploit getExploit() {

        return exploit;
    }

    /**
     * Returns the payload which gets executed after exploiting the vulnerability.
     * 
     * @return The payload which gets executed after exploiting the vulnerability.
     */
    public Payload getPayload() {

        return payload;
    }

}
