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
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import com.quartercode.disconnected.graphics.GraphicsManager;
import com.quartercode.disconnected.graphics.session.DesktopState;
import com.quartercode.disconnected.mocl.extra.FunctionExecutionException;
import com.quartercode.disconnected.mocl.extra.def.ObjectProperty;
import com.quartercode.disconnected.mocl.extra.def.ReferenceProperty;
import com.quartercode.disconnected.sim.Simulation;
import com.quartercode.disconnected.sim.profile.Profile;
import com.quartercode.disconnected.sim.profile.ProfileManager;
import com.quartercode.disconnected.sim.run.TickAction;
import com.quartercode.disconnected.sim.run.TickSimulator;
import com.quartercode.disconnected.sim.run.Ticker;
import com.quartercode.disconnected.sim.run.util.SimulationGenerator;
import com.quartercode.disconnected.util.LogExceptionHandler;
import com.quartercode.disconnected.util.ObjectAdapter;
import com.quartercode.disconnected.util.RandomPool;
import com.quartercode.disconnected.util.Registry;
import com.quartercode.disconnected.util.ResourceStore;
import com.quartercode.disconnected.world.Location;
import com.quartercode.disconnected.world.World;
import com.quartercode.disconnected.world.comp.Computer;
import com.quartercode.disconnected.world.comp.Version;
import com.quartercode.disconnected.world.comp.hardware.CPU;
import com.quartercode.disconnected.world.comp.hardware.HardDrive;
import com.quartercode.disconnected.world.comp.hardware.Mainboard;
import com.quartercode.disconnected.world.comp.hardware.Mainboard.MainboardSlot;
import com.quartercode.disconnected.world.comp.hardware.NetworkInterface;
import com.quartercode.disconnected.world.comp.hardware.RAM;
import com.quartercode.disconnected.world.comp.os.Environment;
import com.quartercode.disconnected.world.comp.os.OperatingSystem;
import com.quartercode.disconnected.world.comp.os.Session;
import com.quartercode.disconnected.world.member.Member;
import com.quartercode.disconnected.world.member.MemberGroup;
import com.quartercode.disconnected.world.member.ai.PlayerController;
import com.quartercode.disconnected.world.member.ai.UserController;
import com.quartercode.disconnected.world.member.interest.DestroyInterest;

/**
 * The main class which initalizes the whole game.
 */
public class Main {

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    /**
     * The main method which initalizes the whole game.
     * 
     * @param args The command line arguments.
     */
    public static void main(String[] args) {

        // Logging configuration
        try {
            LogManager.getLogManager().readConfiguration(Main.class.getResourceAsStream("/config/logging.properties"));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Can't load logging configuration", e);
            return;
        }

        // Default exception handler if the vm throws an exception to the entry point of thread (e.g. main() or run())
        Thread.setDefaultUncaughtExceptionHandler(new LogExceptionHandler());

        // Print information about the software
        LOGGER.info("Version " + Disconnected.getVersion());

        // Parse command line arguments
        Options options = createCommandLineOptions();
        CommandLine line = null;
        try {
            line = new PosixParser().parse(options, args, true);
        } catch (ParseException e) {
            LOGGER.warning(e.getMessage());
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
            LOGGER.log(Level.SEVERE, "Can't fill resource store", e);
            return;
        }

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

        try {
            for (Computer computer : simulation.getWorld().get(World.GET_COMPUTERS).invoke()) {
                computer.get(Computer.GET_OS).invoke().get(OperatingSystem.SET_RUNNING).invoke(true);
            }
        } catch (FunctionExecutionException e) {
            LOGGER.log(Level.SEVERE, "Unknown error while booting up computers", e.getCause());
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
        Disconnected.getGraphicsManager().setState(new DesktopState(profile.getSimulation()));
    }

    @SuppressWarnings ("static-access")
    private static Options createCommandLineOptions() {

        Options options = new Options();
        options.addOption(OptionBuilder.withLongOpt("help").withDescription("Prints a help page").create("h"));
        options.addOption(OptionBuilder.withLongOpt("locale").hasArg().withArgName("locale").withDescription("Sets the locale code to use (e.g. en or de_DE)").create("l"));
        return options;
    }

    /**
     * Fills the given registry with the default values which are needed for running vanilla disconnected.
     * 
     * @param registry The registry to fill.
     */
    public static void fillRegistry(Registry registry) {

        // ----- General -----

        // Properties
        registry.registerClass(ObjectProperty.class);
        registry.registerClass(ReferenceProperty.class);

        // Other
        registry.registerClass(ObjectAdapter.ClassElement.class);
        registry.registerClass(Location.class);

        // ----- General End -----

        // ----- Members -----

        // Mixed member stuff
        registry.registerClass(Member.class);
        registry.registerClass(MemberGroup.class);

        // AI Controllers
        registry.registerClass(PlayerController.class);
        registry.registerClass(UserController.class);

        // Interests
        registry.registerClass(DestroyInterest.class);

        // ----- Members End -----

        // ----- Computers -----

        // Mixed computer stuff
        registry.registerClass(Version.class);
        registry.registerClass(Environment.class);

        // Hardware
        registry.registerClass(Mainboard.class);
        registry.registerClass(MainboardSlot.class);
        registry.registerClass(CPU.class);
        registry.registerClass(RAM.class);
        registry.registerClass(HardDrive.class);
        registry.registerClass(NetworkInterface.class);

        // Programs
        registry.registerClass(Session.class);

        // ----- Computers End -----

        // ----- Themes -----

        registry.registerTheme(Main.class.getResource("/ui/default/default.xml"));
        registry.registerTheme(Main.class.getResource("/ui/shell/shell.xml"));
        registry.registerTheme(Main.class.getResource("/ui/desktop/desktop.xml"));

        // ----- Themes End -----
    }

    /**
     * Fills the given resource store with the default resource objects which are needed for running vanilla disconnected.
     * 
     * @param resourceStore The resource store to fill.
     * @throws IOException Something goes wrong while reading from a jar file or resource.
     */
    public static void fillResourceStore(ResourceStore resourceStore) throws IOException {

        resourceStore.loadFromClasspath("/data");
    }

    private Main() {

    }

}
