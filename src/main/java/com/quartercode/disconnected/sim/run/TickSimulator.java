
package com.quartercode.disconnected.sim.run;

import java.util.ArrayList;
import java.util.List;
import com.quartercode.disconnected.sim.Simulation;
import com.quartercode.disconnected.sim.comp.Computer;
import com.quartercode.disconnected.sim.member.Member;
import com.quartercode.disconnected.sim.member.MemberGroup;
import com.quartercode.disconnected.sim.member.interest.DestroyInterest;
import com.quartercode.disconnected.sim.member.interest.HasTarget;
import com.quartercode.disconnected.sim.member.interest.Interest;
import com.quartercode.disconnected.sim.run.action.Action;
import com.quartercode.disconnected.sim.run.util.SimulationGenerator;
import com.quartercode.disconnected.util.ProbabilityUtil;
import com.quartercode.disconnected.util.RandomPool;

/**
 * This class implements the root tick update mechanisms for the entire simulation.
 */
public class TickSimulator implements TickAction {

    private Simulation simulation;

    /**
     * Creates a new empty tick simulator.
     */
    public TickSimulator() {

    }

    /**
     * Creates a new tick simulator and sets the simulation to simulate.
     * 
     * @param simulation The simulation to simulate.
     */
    public TickSimulator(Simulation simulation) {

        this.simulation = simulation;
    }

    /**
     * Returns the simulation to simulate.
     * 
     * @return The simulation to simulate.
     */
    public Simulation getSimulation() {

        return simulation;
    }

    /**
     * Sets the simulation to simulate to a new one.
     * The action will take place in the next tick.
     * 
     * @param simulation The new simulation to simulate.
     */
    public void setSimulation(Simulation simulation) {

        this.simulation = simulation;
    }

    /**
     * Executes the basic (root) tick update which is called in the same intervals.
     * This calls some subroutines which actually simulate a tick.
     */
    @Override
    public void update() {

        if (simulation != null) {
            // Generate new members and computers
            int newComputers = RandomPool.PUBLIC.nextInt(ProbabilityUtil.gen(0.008F) ? 50 : 8) - 5;
            if (newComputers > 0) {
                List<Computer> computers = SimulationGenerator.generateComputers(simulation, newComputers, simulation.getComputers());
                for (Computer computer : computers) {
                    simulation.addComputer(computer);
                }
                for (Member member : SimulationGenerator.generateMembers(simulation, computers, simulation.getGroups())) {
                    simulation.addMember(member);
                }
            }

            // Clean interests
            for (MemberGroup group : simulation.getGroups()) {
                for (Interest interest : new ArrayList<Interest>(group.getInterests())) {
                    if (interest instanceof HasTarget && !simulation.getMembers().contains( ((HasTarget) interest).getTarget())) {
                        group.removeInterest(interest);
                    }
                }
            }
            for (Member member : simulation.getMembers()) {
                for (Interest interest : new ArrayList<Interest>(member.getInterests())) {
                    if (interest instanceof HasTarget && !simulation.getMembers().contains( ((HasTarget) interest).getTarget())) {
                        member.removeInterest(interest);
                    }
                }
            }

            // Generate global group interests against members with bas reputation
            for (MemberGroup group : simulation.getGroups()) {
                targetLoop:
                for (Member target : simulation.getMembers()) {
                    for (Interest interest : group.getInterests()) {
                        if (interest instanceof HasTarget && ((HasTarget) interest).getTarget().equals(target)) {
                            continue targetLoop;
                        }
                    }

                    if (group.getReputation(target).getValue() <= -10) {
                        if (ProbabilityUtil.genPseudo(-group.getReputation(target).getValue() / 20F)) {
                            float priority = -group.getReputation(target).getValue() / 400F;
                            if (priority > 1) {
                                priority = 1;
                            }
                            group.addInterest(new DestroyInterest(priority, target));
                        }
                    }
                }
            }

            // Execute global group interests
            for (MemberGroup group : simulation.getGroups()) {
                for (Interest interest : new ArrayList<Interest>(group.getInterests())) {
                    for (Member member : simulation.getMembers()) {
                        Action action = interest.getAction(simulation, member);
                        if (action != null) {
                            if (action.execute(simulation, member)) {
                                group.removeInterest(interest);
                            }

                            break;
                        }
                    }
                }
            }

            // Generate member interests against members of other groups
            memberLoop:
            for (Member member : simulation.getMembers()) {
                if (ProbabilityUtil.genPseudo(RandomPool.PUBLIC.nextFloat() / 100F)) {
                    if (member.getInterests().size() < 5) {
                        MemberGroup group = simulation.getGroup(member);
                        targetLoop:
                        for (Member target : simulation.getMembers()) {
                            if (!simulation.getGroup(target).equals(group)) {
                                for (Interest interest : member.getInterests()) {
                                    if (interest instanceof HasTarget && ((HasTarget) interest).getTarget().equals(target)) {
                                        continue targetLoop;
                                    }
                                }

                                if (ProbabilityUtil.genPseudo(RandomPool.PUBLIC.nextFloat() + -group.getReputation(target).getValue() / 100F)) {
                                    float priority = RandomPool.PUBLIC.nextFloat() + -group.getReputation(target).getValue() / /* 200F */40F;
                                    if (priority > 1) {
                                        priority = 1;
                                    }
                                    member.addInterest(new DestroyInterest(priority, target));
                                    continue memberLoop;
                                }
                            }
                        }
                    }
                }
            }

            // Execute member interests
            for (Member member : simulation.getMembers()) {
                if (simulation.getMembers().contains(member)) {
                    for (Interest interest : new ArrayList<Interest>(member.getInterests())) {
                        if (interest instanceof HasTarget && !simulation.getMembers().contains( ((HasTarget) interest).getTarget())) {
                            continue;
                        } else {
                            Action action = interest.getAction(simulation, member);
                            if (action != null) {
                                if (action.execute(simulation, member)) {
                                    member.removeInterest(interest);
                                }

                                break;
                            }
                        }
                    }
                }
            }
        }
    }

}
