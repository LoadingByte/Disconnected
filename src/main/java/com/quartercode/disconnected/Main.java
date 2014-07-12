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
import com.quartercode.classmod.Classmod;
import com.quartercode.disconnected.bridge.HandleInvocationProviderExtension;
import com.quartercode.disconnected.bridge.def.DefaultHandleInvocationProviderExtension.DefaultHandleInvocationProviderExtensionFactory;
import com.quartercode.disconnected.graphics.DefaultGraphicsService;
import com.quartercode.disconnected.graphics.DefaultStates;
import com.quartercode.disconnected.graphics.GraphicsModule;
import com.quartercode.disconnected.graphics.GraphicsService;
import com.quartercode.disconnected.graphics.GraphicsState;
import com.quartercode.disconnected.graphics.desktop.DesktopLaunchButtonModule;
import com.quartercode.disconnected.graphics.desktop.DesktopProgramDescriptor;
import com.quartercode.disconnected.graphics.desktop.DesktopPrograms;
import com.quartercode.disconnected.graphics.desktop.DesktopTaskbarModule;
import com.quartercode.disconnected.graphics.desktop.DesktopWidgetModule;
import com.quartercode.disconnected.graphics.desktop.DesktopWindowAreaModule;
import com.quartercode.disconnected.graphics.desktop.program.FileManagerProgram;
import com.quartercode.disconnected.sim.DefaultTickService;
import com.quartercode.disconnected.sim.TickBridgeProvider;
import com.quartercode.disconnected.sim.TickService;
import com.quartercode.disconnected.sim.TickSimulator;
import com.quartercode.disconnected.sim.profile.DefaultProfileService;
import com.quartercode.disconnected.sim.profile.Profile;
import com.quartercode.disconnected.sim.profile.ProfileSerializationException;
import com.quartercode.disconnected.sim.profile.ProfileService;
import com.quartercode.disconnected.util.ApplicationInfo;
import com.quartercode.disconnected.util.ExitUtil;
import com.quartercode.disconnected.util.ExitUtil.ExitProcessor;
import com.quartercode.disconnected.util.RandomPool;
import com.quartercode.disconnected.util.storage.GlobalStorage;
import com.quartercode.disconnected.util.storage.ResourceStore;
import com.quartercode.disconnected.util.storage.ServiceRegistry;
import com.quartercode.disconnected.world.World;
import com.quartercode.disconnected.world.comp.Computer;
import com.quartercode.disconnected.world.comp.os.OperatingSystem;
import com.quartercode.disconnected.world.event.ProgramLaunchCommandEvent;
import com.quartercode.disconnected.world.event.ProgramLaunchCommandEventHandler;
import com.quartercode.disconnected.world.event.ProgramLaunchInfoRequestEvent;
import com.quartercode.disconnected.world.event.ProgramLaunchInfoRequestEventHandler;
import com.quartercode.disconnected.world.gen.WorldGenerator;
import com.quartercode.eventbridge.EventBridgeFactory;
import com.quartercode.eventbridge.bridge.Bridge;
import com.quartercode.eventbridge.bridge.BridgeConnectorException;
import com.quartercode.eventbridge.bridge.EventPredicate;
import com.quartercode.eventbridge.bridge.module.EventHandler;
import com.quartercode.eventbridge.bridge.module.StandardHandlerModule;
import com.quartercode.eventbridge.extra.connector.LocalBridgeConnector;
import com.quartercode.eventbridge.extra.extension.RequestEventHandler;
import com.quartercode.eventbridge.extra.extension.ReturnEventExtensionReturner;
import com.quartercode.eventbridge.extra.predicate.TypePredicate;
import com.quartercode.eventbridge.factory.FactoryManager;

/**
 * The main class which initializes the whole game.
 */
