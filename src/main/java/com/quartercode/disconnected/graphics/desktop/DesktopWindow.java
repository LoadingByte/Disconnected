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

import java.util.ArrayList;
import java.util.List;
import com.quartercode.disconnected.graphics.GraphicsState;
import de.matthiasmann.twl.Dimension;
import de.matthiasmann.twl.ResizableFrame;
import de.matthiasmann.twl.Widget;

/**
 * A desktop window represents a window on a desktop area.
 * Bascially, it is extending the {@link ResizableFrame} widget.
 */
public class DesktopWindow extends ResizableFrame {

    private final GraphicsState  state;
    private final DesktopWindow  logicalParent;
    private final Dimension      defaultSize;

    private final List<Runnable> closeListeners   = new ArrayList<Runnable>();

    private boolean              layoutCalledOnce = false;

    /**
     * Creates a new desktop window in the given desktop {@link GraphicsState}.
     * The new window is <b>not</b> added to the desktop automatically.
     * 
     * @param state The desktop state the new program will be running in.
     * @param logicalParent The logical parent window of the new window is the window that created it.
     *        It is used to center the new window relative to the logical parent window.
     * @param defaultSize The size the window will have when it is added to the desktop.
     */
    public DesktopWindow(GraphicsState state, DesktopWindow logicalParent, Dimension defaultSize) {

        this.state = state;
        this.defaultSize = defaultSize;
        this.logicalParent = logicalParent;

        setTheme("frame");

        // Clicking on the exit button of the window close it
        addCloseCallback(new Runnable() {

            @Override
            public void run() {

                close();
            }
        });
    }

    /**
     * Returns the desktop {@link GraphicsState} the window is running in.
     * 
     * @return The graphics state that uses the program.
     */
    protected GraphicsState getState() {

        return state;
    }

    /**
     * Returns the logical parent window of the new window is the window that created it.
     * It is used to center the new window relative to the logical parent window.
     * 
     * @return The logical parent window of the window.
     */
    public DesktopWindow getLogicalParent() {

        return logicalParent;
    }

    /**
     * Returns the size the window will have when it is added to the desktop.
     * 
     * @return Thed default size of the window.
     */
    public Dimension getDefaultSize() {

        return defaultSize;
    }

    /**
     * Changes the visibility of the window.
     * If the window is not added to the desktop yet, the {@link #open()} method is called in order to do so.
     * 
     * @param visible Whether the window should be visibility on the desktop.
     */
    @Override
    public void setVisible(boolean visible) {

        if (visible && getParent() == null) {
            open();
        }

        if (visible != isVisible()) {
            super.setVisible(visible);

            if (visible) {
                requestKeyboardFocus();
            }
        }
    }

    /**
     * Adds the window widget to the desktop {@link GraphicsState} window area.
     * This method is typically called by the {@link #setVisible(boolean)} method and should not be used from outside.
     */
    protected void open() {

        ((Widget) state.getModule("windowArea").getValue("area")).add(this);

        setSize(defaultSize.getX(), defaultSize.getY());
    }

    /**
     * Closes the window and removes it from the desktop {@link GraphicsState} that was set during construction.
     */
    public void close() {

        for (Runnable closeListener : closeListeners) {
            closeListener.run();
        }

        getParent().removeChild(this);
    }

    /**
     * Registers a new close listener that is called before the window is removed from the parent desktop.
     * Close listeners can be added twice.
     * 
     * @param listener The close listener to register to the window.
     */
    public void addCloseListener(Runnable listener) {

        closeListeners.add(listener);
    }

    @Override
    protected void layout() {

        super.layout();

        if (!layoutCalledOnce) {
            layoutCalledOnce = true;

            // Center the window
            if (logicalParent == null) {
                setPosition( (getParent().getWidth() - getWidth()) / 2, (getParent().getHeight() - getHeight()) / 2);
            } else {
                int x = logicalParent.getX() + (logicalParent.getWidth() - getWidth()) / 2;
                int y = logicalParent.getY() + (logicalParent.getHeight() - getHeight()) / 2;
                setPosition(x, y);
            }
        }
    }

}
