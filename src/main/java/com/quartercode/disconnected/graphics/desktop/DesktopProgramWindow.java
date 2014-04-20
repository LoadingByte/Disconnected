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
import com.quartercode.disconnected.graphics.component.MultiactionButton;
import com.quartercode.disconnected.util.ResourceBundleGroup;
import de.matthiasmann.twl.Event;
import de.matthiasmann.twl.ResizableFrame;

/**
 * A desktop program window basically is the core component of a desktop program.
 * It is a {@link ResizableFrame}. Programs inherit from the class and add their components and program logic.
 * Desktop program windows are declared and created by {@link DesktopProgramDescriptor}s that also store common data.
 */
public class DesktopProgramWindow extends ResizableFrame {

    private final DesktopProgramDescriptor descriptor;
    private final GraphicsState            state;
    private final MultiactionButton        taskbarButton;

    /**
     * Creates a new desktop program window in the given desktop {@link GraphicsState}.
     * The constructor was called by the given {@link DesktopProgramDescriptor}.
     * The new window is <b>not</b> automatically added to the desktop {@link GraphicsState}.
     * 
     * @param descriptor The {@link DesktopProgramDescriptor} that created the object.
     * @param state The desktop {@link GraphicsState} the new program will be running in.
     */
    public DesktopProgramWindow(DesktopProgramDescriptor descriptor, GraphicsState state) {

        this.descriptor = descriptor;
        this.state = state;

        setTheme("frame");
        setTitle(descriptor.getName());
        addCloseCallback(new Runnable() {

            @Override
            public void run() {

                close();
            }
        });

        taskbarButton = new MultiactionButton();
        taskbarButton.setTheme("taskbar-button-active");
        taskbarButton.setText(descriptor.getName());
        taskbarButton.addCallback(Event.MOUSE_LBUTTON, new Runnable() {

            @Override
            public void run() {

                setVisible(!isVisible());
            }
        });
        taskbarButton.addCallback(Event.MOUSE_RBUTTON, new Runnable() {

            @Override
            public void run() {

                close();
            }
        });

        setVisible(true);
    }

    /**
     * Returns the {@link DesktopProgramDescriptor} that created the window.
     * It defines all the properties of the window.
     * 
     * @return The desktop program descriptor that defines the window.
     */
    public DesktopProgramDescriptor getDescriptor() {

        return descriptor;
    }

    /**
     * Returns the desktop {@link GraphicsState} the program is running in.
     * 
     * @return The graphics state that uses the program.
     */
    protected GraphicsState getState() {

        return state;
    }

    /**
     * Returns a {@link MultiactionButton} that can be used as a short representation of the program in the taskbar.
     * A left click on the button toggles the visibility of the window, a right click closes it.
     * 
     * @return The button for representing the program in the taskbar.
     */
    public MultiactionButton getTaskbarButton() {

        return taskbarButton;
    }

    @Override
    public void setVisible(boolean visible) {

        if (visible != isVisible()) {
            super.setVisible(visible);

            if (visible) {
                requestKeyboardFocus();
            }
        }

        taskbarButton.setTheme(visible ? "taskbar-button-active" : "taskbar-button-inactive");
        taskbarButton.reapplyTheme();
    }

    /**
     * Closes the window and removes it from the desktop {@link GraphicsState} that was set during construction.
     * The method uses the {@link DesktopProgramManager} with the name <code>programManager</code> to remove the window.
     */
    public void close() {

        ((DesktopProgramManager) state.getModule("programManager")).removeWindow(state, this);
    }

}
