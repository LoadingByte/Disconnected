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

import com.quartercode.disconnected.client.graphics.DefaultStates;
import com.quartercode.disconnected.client.graphics.GraphicsModule;
import com.quartercode.disconnected.client.graphics.GraphicsService;
import com.quartercode.disconnected.client.graphics.GraphicsState;
import com.quartercode.disconnected.client.graphics.desktop.DesktopLaunchButtonModule;
import com.quartercode.disconnected.client.graphics.desktop.DesktopProgramDescriptor;
import com.quartercode.disconnected.client.graphics.desktop.DesktopPrograms;
import com.quartercode.disconnected.client.graphics.desktop.DesktopTaskbarModule;
import com.quartercode.disconnected.client.graphics.desktop.DesktopWidgetModule;
import com.quartercode.disconnected.client.graphics.desktop.DesktopWindowAreaModule;
import com.quartercode.disconnected.client.graphics.desktop.program.general.FileManagerDesktopProgram;

/**
 * This class contains methods that configure everything the client needs.
 * For example, a method could load some data into a storage class or add some values to a service configuration.
 */
public class DefaultClientData {

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

        DesktopPrograms.addDescriptor(new FileManagerDesktopProgram());
    }

    /**
     * Adds the default themes to the given {@link GraphicsService}.
     * 
     * @param graphicsService The graphics service the default themes should be added to.
     * @see GraphicsService#getThemes()
     */
    public static void addDefaultGraphicsServiceThemes(GraphicsService graphicsService) {

        graphicsService.addTheme(DefaultClientData.class.getResource("/ui/default/default.xml"));
        graphicsService.addTheme(DefaultClientData.class.getResource("/ui/desktop/desktop.xml"));
    }

    private DefaultClientData() {

    }

}
