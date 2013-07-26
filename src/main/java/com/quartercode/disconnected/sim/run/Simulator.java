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

package com.quartercode.disconnected.sim.run;

import java.util.ArrayList;
import java.util.List;
import com.quartercode.disconnected.sim.Simulation;
import com.quartercode.disconnected.sim.comp.Computer;
import com.quartercode.disconnected.sim.member.Member;
import com.quartercode.disconnected.sim.member.MemberGroup;
import com.quartercode.disconnected.sim.member.interest.DestroyInterest;
import com.quartercode.disconnected.sim.member.interest.Interest;
import com.quartercode.disconnected.sim.member.interest.Target;
import com.quartercode.disconnected.sim.run.action.Action;
import com.quartercode.disconnected.util.ProbabilityUtil;
import com.quartercode.disconnected.util.RandomPool;
import com.quartercode.disconnected.util.SimulationGenerator;

/**
 * This class implements the root simulation update method for executing the simulation.
 * The tick update will actually get called by a running tick thread.
 * 
 * @see Simulation
 * @see TickThread
 */
public class Simulator implements TickAction {

    private final Simulation simulation;
    private TickThread       tickThread;
    private final TickTimer  tickTimer = new TickTimer();

    /**
     * Creates a new simulator and sets the simulation which should be simulated.
     * 
     * @param simulation The simulation which should be simulated.
     */
    public Simulator(Simulation simulation) {

        this.simulation = simulation;
    }

    /**
     * Returns the simulation which is simulated by this simulator.
     * 
     * @return The simulation which is simulated by this simulator.
     */
    public Simulation getSimulation() {

        return simulation;
    }

    /**
     * Returns if the tick thread is currently running.
     * 
     * @return If the tick thread is currently running.
     */
    public boolean isRunning() {

        return tickThread != null && tickThread.isAlive();
    }

    /**
     * Changes the status of the tick thread.
     * This can start and stop the tick update.
     * 
     * @param running If the tick thread should run.
     */
    public void setRunning(boolean running) {

        if (running && !isRunning()) {
            tickThread = new TickThread(tickTimer, this);
            tickThread.start();
        } else if (!running && isRunning()) {
            tickThread.interrupt();
            tickThread = null;
        }
    }

    /**
     * Returns the current tick thread which executes the actual tick update.
     * If the update is not running, this returns null.
     * 
     * @return The current tick thread which executes the actual tick update.
     */
    public TickThread getTickThread() {

        return tickThread;
    }

    /**
     * Returns the final tick timer which schedules delayed and peridoic tasks.
     * The tick timer is always the same object and wont change if you change the running state.
     * 
     * @return The final tick timer which schedules delayed and peridoic tasks.
     */
    public TickTimer getTickTimer() {

        return tickTimer;
    }

    /**
     * Executes the basic tick update which is called in the same intervals.
     * This calls some subroutines which actually simulate a tick.
     */
    @Override
    public void update() {

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
                if (interest instanceof Target && !simulation.getMembers().contains( ((Target) interest).getTarget())) {
                    group.removeInterest(interest);
                }
            }
        }
        for (Member member : simulation.getMembers()) {
            for (Interest interest : new ArrayList<Interest>(member.getInterests())) {
                if (interest instanceof Target && !simulation.getMembers().contains( ((Target) interest).getTarget())) {
                    member.removeInterest(interest);
                }
            }
        }

        // Generate global group interests against members with bas reputation
        for (MemberGroup group : simulation.getGroups()) {
            targetLoop:
            for (Member target : simulation.getMembers()) {
                for (Interest interest : group.getInterests()) {
                    if (interest instanceof Target && ((Target) interest).getTarget().equals(target)) {
                        continue targetLoop;
                    }
                }

                if (group.getReputation(target).getValue() <= /*-50*/-10) {
                    if (ProbabilityUtil.genPseudo(-group.getReputation(target).getValue() / 20F)) {
                        float priority = -group.getReputation(target).getValue() / /* 200F */400F;
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
                                if (interest instanceof Target && ((Target) interest).getTarget().equals(target)) {
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
                    if (interest instanceof Target && !simulation.getMembers().contains( ((Target) interest).getTarget())) {
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
