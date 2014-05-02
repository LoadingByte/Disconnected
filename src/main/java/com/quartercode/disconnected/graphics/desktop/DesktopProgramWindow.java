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
import de.matthiasmann.twl.Widget;

/**
 * A desktop program window is the core component of a desktop program.
 * It extends the {@link DesktopWindow} class and adds some extra functionality that is exclusive to programs.<br>
 * <br>
 * Implementations inherit from this class and add their components and program logic.
 * Desktop program windows are declared and created by {@link DesktopProgramDescriptor}s that also store common data.
 */
public class DesktopProgramWindow extends DesktopWindow {

    private final DesktopProgramDescriptor   descriptor;
    private final DesktopProgramWorldContext worldContext;
    private final MultiactionButton          taskbarButton;

    /**
     * Creates a new desktop program window in the given desktop {@link GraphicsState}.
     * The constructor was called by the given {@link DesktopProgramDescriptor}.
     * The new window is <b>not</b> added to the desktop automatically.
     * 
     * @param state The desktop state the new program will be running in.
     * @param descriptor The program descriptor that created the object.
     * @param worldContext The {@link DesktopProgramWorldContext} that contains information about the environment of the program.
     */
    public DesktopProgramWindow(GraphicsState state, DesktopProgramDescriptor descriptor, DesktopProgramWorldContext worldContext) {

        super(state);

        this.descriptor = descriptor;
        this.worldContext = worldContext;

        new DesktopWindowCenteringMediator(this);
        setTitle(descriptor.getName());

        taskbarButton = new MultiactionButton();
        taskbarButton.setTheme("taskbar-button-active");
        taskbarButton.setText(descriptor.getName());

        // Left click on the taskbar button minimizes/maximizes the window
        taskbarButton.addCallback(Event.MOUSE_LBUTTON, new Runnable() {

            @Override
            public void run() {

                setVisible(!isVisible());
            }
        });

        // Right click on the taskbar button closes the window
        taskbarButton.addCallback(Event.MOUSE_RBUTTON, new Runnable() {

            @Override
            public void run() {

                close();
            }
        });
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
     * Returns a {@link DesktopProgramWorldContext} which contains information about the environment of the program.
     * 
     * @return The {@link DesktopProgramWorldContext} which is assigned to the program.
     */
    public DesktopProgramWorldContext getWorldContext() {

        return worldContext;
    }

    @Override
    public void setVisible(boolean visible) {

        super.setVisible(visible);

        taskbarButton.setTheme(visible ? "taskbar-button-active" : "taskbar-button-inactive");
        taskbarButton.reapplyTheme();
    }

    @Override
    protected void open() {

        super.open();

        ((Widget) getState().getModule("taskbar").getValue("layout")).add(taskbarButton);
    }

    @Override
    public void close() {

        super.close();

        taskbarButton.getParent().removeChild(taskbarButton);

        // Close all popups that are still open
        for (DesktopWindow popup : openPopups) {
            if (popup.isVisible() && popup.getParent() != null) {
                popup.close();
            }
        }
    }

    // ----- Utility -----

    /**
     * Returns the i18n string that is associated with the given key in the {@link ResourceBundleGroup} of the descriptor.
     * 
     * @param key The key the returned value is associated with.
     * @return The i18n string which is associated with the given key.
     */
    protected String getString(String key) {

        return descriptor.getResourceBundle().getString(key);
    }

}
