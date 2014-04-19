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

package com.quartercode.disconnected.sim.gen;

import java.util.ArrayList;
import java.util.List;
import com.quartercode.disconnected.sim.Simulation;
import com.quartercode.disconnected.util.RandomPool;
import com.quartercode.disconnected.world.Location;
import com.quartercode.disconnected.world.World;
import com.quartercode.disconnected.world.comp.ByteUnit;
import com.quartercode.disconnected.world.comp.Computer;
import com.quartercode.disconnected.world.comp.Version;
import com.quartercode.disconnected.world.comp.file.ContentFile;
import com.quartercode.disconnected.world.comp.file.File;
import com.quartercode.disconnected.world.comp.file.FileAddAction;
import com.quartercode.disconnected.world.comp.file.FileRights;
import com.quartercode.disconnected.world.comp.file.FileSystem;
import com.quartercode.disconnected.world.comp.file.FileSystemModule;
import com.quartercode.disconnected.world.comp.file.FileSystemModule.KnownFileSystem;
import com.quartercode.disconnected.world.comp.file.FileUtils;
import com.quartercode.disconnected.world.comp.hardware.CPU;
import com.quartercode.disconnected.world.comp.hardware.HardDrive;
import com.quartercode.disconnected.world.comp.hardware.Hardware;
import com.quartercode.disconnected.world.comp.hardware.Mainboard;
import com.quartercode.disconnected.world.comp.hardware.Mainboard.MainboardSlot;
import com.quartercode.disconnected.world.comp.hardware.Mainboard.NeedsMainboardSlot;
import com.quartercode.disconnected.world.comp.hardware.NetworkInterface;
import com.quartercode.disconnected.world.comp.hardware.RAM;
import com.quartercode.disconnected.world.comp.os.CommonFiles;
import com.quartercode.disconnected.world.comp.os.Configuration;
import com.quartercode.disconnected.world.comp.os.EnvironmentVariable;
import com.quartercode.disconnected.world.comp.os.OperatingSystem;
import com.quartercode.disconnected.world.comp.os.Session;
import com.quartercode.disconnected.world.comp.os.User;
import com.quartercode.disconnected.world.comp.program.Program;
import com.quartercode.disconnected.world.comp.program.ProgramExecutor;
import com.quartercode.disconnected.world.comp.program.general.FileCreateProgram;

/**
 * This utility class generates {@link Simulation}s, {@link World}s and parts of worlds.
 * 
 * @see Simulation
 * @see World
 */
public class SimulationGenerator {

    /**
     * Generates a new {@link Simulation} with the given settings.
     * This basically uses {@link #generateWorld(int, Simulation, RandomPool)} for creating a new world.
     * 
     * @param computers The amount of {@link Computer}s the generator should generate.
     * @param random The {@link RandomPool} to use for the new simulation.
     * @return The generated simulation object.
     */
    public static Simulation generateSimulation(int computers, RandomPool random) {

        Simulation simulation = new Simulation(random);
        simulation.setWorld(generateWorld(computers, simulation, random));
        return simulation;
    }

    /**
     * Generates a new {@link World} with the given settings for the given {@link Simulation}.
     * If you don't want to use the world in a simulation, you can set it to <code>null</code>.
     * 
     * @param computers The amount of {@link Computer}s the generator should generate.
     * @param simulation The simulation the new world is generated for. This may be <code>null</code>.
     * @param random The {@link RandomPool} which is used for generating the world.
     * @return The generated world object.
     */
    public static World generateWorld(int computers, Simulation simulation, RandomPool random) {

        World world = new World(simulation);

        for (Computer computer : generateComputers(computers, random)) {
            world.get(World.COMPUTERS).add(computer);
        }

        return world;
    }

    /**
     * Generates the given amount of {@link Computer}s randomly.
     * 
     * @param amount The amount of computers the generator should generate.
     * @param random The {@link RandomPool} which is used for generating the computers.
     * @return The generated list of computers.
     */
    public static List<Computer> generateComputers(int amount, RandomPool random) {

        return generateComputers(amount, null, random);
    }

    /**
     * Generates the given amount of {@link Computer}s randomly while excluding the {@link Location}s of the given computerss.
     * 
     * @param amount The amount of computers the generator should generate.
     * @param ignore There won't be any computers with one of those locations.
     * @param random The {@link RandomPool} which is used for generating the computers.
     * @return The generated list of computers.
     */
    public static List<Computer> generateComputers(int amount, List<Computer> ignore, RandomPool random) {

        List<Computer> computers = new ArrayList<Computer>();

        List<Location> ignoreLocations = new ArrayList<Location>();
        if (ignore != null) {
            for (Computer computer : ignore) {
                ignoreLocations.add(computer.get(Computer.LOCATION).get());
            }
        }

        for (Location location : LocationGenerator.generateLocations(amount, ignoreLocations, random)) {
            Computer computer = generateComputer();
            computer.get(Computer.LOCATION).set(location);
            computers.add(computer);
        }

        return computers;
    }

