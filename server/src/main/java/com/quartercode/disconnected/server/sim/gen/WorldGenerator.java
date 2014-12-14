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

package com.quartercode.disconnected.server.sim.gen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Set;
import com.quartercode.disconnected.server.registry.ServerRegistries;
import com.quartercode.disconnected.server.registry.VulnSource;
import com.quartercode.disconnected.server.registry.WorldProgram;
import com.quartercode.disconnected.server.util.ProbabilityUtils;
import com.quartercode.disconnected.server.world.World;
import com.quartercode.disconnected.server.world.comp.Computer;
import com.quartercode.disconnected.server.world.comp.config.Config;
import com.quartercode.disconnected.server.world.comp.file.ContentFile;
import com.quartercode.disconnected.server.world.comp.file.FSModule;
import com.quartercode.disconnected.server.world.comp.file.FSModule.KnownFS;
import com.quartercode.disconnected.server.world.comp.file.File;
import com.quartercode.disconnected.server.world.comp.file.FileAddAction;
import com.quartercode.disconnected.server.world.comp.file.FileSystem;
import com.quartercode.disconnected.server.world.comp.hardware.CPU;
import com.quartercode.disconnected.server.world.comp.hardware.HardDrive;
import com.quartercode.disconnected.server.world.comp.hardware.Hardware;
import com.quartercode.disconnected.server.world.comp.hardware.Mainboard;
import com.quartercode.disconnected.server.world.comp.hardware.Mainboard.MainboardSlot;
import com.quartercode.disconnected.server.world.comp.hardware.Mainboard.NeedsMainboardSlot;
import com.quartercode.disconnected.server.world.comp.hardware.NodeNetInterface;
import com.quartercode.disconnected.server.world.comp.hardware.RAM;
import com.quartercode.disconnected.server.world.comp.hardware.RouterNetInterface;
import com.quartercode.disconnected.server.world.comp.net.Backbone;
import com.quartercode.disconnected.server.world.comp.os.OS;
import com.quartercode.disconnected.server.world.comp.prog.EnvVariable;
import com.quartercode.disconnected.server.world.comp.prog.Program;
import com.quartercode.disconnected.server.world.comp.user.User;
import com.quartercode.disconnected.server.world.comp.vuln.VulnContainer;
import com.quartercode.disconnected.shared.util.registry.Registries;
import com.quartercode.disconnected.shared.util.registry.extra.NamedValueUtils;
import com.quartercode.disconnected.shared.world.comp.ByteUnit;
import com.quartercode.disconnected.shared.world.comp.Version;
import com.quartercode.disconnected.shared.world.comp.file.CommonFiles;
import com.quartercode.disconnected.shared.world.comp.file.FileRights;
import com.quartercode.disconnected.shared.world.comp.file.PathUtils;
import com.quartercode.disconnected.shared.world.comp.net.NetID;
import com.quartercode.disconnected.shared.world.general.Location;

/**
 * The world generator utility generates {@link World}s and parts of worlds.
 * 
 * @see World
 */
public class WorldGenerator {

    /**
     * Generates a new {@link World} with the given amount of {@link Computer}s using the given {@link Random} object.
     * 
     * @param random A random object that is used for randomizing the generation process.
     * @param computers The amount of computers the generator should generate.
     * @return The newly generated world object.
     */
    public static World generateWorld(Random random, int computers) {

        World world = new World();

        Backbone backbone = world.getObj(World.BACKBONE);
        for (Computer computer : generateComputers(random, computers, backbone)) {
            world.addToColl(World.COMPUTERS, computer);
        }

        return world;
    }

    /**
     * Generates the given amount of {@link Computer}s randomly.
     * 
     * @param random The {@link Random} object which is used for generating the computers.
     * @param amount The amount of computers the generator should generate.
     * @return The generated list of computers.
     */
    public static List<Computer> generateComputers(Random random, int amount, Backbone backbone) {

        return generateComputers(random, amount, backbone, null);
    }

