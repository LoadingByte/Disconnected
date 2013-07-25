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

package com.quartercode.disconnected.util;

import java.util.ArrayList;
import java.util.List;
import com.quartercode.disconnected.Disconnected;
import com.quartercode.disconnected.sim.Simulation;
import com.quartercode.disconnected.sim.comp.Computer;
import com.quartercode.disconnected.sim.comp.ComputerPart;
import com.quartercode.disconnected.sim.comp.Hardware;
import com.quartercode.disconnected.sim.comp.Location;
import com.quartercode.disconnected.sim.comp.Mainboard;
import com.quartercode.disconnected.sim.comp.Mainboard.MainboradSlot;
import com.quartercode.disconnected.sim.comp.OperatingSystem;
import com.quartercode.disconnected.sim.member.Member;
import com.quartercode.disconnected.sim.member.MemberGroup;

/**
 * This utility class generates a simulation.
 * It's assembling all basic simulation objects (members, groups and computers) and generating basic reputations for all members.
 * The utility can also generate some parts without creating a whole simulation.
 */
public class SimulationGenerator {

    /**
     * Generates a new simulation.
     * 
     * @param computers The amount of computers the generator should generate.
     * @param groups The amount of groups the generator should generate.
     * @return The generated simulation object.
     */
    public static Simulation generateSimulation(int computers, int groups) {

        Simulation simulation = new Simulation();

        // Assemble basic objects
        for (Computer computer : generateComputers(simulation, computers)) {
            simulation.addComputer(computer);
        }
        for (MemberGroup memberGroup : generateMemberGroups(simulation, groups)) {
            simulation.addGroup(memberGroup);
        }
        for (Member member : generateMembers(simulation, simulation.getComputers(), simulation.getGroups())) {
            simulation.addMember(member);
        }

        // Generate reputations
        for (MemberGroup group : simulation.getGroups()) {
            for (Member member : simulation.getMembers()) {
                if (group.getMembers().contains(member)) {
                    group.getReputation(member).addValue(RandomPool.PUBLIC.nextInt(10));
                } else {
                    group.getReputation(member).subtractValue(RandomPool.PUBLIC.nextInt(12));
                }
            }
        }

        return simulation;
    }

    /**
     * Generates the given amount of computers RandomPool.PUBLICly.
     * 
     * @param simulation The simulation to use for generating metadata (like ids).
     * @param amount The amount of computers the generator should generate.
     * @return The generated list of computers.
     */
    public static List<Computer> generateComputers(Simulation simulation, int amount) {

        return generateComputers(simulation, amount, null);
    }

    /**
     * Generates the given amount of computers RandomPool.PUBLICly ignoring the locations of the given computers.
     * 
     * @param simulation The simulation to use for generating metadata (like ids).
     * @param amount The amount of computers the generator should generate.
     * @param ignore The locations of those computers will be ignored.
     * @return The generated list of computers.
     */
    public static List<Computer> generateComputers(Simulation simulation, int amount, List<Computer> ignore) {

        List<Computer> computers = new ArrayList<Computer>();

        List<Location> ignoreLocations = new ArrayList<Location>();
        if (ignore != null) {
            for (Computer computer : ignore) {
                ignoreLocations.add(computer.getLocation());
            }
        }

        int counter = 0;
        for (Computer computer : simulation.getComputers()) {
            int id = Integer.parseInt(computer.getId());
            if (id + 1 > counter) {
                counter = id + 1;
            }
        }

        for (Location location : LocationGenerator.generateLocations(amount, ignoreLocations)) {
            Computer computer = new Computer(String.valueOf(counter));
            counter++;
            computer.setLocation(location);
            computers.add(computer);

            List<ComputerPart> mainboards = Disconnected.getResoureStore().getComputerParts(Mainboard.class);
            computer.setMainboard((Mainboard) mainboards.get(RandomPool.PUBLIC.nextInt(mainboards.size())).clone());

            for (MainboradSlot slot : computer.getMainboard().getSlots()) {
                List<ComputerPart> possibleHardware = Disconnected.getResoureStore().getComputerParts(slot.getType());
                Hardware hardware = (Hardware) possibleHardware.get(RandomPool.PUBLIC.nextInt(possibleHardware.size())).clone();
                computer.addHardware(hardware);
                slot.setContent(hardware);
            }

            List<ComputerPart> operatingSystems = Disconnected.getResoureStore().getComputerParts(OperatingSystem.class);
            computer.addOperatingSystem((OperatingSystem) operatingSystems.get(RandomPool.PUBLIC.nextInt(operatingSystems.size())).clone());
        }

        return computers;
    }

    /**
     * Generates the given amount of member groups RandomPool.PUBLICly.
     * 
     * @param simulation The simulation to use for generating metadata (like ids).
     * @param amount The amount of member groups the generator should generate.
     * @return The generated list of member groups.
     */
    public static List<MemberGroup> generateMemberGroups(Simulation simulation, int amount) {

        List<MemberGroup> groups = new ArrayList<MemberGroup>();

        for (int counter = 0; counter < amount; counter++) {
            groups.add(new MemberGroup());
        }

        return groups;
    }

    /**
     * Generates the given amount of members RandomPool.PUBLICly.
     * 
     * @param simulation The simulation to use for generating metadata (like ids).
     * @param amount The amount of members the generator should generate.
     * @return The generated list of members.
     */
    public static List<Member> generateMembers(Simulation simulation, int amount) {

        List<Member> members = new ArrayList<Member>();

        int idDelta = 0;
        for (Member member : simulation.getMembers()) {
            int id = Integer.parseInt(member.getName().replace("member-", ""));
            if (id + 1 > idDelta) {
                idDelta = id + 1;
            }
        }

        for (int counter = 0; counter < amount; counter++) {
            members.add(new Member("member-" + (idDelta + counter)));
        }

        return members;
    }

    /**
     * Generates a list of members RandomPool.PUBLICly using the given computers and groups.
     * The generated amount of members is euqally to the amount of given computers.
     * Also, every generated member gets RandomPool.PUBLICly sorted into one of the given member groups.
     * 
     * @param simulation The simulation to use for generating metadata (like ids).
     * @param computers The computers to use for generating the members.
     * @param groups The groups to sort the members in.
     * @return The generated list of members.
     */
    public static List<Member> generateMembers(Simulation simulation, List<Computer> computers, List<MemberGroup> groups) {

        List<Member> members = generateMembers(simulation, computers.size());

        for (int counter = 0; counter < members.size(); counter++) {
            groups.get(RandomPool.PUBLIC.nextInt(groups.size())).addMember(members.get(counter));
            members.get(counter).setComputer(computers.get(counter));
        }

        return members;
    }

    private SimulationGenerator() {

    }

}
