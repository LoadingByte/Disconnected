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

import java.util.logging.Level;
import java.util.logging.Logger;
import com.quartercode.classmod.base.FeatureHolder;
import com.quartercode.classmod.extra.ExecutorInvocationException;
import com.quartercode.classmod.extra.Function;
import com.quartercode.classmod.extra.FunctionDefinition;
import com.quartercode.classmod.util.FunctionDefinitionFactory;
import com.quartercode.disconnected.sim.Simulation;

/**
 * This class implements the root tick update mechanisms for the entire simulation.
 */
public class TickSimulator implements TickAction {

    private static final Logger LOGGER = Logger.getLogger(TickSimulator.class.getName());

    private Simulation          simulation;

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
            try {
                // Execute world object ticks
                updateObject(simulation.getWorld());
            } catch (ExecutorInvocationException e) {
                LOGGER.log(Level.SEVERE, "Unexcpected function execution exception during world tick update", e);
            }

            // TEMPDIS
            // // Execute process ticks
            // for (Computer computer : simulation.getWorld().getRoot().get(RootObject.COMPUTERS)) {
            // if (computer.get(Computer.OS).get().isRunning()) {
            // for (Process process : new ArrayList<Process>(computer.get(Computer.OS).get().getProcessManager().getAllProcesses())) {
            // if (process.getState() == ProcessState.RUNNING || process.getState() == ProcessState.INTERRUPTED) {
            // process.getExecutor().updateTasks();
            // } else if (process.isCompletelyStopped()) {
            // process.getParent().unregisterChild(process);
            // }
            // }
            // }
            // }
            //
            // // Send remaining packets from network interfaces
            // for (Computer computer : simulation.getWorld().getRoot().get(RootObject.COMPUTERS)) {
            // for (NetworkInterface networkInterface : computer.get(Computer.HARDWARE).get(NetworkInterface.class)) {
            // Packet packet = null;
            // while ( (packet = networkInterface.nextDeliveryPacket(true)) != null) {
            // packet.getReceiver().getIp().getHost().receivePacket(packet);
            // }
            // }
            // }
            //
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

    private void updateObject(Object object) throws ExecutorInvocationException {

        if (object instanceof TickUpdatable) {
            ((TickUpdatable) object).get(TickUpdatable.TICK_UPDATE).invoke();
        }

        if (object instanceof Iterable) {
            for (Object child : (Iterable<?>) object) {
                updateObject(child);
            }
        }
    }

    /**
     * {@link FeatureHolder}s which implement this interface inherit the {@link #TICK_UPDATE} {@link Function} that is automatically invoked by the tick simulator.
     * The simulator goes over all {@link FeatureHolder}s of a world and invoked that {@link #TICK_UPDATE} {@link Function} on all tick updatables.
     */
    public static interface TickUpdatable extends FeatureHolder {

        /**
         * The tick update {@link Function} is automatically invoked by the tick simulator on every tick.
         * It should execute some activities related to the simulation of the world tree.
         */
        public static final FunctionDefinition<Void> TICK_UPDATE = FunctionDefinitionFactory.create("tickUpdate");

    }

}
