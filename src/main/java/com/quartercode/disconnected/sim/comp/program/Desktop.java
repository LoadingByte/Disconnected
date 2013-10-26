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

package com.quartercode.disconnected.sim.comp.program;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.quartercode.disconnected.Disconnected;
import com.quartercode.disconnected.graphics.session.DesktopWidget;
import com.quartercode.disconnected.graphics.session.Frame;
import com.quartercode.disconnected.sim.comp.program.DesktopSessionProgram.DesktopSession;

/**
 * A desktop can hold and display windows.
 * This class holds the windows, another class in the graphics-package renders them.
 * 
 * @see Window
 */
public class Desktop {

    private DesktopSession        host;
    private final List<Window<?>> windows = new ArrayList<Window<?>>();

    /**
     * Creates a new empty desktop.
     * This is only recommended for direct field access (e.g. for serialization).
     */
    protected Desktop() {

    }

    /**
     * Creates a new desktop.
     * 
     * @param host The hosting desktop session which uses this desktop.
     */
    public Desktop(DesktopSession host) {

        this.host = host;
    }

    /**
     * Returns the hosting desktop session which uses this desktop.
     * 
     * @return The hosting desktop session which uses this desktop.
     */
    public DesktopSession getHost() {

        return host;
    }

    /**
     * Returns a list of all windows this desktop currently holds (all visible and invisible windows).
     * 
     * @return A list of all windows this desktop currently holds (all visible and invisible windows).
     */
    public List<Window<?>> getWindows() {

        return Collections.unmodifiableList(windows);
    }

    /**
     * Adds a new window to the desktop.
     * The new window will be visible if the render frame is visible.
     * 
     * @param window The new window to add to the desktop.
     */
    public void addWindow(final Window<?> window) {

        if (window.desktop != null) {
            throw new IllegalStateException("Can't add window: Window already used elsewhere");
        } else if (window.isClosed()) {
            throw new IllegalStateException("Can't add window: Window already closed");
        } else if (!windows.contains(window)) {
            window.desktop = this;
            windows.add(window);

            Disconnected.getGraphicsManager().invoke(new Runnable() {

                @Override
                public void run() {

                    for (DesktopWidget widget : host.getWidgets()) {
                        widget.callAddWindow(window);
                    }
                }
            });
        }
    }

    /**
     * Removes a window from the desktop.
     * The window wont be just minimized, it will be removed completely.
     * 
     * @param window The window to remove from the desktop.
     */
    public void removeWindow(Window<?> window) {

        if (windows.contains(window)) {
            window.closed = true;
            window.desktop = null;
            windows.remove(window);

            for (DesktopWidget widget : host.getWidgets()) {
                widget.callRemoveWindow(window);
            }
        }
    }

    /**
     * A window represents a frame widget as a lightweight wrapper.
     * Classes on the outside can change visible parameters of the window, like the name in the taskbar or the frame title.
     * 
     * @param <F> The type of the frame this window wraps around.
     * @see Frame
     */
    public static class Window<F extends Frame> {

        private String  name;
        private String  title;
        private boolean closed;

        private Desktop desktop;
        private F       frame;

        /**
         * Creates a new empty window.
         * This is only recommended for direct field access (e.g. for serialization).
         */
        public Window() {

        }

        /**
         * Creates a new window wrapping around the given frame.
         * 
         * @param frame The frame the new window wraps around.
         */
        public Window(F frame) {

            this.frame = frame;
            setVisible(true);
        }

        /**
         * Creates a new window with the given name and title wrapping around the given frame.
         * 
         * @param frame The frame the new window wraps around.
         * @param name The name for the window which will be displayed in the taskbar.
         * @param title The title for the window which will be displayed in the title bar of the frame.
         */
        public Window(F frame, String name, String title) {

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
            frame.setName(name);
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
        public F getFrame() {

            return frame;
        }

        /**
         * Returns the visibility of the wrapped frame.
         * An invisible frame is equal to a minimized one.
         * For closing a frame, you need to use {@link Desktop#removeWindow(Window)} with the parent window object.
         * 
         * @return The visibility of the wrapped frame.
         */
        public boolean isVisible() {

            return !closed && frame.isVisible();
        }

        /**
         * Changes the visibility of the wrapped frame.
         * Making a frame invisible is equal to minimizing it.
         * For closing a frame, you need to use {@link Desktop#removeWindow(Window)} with the parent window object.
         * 
         * @param visible Determinates if the wrapped frame should be visible.
         */
        public void setVisible(final boolean visible) {

            Disconnected.getGraphicsManager().invoke(new Runnable() {

                @Override
                public void run() {

                    frame.setVisible(visible);
                }
            });
        }

        /**
         * Returns if the window is closed.
         * 
         * @return If the window is closed.
         */
        public boolean isClosed() {

            return closed;
        }

        /**
         * Removes the window from the desktop it's on.
         * The window wont be just minimized, it will be closed completely.
         * This just calls {@link Desktop#removeWindow(Window)} on the parent desktop.
         */
        public void close() {

            Disconnected.getGraphicsManager().invoke(new Runnable() {

                @Override
                public void run() {

                    desktop.removeWindow(Window.this);
                }
            });
        }

    }

}