    /**
     * Generates the given amount of {@link Computer}s randomly while excluding the {@link Location}s of the given computers.
     * 
     * @param random The {@link Random} object which is used for generating the computers.
     * @param amount The amount of computers the generator should generate.
     * @param backbone The {@link Backbone} that should be used for connecting some routers to the internet.
     * @param ignore There won't be any computers with one of those locations.
     * @return The generated list of computers.
     */
    public static List<Computer> generateComputers(Random random, int amount, Backbone backbone, List<Computer> ignore) {

        List<Computer> computers = new ArrayList<>();

        List<Location> ignoreLocations = new ArrayList<>();
        if (ignore != null) {
            for (Computer computer : ignore) {
                ignoreLocations.add(computer.getObj(Computer.LOCATION));
            }
        }

        List<Location> locations = LocationGenerator.generateLocations(amount, ignoreLocations, random);
        int lastRouterIndex = -1;
        RouterNetInterface lastRouter = null;
        int lastSubnet = -1;
        int lastNetID = -1;
        for (int index = 0; index < locations.size(); index++) {
            boolean router = false;
            // First computer must be a router
            if (lastRouterIndex < 0) {
                router = true;
                lastRouterIndex = index;
            }
            // Randomly generate routers
            else if (locations.size() - index > 3 && index - lastRouterIndex > 3 && ProbabilityUtils.gen(0.5F, random)) {
                router = true;
            }

            if (router) {
                lastRouterIndex = index;
                lastSubnet++;
                lastNetID = -1;
            } else {
                lastNetID++;
            }

            Computer computer = generateComputer(router);

            // Configure the network interfaces
            if (router) {
                List<Hardware> netInterfaces = getHardwareByType(computer.getColl(Computer.HARDWARE), RouterNetInterface.class);
                lastRouter = (RouterNetInterface) netInterfaces.get(0);
                // Connect the router to the backbone
                backbone.addToColl(Backbone.CHILDREN, lastRouter);
                // Set the router's subnet
                lastRouter.setObj(RouterNetInterface.SUBNET, lastSubnet);
            } else {
                List<Hardware> netInterfaces = getHardwareByType(computer.getColl(Computer.HARDWARE), NodeNetInterface.class);
                NodeNetInterface netInterface = (NodeNetInterface) netInterfaces.get(0);
                // Connect the computer to its router
                netInterface.setObj(NodeNetInterface.CONNECTION, lastRouter);
                // Set the interface's net id
                netInterface.setObj(NodeNetInterface.NET_ID, new NetID(lastSubnet, lastNetID));
            }

            computer.setObj(Computer.LOCATION, locations.get(index));
            computers.add(computer);
        }

        return computers;
    }

    /**
     * Generates a typical {@link Computer} or router with common hardware and the default file systems.
     * 
     * @param router Whether the generated computer is a router.
     * @return A newly generated computer.
     */
    public static Computer generateComputer(boolean router) {

        Computer computer = new Computer();

        Mainboard mainboard = new Mainboard();
        mainboard.setObj(Mainboard.NAME, "MB XYZ 2000 Pro");
        mainboard.addToColl(Mainboard.SLOTS, generateMainboardSlot(CPU.class));
        mainboard.addToColl(Mainboard.SLOTS, generateMainboardSlot(RAM.class));
        mainboard.addToColl(Mainboard.SLOTS, generateMainboardSlot(HardDrive.class));
        mainboard.addToColl(Mainboard.SLOTS, generateMainboardSlot(HardDrive.class));
        mainboard.addToColl(Mainboard.SLOTS, generateMainboardSlot(NodeNetInterface.class));
        computer.addToColl(Computer.HARDWARE, mainboard);

        CPU cpu = new CPU();
        cpu.setObj(CPU.NAME, "Intel Core i7-4950HQ");
        cpu.setObj(CPU.THREADS, 8);
        cpu.setObj(CPU.FREQUENCY, 2400000000L);
        computer.addToColl(Computer.HARDWARE, cpu);

        RAM ram = new RAM();
        ram.setObj(RAM.NAME, "EpicRAM 4194304");
        ram.setObj(RAM.SIZE, ByteUnit.BYTE.convert(4, ByteUnit.MEGABYTE));
        ram.setObj(RAM.FREQUENCY, 1600000000L);
        computer.addToColl(Computer.HARDWARE, ram);

        Hardware netInterface = null;
        if (router) {
            netInterface = new RouterNetInterface();
            netInterface.setObj(RouterNetInterface.NAME, "RNI FiberScore Ultimate");
        } else {
            netInterface = new NodeNetInterface();
            netInterface.setObj(NodeNetInterface.NAME, "NNI FiberScore Ultimate");
        }
        computer.addToColl(Computer.HARDWARE, netInterface);

        HardDrive systemMedium = new HardDrive();
        systemMedium.setObj(HardDrive.NAME, "TheHardDrive 1TB");
        FileSystem systemFs = systemMedium.getObj(HardDrive.FILE_SYSTEM);
        systemFs.setObj(FileSystem.SIZE, ByteUnit.BYTE.convert(1, ByteUnit.TERABYTE));
        computer.addToColl(Computer.HARDWARE, systemMedium);

        HardDrive userMedium = new HardDrive();
        userMedium.setObj(HardDrive.NAME, "TheHardDrive 1TB");
        FileSystem userFs = userMedium.getObj(HardDrive.FILE_SYSTEM);
        userFs.setObj(FileSystem.SIZE, ByteUnit.BYTE.convert(1, ByteUnit.TERABYTE));
        computer.addToColl(Computer.HARDWARE, userMedium);

        for (MainboardSlot slot : getHardwareByType(computer.getColl(Computer.HARDWARE), Mainboard.class).get(0).getColl(Mainboard.SLOTS)) {
            Hardware useHardware = null;
            for (Hardware hardware : computer.getColl(Computer.HARDWARE)) {
                if (!hardware.equals(mainboard)) {
                    if (hardware.getClass().isAnnotationPresent(NeedsMainboardSlot.class) && slot.getObj(MainboardSlot.TYPE).isInstance(hardware)) {
                        useHardware = hardware;
                        break;
                    }
                }
            }

            if (useHardware != null) {
                slot.setObj(MainboardSlot.CONTENT, useHardware);
            }
        }

        OS os = new OS();
        os.setObj(OS.NAME, "Frames");
        os.setObj(OS.VERSION, new Version(3, 7, 65));
        computer.setObj(Computer.OS, os);

        // Generate superuser object
        User superuser = new User();
        superuser.setObj(User.NAME, User.SUPERUSER_NAME);

        // Generate debug file systems
        FSModule fsModule = os.getObj(OS.FS_MODULE);
        addKnownFs(fsModule, systemMedium.getObj(HardDrive.FILE_SYSTEM), CommonFiles.SYSTEM_MOUNTPOINT);
        addKnownFs(fsModule, userMedium.getObj(HardDrive.FILE_SYSTEM), CommonFiles.USER_MOUNTPOINT);

        // Fill debug file systems
        addSystemFiles(systemFs, superuser);
        addUserFiles(userFs, superuser);

        return computer;
    }

