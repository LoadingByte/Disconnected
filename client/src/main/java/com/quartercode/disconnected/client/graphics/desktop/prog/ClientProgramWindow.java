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

package com.quartercode.disconnected.client.graphics.desktop.prog;

import java.util.ArrayList;
import java.util.List;
import com.quartercode.disconnected.client.graphics.GraphicsState;
import com.quartercode.disconnected.client.graphics.component.MultiactionButton;
import com.quartercode.disconnected.client.graphics.desktop.DesktopWindow;
import com.quartercode.disconnected.client.graphics.desktop.DesktopWindowCenteringMediator;
import de.matthiasmann.twl.Event;
import de.matthiasmann.twl.Widget;

/**
 * A client program window can be used by a {@link ClientProgramExecutor} to display a GUI window.
 * It extends the {@link DesktopWindow} class and adds some extra functionality that is exclusive to program windows.
 * That means that it comes with the ability to {@link #openPopup(DesktopWindow, boolean) open popup windows} and has a taskbar button for hiding it.<br>
 * <br>
 * Window implementations inherit from this class and add their components and communication logic.
 * They are then managed by the {@link ClientProgramExecutor}s that use them.
 * 
 * @see ClientProgramExecutor
 */
public class ClientProgramWindow extends DesktopWindow {

    private final MultiactionButton   taskbarButton;
    private final List<DesktopWindow> openPopups = new ArrayList<>();

    /**
     * Creates a new client program window in the given desktop {@link GraphicsState}.
     * The new window is <b>not</b> added to the desktop automatically.
     * 
     * @param state The desktop state the window is part of.
     * @param title The title that should be used for the window's title bar and the taskbar button.
     */
    public ClientProgramWindow(GraphicsState state, String title) {

        super(state);

        new DesktopWindowCenteringMediator(this);
        setTitle(title);

        taskbarButton = new MultiactionButton();
        taskbarButton.setTheme("/desktop-taskbarButton-active");
        taskbarButton.setText(title);

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

    @Override
    public void setVisible(boolean visible) {

        super.setVisible(visible);

        taskbarButton.setTheme(visible ? "/desktop-taskbarButton-active" : "/desktop-taskbarButton-inactive");
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

    /**
     * Opens the given popup {@link DesktopWindow} and registers it as owned by this window.
     * All owned open popups are closed when the window is closed.
     * 
     * @param popup The popup window that should be opened.
     * @param modal Whether the popup should block any interaction on the main window.
     *        That block is removed when the popup is closed.
     */
    public void openPopup(final DesktopWindow popup, boolean modal) {

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
