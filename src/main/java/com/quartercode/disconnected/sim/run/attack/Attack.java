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

import org.apache.commons.lang.Validate;
import com.quartercode.disconnected.sim.member.Member;

/**
 * This class represents an attack to a vulnerable system component.
 * An attack always contains a target, and exploit and a payload. The attacker is not stored in an attack.
 * The payload gets executed after the attacker exploited the vulnerability.
 * 
 * @see Member
 * @see Exploit
 * @see Payload
 */
public class Attack {

    private final Member  target;
    private final Exploit exploit;
    private final Payload payload;

    /**
     * Creates a new attack and sets the target, the exploit and the payload.
     * 
     * @param target The target who gets attacked by the attacker.
     * @param exploit The exploit which exploits the vulnerability.
     * @param payload The payload which gets executed after exploiting the vulnerability.
     */
    public Attack(Member target, Exploit exploit, Payload payload) {

        Validate.notNull(target, "Can't create an attack without a target member");
        Validate.notNull(exploit, "Can't create an attack without an exploit");
        Validate.notNull(payload, "Can't create an attack without a payload");

        this.target = target;
        this.exploit = exploit;
        this.payload = payload;
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

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = prime * result + (exploit == null ? 0 : exploit.hashCode());
        result = prime * result + (payload == null ? 0 : payload.hashCode());
        result = prime * result + (target == null ? 0 : target.hashCode());
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
        Attack other = (Attack) obj;
        if (exploit == null) {
            if (other.exploit != null) {
                return false;
            }
        } else if (!exploit.equals(other.exploit)) {
            return false;
        }
        if (payload == null) {
            if (other.payload != null) {
                return false;
            }
        } else if (!payload.equals(other.payload)) {
            return false;
        }
        if (target == null) {
            if (other.target != null) {
                return false;
            }
        } else if (!target.equals(other.target)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {

        return getClass().getName() + " [target=" + target.toInfoString() + ", exploit=" + exploit + ", payload=" + payload + "]";
    }

}
