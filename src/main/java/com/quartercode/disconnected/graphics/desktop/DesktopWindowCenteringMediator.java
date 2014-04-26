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

import de.matthiasmann.twl.Widget;

/**
 * The desktop window centering mediator centers a {@link DesktopWindow} when it is added to the desktop.
 * The centering can happen absolutely on the whole desktop or relatively to another window.<br>
 * <br>
 * The mediator is applied to a window by creating a new instance and then forgetting about it:
 * 
 * <pre>
 * new DesktopWindowCenteringMediator(window);
 * // or
 * new DesktopWindowCenteringMediator(window, relativeCenter);
 * </pre>
 * 
 * It doesn't matter whether the object is garbage collected since it hasn't any state.
 */
public class DesktopWindowCenteringMediator {

    /**
     * Creates a new mediator that centers the given {@link DesktopWindow} on the desktop it is added to.
     * 
     * @param window The window to center absolutely on its desktop.
     */
    public DesktopWindowCenteringMediator(final DesktopWindow window) {

        window.addFirstLayoutListener(new Runnable() {

            @Override
            public void run() {

                Widget parent = window.getParent();
                window.setPosition( (parent.getWidth() - window.getWidth()) / 2, (parent.getHeight() - window.getHeight()) / 2);
            }

        });
    }

    /**
     * Creates a new mediator that centers the given {@link DesktopWindow} relatively to the given second window.
     * That means that the first window will be positioned over the second window and centered relatively to it.
     * 
     * @param window The window to center relatively to the given second window.
     * @param relativeCenter The window the first window is centered to.
     */
    public DesktopWindowCenteringMediator(final DesktopWindow window, final DesktopWindow relativeCenter) {

        window.addFirstLayoutListener(new Runnable() {

            @Override
            public void run() {

                int x = relativeCenter.getX() + (relativeCenter.getWidth() - window.getWidth()) / 2;
                int y = relativeCenter.getY() + (relativeCenter.getHeight() - window.getHeight()) / 2;
                window.setPosition(x, y);
            }

        });
    }

}
