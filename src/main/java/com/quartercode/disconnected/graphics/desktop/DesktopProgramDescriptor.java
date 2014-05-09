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

package com.quartercode.disconnected.graphics.desktop;

import com.quartercode.disconnected.graphics.GraphicsState;
import com.quartercode.disconnected.util.ResourceBundleGroup;

/**
 * A desktop program descriptor declares some generic data of a desktop program and allows to create new instances.
 * The class should be inherited for each desktop program in order to make the {@link #create(GraphicsState, DesktopProgramContext)} method type-safe.<br>
 * <br>
 * Desktop programs are programs that only have a gui and don't run as a process. They only support the ui.
 * However, such programs create normal processes in order to execute operations.
 * 
 * @see DesktopProgramWindow
 */
public abstract class DesktopProgramDescriptor {

    private final ResourceBundleGroup resourceBundle;
    private final String              nameKey;

    /**
     * Creates a new desktop program descriptor.
     * The name is retrieved from the given {@link ResourceBundleGroup} with the given key.
     * 
     * @param resourceBundle The resource bundle group that provides resources for the program descriptor.
     * @param nameKey The key the name of the described program is provided under in the given resource bundle group.
     */
    public DesktopProgramDescriptor(ResourceBundleGroup resourceBundle, String nameKey) {

        this.resourceBundle = resourceBundle;
        this.nameKey = nameKey;
    }

    /**
     * Returns the name of the program that is described.
     * The name is fetched from the underlying {@link ResourceBundleGroup} every time the method is called.
     * 
     * @return The name of the described program.
     */
    public String getName() {

        return resourceBundle.get().getString(nameKey);
    }

    /**
     * Returns the {@link ResourceBundleGroup} that provides resources for the program descriptor.
     * It may be used by any provided {@link DesktopProgramWindow}.
     * 
     * @return The resource bundle group of the defined program.
     */
    public ResourceBundleGroup getResourceBundle() {

        return resourceBundle;
    }

    /**
     * Creates a new instance of the described desktop program that is running in the given {@link GraphicsState}.
     * The method actually returns a {@link DesktopProgramWindow}.
     * It should be implemented by every desktop program for keeping type-safety.
     * 
     * @param state The {@link GraphicsState} the created {@link DesktopProgramWindow} is running in.
     * @param context The {@link DesktopProgramContext} that contains information about the environment of the program.
     * @return The new instance of the described program as a {@link DesktopProgramWindow}.
     */
    public abstract DesktopProgramWindow create(GraphicsState state, DesktopProgramContext context);

}
