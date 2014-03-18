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

import com.quartercode.disconnected.graphics.GraphicsModule;
import com.quartercode.disconnected.graphics.GraphicsState;

/**
 * A desktop program manager is a {@link GraphicsModule} that allows opening and closing {@link DesktopProgramWindow}s.
 * The interface should be implemented by a module that is then added to a graphics state.
 * The function could be adding the window to a window area and to the taskbar.
 * A manager is used by other modules to execute the actions without depending on other modules.
 * 
 * @see DesktopProgramWindow
 */
public interface DesktopProgramManager extends GraphicsModule {

    /**
     * Opens the given {@link DesktopProgramWindow} on the given desktop {@link GraphicsState}.
     * For example, the method could add the window to the window area or to the taskbar.
     * 
     * @param state The desktop {@link GraphicsState} the given {@link DesktopProgramWindow} should be added to.
     * @param window The {@link DesktopProgramWindow} that should be opened on the givn desktop {@link GraphicsState}.
     */
    public void addWindow(GraphicsState state, DesktopProgramWindow window);

    /**
     * Closes the given {@link DesktopProgramWindow} that was open on the given desktop {@link GraphicsState}.
     * For example, the method could remove the window from the window area or from the taskbar.
     * 
     * @param state The desktop {@link GraphicsState} the given {@link DesktopProgramWindow} should be removed from.
     * @param window The {@link DesktopProgramWindow} that should be closed on the given desktop {@link GraphicsState}.
     */
    public void removeWindow(GraphicsState state, DesktopProgramWindow window);

}
