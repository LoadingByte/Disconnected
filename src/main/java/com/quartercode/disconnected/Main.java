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

package com.quartercode.disconnected;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.quartercode.classmod.util.Classmod;
import com.quartercode.disconnected.graphics.DefaultStates;
import com.quartercode.disconnected.graphics.GraphicsManager;
import com.quartercode.disconnected.graphics.GraphicsModule;
import com.quartercode.disconnected.graphics.GraphicsState;
import com.quartercode.disconnected.graphics.desktop.DefaultDesktopProgramManager;
import com.quartercode.disconnected.graphics.desktop.DesktopLaunchButtonModule;
import com.quartercode.disconnected.graphics.desktop.DesktopProgramDescriptor;
import com.quartercode.disconnected.graphics.desktop.DesktopPrograms;
import com.quartercode.disconnected.graphics.desktop.DesktopTaskbarModule;
import com.quartercode.disconnected.graphics.desktop.DesktopWidgetModule;
import com.quartercode.disconnected.graphics.desktop.DesktopWindowAreaModule;
import com.quartercode.disconnected.sim.Simulation;
import com.quartercode.disconnected.sim.profile.Profile;
import com.quartercode.disconnected.sim.profile.ProfileManager;
import com.quartercode.disconnected.sim.run.TickAction;
import com.quartercode.disconnected.sim.run.TickSimulator;
import com.quartercode.disconnected.sim.run.Ticker;
import com.quartercode.disconnected.sim.run.util.SimulationGenerator;
import com.quartercode.disconnected.util.LogExceptionHandler;
import com.quartercode.disconnected.util.RandomPool;
import com.quartercode.disconnected.util.Registry;
import com.quartercode.disconnected.util.ResourceStore;
import com.quartercode.disconnected.world.World;
import com.quartercode.disconnected.world.comp.Computer;
import com.quartercode.disconnected.world.comp.os.OperatingSystem;

/**
 * The main class which initalizes the whole game.
 */
