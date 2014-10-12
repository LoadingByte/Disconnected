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

package com.quartercode.disconnected.clientdist;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Locale;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;
import com.quartercode.disconnected.client.DefaultClientData;
import com.quartercode.disconnected.client.graphics.DefaultGraphicsService;
import com.quartercode.disconnected.client.graphics.DefaultStates;
import com.quartercode.disconnected.client.graphics.GraphicsService;
import com.quartercode.disconnected.server.DefaultServerData;
import com.quartercode.disconnected.server.sim.DefaultTickService;
import com.quartercode.disconnected.server.sim.TickBridgeProvider;
import com.quartercode.disconnected.server.sim.TickSchedulerUpdater;
import com.quartercode.disconnected.server.sim.TickService;
import com.quartercode.disconnected.server.sim.profile.DefaultProfileService;
import com.quartercode.disconnected.server.sim.profile.Profile;
import com.quartercode.disconnected.server.sim.profile.ProfileSerializationException;
import com.quartercode.disconnected.server.sim.profile.ProfileService;
import com.quartercode.disconnected.server.util.RandomPool;
import com.quartercode.disconnected.server.world.World;
import com.quartercode.disconnected.server.world.comp.Computer;
import com.quartercode.disconnected.server.world.comp.os.OperatingSystem;
import com.quartercode.disconnected.server.world.gen.WorldGenerator;
import com.quartercode.disconnected.shared.DefaultSharedData;
import com.quartercode.disconnected.shared.util.ApplicationInfo;
import com.quartercode.disconnected.shared.util.ExitUtil;
import com.quartercode.disconnected.shared.util.ExitUtil.ExitProcessor;
import com.quartercode.disconnected.shared.util.LogExceptionHandler;
import com.quartercode.disconnected.shared.util.ServiceRegistry;
import com.quartercode.eventbridge.bridge.Bridge;
import com.quartercode.eventbridge.bridge.BridgeConnectorException;
import com.quartercode.eventbridge.extra.connector.LocalBridgeConnector;

/**
 * The main class which initializes the whole game.
 */
