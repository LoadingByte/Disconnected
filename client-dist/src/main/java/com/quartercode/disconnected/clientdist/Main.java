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

package com.quartercode.disconnected.clientdist;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Random;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
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
import com.quartercode.disconnected.server.sim.TickRunnableInvoker;
import com.quartercode.disconnected.server.sim.TickService;
import com.quartercode.disconnected.server.sim.TickWorldUpdater;
import com.quartercode.disconnected.server.sim.gen.WorldGenerator;
import com.quartercode.disconnected.server.sim.scheduler.SchedulerRegistry;
import com.quartercode.disconnected.server.world.World;
import com.quartercode.disconnected.server.world.comp.Computer;
import com.quartercode.disconnected.shared.CommonBootstrap;
import com.quartercode.disconnected.shared.util.ApplicationInfo;
import com.quartercode.disconnected.shared.util.ExitUtil;
import com.quartercode.disconnected.shared.util.ExitUtil.ExitProcessor;
import com.quartercode.disconnected.shared.util.LogExceptionHandler;
import com.quartercode.disconnected.shared.util.ServiceRegistry;
import com.quartercode.disconnected.shared.util.Settings;
import com.quartercode.disconnected.shared.util.ValueInjector;
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

        URL jarLocation = Main.class.getProtectionDomain().getCodeSource().getLocation();
        String jarName = "<unknown>";

        try {
            String tempJarName = Paths.get(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getFileName().toString();
            if (tempJarName.endsWith(".jar")) {
                tempJarName = jarName;
            } else {
                LOGGER.warn("Cannot read application jar name (location '{}') because it doesn't end with '.jar'", jarLocation);
            }
        } catch (URISyntaxException e) {
            LOGGER.warn("Cannot read application jar name (location '{}') due to and unexpected exception", jarLocation, e);
        }

        JAR_NAME = jarName;

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

        // Process the command line arguments
        if (!processCommandLineArguments(args)) {
            return;
        }

        // Bootstrap modules (shared, server, client)
        LOGGER.info("Executing bootstrap");
        CommonBootstrap.bootstrap();

        // DEBUG: Retrieve the game services
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
        final World world = WorldGenerator.generateWorld(random, 10);

        // DEBUG: Create and fill a new scheduler registry
        SchedulerRegistry schedulerRegistry = new SchedulerRegistry();
        schedulerRegistry.addSchedulersFromTree(world);

        // DEBUG: Inject the correct values into the world
        ValueInjector worldValueInjector = new ValueInjector();
        worldValueInjector.put("random", random);
        worldValueInjector.put("bridge", serverBridge);
        worldValueInjector.put("schedulerRegistry", schedulerRegistry);
        worldValueInjector.inject(world);

        // DEBUG: Start "game" with current simulation
        LOGGER.info("DEBUG: Starting test-game with current simulation");
        tickService.getAction(TickWorldUpdater.class).setWorld(world);
        tickService.setRunning(true);
        tickService.getAction(TickRunnableInvoker.class).invoke(new Runnable() {

            @Override
            public void run() {

                for (Computer computer : world.getComputers()) {
                    computer.getOs().setRunning(true);
                }
            }

        });
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

    // The return value expresses whether the main method should be executed further (true) or aborted (false)
    private static boolean processCommandLineArguments(String[] arguments) {

        // Parse command line arguments
        Options options = createCommandLineOptions();
        CommandLine line = null;
        try {
            line = new DefaultParser().parse(options, arguments, true);
        } catch (ParseException e) {
            LOGGER.warn(e.getMessage());
            new HelpFormatter().printHelp("java -jar " + JAR_NAME, options, true);
            return false;
        }

        // Print help if necessary
        if (line.hasOption("help")) {
            LOGGER.info("Printing help and returning");
            new HelpFormatter().printHelp("java -jar " + JAR_NAME, options, true);
            return false;
        }

        // Set locale if necessary
        if (line.hasOption("locale")) {
            Locale.setDefault(LocaleUtils.toLocale(line.getOptionValue("locale")));
        }

        return true;
    }

    private static Options createCommandLineOptions() {

        Options options = new Options();
        options.addOption(Option.builder("h").longOpt("help").desc("Prints a help page").build());
        options.addOption(Option.builder("l").longOpt("locale").hasArg().argName("locale").desc("Sets the locale code to use (e.g. en or de_DE)").build());
        return options;
    }

    private Main() {

    }

}
