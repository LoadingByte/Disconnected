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

package com.quartercode.disconnected.server;

import java.io.IOException;
import com.quartercode.classmod.Classmod;
import com.quartercode.classmod.util.TreeInitializer;
import com.quartercode.disconnected.server.bridge.ClientAwareEventHandler;
import com.quartercode.disconnected.server.bridge.ClientAwareHandlerExtension;
import com.quartercode.disconnected.server.bridge.ClientIdentityExtension;
import com.quartercode.disconnected.server.bridge.DefaultClientAwareHandlerExtension.DefaultClientAwareHandlerExtensionFactory;
import com.quartercode.disconnected.server.bridge.DefaultClientIdentityExtension.DefaultClientIdentityExtensionFactory;
import com.quartercode.disconnected.server.sim.TickSchedulerUpdater;
import com.quartercode.disconnected.server.sim.profile.ProfileSerializer;
import com.quartercode.disconnected.server.util.ResourceStore;
import com.quartercode.disconnected.server.world.comp.file.ContentFile;
import com.quartercode.disconnected.server.world.comp.file.Directory;
import com.quartercode.disconnected.server.world.comp.file.RootFile;
import com.quartercode.disconnected.server.world.comp.file.StringFileTypeMapper;
import com.quartercode.disconnected.server.world.comp.hardware.NodeNetInterface;
import com.quartercode.disconnected.server.world.comp.hardware.RouterNetInterface;
import com.quartercode.disconnected.server.world.comp.os.Session;
import com.quartercode.disconnected.server.world.comp.program.ProgramCommonLocationMapper;
import com.quartercode.disconnected.server.world.comp.program.WorldProcessLaunchCommandHandler;
import com.quartercode.disconnected.server.world.comp.program.general.FileManagerProgram;
import com.quartercode.disconnected.shared.comp.program.GeneralProgramConstants;
import com.quartercode.disconnected.shared.comp.program.SystemProgramConstants;
import com.quartercode.disconnected.shared.event.program.control.WorldProcessLaunchCommand;
import com.quartercode.eventbridge.EventBridgeFactory;
import com.quartercode.eventbridge.bridge.Bridge;
import com.quartercode.eventbridge.bridge.EventPredicate;
import com.quartercode.eventbridge.bridge.module.EventHandler;
import com.quartercode.eventbridge.extra.predicate.TypePredicate;
import com.quartercode.eventbridge.factory.FactoryManager;

/**
 * This class contains methods that configure everything the server needs.
 * For example, a method could load some data into a storage class or add some values to a service configuration.
 */
public class DefaultServerData {

    // ----- General -----

    /**
     * Adds the default custom server mappings to the {@link EventBridgeFactory}.
     */
    public static void addCustomEventBridgeFactoryMappings() {

        FactoryManager factoryManager = EventBridgeFactory.getFactoryManager();

        factoryManager.setFactory(ClientIdentityExtension.class, new DefaultClientIdentityExtensionFactory());
        factoryManager.setFactory(ClientAwareHandlerExtension.class, new DefaultClientAwareHandlerExtensionFactory());
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
        ProfileSerializer.appendToWorldContextPath("com.quartercode.disconnected.server.sim.scheduler");

        // World
        ProfileSerializer.appendToWorldContextPath("com.quartercode.disconnected.server.world");
        ProfileSerializer.appendToWorldContextPath("com.quartercode.disconnected.server.world.comp");
        ProfileSerializer.appendToWorldContextPath("com.quartercode.disconnected.server.world.comp.attack");
        ProfileSerializer.appendToWorldContextPath("com.quartercode.disconnected.server.world.comp.file");
        ProfileSerializer.appendToWorldContextPath("com.quartercode.disconnected.server.world.comp.hardware");
        ProfileSerializer.appendToWorldContextPath("com.quartercode.disconnected.server.world.comp.net");
        ProfileSerializer.appendToWorldContextPath("com.quartercode.disconnected.server.world.comp.os");
        ProfileSerializer.appendToWorldContextPath("com.quartercode.disconnected.server.world.comp.program");
        ProfileSerializer.appendToWorldContextPath("com.quartercode.disconnected.server.world.general");

        // Shared
        ProfileSerializer.appendToWorldContextPath("com.quartercode.disconnected.shared.file");
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
     * Adds the scheduler groups with their priorities to the given {@link TickSchedulerUpdater}.
     * 
     * @param schedulerUpdater The tick scheduler update the groups should be added to.
     */
    public static void addDefaultSchedulerGroups(TickSchedulerUpdater schedulerUpdater) {

        schedulerUpdater.addGroup("computerNetworkUpdate", 700);
        schedulerUpdater.addGroup("computerProgramUpdate", 500);
    }

    /**
     * Adds the default tick server {@link EventHandler}s to the given {@link Bridge}.
     * 
     * @param bridge The bridge to add the default tick server handlers to.
     */
    public static void addDefaultServerHandlers(Bridge bridge) {

        addClientAwareEventHandler(bridge, new WorldProcessLaunchCommandHandler(), new TypePredicate<>(WorldProcessLaunchCommand.class));
    }

    private static void addClientAwareEventHandler(Bridge bridge, ClientAwareEventHandler<?> handler, EventPredicate<?> predicate) {

        bridge.getModule(ClientAwareHandlerExtension.class).addHandler(handler, predicate);
    }

    /**
     * Adds the default {@link StringFileTypeMapper} mappings for the default file types.
     * The following mappings are added:
     * 
     * <table>
     * <tr>
     * <th>Class Type</th>
     * <td>{@link RootFile}</td>
     * <td>{@link Directory}</td>
     * <td>{@link ContentFile}</td>
     * </tr>
     * <tr>
     * <th>String Type</th>
     * <td>rootFile</td>
     * <td>directory</td>
     * <td>contentFile</td>
     * </tr>
     * </table>
     */
    public static void addDefaultStringFileTypeMappings() {

        StringFileTypeMapper.addMapping(RootFile.class, "rootFile");
        StringFileTypeMapper.addMapping(ContentFile.class, "contentFile");
        StringFileTypeMapper.addMapping(Directory.class, "directory");
    }

    /**
     * Adds the default {@link ProgramCommonLocationMapper} mappings for the stock programs.
     */
    public static void addDefaultProgramCommonLocationMappings() {

        // System programs
        ProgramCommonLocationMapper.addMapping(Session.class, SystemProgramConstants.COMLOC_SESSION);

        // General programs
        ProgramCommonLocationMapper.addMapping(FileManagerProgram.class, GeneralProgramConstants.COMLOC_FILE_MANAGER);
    }

    private DefaultServerData() {

    }

}