public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    private static final String JAR_NAME;

    static {

        String jarName = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getName();
        JAR_NAME = jarName.endsWith(".jar") ? jarName : null;

    }

    /**
     * The main method which initializes the whole game.
     * 
     * @param args The command line arguments.
     */
    public static void main(String[] args) {

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
        fillEventBridgeFactory();

        // Print information about the software
        LOGGER.info("Version {}", ApplicationInfo.VERSION);

        // Process the command line arguments
        processCommandLineArguments(args);

        // Fill global storage
        LOGGER.info("Filling global storage with common data");
        fillGlobalStorage();

        // Fill resource store
        LOGGER.info("Filling resource store");
        try {
            fillResourceStore();
        } catch (IOException | RuntimeException e) {
            LOGGER.error("Can't fill resource store", e);
            return;
        }

        // Fill default graphics states
        LOGGER.info("Filling default graphics states");
        fillDefaultGraphicsStates();

        // Initialize profile service and load stored profiles
        LOGGER.info("Initializing profile service");
        ProfileService profileService = new DefaultProfileService(new File("profiles"));
        ServiceRegistry.register(ProfileService.class, profileService);

        // Initialize tick service
        LOGGER.info("Initializing tick service");
        TickService tickService = new DefaultTickService();
        ServiceRegistry.register(TickService.class, tickService);
        tickService.addAction(new TickBridgeProvider());
        tickService.addAction(new TickSimulator());

        LOGGER.info("Filling default server handlers");
        fillDefaultServerHandlers(tickService.getAction(TickBridgeProvider.class).getBridge());

        // Initialize graphics service and start it
        LOGGER.info("Initializing graphics service");
        GraphicsService graphicsService = new DefaultGraphicsService();
        ServiceRegistry.register(GraphicsService.class, graphicsService);
        graphicsService.setRunning(true);

        // DEBUG: Connect the client and server bridges
        LOGGER.info("DEBUG-ACTION: Connect the client and server bridges");
        final Bridge clientBridge = graphicsService.getBridge();
        final Bridge serverBridge = tickService.getAction(TickBridgeProvider.class).getBridge();
        try {
            clientBridge.addConnector(new LocalBridgeConnector(serverBridge));
        } catch (BridgeConnectorException e) {
            LOGGER.error("Can't connect the client and server bridges");
            return;
        }

        // DEBUG: Generate and set new simulation
        LOGGER.info("DEBUG-ACTION: Generating new simulation");
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
        LOGGER.info("DEBUG-ACTION: Starting test-game with current simulation");
        tickService.setRunning(true);
        graphicsService.setState(DefaultStates.DESKTOP.create());
    }

    /**
     * Adds the default custom mappings to the {@link EventBridgeFactory}.
     */
    public static void fillEventBridgeFactory() {

        FactoryManager factoryManager = EventBridgeFactory.getFactoryManager();

        factoryManager.setFactory(HandleInvocationProviderExtension.class, new DefaultHandleInvocationProviderExtensionFactory());
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

    /**
     * Fills the {@link GlobalStorage} with the default values for the context path and default themes.
     */
    public static void fillGlobalStorage() {

        GlobalStorage.put("contextPath", Classmod.CONTEXT_PATH);
        GlobalStorage.put("contextPath", "com.quartercode.disconnected.sim.scheduler");
        GlobalStorage.put("contextPath", "com.quartercode.disconnected.world");
        GlobalStorage.put("contextPath", "com.quartercode.disconnected.world.comp");
        GlobalStorage.put("contextPath", "com.quartercode.disconnected.world.comp.attack");
        GlobalStorage.put("contextPath", "com.quartercode.disconnected.world.comp.file");
        GlobalStorage.put("contextPath", "com.quartercode.disconnected.world.comp.hardware");
        GlobalStorage.put("contextPath", "com.quartercode.disconnected.world.comp.net");
        GlobalStorage.put("contextPath", "com.quartercode.disconnected.world.comp.os");
        GlobalStorage.put("contextPath", "com.quartercode.disconnected.world.comp.program");
        GlobalStorage.put("contextPath", "com.quartercode.disconnected.world.general");

        GlobalStorage.put("themes", Main.class.getResource("/ui/default/default.xml"));
        GlobalStorage.put("themes", Main.class.getResource("/ui/desktop/desktop.xml"));
    }

    /**
     * Fills the global {@link ResourceStore} with the default resource objects which are needed for running Disconnected.
     * 
     * @throws IOException Something goes wrong while reading from a jar file or resource.
     */
    public static void fillResourceStore() throws IOException {

        ResourceStore.loadFromClasspath("/data");
    }

    /**
     * Adds the default {@link GraphicsModule}s to the default {@link GraphicsState}s declared in {@link DefaultStates}.
     */
    public static void fillDefaultGraphicsStates() {

        DefaultStates.DESKTOP.addModule(DesktopWidgetModule.class, "desktopWidget", 100);
        DefaultStates.DESKTOP.addModule(DesktopWindowAreaModule.class, "windowArea", 80);
        DefaultStates.DESKTOP.addModule(DesktopLaunchButtonModule.class, "launchButton", 80);
        DefaultStates.DESKTOP.addModule(DesktopTaskbarModule.class, "taskbar", 80);

        fillDefaultDesktopPrograms();
    }

    /**
     * Adds the default {@link DesktopProgramDescriptor}s to the {@link DesktopPrograms} list.
     * 
     * @see DesktopPrograms
     */
    public static void fillDefaultDesktopPrograms() {

        DesktopPrograms.addDescriptor(new FileManagerProgram());
    }

    /**
     * Adds the default tick server {@link EventHandler}s to the given {@link Bridge}.
     * 
     * @param bridge The bridge to add the default tick server handlers to.
     */
    public static void fillDefaultServerHandlers(Bridge bridge) {

        addRequestHandler(bridge, new ProgramLaunchInfoRequestEventHandler(), new TypePredicate<>(ProgramLaunchInfoRequestEvent.class));
        addEventHandler(bridge, new ProgramLaunchCommandEventHandler(), new TypePredicate<>(ProgramLaunchCommandEvent.class));
    }

    private static void addEventHandler(Bridge bridge, EventHandler<?> handler, EventPredicate<?> predicate) {

        bridge.getModule(StandardHandlerModule.class).addHandler(handler, predicate);
    }

    private static void addRequestHandler(Bridge bridge, RequestEventHandler<?> requestEventHandler, EventPredicate<?> predicate) {

        bridge.getModule(ReturnEventExtensionReturner.class).addRequestHandler(requestEventHandler, predicate);
    }

    private Main() {

    }

}
