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

package com.quartercode.disconnected.sim.comp.session;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.quartercode.disconnected.graphics.session.DesktopWidget;
import com.quartercode.disconnected.sim.comp.Version;
import com.quartercode.disconnected.sim.comp.Vulnerability;
import com.quartercode.disconnected.sim.comp.os.User;
import com.quartercode.disconnected.sim.comp.program.Process;
import com.quartercode.disconnected.util.size.ByteUnit;

/**
 * The desktop session program is a session with a graphical user interface (desktop).
 * It can't be used by the ai.
 * 
 * @see DesktopSession
 * @see Desktop
 * @see SessionProgram
 */
public class DesktopSessionProgram extends SessionProgram {

    /**
     * Creates a new empty desktop session program.
     * This is only recommended for direct field access (e.g. for serialization).
     */
    protected DesktopSessionProgram() {

    }

    /**
     * Creates a new desktop session program for sessions with a graphical user interface (desktop).
     * This also sets the name, the version and the vulnerabilities of the program.
     * 
     * @param name The name the session program has.
     * @param version The current version the session program has.
     * @param vulnerabilities The vulnerabilities the session program has.
     */
    public DesktopSessionProgram(String name, Version version, List<Vulnerability> vulnerabilities) {

        super(name, version, vulnerabilities);
    }

    @Override
    public long getSize() {

        return ByteUnit.BYTE.convert(500, ByteUnit.MEGABYTE);
    }

    @Override
    protected Session openSession(Process host, User user) {

        return new DesktopSession(host, user);
    }

    /**
     * A desktop session is a session with a graphical user interface (desktop).
     * Such a desktop session can't be used by the ai.
     * 
     * @see Desktop
     * @see DesktopSessionProgram
     * @see Session
     */
    public static class DesktopSession extends Session {

        private final Desktop             desktop;
        private final List<DesktopWidget> widgets;

        /**
         * Creates a new desktop session instance and sets the parent process and the user the session is running under.
         * 
         * @param host The parent process of the session instance.
         * @param user The user the session is running under.
         */
        protected DesktopSession(Process host, User user) {

            super(host, user);

            desktop = new Desktop(this);
            widgets = new ArrayList<DesktopWidget>();
        }

        @Override
        public boolean isSerializable() {

            return false;
        }

        /**
         * Returns the desktop the session uses for displaying graphical content.
         * 
         * @return The desktop the session uses.
         */
        public Desktop getDesktop() {

            return desktop;
        }

        /**
         * Returns a list containing all active desktop widgets created by this session.
         * 
         * @return A list with this session's desktop widgets.
         */
        public List<DesktopWidget> getWidgets() {

            return Collections.unmodifiableList(widgets);
        }

        /**
         * Creates a new desktop widget which displays the desktop's content on the display of the player.
         * 
         * @return A new desktop widget for the graphics.
         */
        public DesktopWidget createWidget() {

            DesktopWidget widget = new DesktopWidget(desktop);
            widgets.add(widget);
            return widget;
        }

        /**
         * Closes the given desktop widgets which then stops displaying the session's content.
         * 
         * @param widget The desktop widget to close.
         */
        public void closeWidget(DesktopWidget widget) {

            if (widgets.contains(widget)) {
                widgets.remove(widget);
                if (!widget.isClosed()) {
                    widget.close();
                }
            }
        }

        @Override
        public void close() {

            for (DesktopWidget widget : new ArrayList<DesktopWidget>(widgets)) {
                closeWidget(widget);
            }
        }

    }

}