public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    private static final String JAR_NAME;

    static {

        String jarName = Paths.get(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getFileName().toString();
        JAR_NAME = jarName.endsWith(".jar") ? jarName : null;

    }

    /**
     * The main method which initializes the whole game.
     * 
     * @param args The command line arguments.
     */
    public static void main(String[] args) {

        // Print some information about the software
        LOGGER.info("Starting {} {} by {}", ApplicationInfo.TITLE, ApplicationInfo.VERSION, ApplicationInfo.VENDOR);

        // Set general properties
        initializeGeneral();

        // Process the command line arguments
        processCommandLineArguments(args);

        // Fill the resource store
        fillResourceStore();

        // Add other data (e.g. mappings)
        addOtherData();

        // Initialize the game services
        initializeServices();

        // DEBUG: Retrieve the game services
        ProfileService profileService = ServiceRegistry.lookup(ProfileService.class);
        TickService tickService = ServiceRegistry.lookup(TickService.class);
        GraphicsService graphicsService = ServiceRegistry.lookup(GraphicsService.class);

        // DEBUG: Connect the client and server bridges
        LOGGER.info("DEBUG: Connect the client and server bridges");
        final Bridge clientBridge = graphicsService.getBridge();
        final Bridge serverBridge = tickService.getAction(TickBridgeProvider.class).getBridge();
        try {
            clientBridge.addConnector(new LocalBridgeConnector(serverBridge));
        } catch (BridgeConnectorException e) {
            LOGGER.error("Can't connect the client and server bridges");
            return;
        }

        // DEBUG: Generate and set new simulation
        LOGGER.info("DEBUG: Generating new simulation");
        RandomPool random = new RandomPool(Profile.DEFAULT_RANDOM_POOL_SIZE);
        World world = WorldGenerator.generateWorld(random, 10);

        Profile profile = new Profile("test");
        profile.setWorld(world);
        profile.setRandom(random);
        profileService.addProfile(profile);
        try {
            profileService.setActive(profile);
        } catch (ProfileSerializationException e) {
            // Won't ever happen (we just created a new profile)
        }

        for (Computer computer : world.get(World.COMPUTERS).get()) {
            computer.get(Computer.OS).get().get(OperatingSystem.SET_RUNNING).invoke(true);
        }

        // DEBUG: Start "game" with current simulation
        LOGGER.info("DEBUG: Starting test-game with current simulation");
        tickService.setRunning(true);
        graphicsService.setState(DefaultStates.DESKTOP.create());
    }

    private static void initializeGeneral() {

        // Set default exception handler
        Thread.setDefaultUncaughtExceptionHandler(new LogExceptionHandler());

        // Set default ToStringBuilder style
        ToStringBuilder.setDefaultStyle(ToStringStyle.SHORT_PREFIX_STYLE);

        // Install JUL to SLF4J bridge
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();

        // Inject exit processor
        ExitUtil.injectProcessor(new ExitProcessor() {

            @Override
            public void exit() {

                ServiceRegistry.lookup(GraphicsService.class).setRunning(false);
                ServiceRegistry.lookup(TickService.class).setRunning(false);
            }

        });

        // Add custom mappings to EventBridgeFactory
        DefaultSharedData.addCustomEventBridgeFactoryMappings();
        DefaultServerData.addCustomEventBridgeFactoryMappings();
    }

    private static void processCommandLineArguments(String[] arguments) {

        // Parse command line arguments
        Options options = createCommandLineOptions();
        CommandLine line = null;
        try {
            line = new PosixParser().parse(options, arguments, true);
        } catch (ParseException e) {
            LOGGER.warn(e.getMessage());
            new HelpFormatter().printHelp("java -jar " + JAR_NAME, options, true);
            return;
        }

        // Print help if necessary
        if (line.hasOption("help")) {
            LOGGER.info("Printing help and returning");
            new HelpFormatter().printHelp("java -jar " + JAR_NAME, options, true);
            return;
        }

        // Set locale if necessary
        if (line.hasOption("locale")) {
            Locale.setDefault(LocaleUtils.toLocale(line.getOptionValue("locale")));
        }
    }

    @SuppressWarnings ("static-access")
    private static Options createCommandLineOptions() {

        Options options = new Options();
        options.addOption(OptionBuilder.withLongOpt("help").withDescription("Prints a help page").create("h"));
        options.addOption(OptionBuilder.withLongOpt("locale").hasArg().withArgName("locale").withDescription("Sets the locale code to use (e.g. en or de_DE)").create("l"));
        return options;
    }

    private static void fillResourceStore() {

        // Load the resource store data
        LOGGER.info("Loading resource store data");
        try {
            DefaultServerData.fillResourceStore();
        } catch (IOException | RuntimeException e) {
            LOGGER.error("Can't fill resource store", e);
            return;
        }
    }

    private static void initializeServices() {

        // Initialize profile service and load stored profiles
        LOGGER.info("Initializing profile service and loading stored profiles");
        initializeProfileService();

        // Initialize tick service
        LOGGER.info("Initializing tick service");
        initializeTickService();

        // Initialize graphics service
        LOGGER.info("Initializing graphics service");
        initializeGraphicsService();

        // Start graphics service
        LOGGER.info("Starting graphics service");
        ServiceRegistry.lookup(GraphicsService.class).setRunning(true);
    }

    private static void initializeProfileService() {

        ProfileService profileService = new DefaultProfileService(Paths.get("profiles"));
        ServiceRegistry.register(ProfileService.class, profileService);

        DefaultServerData.addDefaultWorldContextPath();
        DefaultServerData.addDefaultWorldInitializerMappings();
    }

    private static void initializeTickService() {

        TickService tickService = new DefaultTickService();
        ServiceRegistry.register(TickService.class, tickService);

        tickService.addAction(new TickBridgeProvider());
        tickService.addAction(new TickSchedulerUpdater());

        DefaultServerData.addDefaultSchedulerGroups(tickService.getAction(TickSchedulerUpdater.class));
        DefaultServerData.addDefaultServerHandlers(tickService.getAction(TickBridgeProvider.class).getBridge());
    }

    private static void initializeGraphicsService() {

        GraphicsService graphicsService = new DefaultGraphicsService();
        ServiceRegistry.register(GraphicsService.class, graphicsService);

        DefaultClientData.addDefaultGraphicsServiceThemes(graphicsService);
        DefaultClientData.initializeDefaultGraphicsStates();
    }

    private static void addOtherData() {

        LOGGER.info("Adding other data");

        DefaultServerData.addDefaultStringFileTypeMappings();
        DefaultServerData.addDefaultProgramCommonLocationMappings();
    }

    private Main() {

    }

}
