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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.quartercode.disconnected.server.util.ProbabilityUtil;
import com.quartercode.disconnected.server.util.RandomPool;
import com.quartercode.disconnected.server.world.World;
import com.quartercode.disconnected.server.world.comp.ByteUnit;
import com.quartercode.disconnected.server.world.comp.Computer;
import com.quartercode.disconnected.server.world.comp.Version;
import com.quartercode.disconnected.server.world.comp.file.ContentFile;
import com.quartercode.disconnected.server.world.comp.file.File;
import com.quartercode.disconnected.server.world.comp.file.FileAddAction;
import com.quartercode.disconnected.server.world.comp.file.FileRights;
import com.quartercode.disconnected.server.world.comp.file.FileSystem;
import com.quartercode.disconnected.server.world.comp.file.FileSystemModule;
import com.quartercode.disconnected.server.world.comp.file.FileSystemModule.KnownFileSystem;
import com.quartercode.disconnected.server.world.comp.file.FileUtils;
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
import com.quartercode.disconnected.server.world.comp.net.NetID;
import com.quartercode.disconnected.server.world.comp.os.CommonFiles;
import com.quartercode.disconnected.server.world.comp.os.Configuration;
import com.quartercode.disconnected.server.world.comp.os.EnvironmentVariable;
import com.quartercode.disconnected.server.world.comp.os.OperatingSystem;
import com.quartercode.disconnected.server.world.comp.os.Session;
import com.quartercode.disconnected.server.world.comp.os.User;
import com.quartercode.disconnected.server.world.comp.program.Program;
import com.quartercode.disconnected.server.world.comp.program.ProgramExecutor;
import com.quartercode.disconnected.server.world.comp.program.ProgramUtils;
import com.quartercode.disconnected.server.world.comp.program.general.FileCreateProgram;
import com.quartercode.disconnected.server.world.comp.program.general.FileListProgram;
import com.quartercode.disconnected.server.world.comp.program.general.FileRemoveProgram;
import com.quartercode.disconnected.server.world.general.Location;

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

        Backbone backbone = world.get(World.BACKBONE).get();
        for (Computer computer : generateComputers(random, computers, backbone)) {
            world.get(World.COMPUTERS).add(computer);
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
                ignoreLocations.add(computer.get(Computer.LOCATION).get());
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
                List<Hardware> netInterfaces = getHardwareByType(computer.get(Computer.HARDWARE).get(), RouterNetInterface.class);
                lastRouter = (RouterNetInterface) netInterfaces.get(0);
                // Connect the router to the backbone
                backbone.get(Backbone.CHILDREN).add(lastRouter);
                // Set the router's subnet
                lastRouter.get(RouterNetInterface.SUBNET).set(lastSubnet);
            } else {
                List<Hardware> netInterfaces = getHardwareByType(computer.get(Computer.HARDWARE).get(), NodeNetInterface.class);
                NodeNetInterface netInterface = (NodeNetInterface) netInterfaces.get(0);
                // Connect the computer to its router
                netInterface.get(NodeNetInterface.CONNECTION).set(lastRouter);
                // Set the interface's net id
                NetID netId = new NetID();
                netId.get(NetID.SUBNET).set(lastSubnet);
                netId.get(NetID.ID).set(lastNetID);
                netInterface.get(NodeNetInterface.NET_ID).set(netId);
            }

            computer.get(Computer.LOCATION).set(locations.get(index));
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
        mainboard.get(Mainboard.NAME).set("MB XYZ 2000 Pro");
        mainboard.get(Mainboard.SLOTS).add(generateMainboardSlot(CPU.class));
        mainboard.get(Mainboard.SLOTS).add(generateMainboardSlot(RAM.class));
        mainboard.get(Mainboard.SLOTS).add(generateMainboardSlot(HardDrive.class));
        mainboard.get(Mainboard.SLOTS).add(generateMainboardSlot(HardDrive.class));
        mainboard.get(Mainboard.SLOTS).add(generateMainboardSlot(NodeNetInterface.class));
        computer.get(Computer.HARDWARE).add(mainboard);

        CPU cpu = new CPU();
        cpu.get(CPU.NAME).set("Intel Core i7-4950HQ");
        cpu.get(CPU.THREADS).set(8);
        cpu.get(CPU.FREQUENCY).set(2400000000L);
        computer.get(Computer.HARDWARE).add(cpu);

        RAM ram = new RAM();
        ram.get(RAM.NAME).set("EpicRAM 4194304");
        ram.get(RAM.SIZE).set(ByteUnit.BYTE.convert(4, ByteUnit.MEGABYTE));
        ram.get(RAM.FREQUENCY).set(1600000000L);
        computer.get(Computer.HARDWARE).add(ram);

        Hardware netInterface = null;
        if (router) {
            netInterface = new RouterNetInterface();
            netInterface.get(RouterNetInterface.NAME).set("RNI FiberScore Ultimate");
        } else {
            netInterface = new NodeNetInterface();
            netInterface.get(NodeNetInterface.NAME).set("NNI FiberScore Ultimate");
        }
        computer.get(Computer.HARDWARE).add(netInterface);

        HardDrive systemMedium = new HardDrive();
        systemMedium.get(HardDrive.NAME).set("TheHardDrive 1TB");
        FileSystem systemFs = systemMedium.get(HardDrive.FILE_SYSTEM).get();
        systemFs.get(FileSystem.SIZE).set(ByteUnit.BYTE.convert(1, ByteUnit.TERABYTE));
        computer.get(Computer.HARDWARE).add(systemMedium);

        HardDrive userMedium = new HardDrive();
        userMedium.get(HardDrive.NAME).set("TheHardDrive 1TB");
        FileSystem userFs = userMedium.get(HardDrive.FILE_SYSTEM).get();
        userFs.get(FileSystem.SIZE).set(ByteUnit.BYTE.convert(1, ByteUnit.TERABYTE));
        computer.get(Computer.HARDWARE).add(userMedium);

        for (MainboardSlot slot : getHardwareByType(computer.get(Computer.HARDWARE).get(), Mainboard.class).get(0).get(Mainboard.SLOTS).get()) {
            Hardware useHardware = null;
            for (Hardware hardware : computer.get(Computer.HARDWARE).get()) {
                if (!hardware.equals(mainboard)) {
                    if (hardware.getClass().isAnnotationPresent(NeedsMainboardSlot.class) && slot.get(MainboardSlot.TYPE).get().isInstance(hardware)) {
                        useHardware = hardware;
                        break;
                    }
                }
            }

            if (useHardware != null) {
                slot.get(MainboardSlot.CONTENT).set(useHardware);
            }
        }

        OperatingSystem operatingSystem = new OperatingSystem();
        operatingSystem.get(OperatingSystem.NAME).set("Frames");
        operatingSystem.get(OperatingSystem.VERSION).set(createVersion(3, 7, 65));
        computer.get(Computer.OS).set(operatingSystem);

        // Generate superuser object
        User superuser = new User();
        superuser.get(User.NAME).set(User.SUPERUSER_NAME);

        // Generate debug file systems
        FileSystemModule fsModule = operatingSystem.get(OperatingSystem.FS_MODULE).get();
        addKnownFs(fsModule, systemMedium.get(HardDrive.FILE_SYSTEM).get(), CommonFiles.SYSTEM_MOUNTPOINT);
        addKnownFs(fsModule, userMedium.get(HardDrive.FILE_SYSTEM).get(), CommonFiles.USER_MOUNTPOINT);

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

    private static Version createVersion(int major, int minor, int revision) {

        Version version = new Version();
        version.get(Version.MAJOR).set(major);
        version.get(Version.MINOR).set(major);
        version.get(Version.REVISION).set(revision);
        return version;
    }

    private static MainboardSlot generateMainboardSlot(Class<? extends Hardware> type) {

        MainboardSlot slot = new MainboardSlot();
        slot.get(MainboardSlot.TYPE).set(type);
        return slot;
    }

    private static KnownFileSystem addKnownFs(FileSystemModule fsModule, FileSystem fileSystem, String mountpoint) {

        KnownFileSystem known = new KnownFileSystem();
        known.get(KnownFileSystem.FILE_SYSTEM).set(fileSystem);
        known.get(KnownFileSystem.MOUNTPOINT).set(mountpoint);
        fsModule.get(FileSystemModule.KNOWN_FS).add(known);

        return known;
    }

    // Temporary method for generating the kernel and some system programs
    private static void addSystemFiles(FileSystem fileSystem, User superuser) {

        // Add session program
        addProgramFile(fileSystem, superuser, Session.class, createVersion(1, 0, 0));

        // Add system programs
        addProgramFile(fileSystem, superuser, FileListProgram.class, createVersion(1, 0, 0));
        addProgramFile(fileSystem, superuser, FileCreateProgram.class, createVersion(1, 0, 0));
        addProgramFile(fileSystem, superuser, FileRemoveProgram.class, createVersion(1, 0, 0));
    }

    private static void addProgramFile(FileSystem fileSystem, User superuser, Class<? extends ProgramExecutor> executor, Version version) {

        Program program = new Program();
        program.get(Program.VERSION).set(version);
        program.get(Program.EXECUTOR_CLASS).set(executor);

        addContentFile(fileSystem, FileUtils.getComponents(ProgramUtils.getCommonLocation(executor))[1], superuser, "r--xr--xr--x", program);
    }

    // Temporary method for generating some unnecessary programs and personal files
    private static void addUserFiles(FileSystem fileSystem, User superuser) {

        // Generate basic user config
        Configuration userConfig = new Configuration();
        userConfig.get(Configuration.ENTRIES).add(superuser);
        addContentFile(fileSystem, FileUtils.getComponents(CommonFiles.USER_CONFIG)[1], superuser, "rw----------", userConfig);

        // Generate basic environment config
        Configuration envConfig = new Configuration();
        EnvironmentVariable pathVariable = new EnvironmentVariable();
        pathVariable.get(EnvironmentVariable.NAME).set("PATH");
        pathVariable.get(EnvironmentVariable.SET_VALUE_LIST).invoke(Arrays.asList(CommonFiles.SYS_BIN_DIR, CommonFiles.USER_BIN_DIR));
        envConfig.get(Configuration.ENTRIES).add(pathVariable);
        addContentFile(fileSystem, FileUtils.getComponents(CommonFiles.ENVIRONMENT_CONFIG)[1], superuser, "rw--r---r---", envConfig);
    }

    private static ContentFile createContentFile(User owner, String rights, Object content) {

        ContentFile file = new ContentFile();
        file.get(File.OWNER).set(owner);
        file.get(File.RIGHTS).get().get(FileRights.FROM_STRING).invoke(rights);
        file.get(ContentFile.CONTENT).set(content);
        return file;
    }

    private static ContentFile addContentFile(FileSystem fileSystem, String path, User owner, String rights, Object content) {

        ContentFile file = createContentFile(owner, rights, content);
        fileSystem.get(FileSystem.CREATE_ADD_FILE).invoke(file, path).get(FileAddAction.EXECUTE).invoke();
        return file;
    }

    private WorldGenerator() {

    }

}
