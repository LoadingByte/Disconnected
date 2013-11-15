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

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import com.quartercode.disconnected.sim.Simulation;
import com.quartercode.disconnected.sim.comp.ComputerPart;
import com.quartercode.disconnected.sim.comp.Vulnerability;
import com.quartercode.disconnected.sim.comp.Vulnerability.Vulnerable;
import com.quartercode.disconnected.sim.comp.attack.Exploit;
import com.quartercode.disconnected.sim.comp.attack.Payload;
import com.quartercode.disconnected.sim.member.Member;
import com.quartercode.disconnected.sim.member.MemberGroup;
import com.quartercode.disconnected.sim.member.action.Action;
import com.quartercode.disconnected.sim.member.action.ExploitAction;
import com.quartercode.disconnected.sim.world.RootObject;
import com.quartercode.disconnected.util.ProbabilityUtil;

/**
 * This is a simple destroy interest which has a computer as target.
 * The executor of the resulting action should destroy the target.
 * 
 * @see Interest
 * @see HasTarget
 */
public class DestroyInterest extends Interest implements HasTarget {

    @XmlIDREF
    @XmlElement
    private Member target;

    /**
     * Creates a new empty destroy interest.
     * This is only recommended for direct field access (e.g. for serialization).
     */
    protected DestroyInterest() {

    }

    /**
     * Creates a new destroy interest and sets the priority and the computer target.
     * 
     * @param priority The priority of the interest.
     * @param target The member target the interest has.
     */
    public DestroyInterest(float priority, Member target) {

        super(priority);

        this.target = target;
    }

    @Override
    public Member getTarget() {

        return target;
    }

    @Override
    public int getReputationChange(Simulation simulation, Member member, MemberGroup group) {

        int change = (int) (getPriority() * 30);

        if (group.getMembers().contains(member)) {
            if (!group.getInterests().contains(this)) {
                change /= 2;
            }
        } else {
            change = -change;
        }

        return change;
    }

    @Override
    public Action getAction(Simulation simulation, Member member) {

        // Calculate probability for executing the action
        MemberGroup group = simulation.getWorld().getRoot().get(RootObject.MEMBER_GROUPS_PROPERTY).get(member);
        int currentReputation = group.getReputation(member).getValue();
        float probability = getPriority() * (getReputationChange(simulation, member, group) * 20F) / ( (currentReputation == 0 ? 1 : currentReputation) * 100);

        if (ProbabilityUtil.genPseudo(probability, simulation.getRandom())) {
            // Collect all vulnerabilities
            List<Vulnerability> vulnerabilities = new ArrayList<Vulnerability>();
            for (ComputerPart part : member.getComputer().getParts()) {
                if (part instanceof Vulnerable) {
                    vulnerabilities.addAll( ((Vulnerable) part).getVulnerabilities());
                }
            }

            // Take the first avaiable vulnerability and quickly develop a new exploit
            if (vulnerabilities.size() > 0) {
                Exploit exploit = new Exploit(vulnerabilities.get(0));

                // Also develop a brand new payload which immediatly destroys the target computer
                List<String> scripts = new ArrayList<String>();
                scripts.add("simulation.removeMember(member)");
                scripts.add("simulation.getGroup(member).removeMember(member)");
                scripts.add("simulation.removeComputer(member.getComputer())");

                // Use the first avaiable operating system as execution environment
                Payload payload = new Payload(member.getComputer().getOperatingSystem(), scripts);

                return new ExploitAction(this, target, exploit, payload);
            }
        }

        return null;
    }

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (target == null ? 0 : target.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        DestroyInterest other = (DestroyInterest) obj;
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

        return getClass().getName() + " [target=" + target.toInfoString() + ", getPriority()=" + getPriority() + "]";
    }

}
