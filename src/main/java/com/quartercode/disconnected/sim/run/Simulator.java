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
import com.quartercode.disconnected.sim.member.interest.Interest;
import com.quartercode.disconnected.sim.member.interest.SabotageInterest;
import com.quartercode.disconnected.sim.member.interest.Target;
import com.quartercode.disconnected.sim.run.action.Action;
import com.quartercode.disconnected.util.RandomPool;
import com.quartercode.disconnected.util.SimulationGenerator;

/**
 * This class implements the root simulation update method for executing the simulation.
 * The tick update will actually get called by a running tick thread.
 * 
 * @see Simulation
 * @see TickThread
 */
public class Simulator {

    private Simulation simulation;
    private TickThread tickThread;

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
            tickThread = new TickThread(this);
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
     * This method executes the basic tick update which is called in the same intervals.
     * This calls some subroutines which actually simulate a tick.
     */
    public void update() {

        final RandomPool random = new RandomPool(100);

        // Generate new members and computers
        int newComputers = random.nextInt(8) - 5;
        if (newComputers > 0) {
            List<Computer> computers = SimulationGenerator.generateComputers(simulation, newComputers, simulation.getComputers());
            for (Computer computer : computers) {
                simulation.addComputer(computer);
            }
            for (Member member : SimulationGenerator.generateMembers(simulation, computers, simulation.getGroups())) {
                simulation.addMember(member);

                for (MemberGroup group : simulation.getGroups()) {
                    if (group.getMembers().contains(member)) {
                        group.getReputation(member).addValue(random.nextInt(10));
                    } else {
                        group.getReputation(member).addValue(-random.nextInt(15));
                    }
                }
            }
        }

        // Generate global group interests for enemy sabotage (based on reputation)
        for (MemberGroup group : simulation.getGroups()) {
            for (Member member : simulation.getMembers()) {
                Interest existingInterest = null;
                for (Interest interest : group.getInterests()) {
                    if (interest instanceof Target && ((Target) interest).getTarget().equals(member)) {
                        existingInterest = interest;
                        break;
                    }
                }

                if (existingInterest == null && group.getReputation(member).getValue() <= -10) {
                    float probability = -group.getReputation(member).getValue() / 100F;
                    if (probability > 1) {
                        probability = 1;
                    }

                    if (random.nextFloat() <= probability) {
                        int priority = -group.getReputation(member).getValue() / 20;
                        if (priority < 1) {
                            priority = 10;
                        } else if (priority > 10) {
                            probability = 10;
                        }
                        group.addInterest(new SabotageInterest(priority, member));
                    }
                } else if (existingInterest != null && group.getReputation(member).getValue() > -10) {
                    group.removeInterest(existingInterest);
                }
            }
        }

        // Execute global group interests
        for (Member member : simulation.getMembers()) {
            final MemberGroup group = simulation.getGroup(member);
            if (group != null) {
                for (final Interest interest : new ArrayList<Interest>(group.getInterests())) {
                    // Probability calculation still in simulator, getAction() doesn't do it yet
                    int currentReputaion = group.getReputation(member).getValue();
                    float probability = interest.getPriority() * 5F * (interest.getReputationChange(simulation, member, group) * 20F) / ( (currentReputaion == 0 ? 1 : currentReputaion) * 100);
                    if (probability > 1) {
                        probability = 1;
                    }
                    probability /= 50;

                    if (random.nextFloat() <= probability) {
                        new Action() {

                            @Override
                            public void execute(Simulation simulation, final Member member) {

                                // Reputation also still in simulator
                                group.getReputation(member).addValue(2);

                                // Calculate the success before attacking; that's technology! (also: not yet implemented in subroutines)
                                if (random.nextInt(50) == 0) {
                                    Member target = ((Target) interest).getTarget();
                                    group.getReputation(member).addValue(20);
                                    simulation.getGroup(target).getReputation(member).removeValue(20);

                                    // Remove the existing interest
                                    group.removeInterest(interest);

                                    // Finally: The main hack
                                    Action action = interest.getAction(simulation, member);
                                    action.execute(simulation, member);
                                }
                            }
                        }.execute(simulation, member);
                        break;
                    }
                }
            }
        }
    }

}