    /**
     * Generates a typical {@link Computer} with common hardware and the default file systems.
     * 
     * @return A newly generated computer.
     */
    public static Computer generateComputer() {

        Computer computer = new Computer();

        Mainboard mainboard = new Mainboard();
        mainboard.get(Hardware.NAME).set("MB XYZ 2000 Pro");
        mainboard.get(Mainboard.SLOTS).add(generateMainboardSlot(CPU.class));
        mainboard.get(Mainboard.SLOTS).add(generateMainboardSlot(RAM.class));
        mainboard.get(Mainboard.SLOTS).add(generateMainboardSlot(HardDrive.class));
        mainboard.get(Mainboard.SLOTS).add(generateMainboardSlot(HardDrive.class));
        mainboard.get(Mainboard.SLOTS).add(generateMainboardSlot(NetworkInterface.class));
        computer.get(Computer.HARDWARE).add(mainboard);

        CPU cpu = new CPU();
        cpu.get(Hardware.NAME).set("Intel Core i7-4950HQ");
        cpu.get(CPU.THREADS).set(8);
        cpu.get(CPU.FREQUENCY).set(2400000000L);
        computer.get(Computer.HARDWARE).add(cpu);

        RAM ram = new RAM();
        ram.get(Hardware.NAME).set("EpicRAM 4194304");
        ram.get(RAM.SIZE).set(ByteUnit.BYTE.convert(4, ByteUnit.MEGABYTE));
        ram.get(RAM.FREQUENCY).set(1600000000L);
        computer.get(Computer.HARDWARE).add(ram);

        NetworkInterface networkInterface = new NetworkInterface();
        networkInterface.get(Hardware.NAME).set("NI FiberScore Ultimate");
        computer.get(Computer.HARDWARE).add(networkInterface);

        HardDrive systemMedium = new HardDrive();
        systemMedium.get(Hardware.NAME).set("TheHardDrive 1TB");
        FileSystem systemFs = systemMedium.get(HardDrive.FILE_SYSTEM).get();
        systemFs.get(FileSystem.SIZE).set(ByteUnit.BYTE.convert(1, ByteUnit.TERABYTE));
        computer.get(Computer.HARDWARE).add(systemMedium);

        HardDrive userMedium = new HardDrive();
        userMedium.get(Hardware.NAME).set("TheHardDrive 1TB");
        FileSystem userFs = userMedium.get(HardDrive.FILE_SYSTEM).get();
        userFs.get(FileSystem.SIZE).set(ByteUnit.BYTE.convert(1, ByteUnit.TERABYTE));
        computer.get(Computer.HARDWARE).add(userMedium);

        for (MainboardSlot slot : getHardwareByType(computer.get(Computer.HARDWARE).get(), Mainboard.class).get(0).get(Mainboard.SLOTS).get()) {
            Hardware useHardware = null;
            for (Hardware hardware : computer.get(Computer.HARDWARE).get()) {
                if (!hardware.equals(mainboard)) {
                    if (hardware.getClass().isAnnotationPresent(NeedsMainboardSlot.class) && slot.get(MainboardSlot.TYPE).get().isAssignableFrom(hardware.getClass())) {
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

        List<Hardware> hardwareByType = new ArrayList<Hardware>();
        for (Hardware hardwarePart : availableHardware) {
            if (type.isAssignableFrom(hardwarePart.getClass())) {
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

        // Make a shortcut for the bin directory
        String binDir = FileUtils.getComponents(CommonFiles.SYS_BIN_DIR)[1] + File.SEPARATOR;

        // Add session program
        addContentFile(fileSystem, binDir + "session.exe", superuser, "r--xr--xr--x", createProgram(Session.class, createVersion(1, 0, 0)));

        // Add system programs
        addContentFile(fileSystem, binDir + "filecreate.exe", superuser, "r--xr--xr--x", createProgram(FileCreateProgram.class, createVersion(1, 0, 0)));
    }

    private static Program createProgram(Class<? extends ProgramExecutor> executorClass, Version version) {

        Program program = new Program();
        program.get(Program.VERSION).set(version);
        program.get(Program.EXECUTOR_CLASS).set(executorClass);
        return program;
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
        pathVariable.get(EnvironmentVariable.VALUE).set("/system/bin" + EnvironmentVariable.LIST_SEPARATOR + "/user/bin");
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

    private SimulationGenerator() {

    }

}
