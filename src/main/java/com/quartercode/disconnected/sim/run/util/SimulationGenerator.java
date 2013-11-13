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

package com.quartercode.disconnected.sim.run.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.quartercode.disconnected.sim.Location;
import com.quartercode.disconnected.sim.Simulation;
import com.quartercode.disconnected.sim.comp.ByteUnit;
import com.quartercode.disconnected.sim.comp.Computer;
import com.quartercode.disconnected.sim.comp.Version;
import com.quartercode.disconnected.sim.comp.file.File.FileType;
import com.quartercode.disconnected.sim.comp.file.FileContent;
import com.quartercode.disconnected.sim.comp.file.FileRights;
import com.quartercode.disconnected.sim.comp.file.FileSystem;
import com.quartercode.disconnected.sim.comp.file.OutOfSpaceException;
import com.quartercode.disconnected.sim.comp.file.StringContent;
import com.quartercode.disconnected.sim.comp.hardware.CPU;
import com.quartercode.disconnected.sim.comp.hardware.HardDrive;
import com.quartercode.disconnected.sim.comp.hardware.Hardware;
import com.quartercode.disconnected.sim.comp.hardware.Mainboard;
import com.quartercode.disconnected.sim.comp.hardware.Mainboard.MainboradSlot;
import com.quartercode.disconnected.sim.comp.hardware.Mainboard.NeedsMainboardSlot;
import com.quartercode.disconnected.sim.comp.hardware.NetworkInterface;
import com.quartercode.disconnected.sim.comp.hardware.RAM;
import com.quartercode.disconnected.sim.comp.net.IP;
import com.quartercode.disconnected.sim.comp.os.Environment;
import com.quartercode.disconnected.sim.comp.os.Environment.EnvironmentVariable;
import com.quartercode.disconnected.sim.comp.os.Group;
import com.quartercode.disconnected.sim.comp.os.OperatingSystem;
import com.quartercode.disconnected.sim.comp.os.User;
import com.quartercode.disconnected.sim.comp.os.UserManager;
import com.quartercode.disconnected.sim.comp.program.KernelProgram;
import com.quartercode.disconnected.sim.comp.program.desktop.SystemViewerProgram;
import com.quartercode.disconnected.sim.comp.program.desktop.TerminalProgram;
import com.quartercode.disconnected.sim.comp.program.shell.ChangeDirectoryProgram;
import com.quartercode.disconnected.sim.comp.program.shell.DeleteFileProgram;
import com.quartercode.disconnected.sim.comp.program.shell.FileContentProgram;
import com.quartercode.disconnected.sim.comp.program.shell.FileRightsProgram;
import com.quartercode.disconnected.sim.comp.program.shell.ListFilesProgram;
import com.quartercode.disconnected.sim.comp.program.shell.MakeFileProgram;
import com.quartercode.disconnected.sim.comp.session.DesktopSessionProgram;
import com.quartercode.disconnected.sim.comp.session.ShellSessionProgram;
import com.quartercode.disconnected.sim.member.Member;
import com.quartercode.disconnected.sim.member.MemberGroup;
import com.quartercode.disconnected.sim.member.ai.PlayerController;
import com.quartercode.disconnected.sim.member.ai.UserController;
import com.quartercode.disconnected.util.LocationGenerator;
import com.quartercode.disconnected.util.RandomPool;

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
     * @param random The random pool to use for the new simulation.
     * @return The generated simulation object.
     */
    public static Simulation generateSimulation(int computers, int groups, RandomPool random) {

        Simulation simulation = new Simulation(random);

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

        // Add local player
        Member localPlayer = new Member("player");
        localPlayer.setComputer(generateComputers(simulation, 1, simulation.getComputers()).get(0));
        simulation.addComputer(localPlayer.getComputer());
        localPlayer.setAiController(new PlayerController(localPlayer, true));
        simulation.addMember(localPlayer);

        // Generate reputations
        for (MemberGroup group : simulation.getGroups()) {
            for (Member member : simulation.getMembers()) {
                if (group.getMembers().contains(member)) {
                    group.getReputation(member).addValue(simulation.getRandom().nextInt(10));
                } else {
                    group.getReputation(member).subtractValue(simulation.getRandom().nextInt(12));
                }
            }
        }

        return simulation;
    }

    /**
     * Generates the given amount of computers randomly.
     * 
     * @param simulation The simulation to use for generating some data.
     * @param amount The amount of computers the generator should generate.
     * @return The generated list of computers.
     */
    public static List<Computer> generateComputers(Simulation simulation, int amount) {

        return generateComputers(simulation, amount, null);
    }

    /**
     * Generates the given amount of computers randomly ignoring the locations of the given computers.
     * 
     * @param simulation The simulation to use for generating some data.
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

        for (Location location : LocationGenerator.generateLocations(amount, ignoreLocations, simulation.getRandom())) {
            Computer computer = new Computer();
            computer.setLocation(location);
            computers.add(computer);

            List<MainboradSlot> mainboradSlots = new ArrayList<MainboradSlot>();
            mainboradSlots.add(new MainboradSlot(CPU.class));
            mainboradSlots.add(new MainboradSlot(RAM.class));
            mainboradSlots.add(new MainboradSlot(HardDrive.class));
            mainboradSlots.add(new MainboradSlot(HardDrive.class));
            mainboradSlots.add(new MainboradSlot(NetworkInterface.class));
            computer.addHardware(new Mainboard(computer, "MB XYZ 2000 Pro", new Version(1, 2, 5), null, mainboradSlots));

            List<Hardware> hardware = new ArrayList<Hardware>();
            hardware.add(new CPU(computer, "Intel Core i7-4950HQ", new Version(1, 0, 0), null, 8, 2400000000L));
            hardware.add(new RAM(computer, "EpicRAM 4194304", new Version(1, 0, 5), null, ByteUnit.BYTE.convert(4, ByteUnit.MEGABYTE), 1600000000L));

            HardDrive systemMedium = new HardDrive(computer, "TheHardDrive 1TB", new Version(1, 2, 0), null, ByteUnit.BYTE.convert(1, ByteUnit.TERABYTE));
            hardware.add(systemMedium);
            HardDrive userMedium = new HardDrive(computer, "TheHardDrive 1TB", new Version(1, 2, 0), null, ByteUnit.BYTE.convert(1, ByteUnit.TERABYTE));
            hardware.add(userMedium);

            NetworkInterface networkInterface = new NetworkInterface(computer, "NI FiberScore Ultimate", new Version(1, 2, 0), null);
            generateIP(networkInterface, simulation);
            hardware.add(networkInterface);

            for (MainboradSlot slot : computer.getHardware(Mainboard.class).get(0).getSlots()) {
                Hardware useHardware = null;
                for (Hardware testHardware : hardware) {
                    if (testHardware.getClass().isAnnotationPresent(NeedsMainboardSlot.class) && slot.accept(testHardware)) {
                        useHardware = testHardware;
                        break;
                    }
                }

                if (useHardware != null) {
                    computer.addHardware(useHardware);
                    slot.setContent(useHardware);
                    hardware.remove(useHardware);
                }
            }

            computer.setOperatingSystem(new OperatingSystem(computer, "Frames", new Version(3, 7, 65), null));
            computer.getOperatingSystem().getUserManager().addUser(new User(computer.getOperatingSystem(), User.SUPERUSER_NAME));

            Group genpop = new Group(computer.getOperatingSystem(), "genpop");
            computer.getOperatingSystem().getUserManager().addGroup(genpop);
            User genuser = new User(computer.getOperatingSystem(), "genuser");
            genuser.addToGroup(genpop, true);
            computer.getOperatingSystem().getUserManager().addUser(genuser);

            try {
                computer.getOperatingSystem().getFileSystemManager().setMountpoint(systemMedium.getFileSystem(), "system");
                addSystemFiles(systemMedium.getFileSystem(), computer.getOperatingSystem().getUserManager());
                computer.getOperatingSystem().getFileSystemManager().setMountpoint(userMedium.getFileSystem(), "user");
                addUserFiles(userMedium.getFileSystem(), computer.getOperatingSystem().getUserManager());
            }
            catch (OutOfSpaceException e) {
                // Really shouldn't happen
            }
        }

        return computers;
    }

    // Temporary method for generating the kernel and some system programs
    private static void addSystemFiles(FileSystem fileSystem, UserManager userManager) throws OutOfSpaceException {

        User superuser = userManager.getSuperuser();

        // Generate kernel file (temp)
        addFile(fileSystem, "boot/kernel", superuser, new FileRights("r--xr--xr--x"), new KernelProgram(new Version("1.0.0"), null));

        // Generate session programs
        addFile(fileSystem, "bin/lash.exe", superuser, new FileRights("r--xr--xr--x"), new ShellSessionProgram(new Version("1.0.0"), null));
        addFile(fileSystem, "bin/desktops.exe", superuser, new FileRights("r--xr--xr--x"), new DesktopSessionProgram(new Version("1.0.0"), null));

        // Generate system programs
        addFile(fileSystem, "bin/cd.exe", superuser, new FileRights("r--xr--xr--x"), new ChangeDirectoryProgram(new Version("1.0.0"), null));
        addFile(fileSystem, "bin/ls.exe", superuser, new FileRights("r--xr--xr--x"), new ListFilesProgram(new Version("1.0.0"), null));
        addFile(fileSystem, "bin/rights.exe", superuser, new FileRights("r--xr--xr--x"), new FileRightsProgram(new Version("1.0.0"), null));
        addFile(fileSystem, "bin/fc.exe", superuser, new FileRights("r--xr--xr--x"), new FileContentProgram(new Version("1.0.0"), null));
        addFile(fileSystem, "bin/fmk.exe", superuser, new FileRights("r--xr--xr--x"), new MakeFileProgram(new Version("1.0.0"), null));
        addFile(fileSystem, "bin/fdel.exe", superuser, new FileRights("r--xr--xr--x"), new DeleteFileProgram(new Version("1.0.0"), null));

        addFile(fileSystem, "bin/terminal.exe", superuser, new FileRights("r--xr--xr--x"), new TerminalProgram(new Version("1.0.0"), null));
        addFile(fileSystem, "bin/sysviewer.exe", superuser, new FileRights("r--xr--xr--x"), new SystemViewerProgram(new Version("1.0.0"), null));

        // Generate environment
        Environment environment = new Environment();
        environment.addVariable(new EnvironmentVariable("PATH", "/system/bin:/user/bin"));
        addFile(fileSystem, "config/environment.cfg", superuser, new FileRights("rw--r---r---"), new StringContent(environment.toString()));
    }

    // Temporary method for generating some unnecessary programs and personal files
    private static void addUserFiles(FileSystem fileSystem, UserManager userManager) throws OutOfSpaceException {

        User superuser = userManager.getSuperuser();

        // Generate other programs
        // Nothing here yet

        // Generate home directories
        fileSystem.addFile("homes", FileType.DIRECTORY, superuser);
        fileSystem.getFile("homes").setRights(new FileRights("r---r---r---"));
        for (User user : userManager.getUsers()) {
            if (!user.equals(userManager.getSuperuser())) {
                fileSystem.addFile("homes/" + user.getName(), FileType.DIRECTORY, user);
                fileSystem.getFile("homes/" + user.getName()).setRights(new FileRights("rwdx--------"));
            }
        }
    }

    private static void addFile(FileSystem fileSystem, String path, User user, FileRights rights, FileContent content) throws OutOfSpaceException {

        fileSystem.addFile(path, FileType.FILE, user);
        fileSystem.getFile(path).setRights(rights);
        fileSystem.getFile(path).setContent(content);
    }

    private static void generateIP(NetworkInterface host, Simulation simulation) {

        gen:
        while (true) {
            int[] parts = new int[4];
            for (int counter = 0; counter < parts.length; counter++) {
                parts[counter] = simulation.getRandom().nextInt(255) + 1;
            }
            for (Computer computer : simulation.getComputers()) {
                for (NetworkInterface testInterface : computer.getHardware(NetworkInterface.class)) {
                    if (Arrays.equals(testInterface.getIp().getParts(), parts)) {
                        continue gen;
                    }
                }
            }
            host.setIp(new IP(host, parts));
            break;
        }
    }

    /**
     * Generates the given amount of member groups randomly.
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
     * Generates the given amount of members randomly.
     * 
     * @param simulation The simulation to use for generating metadata (like ids).
     * @param amount The amount of members the generator should generate.
     * @return The generated list of members.
     */
    public static List<Member> generateMembers(Simulation simulation, int amount) {

        List<Member> members = new ArrayList<Member>();

        int idDelta = 0;
        for (Member member : simulation.getMembers()) {
            if (member.getName().startsWith("member-")) {
                int id = Integer.parseInt(member.getName().replace("member-", ""));
                if (id + 1 > idDelta) {
                    idDelta = id + 1;
                }
            }
        }

        for (int counter = 0; counter < amount; counter++) {
            Member member = new Member("member-" + (idDelta + counter));
            members.add(member);
            member.setAiController(new UserController(member));
        }

        return members;
    }

    /**
     * Generates a list of members randomly using the given computers and groups.
     * The generated amount of members is euqally to the amount of given computers.
     * Also, every generated member gets randomly sorted into one of the given member groups.
     * 
     * @param simulation The simulation to use for generating metadata (like ids).
     * @param computers The computers to use for generating the members.
     * @param groups The groups to sort the members in.
     * @return The generated list of members.
     */
    public static List<Member> generateMembers(Simulation simulation, List<Computer> computers, List<MemberGroup> groups) {

        List<Member> members = generateMembers(simulation, computers.size());

        for (int counter = 0; counter < members.size(); counter++) {
            groups.get(simulation.getRandom().nextInt(groups.size())).addMember(members.get(counter));
            members.get(counter).setComputer(computers.get(counter));
        }

        return members;
    }

    private SimulationGenerator() {

    }

}
