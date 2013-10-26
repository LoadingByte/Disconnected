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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import com.quartercode.disconnected.graphics.session.ShellWidget;
import com.quartercode.disconnected.sim.comp.Version;
import com.quartercode.disconnected.sim.comp.Vulnerability;
import com.quartercode.disconnected.sim.comp.os.User;
import com.quartercode.disconnected.sim.comp.program.Process;
import com.quartercode.disconnected.util.size.ByteUnit;

/**
 * The shell session program is a session with text-based user interface (terminal).
 * It can be used by the ai, but you can only serialize shell sessions which don't have any active widgets.
 * 
 * @see ShellSession
 * @see Shell
 * @see SessionProgram
 */
@XmlSeeAlso ({ ShellSessionProgram.ShellSession.class })
public class ShellSessionProgram extends SessionProgram {

    /**
     * Creates a new empty shell session program.
     * This is only recommended for direct field access (e.g. for serialization).
     */
    protected ShellSessionProgram() {

    }

    /**
     * Creates a new shell session program for sessions with a text-based user interface (terminal).
     * This also sets the name, the version and the vulnerabilities of the program.
     * 
     * @param name The name the session program has.
     * @param version The current version the session program has.
     * @param vulnerabilities The vulnerabilities the session program has.
     */
    public ShellSessionProgram(String name, Version version, List<Vulnerability> vulnerabilities) {

        super(name, version, vulnerabilities);
    }

    @Override
    public long getSize() {

        return ByteUnit.BYTE.convert(50, ByteUnit.MEGABYTE);
    }

    @Override
    protected Session openSession(Process host, User user) {

        return new ShellSession(host, user);
    }

    /**
     * The shell session program is a session with text-based user interface (terminal).
     * Such a shell session can be used by the ai.
     * You can only serialize shell sessions which don't have any active widgets.
     * 
     * @see Shell
     * @see ShellSessionProgram
     * @see Session
     */
    public static class ShellSession extends Session {

        @XmlElement
        private Shell             shell;
        private List<ShellWidget> widgets;

        /**
         * Creates a new empty shell session.
         * This is only recommended for direct field access (e.g. for serialization).
         */
        protected ShellSession() {

        }

        /**
         * Creates a new shell session instance and sets the parent process and the user the session is running under.
         * 
         * @param host The parent process of the session instance.
         * @param user The user the session is running under.
         */
        protected ShellSession(Process host, User user) {

            super(host, user);

            shell = new Shell(this);
            widgets = new ArrayList<ShellWidget>();
        }

        @Override
        public boolean isSerializable() {

            return getWidgets().size() == 0;
        }

        /**
         * Returns the shell the session uses for displaying graphical content.
         * 
         * @return The shell the session uses.
         */
        public Shell getShell() {

            return shell;
        }

        /**
         * Returns a list containing all active shell widgets created by this session.
         * 
         * @return A list with this session's shell widgets.
         */
        public List<ShellWidget> getWidgets() {

            return Collections.unmodifiableList(widgets);
        }

        /**
         * Closes the given shell widgets which then stops displaying the session's content.
         * 
         * @return A new shell widget for the graphics.
         */
        public ShellWidget createWidget() {

            ShellWidget widget = new ShellWidget(shell);
            widgets.add(widget);
            return widget;
        }

        /**
         * Closes the given shell widgets which then stops displaying the session's content.
         * 
         * @param widget The shell widget to close.
         */
        public void closeWidget(ShellWidget widget) {

            if (widgets.contains(widget)) {
                widgets.remove(widget);
                if (!widget.isClosed()) {
                    widget.close();
                }
            }
        }

        @Override
        public void close() {

            for (ShellWidget widget : new ArrayList<ShellWidget>(widgets)) {
                closeWidget(widget);
            }
        }

    }

}
