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
import com.quartercode.disconnected.sim.Simulation;
import com.quartercode.disconnected.world.comp.Computer;
import com.quartercode.disconnected.world.comp.hardware.NetworkInterface;
import com.quartercode.disconnected.world.comp.net.Packet;
import com.quartercode.disconnected.world.comp.program.Process;
import com.quartercode.disconnected.world.comp.program.Process.ProcessState;
import com.quartercode.disconnected.world.general.RootObject;

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
            // Execute process ticks
            for (Computer computer : simulation.getWorld().getRoot().get(RootObject.COMPUTERS_PROPERTY)) {
                if (computer.getOperatingSystem().isRunning()) {
                    for (Process process : new ArrayList<Process>(computer.getOperatingSystem().getProcessManager().getAllProcesses())) {
                        if (process.getState() == ProcessState.RUNNING || process.getState() == ProcessState.INTERRUPTED) {
                            process.getExecutor().updateTasks();
                        } else if (process.isCompletelyStopped()) {
                            process.getParent().unregisterChild(process);
                        }
                    }
                }
            }

            // Send remaining packets from network interfaces
            for (Computer computer : simulation.getWorld().getRoot().get(RootObject.COMPUTERS_PROPERTY)) {
                for (NetworkInterface networkInterface : computer.getHardware(NetworkInterface.class)) {
                    Packet packet = null;
                    while ( (packet = networkInterface.nextDeliveryPacket(true)) != null) {
                        packet.getReceiver().getIp().getHost().receivePacket(packet);
                    }
                }
            }

            // TEMPDIS
            // // Generate new members and computers
            // int newComputers = simulation.RANDOM.nextInt(ProbabilityUtil.gen(0.008F, simulation.RANDOM) ? 50 : 8) - 5;
            // if (newComputers > 0) {
            // List<Computer> computers = SimulationGenerator.generateComputers(simulation, newComputers, simulation.getComputers());
            // for (Computer computer : computers) {
            // simulation.addComputer(computer);
            // }
            // for (Member member : SimulationGenerator.generateMembers(simulation, computers, simulation.getGroups())) {
            // simulation.addMember(member);
            // }
            // }
            //
            // // Clean interests
            // for (MemberGroup group : simulation.getGroups()) {
            // for (Interest interest : new ArrayList<Interest>(group.getInterests())) {
            // if (interest instanceof HasTarget && !simulation.getMembers().contains( ((HasTarget) interest).getTarget())) {
            // group.removeInterest(interest);
            // }
            // }
            // }
            // for (Member member : simulation.getMembers()) {
            // for (Interest interest : new ArrayList<Interest>(member.getBrainData(Interest.class))) {
            // if (interest instanceof HasTarget && !simulation.getMembers().contains( ((HasTarget) interest).getTarget())) {
            // member.removeBrainData(interest);
            // }
            // }
            // }
            //
            // // Generate global group interests against members with bad reputation
            // for (MemberGroup group : simulation.getGroups()) {
            // targetLoop:
            // for (Member target : simulation.getMembers()) {
            // for (Interest interest : group.getInterests()) {
            // if (interest instanceof HasTarget && ((HasTarget) interest).getTarget().equals(target)) {
            // continue targetLoop;
            // }
            // }
            //
            // if (group.getReputation(target).getValue() <= -10) {
            // if (ProbabilityUtil.genPseudo(-group.getReputation(target).getValue() / 20F, simulation.RANDOM)) {
            // float priority = -group.getReputation(target).getValue() / 400F;
            // if (priority > 1) {
            // priority = 1;
            // }
            // group.addInterest(new DestroyInterest(priority, target));
            // }
            // }
            // }
            // }
            //
            // // Execute global group interests
            // for (MemberGroup group : simulation.getGroups()) {
            // for (Interest interest : new ArrayList<Interest>(group.getInterests())) {
            // for (Member member : simulation.getMembers()) {
            // Action action = interest.getAction(simulation, member);
            // if (action != null) {
            // if (action.execute(simulation, member)) {
            // group.removeInterest(interest);
            // }
            //
            // break;
            // }
            // }
            // }
            // }
            //
            // // Simulate members
            // for (Member member : new ArrayList<Member>(simulation.getMembers())) {
            // if (member.getAiController() != null) {
            // member.getAiController().update(simulation);
            // }
            // }
        }
    }

}
