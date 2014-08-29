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

import java.io.IOException;
import com.quartercode.classmod.Classmod;
import com.quartercode.classmod.util.TreeInitializer;
import com.quartercode.disconnected.bridge.HandleInvocationProviderExtension;
import com.quartercode.disconnected.bridge.def.DefaultHandleInvocationProviderExtension.DefaultHandleInvocationProviderExtensionFactory;
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
import com.quartercode.disconnected.sim.profile.ProfileSerializer;
import com.quartercode.disconnected.util.storage.ResourceStore;
import com.quartercode.disconnected.world.comp.hardware.NodeNetInterface;
import com.quartercode.disconnected.world.comp.hardware.RouterNetInterface;
import com.quartercode.disconnected.world.event.ProgramLaunchCommandEvent;
import com.quartercode.disconnected.world.event.ProgramLaunchCommandEventHandler;
import com.quartercode.disconnected.world.event.ProgramLaunchInfoRequestEvent;
import com.quartercode.disconnected.world.event.ProgramLaunchInfoRequestEventHandler;
import com.quartercode.eventbridge.EventBridgeFactory;
import com.quartercode.eventbridge.bridge.Bridge;
import com.quartercode.eventbridge.bridge.EventPredicate;
import com.quartercode.eventbridge.bridge.module.EventHandler;
import com.quartercode.eventbridge.bridge.module.StandardHandlerModule;
import com.quartercode.eventbridge.extra.extension.RequestEventHandler;
import com.quartercode.eventbridge.extra.extension.ReturnEventExtensionReturner;
import com.quartercode.eventbridge.extra.predicate.TypePredicate;
import com.quartercode.eventbridge.factory.FactoryManager;

/**
 * This class contains methods that configure everything in the game.
 * For example, a method could load some data into a storage class or add some values to a service configuration.
 */
public class DefaultData {

    // ----- General -----

    /**
     * Adds the default custom mappings to the {@link EventBridgeFactory}.
     */
    public static void addCustomEventBridgeFactoryMappings() {

        FactoryManager factoryManager = EventBridgeFactory.getFactoryManager();

        factoryManager.setFactory(HandleInvocationProviderExtension.class, new DefaultHandleInvocationProviderExtensionFactory());
    }

    /**
     * Fills the global {@link ResourceStore} with the default resource objects which are needed for running the game.
     * 
     * @throws IOException Something goes wrong while reading from a jar file or resource.
     */
    public static void fillResourceStore() throws IOException {

        ResourceStore.loadFromClasspath("/data");
    }

    // ----- Profiles -----

    /**
     * Appends the default packages to the world context path.
     * That includes the classmod context path {@link Classmod#CONTEXT_PATH}.
     * 
     * @see ProfileSerializer#appendToWorldContextPath(String)
     */
    public static void addDefaultWorldContextPath() {

        // Classmod
        ProfileSerializer.appendToWorldContextPath(Classmod.CONTEXT_PATH);

        // Utility
        ProfileSerializer.appendToWorldContextPath("com.quartercode.disconnected.sim.scheduler");

        // World
        ProfileSerializer.appendToWorldContextPath("com.quartercode.disconnected.world");
        ProfileSerializer.appendToWorldContextPath("com.quartercode.disconnected.world.comp");
        ProfileSerializer.appendToWorldContextPath("com.quartercode.disconnected.world.comp.attack");
        ProfileSerializer.appendToWorldContextPath("com.quartercode.disconnected.world.comp.file");
        ProfileSerializer.appendToWorldContextPath("com.quartercode.disconnected.world.comp.hardware");
        ProfileSerializer.appendToWorldContextPath("com.quartercode.disconnected.world.comp.net");
        ProfileSerializer.appendToWorldContextPath("com.quartercode.disconnected.world.comp.os");
        ProfileSerializer.appendToWorldContextPath("com.quartercode.disconnected.world.comp.program");
        ProfileSerializer.appendToWorldContextPath("com.quartercode.disconnected.world.general");
    }

    /**
     * Adds the default world initializer mappings to the {@link TreeInitializer} which is provided by the {@link ProfileSerializer}.
     * 
     * @see ProfileSerializer#getWorldInitializer()
     */
    public static void addDefaultWorldInitializerMappings() {

        TreeInitializer worldInitializer = ProfileSerializer.getWorldInitializer();

        worldInitializer.addInitializationDefinition(NodeNetInterface.class, NodeNetInterface.CONNECTION);
        worldInitializer.addInitializationDefinition(RouterNetInterface.class, RouterNetInterface.BACKBONE_CONNECTION);
        worldInitializer.addInitializationDefinition(RouterNetInterface.class, RouterNetInterface.NEIGHBOURS);
    }

    // ----- Simulation -----

    /**
     * Adds the default tick server {@link EventHandler}s to the given {@link Bridge}.
     * 
     * @param bridge The bridge to add the default tick server handlers to.
     */
    public static void addDefaultServerHandlers(Bridge bridge) {

        addRequestHandler(bridge, new ProgramLaunchInfoRequestEventHandler(), new TypePredicate<>(ProgramLaunchInfoRequestEvent.class));
        addEventHandler(bridge, new ProgramLaunchCommandEventHandler(), new TypePredicate<>(ProgramLaunchCommandEvent.class));
    }

    private static void addEventHandler(Bridge bridge, EventHandler<?> handler, EventPredicate<?> predicate) {

        bridge.getModule(StandardHandlerModule.class).addHandler(handler, predicate);
    }

    private static void addRequestHandler(Bridge bridge, RequestEventHandler<?> requestEventHandler, EventPredicate<?> predicate) {

        bridge.getModule(ReturnEventExtensionReturner.class).addRequestHandler(requestEventHandler, predicate);
    }

    // ----- Graphics -----

    /**
     * Adds the default {@link GraphicsModule}s to the default {@link GraphicsState}s that are declared in {@link DefaultStates}.
     * Also invokes the {@link #addDefaultDesktopPrograms()} method.
     */
    public static void initializeDefaultGraphicsStates() {

        DefaultStates.DESKTOP.addModule(DesktopWidgetModule.class, "desktopWidget", 100);
        DefaultStates.DESKTOP.addModule(DesktopWindowAreaModule.class, "windowArea", 80);
        DefaultStates.DESKTOP.addModule(DesktopLaunchButtonModule.class, "launchButton", 80);
        DefaultStates.DESKTOP.addModule(DesktopTaskbarModule.class, "taskbar", 80);

        addDefaultDesktopPrograms();
    }

    /**
     * Adds the default {@link DesktopProgramDescriptor}s to the {@link DesktopPrograms} list.
     * 
     * @see DesktopPrograms
     */
    public static void addDefaultDesktopPrograms() {

        DesktopPrograms.addDescriptor(new FileManagerProgram());
    }

    /**
     * Adds the default themes to the given {@link GraphicsService}.
     * 
     * @param graphicsService The graphics service the default themes should be added to.
     * @see GraphicsService#getThemes()
     */
    public static void addDefaultGraphicsServiceThemes(GraphicsService graphicsService) {

        graphicsService.addTheme(Main.class.getResource("/ui/default/default.xml"));
        graphicsService.addTheme(Main.class.getResource("/ui/desktop/desktop.xml"));
    }

    private DefaultData() {

    }

}
