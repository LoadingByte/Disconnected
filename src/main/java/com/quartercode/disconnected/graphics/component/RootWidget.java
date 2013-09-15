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

package com.quartercode.disconnected.graphics.component;

import de.matthiasmann.twl.Widget;

/**
 * The root widget acts like a root node for TWL. The root widget holds the top-level child widget (the state which defines what should be drawn).
 * 
 * @see GraphicsState
 */
public class RootWidget extends Widget {

    private GraphicsState state;

    /**
     * Creates a new root widget which acts like a root node for other widgets.
     */
    public RootWidget() {

        setTheme("");
    }

    /**
     * Returns the current top-level child widget.
     * It represents the state which defines what should be drawn.
     * 
     * @return Returns the current top-level child widget.
     */
    public GraphicsState getState() {

        return state;
    }

    /**
     * Sets the current top-level child widget to a new one.
     * It represents the state which defines what should be drawn.
     * 
     * @param state The new top-level child widget.
     */
    public void setState(GraphicsState state) {

        this.state = state;
    }

    @Override
    protected void layout() {

        if (state != null) {
            state.setSize(getWidth(), getHeight());
        }
    }

}
