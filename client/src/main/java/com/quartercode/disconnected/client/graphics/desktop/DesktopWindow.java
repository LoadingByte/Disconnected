/*
 * This file is part of Disconnected.
 * Copyright (c) 2013 QuarterCode <http://quartercode.com/>
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
import de.matthiasmann.twl.ResizableFrame;
import de.matthiasmann.twl.Widget;

/**
 * A desktop window represents a window on a desktop area.
 * Basically, it is extending the {@link ResizableFrame} widget.
 */
public class DesktopWindow extends ResizableFrame {

    private final GraphicsState  state;

    private final List<Runnable> openListeners        = new ArrayList<>();
    private final List<Runnable> closeListeners       = new ArrayList<>();
    private final List<Runnable> firstLayoutListeners = new ArrayList<>();

    private boolean              layoutCalledOnce     = false;

    /**
     * Creates a new desktop window in the given desktop {@link GraphicsState}.
     * The new window is <b>not</b> added to the desktop automatically.
     *
     * @param state The desktop state the window is part of.
     */
    public DesktopWindow(GraphicsState state) {

        this.state = state;

        setTheme("/desktop-frame");

        // Clicking on the exit button of the window close it
        addCloseCallback(new Runnable() {

            @Override
            public void run() {

                close();
            }

        });
    }

    /**
     * Returns the desktop {@link GraphicsState} the window is part of.
     *
     * @return The graphics state that uses the program.
     */
    public GraphicsState getState() {

        return state;
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
     * This method is typically called by the {@link #setVisible(boolean)} method and should not be used from the outside.
     */
    protected void open() {

        ((Widget) state.getModule("windowArea").getValue("area")).add(this);

        for (Runnable openListener : openListeners) {
            openListener.run();
        }
    }

    /**
     * Registers a new open listener that is called after the window was added to the parent desktop.
     * Open listeners can be added twice.
     *
     * @param listener The open listener to register to the window.
     */
    public void addOpenListener(Runnable listener) {

        openListeners.add(listener);
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

            for (Runnable firstLayoutListener : firstLayoutListeners) {
                firstLayoutListener.run();
            }
        }
    }

    /**
     * Registers a new first layout listener that is called after the first time the {@link #layout()} method was invoked.
     * First layout listeners can be added twice.
     *
     * @param listener The first layout listener to register to the window.
     */
    public void addFirstLayoutListener(Runnable listener) {

        firstLayoutListeners.add(listener);
    }

}
