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
 * The desktop window default size mediator sets the size of a {@link DesktopWindow} when it is added to the desktop.
 * The mediator is applied to a window by creating a new instance and then forgetting about it:
 * 
 * <pre>
 * new DesktopWindowDefaultSizeMediator(window, new Dimension(500, 200));
 * </pre>
 * 
 * It doesn't matter whether the object is garbage collected since it hasn't any state.
 */
public class DesktopWindowDefaultSizeMediator {

    /**
     * Creates a new mediator that defaults the size of the given {@link DesktopWindow} to a given one when the window is added to the desktop.
     * 
     * @param window The window whose size should be defaulted.
     * @param defaultSize The default size of the given window.
     */
    public DesktopWindowDefaultSizeMediator(final DesktopWindow window, final Dimension defaultSize) {

        window.addOpenListener(new Runnable() {

            @Override
            public void run() {

                window.setSize(defaultSize.getX(), defaultSize.getY());
            }

        });
    }

}
