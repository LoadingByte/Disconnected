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

import com.quartercode.disconnected.client.graphics.AbstractGraphicsModule;
import com.quartercode.disconnected.client.graphics.GraphicsState;
import de.matthiasmann.twl.DesktopArea;
import de.matthiasmann.twl.Widget;

/**
 * The desktop window area module makes a {@link DesktopArea} available.
 * The area spans over the whole screen and can hold {@link DesktopProgramWindow}s.
 * It is available under the key {@code area}.
 */
public class DesktopWindowAreaModule extends AbstractGraphicsModule {

    private DesktopArea windowArea;

    @Override
    public void add(GraphicsState state) {

        windowArea = new DesktopArea();
        windowArea.setTheme("");
        ((Widget) state.getModule("desktopWidget").getValue("widget")).add(windowArea);
        setValue("area", windowArea);
    }

    @Override
    public void layout(GraphicsState state) {

        windowArea.adjustSize();
        windowArea.setSize(state.getWidth(), state.getHeight());
    }

}
