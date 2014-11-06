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

package com.quartercode.disconnected.shared.world.comp.program.names;

/**
 * A list of all system program names (e.g. {@code "session"}).
 * System programs are the core programs an operating system needs to function.
 */
public class SystemProgs {

    /**
     * The session program which represents a logged-in user.
     * All processes which are somehow children of a session run under the user that started the session, except when there's another
     * session process between the child process and the session.
     * In that case, the "nearest" session process is used.
     */
    public static final String SESSION = "session";

    private SystemProgs() {

    }

}
