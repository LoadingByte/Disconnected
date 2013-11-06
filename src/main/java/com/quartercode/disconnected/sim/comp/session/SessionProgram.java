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

import java.util.List;
import java.util.Map;
import javax.xml.bind.annotation.XmlIDREF;
import com.quartercode.disconnected.sim.comp.Version;
import com.quartercode.disconnected.sim.comp.Vulnerability;
import com.quartercode.disconnected.sim.comp.os.User;
import com.quartercode.disconnected.sim.comp.program.ArgumentException;
import com.quartercode.disconnected.sim.comp.program.ArgumentException.ArgumentExceptionType;
import com.quartercode.disconnected.sim.comp.program.Parameter;
import com.quartercode.disconnected.sim.comp.program.Parameter.ArgumentType;
import com.quartercode.disconnected.sim.comp.program.Process;
import com.quartercode.disconnected.sim.comp.program.Program;
import com.quartercode.disconnected.sim.comp.program.ProgramExecutor;
import com.quartercode.disconnected.util.InfoString;

/**
 * This class represents program which opens a session.
 * For opening an actual session, you just have to create a process out of this with the argument "user".
 * 
 * @see Program
 * @see Session
 */
public abstract class SessionProgram extends Program {

    /**
     * Creates a new empty session program.
     * This is only recommended for direct field access (e.g. for serialization).
     */
    protected SessionProgram() {

    }

    /**
     * Creates a new session program and sets the name, the version and the vulnerabilities.
     * 
     * @param name The name the session program has.
     * @param version The current version the session program has.
     * @param vulnerabilities The vulnerabilities the session program has.
     */
    protected SessionProgram(String name, Version version, List<Vulnerability> vulnerabilities) {

        super(name, version, vulnerabilities);
    }

    @Override
    protected void addParameters() {

        addParameter(Parameter.createArgument("user", "u", ArgumentType.STRING, false, true));
    }

    @Override
    protected ProgramExecutor createExecutorInstance(Process host, Map<String, Object> arguments) throws ArgumentException {

        String username = (String) arguments.get("user");
        User user = null;
        for (User testUser : host.getHost().getUserManager().getUsers()) {
            if (testUser.getName().equals(username)) {
                user = testUser;
            }
        }

        if (user == null) {
            throw new ArgumentException(this, arguments, getParameter("user"), ArgumentExceptionType.WRONG_ARGUMENT_TYPE);
        } else {
            return openSession(host, user);
        }
    }

    /**
     * Creates and returns a new session instance of the implementing session program.
     * 
     * @param host The host process of the session process.
     * @param user The user the new session will run under.
     * @return The new open session instance.
     */
    protected abstract Session openSession(Process host, User user);

    /**
     * A session is an instance of a session program.
     * Sessions are used for letting users interact with a system.
     * Such a session is simply a process, every child of this process can use the rights provided by the parent session.
     * 
     * @see SessionProgram
     */
    public static abstract class Session extends ProgramExecutor implements InfoString {

        @XmlIDREF
        private User user;

        /**
         * Creates a new empty session.
         * This is only recommended for direct field access (e.g. for serialization).
         */
        protected Session() {

        }

        /**
         * Creates a new session instance and sets the parent process and the user the session is running under.
         * 
         * @param host The parent process of the session instance.
         * @param user The user the session is running under.
         */
        protected Session(Process host, User user) {

            super(host);
            this.user = user;
        }

        /**
         * Returns the user the session is running under.
         * Every child process of this session can use the rights provided by this session.
         * 
         * @return The user the session is running under.
         */
        public User getUser() {

            return user;
        }

        /**
         * Returns true if this session instance is serializable, false if not.
         * Sessions which are not serializable must be closed before the simulation can be serialized.
         * 
         * @return True if this session instance is serializable.
         */
        public abstract boolean isSerializable();

        /**
         * Returns true if the given executor type can be executed in this session.
         * 
         * @param executor The executor type to check.
         * @return True if the given executor type can be executed in this session.
         */
        public abstract boolean accept(Class<? extends ProgramExecutor> executor);

        /**
         * Closes the session.
         * You need to close sessions after usage, so the operating system can free the resources.
         * Sessions which are not serializable must also be closed before the simulation can be serialized.
         */
        protected abstract void close();

        @Override
        public final void update() {

            // Do nothing
        }

        @Override
        public String toInfoString() {

            return user.getName() + "@" + getHost().getHost().getId();
        }

    }

}
