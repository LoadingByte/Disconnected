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
import java.util.List;
import com.quartercode.disconnected.sim.Simulation;
import com.quartercode.disconnected.util.LocationGenerator;
import com.quartercode.disconnected.util.RandomPool;
import com.quartercode.disconnected.world.World;
import com.quartercode.disconnected.world.comp.ByteUnit;
import com.quartercode.disconnected.world.comp.Computer;
import com.quartercode.disconnected.world.comp.Version;
import com.quartercode.disconnected.world.comp.file.File.FileType;
import com.quartercode.disconnected.world.comp.file.FileContent;
import com.quartercode.disconnected.world.comp.file.FileRights;
import com.quartercode.disconnected.world.comp.file.FileSystem;
import com.quartercode.disconnected.world.comp.file.OutOfSpaceException;
import com.quartercode.disconnected.world.comp.file.StringContent;
import com.quartercode.disconnected.world.comp.hardware.CPU;
import com.quartercode.disconnected.world.comp.hardware.HardDrive;
import com.quartercode.disconnected.world.comp.hardware.Hardware;
import com.quartercode.disconnected.world.comp.hardware.Mainboard;
import com.quartercode.disconnected.world.comp.hardware.Mainboard.MainboradSlot;
import com.quartercode.disconnected.world.comp.hardware.Mainboard.NeedsMainboardSlot;
import com.quartercode.disconnected.world.comp.hardware.NetworkInterface;
import com.quartercode.disconnected.world.comp.hardware.RAM;
import com.quartercode.disconnected.world.comp.os.Environment;
import com.quartercode.disconnected.world.comp.os.Environment.EnvironmentVariable;
import com.quartercode.disconnected.world.comp.os.Group;
import com.quartercode.disconnected.world.comp.os.OperatingSystem;
import com.quartercode.disconnected.world.comp.os.User;
import com.quartercode.disconnected.world.comp.os.UserManager;
import com.quartercode.disconnected.world.comp.program.KernelProgram;
import com.quartercode.disconnected.world.comp.program.desktop.SystemViewerProgram;
import com.quartercode.disconnected.world.comp.program.desktop.TerminalProgram;
import com.quartercode.disconnected.world.comp.program.shell.ChangeDirectoryProgram;
import com.quartercode.disconnected.world.comp.program.shell.DeleteFileProgram;
import com.quartercode.disconnected.world.comp.program.shell.FileContentProgram;
import com.quartercode.disconnected.world.comp.program.shell.FileRightsProgram;
import com.quartercode.disconnected.world.comp.program.shell.ListFilesProgram;
import com.quartercode.disconnected.world.comp.program.shell.MakeFileProgram;
import com.quartercode.disconnected.world.comp.session.DesktopSessionProgram;
import com.quartercode.disconnected.world.comp.session.ShellSessionProgram;
import com.quartercode.disconnected.world.general.Location;
import com.quartercode.disconnected.world.general.RootObject;
import com.quartercode.disconnected.world.member.Member;
import com.quartercode.disconnected.world.member.MemberGroup;
import com.quartercode.disconnected.world.member.Reputation;
import com.quartercode.disconnected.world.member.ai.PlayerController;
import com.quartercode.disconnected.world.member.ai.UserController;

/**
 * This utility class generates a {@link Simulation}s and {@link World}s.
 * It's assembling all basic {@link World} objects ({@link Member}s, {@link MemberGroup}s and {@link Computer}s) and generating basic {@link Reputation}s.
 * The utility can also generate some parts without creating a whole {@link World}.
 */
public class SimulationGenerator {

    /**
     * Generates a new {@link Simulation} with the given settings.
     * This basically uses {@link #generateWorld(int, int, Simulation, RandomPool)} for creating a new world.
     * 
     * @param computers The amount of {@link Computer}s the generator should generate.
     * @param groups The amount of {@link MemberGroup}s generator should generate.
     * @param random The {@link RandomPool} to use for the new {@link Simulation}.
     * @return The generated {@link Simulation} object.
     */
    public static Simulation generateSimulation(int computers, int groups, RandomPool random) {

        Simulation simulation = new Simulation(random);
        simulation.setWorld(generateWorld(computers, groups, simulation, random));
        return simulation;
    }

