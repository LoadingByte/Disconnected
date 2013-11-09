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
import java.util.List;
import java.util.ResourceBundle;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import com.quartercode.disconnected.sim.comp.Version;
import com.quartercode.disconnected.sim.comp.Vulnerability;
import com.quartercode.disconnected.sim.comp.os.User;
import com.quartercode.disconnected.sim.comp.program.Process;
import com.quartercode.disconnected.sim.comp.program.ProgramExecutor;
import com.quartercode.disconnected.sim.comp.program.shell.ShellProgramExecutor;
import com.quartercode.disconnected.util.ResourceBundles;
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
     * This also sets the version and the vulnerabilities of the program.
     * 
     * @param version The current version the session program has.
     * @param vulnerabilities The vulnerabilities the session program has.
     */
    public ShellSessionProgram(Version version, List<Vulnerability> vulnerabilities) {

        super(ResourceBundles.SHELL.getString("name"), version, vulnerabilities);
    }

    @Override
    public long getSize() {

        return ByteUnit.BYTE.convert(50, ByteUnit.MEGABYTE);
    }

    @Override
    public ResourceBundle getResourceBundle() {

        return ResourceBundles.SHELL;
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
        private Shell                    shell;
        private List<ShellUserInterface> userInterfaces;

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
            userInterfaces = new ArrayList<ShellUserInterface>();
        }

        @Override
        public boolean isSerializable() {

            for (ShellUserInterface userInterface : userInterfaces) {
                if (!userInterface.isSerializable()) {
                    return false;
                }
            }

            return true;
        }

        @Override
        public boolean accept(Class<? extends ProgramExecutor> executor) {

            return ShellProgramExecutor.class.isAssignableFrom(executor);
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
         * Returns a list which contains all active user interfaces which use this session.
         * 
         * @return All active user interfaces which use this session.
         */
        public List<ShellUserInterface> getUserInterfaces() {

            return userInterfaces;
        }

        /**
         * Registers a new user interface to the shell session.
         * 
         * @param userInterface The user interface to register.
         */
        public void registerUserInterface(ShellUserInterface userInterface) {

            if (!userInterfaces.contains(userInterface)) {
                userInterfaces.add(userInterface);
                userInterface.updateCurrentDirectory(shell.getCurrentDirectory());
            }
        }

        /**
         * Unregisters the given user interface from the shell session.
         * Important: This only unregisters the interface and doesn't close it!
         * 
         * @param userInterface The user interface to unregister.
         */
        public void unregisterUserInterface(ShellUserInterface userInterface) {

            if (userInterfaces.contains(userInterface)) {
                userInterfaces.remove(userInterface);
            }
        }

        @Override
        public void close() {

            for (ShellUserInterface userInterface : new ArrayList<ShellUserInterface>(userInterfaces)) {
                userInterface.close();
                unregisterUserInterface(userInterface);
            }
        }

    }

}