    private static List<Hardware> getHardwareByType(List<Hardware> availableHardware, Class<? extends Hardware> type) {

        List<Hardware> hardwareByType = new ArrayList<>();
        for (Hardware hardwarePart : availableHardware) {
            if (type.isInstance(hardwarePart)) {
                hardwareByType.add(hardwarePart);
            }
        }

        return hardwareByType;
    }

    private static MainboardSlot generateMainboardSlot(Class<? extends Hardware> type) {

        MainboardSlot slot = new MainboardSlot();
        slot.setObj(MainboardSlot.TYPE, type);
        return slot;
    }

    private static KnownFS addKnownFs(FSModule fsModule, FileSystem fileSystem, String mountpoint) {

        KnownFS known = new KnownFS();
        known.setObj(KnownFS.FILE_SYSTEM, fileSystem);
        known.setObj(KnownFS.MOUNTPOINT, mountpoint);
        fsModule.addToColl(FSModule.KNOWN_FS, known);

        return known;
    }

    // Temporary method for generating the kernel and some system programs
    private static void addSystemFiles(FileSystem fileSystem, User superuser) {

        // Add system programs
        addProgramFile(fileSystem, superuser, "session", new Version(1, 0, 0));

        // Add general programs
        addProgramFile(fileSystem, superuser, "fileManager", new Version(1, 0, 0));
    }

    // Temporary method for generating some unnecessary programs and personal files
    private static void addUserFiles(FileSystem fileSystem, User superuser) {

        // Generate basic user config
        Config userConfig = new Config();
        userConfig.addToColl(Config.ENTRIES, superuser);
        addContentFile(fileSystem, PathUtils.splitAfterMountpoint(CommonFiles.USER_CONFIG)[1], superuser, "o:r", userConfig);

        // Generate basic environment config
        Config envConfig = new Config();
        EnvVariable pathVariable = new EnvVariable();
        pathVariable.setObj(EnvVariable.NAME, "PATH");
        pathVariable.invoke(EnvVariable.SET_VALUE_LIST, Arrays.asList(CommonFiles.SYS_BIN_DIR, CommonFiles.USER_BIN_DIR));
        envConfig.addToColl(Config.ENTRIES, pathVariable);
        addContentFile(fileSystem, PathUtils.splitAfterMountpoint(CommonFiles.ENVIRONMENT_CONFIG)[1], superuser, "o:r", envConfig);
    }

    private static ContentFile createContentFile(User owner, String rights, Object content) {

        ContentFile file = new ContentFile();
        file.setObj(File.OWNER, owner);
        file.setObj(File.RIGHTS, new FileRights(rights));
        file.setObj(ContentFile.CONTENT, content);
        return file;
    }

    private static ContentFile addContentFile(FileSystem fileSystem, String path, User owner, String rights, Object content) {

        ContentFile file = createContentFile(owner, rights, content);
        fileSystem.invoke(FileSystem.CREATE_ADD_FILE, file, path).invoke(FileAddAction.EXECUTE);
        return file;
    }

    private static void addProgramFile(FileSystem fileSystem, User superuser, String programName, Version version) {

        WorldProgram programData = NamedValueUtils.getByName(Registries.get(ServerRegistries.WORLD_PROGRAMS), programName);

        Program program = new Program();
        program.setObj(Program.NAME, programName);
        program.setObj(Program.VERSION, version);

        // Try to generate 3 vulnerabilities
        Set<VulnSource> vulnSources = Registries.get(ServerRegistries.VULN_SOURCES).getValuesByUsage("worldProgram", programData.getName());
        if (!vulnSources.isEmpty()) {
            program.getObj(Program.VULN_CONTAINER).invoke(VulnContainer.GENERATE_VULNS, vulnSources, 3);
        }

        String programPath = programData.getCommonLocation().toString();
        addContentFile(fileSystem, PathUtils.splitAfterMountpoint(programPath)[1], superuser, "o:rx", program);
    }

    private WorldGenerator() {

    }

}
