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

package com.quartercode.disconnected.client;

import java.io.IOException;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.quartercode.disconnected.client.graphics.DefaultGraphicsService;
import com.quartercode.disconnected.client.graphics.DefaultStates;
import com.quartercode.disconnected.client.graphics.GraphicsService;
import com.quartercode.disconnected.client.graphics.desktop.DesktopLaunchButtonModule;
import com.quartercode.disconnected.client.graphics.desktop.DesktopTaskbarModule;
import com.quartercode.disconnected.client.graphics.desktop.DesktopWidgetModule;
import com.quartercode.disconnected.client.graphics.desktop.DesktopWindowAreaModule;
import com.quartercode.disconnected.client.registry.ClientRegistries;
import com.quartercode.disconnected.client.registry.Theme;
import com.quartercode.disconnected.client.registry.config.ConfigureClientProgramCommand;
import com.quartercode.disconnected.client.registry.config.ConfigureThemeCommand;
import com.quartercode.disconnected.client.util.TWLSpritesheetGenerator;
import com.quartercode.disconnected.shared.config.ConfigService;
import com.quartercode.disconnected.shared.config.util.RemoveNamedValueCommand;
import com.quartercode.disconnected.shared.registry.Registries;
import com.quartercode.disconnected.shared.util.IOFileUtils;
import com.quartercode.disconnected.shared.util.ResourceLister;
import com.quartercode.disconnected.shared.util.ServiceRegistry;
import com.quartercode.disconnected.shared.util.TempFileManager;

/**
 * This class contains the {@link #initialize()} method that configures everything that the server needs.
 * For example, the method could add some service implementations.
 */
public class ClientInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientInitializer.class);

    private static boolean      initialized;

    /**
     * Initializes everything that the server needs.
     * For example, this method could add some service implementations.
     */
    public static void initialize() {

        if (initialized) {
            return;
        }
        initialized = true;

        // Add client config command parsers
        addConfigCommandParsers(ServiceRegistry.lookup(ConfigService.class));

        // Initialize client services
        LOGGER.info("Initializing and starting graphics service");
        initializeGraphicsService();
    }

    private static void addConfigCommandParsers(ConfigService service) {

        // Themes
        service.addCommand("configureTheme", new ConfigureThemeCommand(Registries.get(ClientRegistries.THEMES)));
        service.addCommand("removeTheme", new RemoveNamedValueCommand<>("theme", Registries.get(ClientRegistries.THEMES)));

        // Client programs
        service.addCommand("configureClientProgram", new ConfigureClientProgramCommand(Registries.get(ClientRegistries.CLIENT_PROGRAMS)));
        service.addCommand("removeClientProgram", new RemoveNamedValueCommand<>("client program", Registries.get(ClientRegistries.CLIENT_PROGRAMS)));
    }

    private static void initializeGraphicsService() {

        GraphicsService graphicsService = new DefaultGraphicsService();
        ServiceRegistry.register(GraphicsService.class, graphicsService);

        initializeDefaultGraphicsStates();
        generateSpritesheets(graphicsService);

        graphicsService.setRunning(true);
    }

    private static void initializeDefaultGraphicsStates() {

        DefaultStates.DESKTOP.addModule(DesktopWidgetModule.class, "desktopWidget", 100);
        DefaultStates.DESKTOP.addModule(DesktopWindowAreaModule.class, "windowArea", 90);
        DefaultStates.DESKTOP.addModule(DesktopLaunchButtonModule.class, "launchButton", 80);
        DefaultStates.DESKTOP.addModule(DesktopTaskbarModule.class, "taskbar", 80);
    }

    private static void generateSpritesheets(GraphicsService graphicsService) {

        // Generate spritesheets
        LOGGER.debug("Generating spritesheets");
        try (ResourceLister resourceLister = new ResourceLister("/ui/sprites")) {
            // Assume that there is only one sprites directory on the classpath
            Path spritesDir = resourceLister.getResourcePaths().get(0);

            // Copy the sprites into a temporary directory to avoid jar problems
            Path tmpSpritesDir = TempFileManager.getTempDir().resolve("sprites");
            IOFileUtils.copyDirectory(spritesDir, tmpSpritesDir);

            // Generate the spritesheets and add the resulting twl config theme
            Path spriteTheme = TWLSpritesheetGenerator.generate(tmpSpritesDir, TempFileManager.getTempDir().resolve("spritesheets"));
            Registries.get(ClientRegistries.THEMES).addValue(new Theme("spritesheetTwlConfig", spriteTheme.toUri().toURL(), 1000000));
        } catch (IOException e) {
            throw new RuntimeException("Cannot generate sprite theme", e);
        }
    }

    private ClientInitializer() {

    }

}
