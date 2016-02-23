/*
 * This file is part of Disconnected.
 * Copyright (c) 2013 QuarterCode <http://quartercode.com/>
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

import static com.quartercode.disconnected.shared.world.comp.file.CommonFiles.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.apache.commons.lang3.Validate;
import com.quartercode.disconnected.server.registry.ServerRegistries;
import com.quartercode.disconnected.server.registry.VulnSource;
import com.quartercode.disconnected.server.registry.WorldProgram;
import com.quartercode.disconnected.server.util.ProbabilityUtils;
import com.quartercode.disconnected.server.world.World;
import com.quartercode.disconnected.server.world.comp.Computer;
import com.quartercode.disconnected.server.world.comp.config.Config;
import com.quartercode.disconnected.server.world.comp.file.ContentFile;
import com.quartercode.disconnected.server.world.comp.file.FileSystem;
import com.quartercode.disconnected.server.world.comp.file.FileSystemModule;
import com.quartercode.disconnected.server.world.comp.file.FileSystemModule.KnownFileSystem;
import com.quartercode.disconnected.server.world.comp.file.InvalidPathException;
import com.quartercode.disconnected.server.world.comp.file.OccupiedPathException;
import com.quartercode.disconnected.server.world.comp.file.OutOfSpaceException;
import com.quartercode.disconnected.server.world.comp.hardware.CPU;
import com.quartercode.disconnected.server.world.comp.hardware.HardDrive;
import com.quartercode.disconnected.server.world.comp.hardware.Hardware;
import com.quartercode.disconnected.server.world.comp.hardware.Mainboard;
import com.quartercode.disconnected.server.world.comp.hardware.Mainboard.MainboardSlot;
import com.quartercode.disconnected.server.world.comp.hardware.Mainboard.NeedsMainboardSlot;
import com.quartercode.disconnected.server.world.comp.hardware.NetInterface;
import com.quartercode.disconnected.server.world.comp.hardware.RAM;
import com.quartercode.disconnected.server.world.comp.net.NetNode;
import com.quartercode.disconnected.server.world.comp.net.Network;
import com.quartercode.disconnected.server.world.comp.net.nodes.BridgeNode;
import com.quartercode.disconnected.server.world.comp.net.nodes.ComputerConnectedNode;
import com.quartercode.disconnected.server.world.comp.net.nodes.DeviceNode;
import com.quartercode.disconnected.server.world.comp.net.nodes.DownlinkRouterNode;
import com.quartercode.disconnected.server.world.comp.net.nodes.UplinkRouterNode;
import com.quartercode.disconnected.server.world.comp.os.OperatingSystem;
import com.quartercode.disconnected.server.world.comp.os.config.EnvVariable;
import com.quartercode.disconnected.server.world.comp.os.config.User;
import com.quartercode.disconnected.server.world.comp.prog.Program;
import com.quartercode.disconnected.shared.util.registry.Registries;
import com.quartercode.disconnected.shared.util.registry.extra.NamedValueUtils;
import com.quartercode.disconnected.shared.world.comp.ByteUnit;
import com.quartercode.disconnected.shared.world.comp.Version;
import com.quartercode.disconnected.shared.world.comp.file.PathUtils;
import com.quartercode.disconnected.shared.world.comp.net.NetId;
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
     * @throws GenerationException If a severe bug occurs and the generator can not recover from it.
     */
    public static World generateWorld(Random random, int computers) throws GenerationException {

        World world = new World();

        Network rootNetwork = world.getRootNetwork();
        for (Computer computer : generateComputers(random, computers, rootNetwork)) {
            world.addComputer(computer);
        }

        return world;
    }

    /**
     * Generates the given amount of {@link Computer}s randomly.
     *
     * @param random The {@link Random} object which is used for generating the computers.
     * @param amount The amount of computers the generator should generate.
     * @param rootNetwork The root {@link Network} of the Internet.
     * @return The generated list of computers.
     * @throws GenerationException If a severe bug occurs and the generator can not recover from it.
     */
    public static List<Computer> generateComputers(Random random, int amount, Network rootNetwork) throws GenerationException {

        return generateComputers(random, amount, rootNetwork, null);
    }

    /**
     * Generates the given amount of {@link Computer}s randomly while excluding the {@link Location}s of the given computers.
     *
     * @param random The {@link Random} object which is used for generating the computers.
     * @param amount The amount of computers the generator should generate.
     * @param rootNetwork The root {@link Network} of the Internet.
     * @param ignore There won't be any computers with one of those locations.
     * @return The generated list of computers.
     * @throws GenerationException If a severe bug occurs and the generator can not recover from it.
     */
    public static List<Computer> generateComputers(Random random, int amount, Network rootNetwork, List<Computer> ignore) throws GenerationException {

        Validate.isTrue(amount > 0, "Cannot generate %d computers; amount must be > 0", amount);

        /*
         * Generate the locations of the computers.
         */

        List<Location> ignoreLocations = new ArrayList<>();
        if (ignore != null) {
            for (Computer computer : ignore) {
                ignoreLocations.add(computer.getLocation());
            }
        }

        List<Location> locations = LocationGenerator.generateLocations(amount, ignoreLocations, random);

        /*
         * Generate the actual computers and the corresponding network nodes.
         */

        List<Computer> computers = new ArrayList<>();

        // For each network (represented by the network's uplink target net id), this map stores all its subnetworks
        Map<NetId, List<Network>> subnetworks = new HashMap<>();
        // The root network has the UTNI "null" since it is impossible to create an empty net id
        subnetworks.put(null, new ArrayList<Network>());

        // The first node to be generated is a bridge node in the root network
        // That way, there's always a point to connect to in the root network (in subnetworks, this job is done by the uplink router nodes)
        // Note that this node is added to the network as soon as the second node is generated because a single node without connections is not possible in a network
        BridgeNode firstNetNode = new BridgeNode();
        computers.add(generateComputerAndSetNetNode(locations, 0, firstNetNode));

        for (int computerIndex = 1; computerIndex < amount; computerIndex++) {
            // The amount of computers which is left to be generated (including the one which is currently being built)
            int availableComputers = amount - computerIndex;

            // The network the new net node should be generated in
            Network network;
            // The net node the new net node should connect to
            NetNode connectionPartnerNode;

            if (firstNetNode != null) {
                network = rootNetwork;
                connectionPartnerNode = firstNetNode;
                firstNetNode = null;
            } else {
                network = selectNetwork(random, rootNetwork, subnetworks);
                connectionPartnerNode = getRandomFreeNetNode(random, network.getNetNodes());
            }

            // Select the type of network node which should be generated
            // There are at least 3 available computers required for generating a downlink router node (first branch) because an uplink node and a
            // single device node also need to be generated (so that the new subnetwork isn't totally empty)
            if (availableComputers >= 3 && ProbabilityUtils.gen(0.3F, random)) {
                // Generate the downlink node
                DownlinkRouterNode downlinkNode = new DownlinkRouterNode();
                network.addConnection(downlinkNode, connectionPartnerNode);
                computers.add(generateComputerAndSetNetNode(locations, computerIndex, downlinkNode));
                // Add the new subnetwork to the subnetwork map
                Network subnetwork = downlinkNode.getSubNetwork();
                subnetworks.get(network.getUplinkTargetNetId()).add(subnetwork);
                subnetworks.put(subnetwork.getUplinkTargetNetId(), new ArrayList<Network>());

                // We want to generate another computer
                computerIndex++;

                // Generate the uplink node
                UplinkRouterNode uplinkNode = new UplinkRouterNode();
                computers.add(generateComputerAndSetNetNode(locations, computerIndex, uplinkNode));

                // We want to generate another computer
                computerIndex++;

                // Generate the first device node in the new network
                DeviceNode deviceNode = new DeviceNode();
                computers.add(generateComputerAndSetNetNode(locations, computerIndex, deviceNode));
                subnetwork.addConnection(uplinkNode, deviceNode);
            } else {
                // Generate a device node
                DeviceNode deviceNode = new DeviceNode();
                network.addConnection(deviceNode, connectionPartnerNode);
                computers.add(generateComputerAndSetNetNode(locations, computerIndex, deviceNode));
            }
        }

        return computers;
    }

    private static Network selectNetwork(Random random, Network currentNetwork, Map<NetId, List<Network>> subnetworks) {

        List<Network> currentSubnetworks = subnetworks.get(currentNetwork.getUplinkTargetNetId());

        if (currentSubnetworks.isEmpty() || ProbabilityUtils.gen(0.5F, random)) {
            return currentNetwork;
        } else {
            Network newCurrentNetwork = currentSubnetworks.get(random.nextInt(currentSubnetworks.size()));
            return selectNetwork(random, newCurrentNetwork, subnetworks);
        }
    }

    private static NetNode getRandomFreeNetNode(Random random, Collection<NetNode> netNodes) {

        int item = random.nextInt(netNodes.size()); // In real life, the Random object should be rather more shared than this

        int counter = 0;
        for (NetNode netNode : netNodes) {
            if (counter == item) {
                if (netNode.getMaxConnections() > 1) {
                    return netNode;
                } else {
                    // The net node isn't free -> try again
                    return getRandomFreeNetNode(random, netNodes);
                }
            } else {
                counter = counter + 1;
            }
        }

        // Will never be reached
        return null;
    }

    private static Computer generateComputerAndSetNetNode(List<Location> locations, int computerIndex, ComputerConnectedNode netNode) throws GenerationException {

        Computer computer = generateComputer(locations.get(computerIndex));
        computer.getSingleHardwareByType(NetInterface.class).setNetNode(netNode);
        return computer;
    }

    /**
     * Generates a typical {@link Computer} or router with the common hardware and default file systems.
     *
     * @param router Whether the generated computer is a router.
     * @return A newly generated computer.
     * @throws GenerationException If a severe bug occurs and the generator can not recover from it.
     */
    public static Computer generateComputer(Location location) throws GenerationException {

        OperatingSystem os = new OperatingSystem("Frames", new Version(3, 7, 65));
        Computer computer = new Computer(location, os);

        List<MainboardSlot<?>> mainboardSlots = new ArrayList<>();
        mainboardSlots.add(new MainboardSlot<>(CPU.class));
        mainboardSlots.add(new MainboardSlot<>(RAM.class));
        mainboardSlots.add(new MainboardSlot<>(HardDrive.class));
        mainboardSlots.add(new MainboardSlot<>(HardDrive.class));
        mainboardSlots.add(new MainboardSlot<>(NetInterface.class));
        Mainboard mainboard = new Mainboard("MB XYZ 2000 Pro", mainboardSlots);
        computer.addHardware(mainboard);

        computer.addHardware(new CPU("Intel Core i7-4950HQ", 8, 2400000000L));
        computer.addHardware(new RAM("EpicRAM 4194304", ByteUnit.BYTE.convert(4, ByteUnit.MEGABYTE), 1600000000L));
        computer.addHardware(new NetInterface("NNI FiberScore Ultimate"));

        FileSystem systemFs = new FileSystem(ByteUnit.BYTE.convert(1, ByteUnit.TERABYTE));
        computer.addHardware(new HardDrive("TheHardDrive 1TB", systemFs));
        FileSystem userFs = new FileSystem(ByteUnit.BYTE.convert(1, ByteUnit.TERABYTE));
        computer.addHardware(new HardDrive("TheHardDrive 1TB", userFs));

        for (MainboardSlot<?> slot : mainboard.getSlots()) {
            fillInMainboardSlot(slot);
        }

        // Generate superuser object
        User superuser = new User(User.SUPERUSER_NAME);

        // Fill debug file systems
        addSystemFiles(systemFs, superuser);
        addUserFiles(userFs, superuser);

        // Add debug known file systems
        FileSystemModule fsModule = os.getFsModule();
        addKnownFs(fsModule, systemFs, SYSTEM_MOUNTPOINT);
        addKnownFs(fsModule, userFs, USER_MOUNTPOINT);

        return computer;
    }

    @SuppressWarnings ("unchecked")
    private static <T extends Hardware> void fillInMainboardSlot(MainboardSlot<T> slot) {

        Mainboard mainboard = slot.getSingleParent();
        Computer computer = mainboard.getSingleParent();

        Hardware useHardware = null;
        for (Hardware hardware : computer.getHardware()) {
            if (hardware != mainboard) {
                if (hardware.getClass().isAnnotationPresent(NeedsMainboardSlot.class) && slot.getContentType().isInstance(hardware)) {
                    useHardware = hardware;
                    break;
                }
            }
        }

        if (useHardware != null) {
            slot.setContent((T) useHardware);
        }
    }

    private static void addKnownFs(FileSystemModule fsModule, FileSystem fileSystem, String mountpoint) {

        KnownFileSystem knownFs = new KnownFileSystem(fileSystem);
        knownFs.setMountpoint(mountpoint);
        fsModule.addKnownFs(knownFs);
    }

    // Temporary method for generating the kernel and some system programs
    private static void addSystemFiles(FileSystem fileSystem, User superuser) throws GenerationException {

        // Add system programs
        addProgramFile(fileSystem, SYSTEM_PROGRAM_DIR, superuser, "session", new Version(1, 0, 0));

        // Add general programs
        // TODO: Uncomment these
        // addProgramFile(fileSystem, SYSTEM_PROGRAM_DIR, superuser, "fileManager", new Version(1, 0, 0));
        // addProgramFile(fileSystem, SYSTEM_PROGRAM_DIR, superuser, "processManager", new Version(1, 0, 0));
    }

    // Temporary method for generating some unessential programs and personal files
    private static void addUserFiles(FileSystem fileSystem, User superuser) throws GenerationException {

        // Generate basic user config
        Config<User> userConfig = new Config<>();
        userConfig.addEntry(superuser);
        addContentFile(fileSystem, PathUtils.splitAfterMountpoint(USER_CONFIG)[1], superuser, "o:r", userConfig);

        // Generate basic environment config
        Config<EnvVariable> envConfig = new Config<>();
        envConfig.addEntry(new EnvVariable("PROGRAM_DIRS", Arrays.asList(SYSTEM_PROGRAM_DIR, USER_PROGRAM_DIR)));
        addContentFile(fileSystem, PathUtils.splitAfterMountpoint(ENVIRONMENT_CONFIG)[1], superuser, "o:r", envConfig);
    }

    private static ContentFile createContentFile(User owner, String rights, Object content) {

        ContentFile file = new ContentFile(owner);
        file.getRights().importRights(rights);

        try {
            file.setContent(content);
        } catch (OutOfSpaceException e) {
            // Will never happen since the file hasn't been added to a file system yet
        }

        return file;
    }

    private static ContentFile addContentFile(FileSystem fileSystem, String path, User owner, String rights, Object content) throws GenerationException {

        ContentFile file = createContentFile(owner, rights, content);

        try {
            fileSystem.prepareAddFile(file, path).execute();
        } catch (InvalidPathException | OccupiedPathException | OutOfSpaceException e) {
            // If any one of these exceptions happens, the world generator has a severe bug and cannot recover from the error
            // Therefore, an exception is thrown in order to indicate that the generation is not possible
            throw new GenerationException("Severe internal exception while trying to add the file '" + path + "' to a file system", e);
        }

        return file;
    }

    private static void addProgramFile(FileSystem fileSystem, String dir, User owner, String programName, Version version) throws GenerationException {

        WorldProgram programData = NamedValueUtils.getByName(Registries.get(ServerRegistries.WORLD_PROGRAMS), programName);
        Program program = new Program(programName, version);

        // Try to generate 3 vulnerabilities
        Set<VulnSource> vulnSources = Registries.get(ServerRegistries.VULN_SOURCES).getValuesByUsage("worldProgram", programData.getName());
        if (!vulnSources.isEmpty()) {
            program.getVulnContainer().generateVulnerabilities(vulnSources, 3);
        }

        String programPath = PathUtils.resolve(dir, programData + ".exe");
        addContentFile(fileSystem, PathUtils.splitAfterMountpoint(programPath)[1], owner, "o:rx", program);
    }

    private WorldGenerator() {

    }

}
