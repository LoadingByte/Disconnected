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

package com.quartercode.disconnected.graphics.session;

import com.quartercode.disconnected.sim.comp.program.Process;
import com.quartercode.disconnected.sim.comp.session.Desktop;
import com.quartercode.disconnected.sim.comp.session.Desktop.Window;
import de.matthiasmann.twl.BoxLayout;
import de.matthiasmann.twl.BoxLayout.Direction;
import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.DesktopArea;
import de.matthiasmann.twl.Event;
import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.Widget;

/**
 * A desktop can display windows on his window area. Every window has a frame widget which gets rendered.
 * 
 * @see Desktop
 * @see Window
 * @see Frame
 */
public class DesktopWidget extends Widget {

    private Desktop    desktop;

    public DesktopArea windowArea;
    public BoxLayout   taskbar;
    public Button      launchButton;

    /**
     * Creates a new desktop widget and sets it up.
     * 
     * @param desktop The desktop to use for the widget.
     */
    public DesktopWidget(Desktop desktop) {

        this.desktop = desktop;

        setTheme("desktop");

        windowArea = new DesktopArea();
        windowArea.setTheme("");
        add(windowArea);

        taskbar = new BoxLayout(Direction.HORIZONTAL);
        taskbar.setSpacing(5);
        taskbar.setTheme("");
        add(taskbar);

        launchButton = new Button();
        launchButton.setTheme("launch-button");
        launchButton.setText("Launch");
        launchButton.addCallback(new Runnable() {

            @Override
            public void run() {

                // TODO: Display launch menu
                Process sessionProcess = DesktopWidget.this.desktop.getHost().getHost();
                // sessionProcess.createChild(sessionProcess.getHost().getFileSystemManager().getFile("/system/bin/sysviewer.exe"), null);
                sessionProcess.createChild(sessionProcess.getHost().getFileSystemManager().getFile("/system/bin/terminal.exe"), null);
            }
        });
        add(launchButton);
    }

    /**
     * Returns the desktop this widget is rendering.
     * The returned object holds all windows which can be displayed on this widget.
     * 
     * @return The desktop this widget is rendering.
     */
    public Desktop getDesktop() {

        return desktop;
    }

    /**
     * Calls the utility to add a new window to the desktop area.
     * The new window will be visible if the render frame is visible.
     * 
     * @param window The new window to add to the desktop.
     */
    public void callAddWindow(final Window<?> window) {

        window.getFrame().addCloseCallback(new Runnable() {

            @Override
            public void run() {

                desktop.removeWindow(window);
            }
        });
        window.getFrame().getTaskbarButton().addCallback(Event.MOUSE_RBUTTON, new Runnable() {

            @Override
            public void run() {

                desktop.removeWindow(window);
            }
        });
        window.getFrame().getTaskbarButton().addCallback(Event.MOUSE_LBUTTON, new Runnable() {

            @Override
            public void run() {

                window.setVisible(!window.isVisible());
            }
        });

        try {
            windowArea.add(window.getFrame());
            window.getFrame().setCenter(0.2F, 0.2F);
            windowArea.invalidateLayout();

            taskbar.add(window.getFrame().getTaskbarButton());
            taskbar.invalidateLayout();
        }
        catch (IllegalArgumentException e) {
            // TODO: Replace workaround with real solution
        }
    }

    /**
     * Removes a window from the desktop.
     * The window wont be just minimized, it will be removed until it gets added again.
     * 
     * @param window The window to remove from the desktop.
     */
    public void callRemoveWindow(Window<?> window) {

        windowArea.removeChild(window.getFrame());
        windowArea.invalidateLayout();

        taskbar.removeChild(window.getFrame().getTaskbarButton());
        taskbar.invalidateLayout();
    }

    /**
     * Returns true if the desktop widget already stopped displaying the session's content and is closed.
     * 
     * @return True if the desktop widget is already closed.
     */
    public boolean isClosed() {

        return desktop == null;
    }

    /**
     * Closes the desktop widget and stops displaying the session's content.
     */
    public void close() {

        if (!isClosed()) {
            for (Window<?> window : desktop.getWindows()) {
                callRemoveWindow(window);
            }
            desktop = null;
            desktop.getHost().closeWidget(this);
        }
    }

    @Override
    protected void layout() {

        windowArea.setSize(getParent().getWidth(), getParent().getHeight());

        launchButton.adjustSize();
        launchButton.setPosition(10, getParent().getHeight() - launchButton.getHeight() - 5);

        taskbar.adjustSize();
        taskbar.setPosition(launchButton.getWidth() + 30, getParent().getHeight() - taskbar.getHeight() - 5);
    }

    @Override
    protected void afterAddToGUI(GUI gui) {

        for (Window<?> window : desktop.getWindows()) {
            callAddWindow(window);
        }
    }

    @Override
    protected void beforeRemoveFromGUI(GUI gui) {

        close();
    }

}
