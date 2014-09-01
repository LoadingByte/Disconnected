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
import de.matthiasmann.twl.BoxLayout;
import de.matthiasmann.twl.BoxLayout.Direction;
import de.matthiasmann.twl.Widget;

/**
 * The desktop taskbar module adds a {@link BoxLayout} that contains all taskbar buttons.
 * That {@link BoxLayout} is stored under the key {@code desktopWidget}.
 * An example usage of the module is setting a desktop background through the {@code desktop} theme.
 */
public class DesktopTaskbarModule extends AbstractGraphicsModule {

    private BoxLayout layout;

    @Override
    public void add(GraphicsState state) {

        layout = new BoxLayout(Direction.HORIZONTAL);
        layout.setSpacing(5);
        layout.setTheme("");
        ((Widget) state.getModule("desktopWidget").getValue("widget")).add(layout);
        setValue("layout", layout);
    }

    @Override
    public void layout(GraphicsState state) {

        layout.adjustSize();
        layout.setPosition( ((Widget) state.getModule("launchButton").getValue("button")).getWidth() + 30, state.getHeight() - layout.getHeight() - 10);
    }

}
