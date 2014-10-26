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

package com.quartercode.disconnected.server.world.gen;

import static com.quartercode.disconnected.server.world.comp.program.ProgramCommonLocationMapper.getCommonLocation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.quartercode.disconnected.server.util.ProbabilityUtil;
import com.quartercode.disconnected.server.util.RandomPool;
import com.quartercode.disconnected.server.world.World;
import com.quartercode.disconnected.server.world.comp.Computer;
import com.quartercode.disconnected.server.world.comp.file.ContentFile;
import com.quartercode.disconnected.server.world.comp.file.File;
import com.quartercode.disconnected.server.world.comp.file.FileAddAction;
import com.quartercode.disconnected.server.world.comp.file.FileSystem;
import com.quartercode.disconnected.server.world.comp.file.FileSystemModule;
import com.quartercode.disconnected.server.world.comp.file.FileSystemModule.KnownFileSystem;
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
import com.quartercode.disconnected.server.world.comp.os.Configuration;
import com.quartercode.disconnected.server.world.comp.os.EnvironmentVariable;
import com.quartercode.disconnected.server.world.comp.os.OperatingSystem;
import com.quartercode.disconnected.server.world.comp.os.Session;
import com.quartercode.disconnected.server.world.comp.os.User;
import com.quartercode.disconnected.server.world.comp.program.Program;
import com.quartercode.disconnected.server.world.comp.program.ProgramExecutor;
import com.quartercode.disconnected.server.world.comp.program.general.FileManagerProgram;
import com.quartercode.disconnected.shared.comp.ByteUnit;
import com.quartercode.disconnected.shared.comp.Version;
import com.quartercode.disconnected.shared.comp.file.CommonFiles;
import com.quartercode.disconnected.shared.comp.file.FileRights;
import com.quartercode.disconnected.shared.comp.file.PathUtils;
import com.quartercode.disconnected.shared.comp.net.NetID;
import com.quartercode.disconnected.shared.general.Location;

/**
 * The world generator utility generates {@link World}s and parts of worlds.
 * 
 * @see World
 */
public class WorldGenerator {

    /**
     * Generates a new {@link World} with the given amount of {@link Computer}s using the given {@link RandomPool}.
     * 
     * @param random A {@link RandomPool} that is used for randomizing the generation process.
     * @param computers The amount of computers the generator should generate.
     * @return The newly generated world object.
     */
    public static World generateWorld(RandomPool random, int computers) {

        World world = new World();

        Backbone backbone = world.getObj(World.BACKBONE);
        for (Computer computer : generateComputers(random, computers, backbone)) {
            world.addCol(World.COMPUTERS, computer);
        }

        return world;
    }

    /**
     * Generates the given amount of {@link Computer}s randomly.
     * 
     * @param random The {@link RandomPool} which is used for generating the computers.
     * @param amount The amount of computers the generator should generate.
     * @return The generated list of computers.
     */
    public static List<Computer> generateComputers(RandomPool random, int amount, Backbone backbone) {

        return generateComputers(random, amount, backbone, null);
    }

