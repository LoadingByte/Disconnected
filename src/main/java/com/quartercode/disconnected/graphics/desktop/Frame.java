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

import org.apache.commons.lang.Validate;
import de.matthiasmann.twl.ResizableFrame;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twleffects.MinimizeEffect;

/**
 * A frame is the displayed component of a desktop window.
 * The frame widget renders the visible part of the frame onto the window area.
 * 
 * @see Window
 */
public class Frame extends ResizableFrame {

    /**
     * Creates and prepares a new frame.
     */
    public Frame() {

        setTheme("/frame");
    }

    /**
     * Changes the visibility of the frame.
     * Making a frame invisible is equal to minimizing it.
     * For closing a frame, you need to use {@link Desktop#removeWindow(Window)} with the parent window object.
     * 
     * @param visible Determinates if the frame should be visible.
     */
    @Override
    public void setVisible(boolean visible) {

        if (visible != isVisible()) {
            super.setVisible(visible);

            if (visible) {
                requestKeyboardFocus();
            } else if (getFadeDurationHide() > 0) {
                MinimizeEffect minimizeEffect = new MinimizeEffect(this);
                minimizeEffect.setAnimationDuration(getFadeDurationHide());
                setRenderOffscreen(minimizeEffect);
            }
        }
    }

    /**
     * Sets the center of the frame to the given relative coordinates (0 - 1).
     * 
     * @param x The new relative x center of the frame (0 - 1).
     * @param y The new relative y center of the frame (0 - 1).
     */
    public void center(float x, float y) {

        Validate.isTrue(x >= 0 && x <= 1, "Relative x coordinate must be in range 0 <= x <= 1: " + x);
        Validate.isTrue(y >= 0 && y <= 1, "Relative y coordinate must be in range 0 <= y <= 1: " + y);

        Widget parent = getParent();
        if (parent != null) {
            setPosition(parent.getInnerX() + (int) ( (parent.getInnerWidth() - getWidth()) * x), parent.getInnerY() + (int) ( (parent.getInnerHeight() - getHeight()) * y));
        }
    }

}
