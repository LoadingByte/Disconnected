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

import org.apache.commons.lang3.Validate;
import de.matthiasmann.twl.Dimension;
import de.matthiasmann.twl.Widget;

/**
 * Some utility methods that are related to {@link DesktopWindow}s and should be used to remove boilerplate code.
 *
 * @see DesktopWindow
 */
public class DesktopWindowUtils {

    /**
     * Defaults the size of the given {@link DesktopWindow}.
     * The actual size setting operation is executed when the window is opened.
     *
     * @param window The window whose size should be defaulted.
     * @param defaultSize The default size for the given window.
     */
    public static void setDefaultSize(final DesktopWindow window, final Dimension defaultSize) {

        Validate.notNull(window, "Desktop window cannot be null");
        Validate.notNull(defaultSize, "Default size data object cannot be null");

        window.addOpenListener(new Runnable() {

            @Override
            public void run() {

                window.setSize(defaultSize.getX(), defaultSize.getY());
            }

        });
    }

    /**
     * Sets the minimum size of the given {@link DesktopWindow}.
     * The actual minimum size setting operation is executed when the window is opened.
     * That is necessary because any calls prior to that moment are ignored.
     *
     * @param window The window whose minimum size limit should be set.
     * @param minSize The minimum size limit for the given window.
     */
    public static void setMinimumSize(final DesktopWindow window, final Dimension minSize) {

        Validate.notNull(window, "Desktop window cannot be null");
        Validate.notNull(minSize, "Minimum size data object cannot be null");

        window.addOpenListener(new Runnable() {

            @Override
            public void run() {

                window.setMinSize(minSize.getX(), minSize.getY());
            }

        });
    }

    /**
     * Sets the maximum size of the given {@link DesktopWindow}.
     * The actual maximum size setting operation is executed when the window is opened.
     * That is necessary because any calls prior to that moment are ignored.
     *
     * @param window The window whose maximum size limit should be set.
     * @param maxSize The maximum size limit for the given window.
     */
    public static void setMaximumSize(final DesktopWindow window, final Dimension maxSize) {

        Validate.notNull(window, "Desktop window cannot be null");
        Validate.notNull(maxSize, "Maximum size data object cannot be null");

        window.addOpenListener(new Runnable() {

            @Override
            public void run() {

                window.setMaxSize(maxSize.getX(), maxSize.getY());
            }

        });
    }

    /**
     * Centers the given {@link DesktopWindow} on the desktop it is added to.
     * The actual centering operation is executed after the first {@link DesktopWindow#layout()} has been made.
     * That is necessary because the required sizes are only available after that moment.
     *
     * @param window The window that should be absolutely centered on its desktop.
     */
    public static void center(final DesktopWindow window) {

        Validate.notNull(window, "Desktop window cannot be null");

        window.addFirstLayoutListener(new Runnable() {

            @Override
            public void run() {

                Widget parent = window.getParent();
                window.setPosition( (parent.getWidth() - window.getWidth()) / 2, (parent.getHeight() - window.getHeight()) / 2);
            }

        });
    }

    /**
     * Centers the given {@link DesktopWindow} relative to the given second window.
     * That means that the middle/center of the first window will be exactly positioned over the middle/center of the second window.
     * The actual centering operation is executed after the first {@link DesktopWindow#layout()} has been made.
     * That is necessary because the required sizes are only available after that moment.
     *
     * @param window The window that should be relatively centered to the given second window.
     * @param relativeCenter The window the first window is centered relative to.
     */
    public static void center(final DesktopWindow window, final DesktopWindow relativeCenter) {

        Validate.notNull(window, "Desktop window cannot be null");
        Validate.notNull(relativeCenter, "Relative center desktop window cannot be null");

        window.addFirstLayoutListener(new Runnable() {

            @Override
            public void run() {

                int x = relativeCenter.getX() + (relativeCenter.getWidth() - window.getWidth()) / 2;
                int y = relativeCenter.getY() + (relativeCenter.getHeight() - window.getHeight()) / 2;
                window.setPosition(x, y);
            }

        });
    }

    private DesktopWindowUtils() {

    }

}
