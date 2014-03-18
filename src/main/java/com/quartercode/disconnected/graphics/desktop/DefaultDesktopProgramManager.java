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

import com.quartercode.disconnected.graphics.AbstractGraphicsModule;
import com.quartercode.disconnected.graphics.GraphicsState;
import de.matthiasmann.twl.Widget;

/**
 * The default desktop program manager updates the window area and the taskbar on window changes.
 * See {@link DesktopProgramManager} for more detail on what a program manager is.
 * 
 * @see DesktopProgramManager
 * @see DesktopProgramWindow
 */
public class DefaultDesktopProgramManager extends AbstractGraphicsModule implements DesktopProgramManager {

    @Override
    public void addWindow(GraphicsState state, DesktopProgramWindow window) {

        ((Widget) state.getModule("windowArea").getValue("area")).add(window);
        ((Widget) state.getModule("taskbar").getValue("layout")).add(window.getTaskbarButton());
    }

    @Override
    public void removeWindow(GraphicsState state, DesktopProgramWindow window) {

        ((Widget) state.getModule("windowArea").getValue("area")).removeChild(window);
        ((Widget) state.getModule("taskbar").getValue("layout")).removeChild(window.getTaskbarButton());
    }

}