public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    private static boolean      exitUnderway;

    /**
     * The main method which initalizes the whole game.
     * 
     * @param args The command line arguments.
     */
    public static void main(String[] args) {

        // Default exception handler if the vm throws an exception to the entry point of thread (e.g. main() or run())
        Thread.setDefaultUncaughtExceptionHandler(new LogExceptionHandler());

        // Print information about the software
        LOGGER.info("Version {}", Disconnected.getVersion());

        // Parse command line arguments
        Options options = createCommandLineOptions();
        CommandLine line = null;
        try {
            line = new PosixParser().parse(options, args, true);
        } catch (ParseException e) {
            LOGGER.warn(e.getMessage());
            new HelpFormatter().printHelp("java -jar disconnected-" + Disconnected.getVersion() + ".jar", options, true);
        }

        // Print help if necessary
        if (line.hasOption("help")) {
            LOGGER.info("Printing help and returning");
            new HelpFormatter().printHelp("java -jar disconnected-" + Disconnected.getVersion() + ".jar", options, true);
            return;
        }

        // Set locale if necessary
        if (line.hasOption("locale")) {
            String[] localeParts = line.getOptionValue("locale").split("_");
            String language = "";
            String country = "";
            String variant = "";
            if (localeParts.length >= 1) {
                language = localeParts[0];
            }
            if (localeParts.length >= 2) {
                country = localeParts[1];
            }
            if (localeParts.length >= 3) {
                variant = localeParts[2];
            }
            Locale.setDefault(new Locale(language, country, variant));
        }

        // Initalize & fill registry
        LOGGER.info("Initalizing & filling class registry");
        Disconnected.setRegistry(new Registry());
        fillRegistry(Disconnected.getRegistry());

        // Initalize & fill resource store
        try {
            LOGGER.info("Initalizing & filling resource store");
            Disconnected.setRS(new ResourceStore());
            fillResourceStore(Disconnected.getRS());
        } catch (Exception e) {
            LOGGER.error("Can't fill resource store", e);
            return;
        }

        LOGGER.info("Initalizing default graphics states");
        initializeDefaultGraphicsStates();
        fillDefaultDesktopPrograms();

        // Initalize profile manager and load stored profiles
        LOGGER.info("Initalizing profile manager");
        Disconnected.setProfileManager(new ProfileManager(new File("profiles")));

        // Initalize graphics manager and start it
        LOGGER.info("Initalizing & starting graphics manager");
        Disconnected.setGraphicsManager(new GraphicsManager());
        Disconnected.getGraphicsManager().setRunning(true);

        // Initalize ticker
        LOGGER.info("Initalizing ticker");
        List<TickAction> tickActions = new ArrayList<TickAction>();
        tickActions.add(new TickSimulator());
        Disconnected.setTicker(new Ticker(tickActions.toArray(new TickAction[tickActions.size()])));

        // DEBUG: Generate and set new simulation
        LOGGER.info("DEBUG-ACTION: Generating new simulation");
        Simulation simulation = SimulationGenerator.generateSimulation(10, 2, new RandomPool(Simulation.RANDOM_POOL_SIZE));
        for (Computer computer : simulation.getWorld().get(World.COMPUTERS).get()) {
            computer.get(Computer.OS).get().get(OperatingSystem.SET_RUNNING).invoke(true);
        }

        Profile profile = new Profile("test", simulation);
        Disconnected.getProfileManager().addProfile(profile);
        try {
            Disconnected.getProfileManager().setActive(profile);
        } catch (Exception e) {
            e.printStackTrace();
            // Won't ever happen (we just created a new profile)
        }

        // DEBUG: Start "game" with current simulation
        LOGGER.info("DEBUG-ACTION: Starting test-game with current simulation");
        Disconnected.getTicker().setRunning(true);
        Disconnected.getGraphicsManager().setState(DefaultStates.DESKTOP.create());
    }

    @SuppressWarnings ("static-access")
    private static Options createCommandLineOptions() {

        Options options = new Options();
        options.addOption(OptionBuilder.withLongOpt("help").withDescription("Prints a help page").create("h"));
        options.addOption(OptionBuilder.withLongOpt("locale").hasArg().withArgName("locale").withDescription("Sets the locale code to use (e.g. en or de_DE)").create("l"));
        return options;
    }

    /**
     * Fills the given registry with the default values which are needed for running Disconnected.
     * 
     * @param registry The registry to fill.
     */
    public static void fillRegistry(Registry registry) {

        registry.registerContextPathEntry(Classmod.CONTEXT_PATH);
        registry.registerContextPathEntry("com.quartercode.disconnected.sim.run");
        registry.registerContextPathEntry("com.quartercode.disconnected.world");
        registry.registerContextPathEntry("com.quartercode.disconnected.world.comp");
        registry.registerContextPathEntry("com.quartercode.disconnected.world.comp.attack");
        registry.registerContextPathEntry("com.quartercode.disconnected.world.comp.file");
        registry.registerContextPathEntry("com.quartercode.disconnected.world.comp.hardware");
        registry.registerContextPathEntry("com.quartercode.disconnected.world.comp.net");
        registry.registerContextPathEntry("com.quartercode.disconnected.world.comp.os");
        registry.registerContextPathEntry("com.quartercode.disconnected.world.comp.program");

        registry.registerTheme(Main.class.getResource("/ui/default/default.xml"));
        registry.registerTheme(Main.class.getResource("/ui/shell/shell.xml"));
        registry.registerTheme(Main.class.getResource("/ui/desktop/desktop.xml"));
    }

    /**
     * Fills the given resource store with the default resource objects which are needed for running Disconnected.
     * 
     * @param resourceStore The resource store to fill.
     * @throws IOException Something goes wrong while reading from a jar file or resource.
     */
    public static void fillResourceStore(ResourceStore resourceStore) throws IOException {

        resourceStore.loadFromClasspath("/data");
    }

    /**
     * Adds the default {@link GraphicsModule}s to the default {@link GraphicsState}s declared in {@link DefaultStates}.
     */
    public static void initializeDefaultGraphicsStates() {

        DefaultStates.DESKTOP.addModule(DesktopWidgetModule.class, "desktopWidget", 100);
        DefaultStates.DESKTOP.addModule(DesktopWindowAreaModule.class, "windowArea", 80);
        DefaultStates.DESKTOP.addModule(DesktopLaunchButtonModule.class, "launchButton", 80);
        DefaultStates.DESKTOP.addModule(DesktopTaskbarModule.class, "taskbar", 80);
        DefaultStates.DESKTOP.addModule(DefaultDesktopProgramManager.class, "programManager", 0);
    }

    /**
     * Adds the default {@link DesktopProgramDescriptor}s to the {@link DesktopPrograms} list.
     * 
     * @see DesktopPrograms
     */
    public static void fillDefaultDesktopPrograms() {

    }

    public static synchronized void exit() {

        if (!exitUnderway) {
            exitUnderway = true;

            Disconnected.getGraphicsManager().setRunning(false);
            Disconnected.getTicker().setRunning(false);
        }
    }

    private Main() {

    }

}
