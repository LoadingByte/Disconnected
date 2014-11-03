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

import de.matthiasmann.twl.Dimension;

/**
 * The desktop window size limits mediator sets the min and max size of a {@link DesktopWindow} when it is added to the desktop.
 * Note that one parameter might be null if a certain size limit should not be set.
 * The mediator is applied to a window by creating a new instance and then forgetting about it:
 * 
 * <pre>
 * new DesktopWindowSizeLimitsMediator(window, new Dimension(400, 200), new Dimension(800, 400));
 * </pre>
 * 
 * It doesn't matter whether the object is garbage collected since it hasn't any state.
 */
public class DesktopWindowSizeLimitsMediator {

    /**
     * Creates a new mediator that sets the size limits of the given {@link DesktopWindow} to the given ones when the window is added to the desktop.
     * 
     * @param window The window whose size limits should be set.
     * @param minSize The minimum size of the given window.
     * @param maxSize The maximum size of the given window.
     */
    public DesktopWindowSizeLimitsMediator(final DesktopWindow window, final Dimension minSize, final Dimension maxSize) {

        window.addOpenListener(new Runnable() {

            @Override
            public void run() {

                if (minSize != null) {
                    window.setMinSize(minSize.getX(), minSize.getY());
                }

                if (maxSize != null) {
                    window.setMaxSize(maxSize.getX(), maxSize.getY());
                }
            }

        });
    }

}