    /**
     * Generates a new {@link World} with the given settings for the given {@link Simulation}.
     * If you don't want to use the {@link World} in a simulation, you can set it to null.
     * 
     * @param computers The amount of {@link Computer}s the generator should generate.
     * @param groups The amount of {@link MemberGroup}s generator should generate.
     * @param simulation The {@link Simulation} the new {@link World} is generated for. This can be null.
     * @param random The {@link RandomPool} which is used for generating the {@link World}.
     * @return The generated {@link World} object.
     */
    public static World generateWorld(int computers, int groups, Simulation simulation, RandomPool random) {

        World world = new World(simulation);

        // Assemble basic objects
        for (Computer computer : generateComputers(computers, random)) {
            world.getRoot().get(RootObject.COMPUTERS).add(computer);
        }
        for (MemberGroup memberGroup : generateMemberGroups(groups, random)) {
            world.getRoot().get(RootObject.GROUPS).add(memberGroup);
        }
        for (Member member : generateMembers(world.getRoot().get(RootObject.COMPUTERS), world.getRoot().get(RootObject.GROUPS), random)) {
            world.getRoot().get(RootObject.MEMBERS).add(member);
        }

        // Add local player
        Member localPlayer = new Member("player");
        localPlayer.setComputer(generateComputers(1, world.getRoot().get(RootObject.COMPUTERS), random).get(0));
        world.getRoot().get(RootObject.COMPUTERS).add(localPlayer.getComputer());
        localPlayer.setAiController(new PlayerController(localPlayer, true));
        world.getRoot().get(RootObject.MEMBERS).add(localPlayer);

        // Generate reputations
        for (MemberGroup group : world.getRoot().get(RootObject.GROUPS)) {
            for (Member member : world.getRoot().get(RootObject.MEMBERS)) {
                if (group.getMembers().contains(member)) {
                    group.getReputation(member).addValue(random.nextInt(10));
                } else {
                    group.getReputation(member).subtractValue(random.nextInt(12));
                }
            }
        }

        return world;
    }

    /**
     * Generates the given amount of {@link Computer}s randomly.
     * 
     * @param amount The amount of {@link Computer}s the generator should generate.
     * @param random The {@link RandomPool} which is used for generating the {@link Computer}s.
     * @return The generated list of {@link Computer}s.
     */
    public static List<Computer> generateComputers(int amount, RandomPool random) {

        return generateComputers(amount, null, random);
    }

    /**
     * Generates the given amount of {@link Computer} randomly ignoring the {@link Location}s of the given {@link Computer}s.
     * 
     * @param amount The amount of {@link Computer}s the generator should generate.
     * @param ignore There wont be any {@link Computer}s with one of those {@link Location}s.
     * @param random The {@link RandomPool} which is used for generating the {@link Computer}s.
     * @return The generated list of {@link Computer}s.
     */
    public static List<Computer> generateComputers(int amount, List<Computer> ignore, RandomPool random) {

        List<Computer> computers = new ArrayList<Computer>();

        List<Location> ignoreLocations = new ArrayList<Location>();
        if (ignore != null) {
            for (Computer computer : ignore) {
                ignoreLocations.add(computer.getLocation());
            }
        }

        for (Location location : LocationGenerator.generateLocations(amount, ignoreLocations, random)) {
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
            hardware.add(new NetworkInterface(computer, "NI FiberScore Ultimate", new Version(1, 2, 0), null));

            HardDrive systemMedium = new HardDrive(computer, "TheHardDrive 1TB", new Version(1, 2, 0), null, ByteUnit.BYTE.convert(1, ByteUnit.TERABYTE));
            hardware.add(systemMedium);
            HardDrive userMedium = new HardDrive(computer, "TheHardDrive 1TB", new Version(1, 2, 0), null, ByteUnit.BYTE.convert(1, ByteUnit.TERABYTE));
            hardware.add(userMedium);

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

    /**
     * Generates the given amount of {@link MemberGroup}s randomly.
     * 
     * @param amount The amount of {@link MemberGroup}s the generator should generate.
     * @param random The {@link RandomPool} which is used for generating the {@link MemberGroup}s.
     * @return The generated list of {@link MemberGroup}s.
     */
    public static List<MemberGroup> generateMemberGroups(int amount, RandomPool random) {

        List<MemberGroup> groups = new ArrayList<MemberGroup>();

        for (int counter = 0; counter < amount; counter++) {
            groups.add(new MemberGroup());
        }

        return groups;
    }

    /**
     * Generates the given amount of {@link Member}s randomly.
     * 
     * @param amount The amount of {@link Member}s the generator should generate.
     * @param random The {@link RandomPool} which is used for generating the {@link Member}s.
     * @return The generated list of {@link Member}s.
     */
    public static List<Member> generateMembers(int amount, RandomPool random) {

        List<Member> members = new ArrayList<Member>();

        for (int counter = 0; counter < amount; counter++) {
            Member member = new Member("member-" + random.nextInt());
            members.add(member);
            member.setAiController(new UserController(member));
        }

        return members;
    }

    /**
     * Generates a list of {@link Member}s randomly using the given {@link Computer}s and {@link MemberGroup}s.
     * The generated amount of {@link Member}s is equally to the amount of given {@link Computer}s.
     * Also, every generated {@link Member} is randomly sorted into one of the given {@link MemberGroup}s.
     * 
     * @param computers The {@link Computer}s to use for generating the {@link Member}s.
     * @param groups The {@link MemberGroup}s the {@link Member}s are randomly sorted into.
     * @param random The {@link RandomPool} which is used for generating the {@link Member}s and doing the sorting.
     * @return The generated list of {@link Member}s.
     */
    public static List<Member> generateMembers(List<Computer> computers, List<MemberGroup> groups, RandomPool random) {

        List<Member> members = generateMembers(computers.size(), random);

        for (int counter = 0; counter < members.size(); counter++) {
            groups.get(random.nextInt(groups.size())).addMember(members.get(counter));
            members.get(counter).setComputer(computers.get(counter));
        }

        return members;
    }

    private SimulationGenerator() {

    }

}
