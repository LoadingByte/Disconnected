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

package com.quartercode.disconnected.client.graphics.desktop;

import java.util.ArrayList;
import java.util.List;
import com.quartercode.disconnected.client.graphics.GraphicsState;
import com.quartercode.disconnected.client.graphics.component.MultiactionButton;
import com.quartercode.disconnected.client.util.ResourceBundleGroup;
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

    private final DesktopProgramDescriptor descriptor;
    private final DesktopProgramContext    context;
    private final MultiactionButton        taskbarButton;

    private final List<DesktopWindow>      openPopups = new ArrayList<>();

    /**
     * Creates a new desktop program window in the given desktop {@link GraphicsState}.
     * The constructor was called by the given {@link DesktopProgramDescriptor}.
     * The new window is <b>not</b> added to the desktop automatically.
     * 
     * @param state The desktop state the new program will be running in.
     * @param descriptor The program descriptor that created the object.
     * @param context The {@link DesktopProgramContext} that contains information about the environment of the program.
     */
    public DesktopProgramWindow(GraphicsState state, DesktopProgramDescriptor descriptor, DesktopProgramContext context) {

        super(state);

        this.descriptor = descriptor;
        this.context = context;

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
     * Returns a {@link DesktopProgramContext} which contains information about the environment of the program.
     * 
     * @return The {@link DesktopProgramContext} which is assigned to the program.
     */
    public DesktopProgramContext getContext() {

        return context;
    }

    @Override
    public void setVisible(boolean visible) {

        super.setVisible(visible);

        taskbarButton.setTheme(visible ? "taskbar-button-active" : "taskbar-button-inactive");
        taskbarButton.reapplyTheme();

        // Toggle visibility for all popups
        for (DesktopWindow popup : openPopups) {
            popup.setVisible(visible);
        }
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

    protected void openPopup(final DesktopWindow popup, boolean modal) {

        // Can't open new popup while a modal popup is present
        if (!isEnabled()) {
            throw new IllegalStateException("Cannot open new popups while program window is disabled");
        }

        // Add new popup to list
        openPopups.add(popup);

        // Add close listener that removes the popup from the list when it is closed
        popup.addCloseListener(new Runnable() {

            @Override
            public void run() {

                openPopups.remove(popup);
            }

        });

        // Center the popup relative to the parent window
        new DesktopWindowCenteringMediator(popup, this);

        if (modal) {
            // Disable the main program window if the popup is modal
            setEnabled(false);

            // Add close listener that enables the main program window when the popup is closed
            popup.addCloseListener(new Runnable() {

                @Override
                public void run() {

                    setEnabled(true);
                }

            });
        }

        // Show the popup
        popup.setVisible(true);
    }

}
