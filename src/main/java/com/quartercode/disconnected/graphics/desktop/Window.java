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

import com.quartercode.disconnected.graphics.component.MultiactionButton;

/**
 * A window represents a frame widget as a lightweight wrapper.
 * Classes on the outside can change visible parameters of the window, like the name in the taskbar or the frame title.
 * 
 * @see Frame
 */
public class Window {

    private String                  name;
    private String                  title;

    private final Frame             frame;
    private final MultiactionButton taskbarButton;

    /**
     * Creates a new window wrapping around the given frame.
     * 
     * @param frame The frame the new window wraps around.
     */
    public Window(Frame frame) {

        this.frame = frame;

        taskbarButton = new MultiactionButton();
        setVisible(true);
    }

    /**
     * Creates a new window with the given name and title wrapping around the given frame.
     * 
     * @param frame The frame the new window wraps around.
     * @param name The name for the window which will be displayed in the taskbar.
     * @param title The title for the window which will be displayed in the title bar of the frame.
     */
    public Window(Frame frame, String name, String title) {

        this(frame);

        setName(name);
        setTitle(title);
    }

    /**
     * Returns the name for the window which will be displayed in the taskbar.
     * 
     * @return The name for the window which will be displayed in the taskbar.
     */
    public String getName() {

        return name;
    }

    /**
     * Sets the name for the window to a new one.
     * The name will be displayed in the taskbar.
     * 
     * @param name The new name for the window.
     */
    public void setName(String name) {

        this.name = name;
        taskbarButton.setText(name);
    }

    /**
     * Returns the title for the window which will be displayed in the title bar of the frame.
     * 
     * @return The title for the window which will be displayed in the title bar of the frame.
     */
    public String getTitle() {

        return title;
    }

    /**
     * Sets the title for the window to a new one.
     * The title will be displayed in the title bar of the frame
     * 
     * @param title The new title for the window.
     */
    public void setTitle(String title) {

        this.title = title;
        frame.setTitle(title);
    }

    /**
     * Returns the frame this window wraps around.
     * The frame object should only be used to add hooks.
     * 
     * @return The frame this window wraps around.
     */
    protected Frame getFrame() {

        return frame;
    }

    /**
     * Returns the taskbar button which represents the frame.
     * The taskbar button object should only be used to add hooks.
     * 
     * @return the taskbar button which represents the frame.
     */
    protected MultiactionButton getTaskbarButton() {

        return taskbarButton;
    }

    /**
     * Returns the visibility of the wrapped frame.
     * An invisible frame is equal to a minimized one.
     * For closing a frame, you need to use {@link Desktop#removeWindow(Window)} with the parent window object.
     * 
     * @return The visibility of the wrapped frame.
     */
    public boolean isVisible() {

        return frame.isVisible();
    }

    /**
     * Changes the visibility of the wrapped frame.
     * Making a frame invisible is equal to minimizing it.
     * For closing a frame, you need to use {@link Desktop#removeWindow(Window)} with the parent window object.
     * 
     * @param visible Determinates if the wrapped frame should be visible.
     */
    public void setVisible(boolean visible) {

        frame.setVisible(visible);

        taskbarButton.setTheme(visible ? "/taskbar-button-active" : "/taskbar-button-inactive");
        taskbarButton.reapplyTheme();
    }

}
