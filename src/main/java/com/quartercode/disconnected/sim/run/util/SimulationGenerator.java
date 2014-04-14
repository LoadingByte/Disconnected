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
import com.quartercode.disconnected.mocl.extra.FunctionExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.quartercode.disconnected.sim.Simulation;
import com.quartercode.disconnected.util.RandomPool;
import com.quartercode.disconnected.world.Location;
import com.quartercode.disconnected.world.World;
import com.quartercode.disconnected.world.comp.ByteUnit;
import com.quartercode.disconnected.world.comp.Computer;
import com.quartercode.disconnected.world.comp.Version;
import com.quartercode.disconnected.world.comp.file.ContentFile;
import com.quartercode.disconnected.world.comp.file.Directory;
import com.quartercode.disconnected.world.comp.file.File;
import com.quartercode.disconnected.world.comp.file.FileRights;
import com.quartercode.disconnected.world.comp.file.FileSystem;
import com.quartercode.disconnected.world.comp.hardware.CPU;
import com.quartercode.disconnected.world.comp.hardware.HardDrive;
import com.quartercode.disconnected.world.comp.hardware.Hardware;
import com.quartercode.disconnected.world.comp.hardware.Mainboard;
import com.quartercode.disconnected.world.comp.hardware.Mainboard.MainboardSlot;
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
import com.quartercode.disconnected.world.comp.program.Program;
import com.quartercode.disconnected.world.comp.program.ProgramExecutor;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(SimulationGenerator.class);

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

        try {
            World world = new World(simulation);

            // Assemble basic objects
            for (Computer computer : generateComputers(world, computers, random)) {
                world.get(World.ADD_COMPUTERS).invoke(computer);
            }
            for (MemberGroup memberGroup : generateMemberGroups(world, groups, random)) {
                world.get(World.ADD_GROUPS).invoke(memberGroup);
            }
            for (Member member : generateMembers(world.get(World.GET_COMPUTERS).invoke(), world.get(World.GET_GROUPS).invoke(), random)) {
                world.get(World.ADD_MEMBERS).invoke(member);
            }

            // Add local player
            Member localPlayer = new Member("player");
            localPlayer.setComputer(generateComputers(world, 1, world.get(World.GET_COMPUTERS).invoke(), random).get(0));
            world.get(World.ADD_COMPUTERS).invoke(localPlayer.getComputer());
            localPlayer.setAiController(new PlayerController(localPlayer, true));
            world.get(World.ADD_MEMBERS).invoke(localPlayer);

            // Generate reputations
            for (MemberGroup group : world.get(World.GET_GROUPS).invoke()) {
                for (Member member : world.get(World.GET_MEMBERS).invoke()) {
                    if (group.getMembers().contains(member)) {
                        group.getReputation(member).addValue(random.nextInt(10));
                    } else {
                        group.getReputation(member).subtractValue(random.nextInt(12));
                    }
                }
            }

            return world;
        } catch (ExecutorInvocationException e) {
            LOGGER.error("Unexpected exception while generating simulation", e);
            return null;
        }

    }

    /**
     * Generates the given amount of {@link Computer}s randomly.
     * 
     * @param world The {@link World} the new {@link Computer}s will be in.
     * @param amount The amount of {@link Computer}s the generator should generate.
     * @param random The {@link RandomPool} which is used for generating the {@link Computer}s.
     * @return The generated list of {@link Computer}s.
     */
    public static List<Computer> generateComputers(World world, int amount, RandomPool random) {

        return generateComputers(world, amount, null, random);
    }

    /**
     * Generates the given amount of {@link Computer} randomly ignoring the {@link Location}s of the given {@link Computer}s.
     * 
     * @param world The {@link World} the new {@link Computer}s will be in.
     * @param amount The amount of {@link Computer}s the generator should generate.
     * @param ignore There won't be any {@link Computer}s with one of those {@link Location}s.
     * @param random The {@link RandomPool} which is used for generating the {@link Computer}s.
     * @return The generated list of {@link Computer}s.
     */
    public static List<Computer> generateComputers(World world, int amount, List<Computer> ignore, RandomPool random) {

        try {
            List<Computer> computers = new ArrayList<Computer>();

            List<Location> ignoreLocations = new ArrayList<Location>();
            if (ignore != null) {
                for (Computer computer : ignore) {
                    ignoreLocations.add(computer.get(Computer.GET_LOCATION).invoke());
                }
            }

            for (Location location : LocationGenerator.generateLocations(amount, ignoreLocations, random)) {
                Computer computer = new Computer();
                computer.get(Computer.SET_LOCATION).invoke(location);
                computers.add(computer);

                Mainboard mainboard = new Mainboard();
                mainboard.setLocked(false);
                mainboard.get(Hardware.SET_NAME).invoke("MB XYZ 2000 Pro");
                mainboard.get(Mainboard.ADD_SLOTS).invoke(generateMainboardSlot(CPU.class));
                mainboard.get(Mainboard.ADD_SLOTS).invoke(generateMainboardSlot(RAM.class));
                mainboard.get(Mainboard.ADD_SLOTS).invoke(generateMainboardSlot(HardDrive.class));
                mainboard.get(Mainboard.ADD_SLOTS).invoke(generateMainboardSlot(HardDrive.class));
                mainboard.get(Mainboard.ADD_SLOTS).invoke(generateMainboardSlot(NetworkInterface.class));
                mainboard.setLocked(true);
                computer.get(Computer.ADD_HARDWARE).invoke(mainboard);

                CPU cpu = new CPU();
                cpu.setLocked(false);
                cpu.get(Hardware.SET_NAME).invoke("Intel Core i7-4950HQ");
                cpu.get(CPU.SET_THREADS).invoke(8);
                cpu.get(CPU.SET_FREQUENCY).invoke(2400000000L);
                cpu.setLocked(true);
                computer.get(Computer.ADD_HARDWARE).invoke(cpu);

                RAM ram = new RAM();
                ram.setLocked(false);
                ram.get(Hardware.SET_NAME).invoke("EpicRAM 4194304");
                ram.get(RAM.SET_SIZE).invoke(ByteUnit.BYTE.convert(4, ByteUnit.MEGABYTE));
                ram.get(RAM.SET_FREQUENCY).invoke(1600000000L);
                ram.setLocked(true);
                computer.get(Computer.ADD_HARDWARE).invoke(ram);

                NetworkInterface networkInterface = new NetworkInterface();
                networkInterface.setLocked(false);
                networkInterface.get(Hardware.SET_NAME).invoke("NI FiberScore Ultimate");
                networkInterface.setLocked(true);
                computer.get(Computer.ADD_HARDWARE).invoke(networkInterface);

                HardDrive systemMedium = new HardDrive();
                systemMedium.setLocked(false);
                systemMedium.get(Hardware.SET_NAME).invoke("TheHardDrive 1TB");
                systemMedium.get(HardDrive.GET_FILE_SYSTEM).invoke().get(FileSystem.SET_SIZE).invoke(ByteUnit.BYTE.convert(1, ByteUnit.TERABYTE));
                systemMedium.setLocked(true);
                computer.get(Computer.ADD_HARDWARE).invoke(systemMedium);

                HardDrive userMedium = new HardDrive();
                userMedium.setLocked(false);
                userMedium.get(Hardware.SET_NAME).invoke("TheHardDrive 1TB");
                userMedium.get(HardDrive.GET_FILE_SYSTEM).invoke().get(FileSystem.SET_SIZE).invoke(ByteUnit.BYTE.convert(1, ByteUnit.TERABYTE));
                userMedium.setLocked(true);
                computer.get(Computer.ADD_HARDWARE).invoke(userMedium);

                for (MainboardSlot slot : computer.get(Computer.GET_HARDWARE_BY_TYPE).invoke(Mainboard.class).get(0).get(Mainboard.GET_SLOTS).invoke()) {
                    Hardware useHardware = null;
                    for (Hardware hardware : computer.get(Computer.GET_HARDWARE).invoke()) {
                        if (!hardware.equals(mainboard)) {
                            if (hardware.getClass().isAnnotationPresent(NeedsMainboardSlot.class) && slot.get(MainboardSlot.GET_TYPE).invoke().isAssignableFrom(hardware.getClass())) {
                                useHardware = hardware;
                                break;
                            }
                        }
                    }

                    if (useHardware != null) {
                        slot.get(MainboardSlot.SET_CONTENT).invoke(useHardware);
                    }
                }

                OperatingSystem operatingSystem = new OperatingSystem();
                operatingSystem.get(OperatingSystem.SET_NAME).invoke("Franes");
                operatingSystem.get(OperatingSystem.SET_VERSION).invoke(createVersion(3, 7, 65));
                computer.get(Computer.SET_OS).invoke(operatingSystem);

                // Generate debug users and groups
                User superUser = operatingSystem.get(UserSyscalls.ADD_USER).invoke(User.SUPERUSER_NAME);
                Group defaultGroup = operatingSystem.get(UserSyscalls.ADD_GROUP).invoke("defaultgroup");
                User defaultUser = operatingSystem.get(UserSyscalls.ADD_USER).invoke("defaultuser");
                defaultUser.get(User.ADD_TO_GROUPS).invoke(defaultGroup);

                operatingSystem.getFileSystemManager().setMountpoint(systemMedium.get(HardDrive.GET_FILE_SYSTEM).invoke(), "system");
                addSystemFiles(systemMedium.get(HardDrive.GET_FILE_SYSTEM).invoke(), operatingSystem.getUserManager());
                operatingSystem.getFileSystemManager().setMountpoint(userMedium.get(HardDrive.GET_FILE_SYSTEM).invoke(), "user");
                addUserFiles(userMedium.get(HardDrive.GET_FILE_SYSTEM).invoke(), operatingSystem.getUserManager());
            }

            return computers;
        } catch (ExecutorInvocationException e) {
            LOGGER.error("Unexpected exception while generating computer", e);
            return null;
        }
    }

    private static Version createVersion(int major, int minor, int revision) throws FunctionExecutionException {

        Version version = new Version();
        version.setLocked(false);
        version.get(Version.SET_MAJOR).invoke(major);
        version.get(Version.SET_MINOR).invoke(major);
        version.get(Version.SET_REVISION).invoke(revision);
        version.setLocked(true);
        return version;
    }

    private static MainboardSlot generateMainboardSlot(Class<? extends Hardware> type) throws FunctionExecutionException {

        MainboardSlot slot = new MainboardSlot();
        slot.setLocked(false);
        slot.get(MainboardSlot.SET_TYPE).invoke(type);
        slot.setLocked(true);
        return slot;
    }

    // Temporary method for generating the kernel and some system programs
    private static void addSystemFiles(FileSystem fileSystem, UserManager userManager) throws FunctionExecutionException {

        User superuser = userManager.getSuperuser();

        // Generate kernel file (temp)
        addContentFile(fileSystem, "boot/kernel", superuser, "r--xr--xr--x", createProgram(KernelProgram.class, createVersion(1, 0, 0)));

        // Generate session programs
        addContentFile(fileSystem, "bin/lash.exe", superuser, "r--xr--xr--x", createProgram(ShellSessionProgram.class, createVersion(1, 0, 0)));
        addContentFile(fileSystem, "bin/desktops.exe", superuser, "r--xr--xr--x", createProgram(DesktopSessionProgram.class, createVersion(1, 0, 0)));

        // Generate system programs
        addContentFile(fileSystem, "bin/cd.exe", superuser, "r--xr--xr--x", createProgram(ChangeDirectoryProgram.class, createVersion(1, 0, 0)));
        addContentFile(fileSystem, "bin/ls.exe", superuser, "r--xr--xr--x", createProgram(ListFilesProgram.class, createVersion(1, 0, 0)));
        addContentFile(fileSystem, "bin/rights.exe", superuser, "r--xr--xr--x", createProgram(FileRightsProgram.class, createVersion(1, 0, 0)));
        addContentFile(fileSystem, "bin/fc.exe", superuser, "r--xr--xr--x", createProgram(FileContentProgram.class, createVersion(1, 0, 0)));
        addContentFile(fileSystem, "bin/fmk.exe", superuser, "r--xr--xr--x", createProgram(MakeFileProgram.class, createVersion(1, 0, 0)));
        addContentFile(fileSystem, "bin/fdel.exe", superuser, "r--xr--xr--x", createProgram(DeleteFileProgram.class, createVersion(1, 0, 0)));

        addContentFile(fileSystem, "bin/terminal.exe", superuser, "r--xr--xr--x", createProgram(TerminalProgram.class, createVersion(1, 0, 0)));
        addContentFile(fileSystem, "bin/sysviewer.exe", superuser, "r--xr--xr--x", createProgram(SystemViewerProgram.class, createVersion(1, 0, 0)));

        // Generate environment
        Environment environment = new Environment();
        environment.addVariable(new EnvironmentVariable("PATH", "/system/bin:/user/bin"));
        addContentFile(fileSystem, "etc/environment.cfg", superuser, "rw--r---r---", environment.toString());

        // Generate kernel module configuration
        StringBuilder kernelModules = new StringBuilder();
        kernelModules.append("bin/filesysd.exe").append("\n");
        addContentFile(fileSystem, "etc/kernelmodules.cfg", superuser, "rw--r---r---", kernelModules.toString());
    }

    private static Program createProgram(Class<? extends ProgramExecutor> executorClass, Version version) throws FunctionExecutionException {

        try {
            Program program = new Program();
            program.setLocked(false);
            program.get(Program.SET_VERSION).invoke(version);
            program.get(Program.SET_EXECUTOR_CLASS).invoke(executorClass);
            return program;
        } catch (ExecutorInvocationException e) {
            LOGGER.error("Unexpected exception during the initialization of a new program object", e);
        }

        return null;
    }

    // Temporary method for generating some unnecessary programs and personal files
    private static void addUserFiles(FileSystem fileSystem, UserManager userManager) throws FunctionExecutionException {

        User superuser = userManager.getSuperuser();

        // Generate other programs
        // Nothing here yet

        // Generate home directories
        addDirectory(fileSystem, "homes", superuser, "r---r---r---");
        for (User user : userManager.getUsers()) {
            if (!user.equals(userManager.getSuperuser())) {
                addDirectory(fileSystem, "homes/" + user.getName(), user, "rwdx--------");
            }
        }
    }

    private static void addDirectory(FileSystem fileSystem, String path, User owner, String rights) throws FunctionExecutionException {

        Directory directory = new Directory();
        directory.get(File.SET_OWNER).invoke(owner);
        directory.get(File.GET_RIGHTS).invoke().get(FileRights.FROM_STRING).invoke(rights);

        fileSystem.get(FileSystem.ADD_FILE).invoke(directory, path);
    }

    private static void addContentFile(FileSystem fileSystem, String path, User owner, String rights, Object content) throws FunctionExecutionException {

        ContentFile file = new ContentFile();
        file.get(File.SET_OWNER).invoke(owner);
        file.get(File.GET_RIGHTS).invoke().get(FileRights.FROM_STRING).invoke(rights);
        file.get(ContentFile.SET_CONTENT).invoke(content);

        fileSystem.get(FileSystem.ADD_FILE).invoke(file, path);
    }

    /**
     * Generates the given amount of {@link MemberGroup}s randomly.
     * 
     * @param world The {@link World} the new {@link MemberGroup}s will be in.
     * @param amount The amount of {@link MemberGroup}s the generator should generate.
     * @param random The {@link RandomPool} which is used for generating the {@link MemberGroup}s.
     * @return The generated list of {@link MemberGroup}s.
     */
    public static List<MemberGroup> generateMemberGroups(World world, int amount, RandomPool random) {

        List<MemberGroup> groups = new ArrayList<MemberGroup>();

        for (int counter = 0; counter < amount; counter++) {
            groups.add(new MemberGroup());
        }

        return groups;
    }

    /**
     * Generates the given amount of {@link Member}s randomly.
     * 
     * @param world The {@link World} the new {@link Member}s will be in.
     * @param amount The amount of {@link Member}s the generator should generate.
     * @param random The {@link RandomPool} which is used for generating the {@link Member}s.
     * @return The generated list of {@link Member}s.
     */
    public static List<Member> generateMembers(World world, int amount, RandomPool random) {

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

        List<Member> members = generateMembers(computers.get(0).getWorld(), computers.size(), random);

        for (int counter = 0; counter < members.size(); counter++) {
            groups.get(random.nextInt(groups.size())).addMember(members.get(counter));
            members.get(counter).setComputer(computers.get(counter));
        }

        return members;
    }

    private SimulationGenerator() {

    }

}