    /**
     * Generates the given amount of {@link Computer}s randomly while excluding the {@link Location}s of the given computerss.
     * 
     * @param random The {@link RandomPool} which is used for generating the computers.
     * @param amount The amount of computers the generator should generate.
     * @param backbone The {@link Backbone} that should be used for connecting some routers to the internet.
     * @param ignore There won't be any computers with one of those locations.
     * @return The generated list of computers.
     */
    public static List<Computer> generateComputers(RandomPool random, int amount, Backbone backbone, List<Computer> ignore) {

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
            else if (locations.size() - index > 3 && index - lastRouterIndex > 3 && ProbabilityUtil.gen(0.5F, random)) {
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
                List<Hardware> netInterfaces = getHardwareByType(computer.getCol(Computer.HARDWARE), RouterNetInterface.class);
                lastRouter = (RouterNetInterface) netInterfaces.get(0);
                // Connect the router to the backbone
                backbone.addCol(Backbone.CHILDREN, lastRouter);
                // Set the router's subnet
                lastRouter.setObj(RouterNetInterface.SUBNET, lastSubnet);
            } else {
                List<Hardware> netInterfaces = getHardwareByType(computer.getCol(Computer.HARDWARE), NodeNetInterface.class);
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
        mainboard.addCol(Mainboard.SLOTS, generateMainboardSlot(CPU.class));
        mainboard.addCol(Mainboard.SLOTS, generateMainboardSlot(RAM.class));
        mainboard.addCol(Mainboard.SLOTS, generateMainboardSlot(HardDrive.class));
        mainboard.addCol(Mainboard.SLOTS, generateMainboardSlot(HardDrive.class));
        mainboard.addCol(Mainboard.SLOTS, generateMainboardSlot(NodeNetInterface.class));
        computer.addCol(Computer.HARDWARE, mainboard);

        CPU cpu = new CPU();
        cpu.setObj(CPU.NAME, "Intel Core i7-4950HQ");
        cpu.setObj(CPU.THREADS, 8);
        cpu.setObj(CPU.FREQUENCY, 2400000000L);
        computer.addCol(Computer.HARDWARE, cpu);

        RAM ram = new RAM();
        ram.setObj(RAM.NAME, "EpicRAM 4194304");
        ram.setObj(RAM.SIZE, ByteUnit.BYTE.convert(4, ByteUnit.MEGABYTE));
        ram.setObj(RAM.FREQUENCY, 1600000000L);
        computer.addCol(Computer.HARDWARE, ram);

        Hardware netInterface = null;
        if (router) {
            netInterface = new RouterNetInterface();
            netInterface.setObj(RouterNetInterface.NAME, "RNI FiberScore Ultimate");
        } else {
            netInterface = new NodeNetInterface();
            netInterface.setObj(NodeNetInterface.NAME, "NNI FiberScore Ultimate");
        }
        computer.addCol(Computer.HARDWARE, netInterface);

        HardDrive systemMedium = new HardDrive();
        systemMedium.setObj(HardDrive.NAME, "TheHardDrive 1TB");
        FileSystem systemFs = systemMedium.getObj(HardDrive.FILE_SYSTEM);
        systemFs.setObj(FileSystem.SIZE, ByteUnit.BYTE.convert(1, ByteUnit.TERABYTE));
        computer.addCol(Computer.HARDWARE, systemMedium);

        HardDrive userMedium = new HardDrive();
        userMedium.setObj(HardDrive.NAME, "TheHardDrive 1TB");
        FileSystem userFs = userMedium.getObj(HardDrive.FILE_SYSTEM);
        userFs.setObj(FileSystem.SIZE, ByteUnit.BYTE.convert(1, ByteUnit.TERABYTE));
        computer.addCol(Computer.HARDWARE, userMedium);

        for (MainboardSlot slot : getHardwareByType(computer.getCol(Computer.HARDWARE), Mainboard.class).get(0).getCol(Mainboard.SLOTS)) {
            Hardware useHardware = null;
            for (Hardware hardware : computer.getCol(Computer.HARDWARE)) {
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

        OperatingSystem operatingSystem = new OperatingSystem();
        operatingSystem.setObj(OperatingSystem.NAME, "Frames");
        operatingSystem.setObj(OperatingSystem.VERSION, new Version(3, 7, 65));
        computer.setObj(Computer.OS, operatingSystem);

        // Generate superuser object
        User superuser = new User();
        superuser.setObj(User.NAME, User.SUPERUSER_NAME);

        // Generate debug file systems
        FileSystemModule fsModule = operatingSystem.getObj(OperatingSystem.FS_MODULE);
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

    private static KnownFileSystem addKnownFs(FileSystemModule fsModule, FileSystem fileSystem, String mountpoint) {

        KnownFileSystem known = new KnownFileSystem();
        known.setObj(KnownFileSystem.FILE_SYSTEM, fileSystem);
        known.setObj(KnownFileSystem.MOUNTPOINT, mountpoint);
        fsModule.addCol(FileSystemModule.KNOWN_FS, known);

        return known;
    }

    // Temporary method for generating the kernel and some system programs
    private static void addSystemFiles(FileSystem fileSystem, User superuser) {

        // Add system programs
        addProgramFile(fileSystem, superuser, Session.class, new Version(1, 0, 0));

        // Add general programs
        addProgramFile(fileSystem, superuser, FileManagerProgram.class, new Version(1, 0, 0));
    }

    private static void addProgramFile(FileSystem fileSystem, User superuser, Class<? extends ProgramExecutor> executor, Version version) {

        Program program = new Program();
        program.setObj(Program.VERSION, version);
        program.setObj(Program.EXECUTOR_CLASS, executor);

        addContentFile(fileSystem, PathUtils.splitAfterMountpoint(getCommonLocation(executor).toString())[1], superuser, "o:rx", program);
    }

    // Temporary method for generating some unnecessary programs and personal files
    private static void addUserFiles(FileSystem fileSystem, User superuser) {

        // Generate basic user config
        Configuration userConfig = new Configuration();
        userConfig.addCol(Configuration.ENTRIES, superuser);
        addContentFile(fileSystem, PathUtils.splitAfterMountpoint(CommonFiles.USER_CONFIG)[1], superuser, "o:r", userConfig);

        // Generate basic environment config
        Configuration envConfig = new Configuration();
        EnvironmentVariable pathVariable = new EnvironmentVariable();
        pathVariable.setObj(EnvironmentVariable.NAME, "PATH");
        pathVariable.invoke(EnvironmentVariable.SET_VALUE_LIST, Arrays.asList(CommonFiles.SYS_BIN_DIR, CommonFiles.USER_BIN_DIR));
        envConfig.addCol(Configuration.ENTRIES, pathVariable);
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

    private WorldGenerator() {

    }

}
