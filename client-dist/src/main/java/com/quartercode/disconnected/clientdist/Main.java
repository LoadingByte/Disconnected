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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Random;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;
import ch.qos.logback.classic.Level;
import com.quartercode.disconnected.client.graphics.DefaultStates;
import com.quartercode.disconnected.client.graphics.GraphicsService;
import com.quartercode.disconnected.server.sim.TickBridgeProvider;
import com.quartercode.disconnected.server.sim.TickService;
import com.quartercode.disconnected.server.sim.gen.WorldGenerator;
import com.quartercode.disconnected.server.sim.profile.Profile;
import com.quartercode.disconnected.server.sim.profile.ProfileSerializationException;
import com.quartercode.disconnected.server.sim.profile.ProfileService;
import com.quartercode.disconnected.server.world.World;
import com.quartercode.disconnected.server.world.comp.Computer;
import com.quartercode.disconnected.server.world.comp.os.OperatingSystem;
import com.quartercode.disconnected.shared.CommonBootstrap;
import com.quartercode.disconnected.shared.util.ApplicationInfo;
import com.quartercode.disconnected.shared.util.ExitUtil;
import com.quartercode.disconnected.shared.util.ExitUtil.ExitProcessor;
import com.quartercode.disconnected.shared.util.LogExceptionHandler;
import com.quartercode.disconnected.shared.util.ServiceRegistry;
import com.quartercode.disconnected.shared.util.Settings;
import com.quartercode.disconnected.shared.util.TempFileManager;
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

        // Initialize settings and process default ones
        initializeSettings();
        processDefaultSettings();

        // Initialize temp file manager
        initializeTempFileManager();

        // Process the command line arguments
        processCommandLineArguments(args);

        // Bootstrap modules (shared, server, client)
        LOGGER.info("Executing bootstrap");
        CommonBootstrap.bootstrap();

        // DEBUG: Retrieve the game services
        ProfileService profileService = ServiceRegistry.lookup(ProfileService.class);
        TickService tickService = ServiceRegistry.lookup(TickService.class);
        GraphicsService graphicsService = ServiceRegistry.lookup(GraphicsService.class);

        // DEBUG: Start graphics service
        graphicsService.setRunning(true);

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
        Random random = new Random(1);
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

        // DEBUG: Start "game" with current simulation
        LOGGER.info("DEBUG: Starting test-game with current simulation");
        tickService.setRunning(true);
        for (Computer computer : world.get(World.COMPUTERS).get()) {
            computer.get(Computer.OS).get().get(OperatingSystem.SET_RUNNING).invoke(true);
        }
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
    }

    private static void initializeSettings() {

        // Load the settings
        LOGGER.info("Loading settings file");
        Settings.setSettingsFile(Paths.get("settings.properties"));

        // Initialize the default settings
        Settings.initializeSetting("debugLogging", "false");
        Settings.initializeSetting("debugLoggingPackages", "com.quartercode");
    }

    private static void processDefaultSettings() {

        // debugLogging and debugLoggingPackages
        if (Settings.getSetting("debugLogging").equals("true")) {
            // Iterate over all packages which are marked for debug logging
            for (String loggerPackage : StringUtils.split(Settings.getSetting("debugLoggingPackages"), ',')) {
                // Retrieve the root logger for the current package and set its level to debug
                ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(loggerPackage)).setLevel(Level.DEBUG);
            }
        }
    }

    private static void initializeTempFileManager() {

        Path parentTempDir = Paths.get("tmp");

        LOGGER.debug("Initializing temp file manager under '{}'", parentTempDir);
        try {
            TempFileManager.initialize(parentTempDir);
        } catch (IOException e) {
            throw new RuntimeException("Error while initializing temp file manager under '" + parentTempDir + "'", e);
        }
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

    private Main() {

    }

}
